package com.peng.certrecognition.ocr;

import com.peng.certrecognition.configuration.Constants;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.util.*;

public class Segmentation {
    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public static Map<String, String> segment(String imgName) {
        Map<String, String> result = new HashMap<>();
        String preImgName = imgName.split("\\.")[0];
        String sourceDir = String.format("%s%s%s%s", Constants.OCR_PATH, Constants.IMG_SOURCE, File.separator, imgName);
        String grayDir = String.format("%s%s%s%s", Constants.OCR_PATH, Constants.IMG_GRAY, File.separator, imgName);
        String threshDir = String.format("%s%s%s%s", Constants.OCR_PATH, Constants.IMG_THRESH, File.separator, imgName);
        String horizontalDir = String.format("%s%s%s%s", Constants.OCR_PATH, Constants.IMG_HORIZONTAL, File.separator, imgName);
        String verticalDir = String.format("%s%s%s%s", Constants.OCR_PATH, Constants.IMG_VERTICAL, File.separator, imgName);
        String maskDir = String.format("%s%s%s%s", Constants.OCR_PATH, Constants.IMG_MASK, File.separator, imgName);
        String jointsDir = String.format("%s%s%s%s", Constants.OCR_PATH, Constants.IMG_JOINTS, File.separator, imgName);
        String roisDir = String.format("%s%s%s%s%s", Constants.OCR_PATH, Constants.IMG_ROIS, File.separator, preImgName, File.separator);
        String cellsDir = String.format("%s%s%s%s%s", Constants.OCR_PATH, Constants.IMG_CELLS, File.separator, preImgName, File.separator);

        Mat src = Imgcodecs.imread(sourceDir);
        if (src.empty()) {
            System.out.println("not found img");
            return null;
        }

        // 先转为灰度图
        Mat gray = new Mat();
        Imgproc.cvtColor(src, gray, Imgproc.COLOR_BGR2GRAY);
        saveFile(grayDir, gray);

        // 腐蚀（黑色区域变大）
        int erodeSize = src.cols() / 300;
        if (erodeSize % 2 == 0) {
            erodeSize++;
        }
        Mat erode = new Mat();
        Mat element = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(erodeSize, erodeSize));
        Imgproc.erode(gray, erode, element);

        // 高斯模糊化
        int blurSize = src.cols() / 200;
        if (blurSize % 2 == 0) {
            blurSize++;
        }
        Mat blur = new Mat();
        Imgproc.GaussianBlur(erode, blur, new Size(blurSize, blurSize), 0, 0);

        // 封装的二值化
        Mat thresh = gray.clone();
        Mat notGray = new Mat();
        Core.bitwise_not(gray, notGray);
        Imgproc.adaptiveThreshold(notGray, thresh, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 15, -2);
        saveFile(threshDir, thresh);

        int scale = 30;
        // 使用二值化后的图像来获取表格横线
        Mat horizontal = thresh.clone();
        int horizontalSize = horizontal.cols() / scale;
        Mat horizontalStructure = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(horizontalSize, 1));
        Imgproc.erode(horizontal, horizontal, horizontalStructure, new Point(-1, -1), 1);
        Imgproc.dilate(horizontal, horizontal, horizontalStructure, new Point(-1, -1), 1);
        saveFile(horizontalDir, horizontal);

        // 使用二值化后的图像来获取表格纵线
        Mat vertical = thresh.clone();
        int verticalSize = vertical.rows() / scale;
        Mat verticalStructure = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(1, verticalSize));
        Imgproc.erode(vertical, vertical, verticalStructure, new Point(-1, -1), 1);
        Imgproc.dilate(vertical, vertical, verticalStructure, new Point(-1, -1), 1);
        saveFile(verticalDir, vertical);

        // 合并表格横纵线
        Mat mask = new Mat();
        Core.add(horizontal, vertical, mask);
        saveFile(maskDir, mask);

        // 定位横纵线交汇的点
        Mat joints = new Mat();
        Core.bitwise_and(horizontal, vertical, joints);
        saveFile(jointsDir, joints);

        // 找轮廓
        Mat hierarchy = new Mat();
        List<MatOfPoint> contours = new ArrayList<>();
        Imgproc.findContours(mask, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE, new Point(0, 0));

        List<MatOfPoint> contours_poly = contours;
        Rect[] boundRect = new Rect[contours.size()];
        List<Mat> rois = new ArrayList<>();
        List<Rect> haveRect = new ArrayList<>();
        Map<String, Map<String, List<Integer>>> mapPoint = new HashMap<>();
        // 循环所有找到的轮廓点
        for (int i = 0; i < contours.size(); i++) {
            // 每个轮廓的点
            double area = Imgproc.contourArea(contours.get(i));
            if (area < 100) {
                continue;
            }
            Imgproc.approxPolyDP(new MatOfPoint2f(contours.get(i).toArray()), new MatOfPoint2f(contours_poly.get(i).toArray()), 3, true);
            boundRect[i] = Imgproc.boundingRect(contours_poly.get(i));
            Mat roi = joints.submat(boundRect[i]);
            List<MatOfPoint> table_contours = new ArrayList<>();
            Imgproc.findContours(roi, table_contours, new Mat(), Imgproc.RETR_CCOMP, Imgproc.CHAIN_APPROX_SIMPLE);
            if (table_contours.size() < 4) {
                continue;
            }
            List<Integer> xThis = new ArrayList<>();
            List<Integer> yThis = new ArrayList<>();
            for (MatOfPoint matOfPoint : table_contours) {
                for (Point point : matOfPoint.toArray()) {
                    xThis.add(new Double(point.x).intValue());
                    yThis.add(new Double(point.y).intValue());
                }
            }
            Collections.sort(xThis);
            Collections.sort(yThis);
            haveRect.add(boundRect[i]);
            Map<String, List<Integer>> x = new HashMap<>();
            x.put("x", xThis);
            x.put("y", yThis);
            mapPoint.put("rect" + (haveRect.size() - 1), x);
            // 保存图片
            rois.add(src.submat(boundRect[i]).clone());
            // 将矩形画在原图上
            Imgproc.rectangle(src, boundRect[i].tl(), boundRect[i].br(), new Scalar(0, 255, 0), 1, 8, 0);
        }

        for (int r = 0; r < rois.size(); r++) {
            Mat roi = rois.get(r);
            saveFile(roisDir.concat(r + ".png"), roi);
            int width = haveRect.get(r).width, height = haveRect.get(r).height;
            Map<String, List<Integer>> mapData = mapPoint.get("rect" + r);
            List<Integer> xPoint = mapData.get("x");
            List<Integer> yPoint = mapData.get("y");

            // 纵切，分割出每一列
            int colsNum = 0, colsMinWidth = 5;
            List<Mat> mats = new ArrayList<>();
            for (int i = 0; i < xPoint.size(); i++) {
                if (i == 0) {
                    Mat img = new Mat(roi, new Rect(0, 0, xPoint.get(i), height));
                    if (img.cols() > colsMinWidth) {
                        mats.add(img);
                        colsNum++;
                    }
                } else if (i == xPoint.size() - 1) {
                    Mat img = new Mat(roi, new Rect(xPoint.get(i), 0, width - xPoint.get(i), height));
                    if (img.cols() > colsMinWidth) {
                        mats.add(img);
                        colsNum++;
                    }
                } else {
                    Mat img = new Mat(roi, new Rect(xPoint.get(i - 1), 0, xPoint.get(i) - xPoint.get(i - 1), height));
                    if (img.cols() > colsMinWidth) {
                        mats.add(img);
                        colsNum++;
                    }
                }
            }

            // 横切，在获得每一列的基础上，分割出每一个单元格
            int rowsNum = 0, rowsMinHeight = 5;
            for (int col = 0; col < mats.size(); col++) {
                Mat mat = mats.get(col);
                int row = 0;
                for (int i = 0; i < yPoint.size(); i++) {
                    if (i == 0) {
                        Mat cell = new Mat(mat, new Rect(0, 0, mat.cols(), yPoint.get(i)));
                        if (cell.rows() > rowsMinHeight) {
                            saveFile(cellsDir.concat(String.format("%d_%d_%d.png", r, row, col)), cell);
                            row++;
                        }
                    } else if (i == yPoint.size() - 1) {
                        Mat cell = new Mat(mat, new Rect(0, yPoint.get(i), mat.cols(), mat.rows() - yPoint.get(i)));
                        if (cell.rows() > rowsMinHeight) {
                            saveFile(cellsDir.concat(String.format("%d_%d_%d.png", r, row, col)), cell);
                            row++;
                        }
                    } else {
                        Mat cell = new Mat(mat, new Rect(0, yPoint.get(i - 1), mat.cols(), yPoint.get(i) - yPoint.get(i - 1)));
                        if (cell.rows() > rowsMinHeight) {
                            saveFile(cellsDir.concat(String.format("%d_%d_%d.png", r, row, col)), cell);
                            row++;
                        }
                    }
                }
                rowsNum = row;
            }
            result.put(String.format("table_%d", r), String.format("%d_%d", rowsNum, colsNum));
        }
        return result;
    }

    private static void saveFile(String filename, Mat img) {
        File file = new File(filename);
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        Imgcodecs.imwrite(filename, img);
    }

}
