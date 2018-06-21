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

public class SegmentationOld {
    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public static Map<String, List<String>> segment(String imgName) {
        Map<String, List<String>> result = new HashMap<>();
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
            return result;
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

        int scale = 40;
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
        Map<String, Map<String, Map<Integer, List<Integer>>>> mapPoint = new HashMap<>();
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
            TreeMap<Integer, List<Integer>> xThis = new TreeMap<>(new Comparator<Integer>() {
                @Override
                public int compare(Integer o1, Integer o2) {
                    return o1.compareTo(o2);
                }
            });
            TreeMap<Integer, List<Integer>> yThis = new TreeMap<>(new Comparator<Integer>() {
                @Override
                public int compare(Integer o1, Integer o2) {
                    return o1.compareTo(o2);
                }
            });
            for (MatOfPoint matOfPoint : table_contours) {
                for (Point point : matOfPoint.toArray()) {
                    int curX = (int) point.x;
                    int curY = (int) point.y;
                    if (xThis.containsKey(curX)) {
                        xThis.get(curX).add(curY);
                    } else {
                        List<Integer> l = new ArrayList<>();
                        l.add(curY);
                        xThis.put(curX, l);
                    }
                    if (yThis.containsKey(curY)) {
                        yThis.get(curY).add(curX);
                    } else {
                        List<Integer> l = new ArrayList<>();
                        l.add(curX);
                        yThis.put(curY, l);
                    }
                }
            }
            for (List<Integer> xTh : xThis.values()) {
                Collections.sort(xTh);
            }
            for (List<Integer> yTh : yThis.values()) {
                Collections.sort(yTh);
            }

            haveRect.add(boundRect[i]);
            Map<String, Map<Integer, List<Integer>>> x = new HashMap<>();
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
            Map<Integer, List<Integer>> yPoints = mapPoint.get("rect" + r).get("y");

            // 先横切，再纵切，分割出每一个单元格
            int rowsNum = 0, rowsMinHeight = 5, colsNum, colsMinWidth = 5;
            int i = 0, curY, lastY = 0;
            List<Integer> curX;
            List<String> curRoi = new ArrayList<>();
            for (Map.Entry<Integer, List<Integer>> entry : yPoints.entrySet()) {
                curY = entry.getKey();
                curX = entry.getValue();
                if (i == 0) {
                    if (curY > rowsMinHeight) {
                        colsNum = 0;
                        for (int j = 0; j < curX.size(); j++) {
                            if (j == 0) {
                                if (curX.get(j) > colsMinWidth) {
                                    Mat cell = new Mat(roi, new Rect(0, 0, curX.get(j), curY));
                                    saveFile(cellsDir.concat(String.format("%d_%d_%d.png", r, rowsNum, colsNum)), cell);
                                    colsNum++;
                                }
                            } else if (j == entry.getValue().size() - 1) {
                                if (width - curX.get(j) > colsMinWidth) {
                                    Mat cell = new Mat(roi, new Rect(curX.get(j), 0, width - curX.get(j), curY));
                                    saveFile(cellsDir.concat(String.format("%d_%d_%d.png", r, rowsNum, colsNum)), cell);
                                    colsNum++;
                                }
                            } else {
                                if (curX.get(j) - curX.get(j - 1) > colsMinWidth) {
                                    Mat cell = new Mat(roi, new Rect(curX.get(j - 1), 0, curX.get(j) - curX.get(j - 1), curY));
                                    saveFile(cellsDir.concat(String.format("%d_%d_%d.png", r, rowsNum, colsNum)), cell);
                                    colsNum++;
                                }
                            }
                        }
                        curRoi.add(String.format("%d_%d", rowsNum, colsNum));
                        lastY = curY;
                        rowsNum++;
                    }
                } else if (i == yPoints.size() - 1) {
                    if (height - curY > rowsMinHeight) {
                        colsNum = 0;
                        for (int j = 0; j < curX.size(); j++) {
                            if (j == 0) {
                                if (curX.get(j) > colsMinWidth) {
                                    Mat cell = new Mat(roi, new Rect(0, curY, curX.get(j), height - curY));
                                    saveFile(cellsDir.concat(String.format("%d_%d_%d.png", r, rowsNum, colsNum)), cell);
                                    colsNum++;
                                }
                            } else if (j == entry.getValue().size() - 1) {
                                if (width - curX.get(j) > colsMinWidth) {
                                    Mat cell = new Mat(roi, new Rect(curX.get(j), curY, width - curX.get(j), height - curY));
                                    saveFile(cellsDir.concat(String.format("%d_%d_%d.png", r, rowsNum, colsNum)), cell);
                                    colsNum++;
                                }
                            } else {
                                if (curX.get(j) - curX.get(j - 1) > colsMinWidth) {
                                    Mat cell = new Mat(roi, new Rect(curX.get(j - 1), curY, curX.get(j) - curX.get(j - 1), height - curY));
                                    saveFile(cellsDir.concat(String.format("%d_%d_%d.png", r, rowsNum, colsNum)), cell);
                                    colsNum++;
                                }
                            }
                        }
                        curRoi.add(String.format("%d_%d", rowsNum, colsNum));
                        lastY = curY;
                        rowsNum++;
                    }
                } else {
                    if (curY - lastY > rowsMinHeight) {
                        colsNum = 0;
                        for (int j = 0; j < curX.size(); j++) {
                            if (j == 0) {
                                if (curX.get(j) > colsMinWidth) {
                                    Mat cell = new Mat(roi, new Rect(0, lastY, curX.get(j), curY - lastY));
                                    saveFile(cellsDir.concat(String.format("%d_%d_%d.png", r, rowsNum, colsNum)), cell);
                                    colsNum++;
                                }
                            } else if (j == entry.getValue().size() - 1) {
                                if (width - curX.get(j) > colsMinWidth) {
                                    Mat cell = new Mat(roi, new Rect(curX.get(j), lastY, width - curX.get(j), curY - lastY));
                                    saveFile(cellsDir.concat(String.format("%d_%d_%d.png", r, rowsNum, colsNum)), cell);
                                    colsNum++;
                                }
                            } else {
                                if (curX.get(j) - curX.get(j - 1) > colsMinWidth) {
                                    Mat cell = new Mat(roi, new Rect(curX.get(j - 1), lastY, curX.get(j) - curX.get(j - 1), curY - lastY));
                                    saveFile(cellsDir.concat(String.format("%d_%d_%d.png", r, rowsNum, colsNum)), cell);
                                    colsNum++;
                                }
                            }
                        }
                        curRoi.add(String.format("%d_%d", rowsNum, colsNum));
                        lastY = curY;
                        rowsNum++;
                    }
                }
                i++;
            }
            result.put(String.format("table_%d", r), curRoi);
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
