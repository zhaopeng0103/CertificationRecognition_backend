package com.peng.certrecognition.ocr;

import com.peng.certrecognition.configuration.Constants;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class HorizontalCorrection {
    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public static void contoursBase(String photoPath, String photoName) {
        String rotateDir = String.format("%s%s%s%s", Constants.OCR_PATH, Constants.IMG_ROTATE, File.separator, photoName);
        String horCorrectionDir = String.format("%s%s%s%s", Constants.OCR_PATH, Constants.IMG_SOURCE, File.separator, photoName);

        Mat src = Imgcodecs.imread(photoPath.concat(photoName));
        if (src.empty()) {
            System.out.println("not found img");
            return;
        }

        // 先转为灰度图
        Mat gray = new Mat();
        Imgproc.cvtColor(src, gray, Imgproc.COLOR_BGR2GRAY);

        // 二值化
        Mat thresh = new Mat();
        Imgproc.threshold(gray, thresh, 100, 200, Imgproc.THRESH_BINARY);

        // 查找轮廓
        List<MatOfPoint> contours = new ArrayList<>();
        Imgproc.findContours(thresh, contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_NONE);

        Mat roiSrc = new Mat(src.rows(), src.cols(), CvType.CV_8UC3);
        roiSrc.setTo(new Scalar(0));
        // 填充轮廓
        Imgproc.drawContours(thresh, contours, -1, new Scalar(255), 2);
        src.copyTo(roiSrc, thresh);

        Mat rotationImg = new Mat(roiSrc.rows(), roiSrc.cols(), CvType.CV_8UC1);
        rotationImg.setTo(new Scalar(0));
        for (MatOfPoint contour : contours) {
            Point[] rectPoint = new Point[4];
            RotatedRect rect = Imgproc.minAreaRect(new MatOfPoint2f(contour.toArray()));
            // 获取4个顶点坐标
            rect.points(rectPoint);
            // 与水平线的角度
            double angle = rect.angle;

            double line1 = Math.sqrt((rectPoint[1].y - rectPoint[0].y) * (rectPoint[1].y - rectPoint[0].y) + (rectPoint[1].x - rectPoint[0].x) * (rectPoint[1].x - rectPoint[0].x));
            double line2 = Math.sqrt((rectPoint[3].y - rectPoint[0].y) * (rectPoint[3].y - rectPoint[0].y) + (rectPoint[3].x - rectPoint[0].x) * (rectPoint[3].x - rectPoint[0].x));

            // 面积太小的直接pass
            if (line1 * line2 < 600) {
                continue;
            }
            // 为了让正方形横着放，所以旋转角度是不一样的。竖放的，加90度，翻过来
            if (line1 > line2) {
                angle = 90 + angle;
            }

            // 对roiSrc旋转
            Point center = rect.center;// 中心点
            Mat mat = Imgproc.getRotationMatrix2D(center, angle, 1); // 计算旋转缩放的变换矩阵
            Imgproc.warpAffine(roiSrc, rotationImg, mat, roiSrc.size(), 1, 0, new Scalar(0)); // 仿射变换
        }
        saveFile(rotateDir, rotationImg);

        // 对旋转后的图片进行轮廓提取
        List<MatOfPoint> contours2 = new ArrayList<>();
        Mat secondFindImg = new Mat();
        Imgproc.cvtColor(rotationImg, secondFindImg, Imgproc.COLOR_BGR2GRAY);
        Imgproc.threshold(secondFindImg, secondFindImg, 80, 200, Imgproc.THRESH_BINARY);
        Imgproc.findContours(secondFindImg, contours2, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_NONE);
        for (MatOfPoint contour : contours2) {
            Rect rect = Imgproc.boundingRect(new MatOfPoint(contour.toArray()));
            if (rect.area() < 600) {
                continue;
            }
            saveFile(horCorrectionDir, rotationImg.submat(rect));
        }
    }

    public static void linesBase(String photoPath, String photoName) {
        String rotateDir = String.format("%s%s%s%s", Constants.OCR_PATH, Constants.IMG_ROTATE, File.separator, photoName);
        String sourceDir = String.format("%s%s%s%s", Constants.OCR_PATH, Constants.IMG_SOURCE, File.separator, photoName);

        Mat src = Imgcodecs.imread(photoPath.concat(photoName));
        if (src.empty()) {
            System.out.println("not found img");
            return;
        }
        Mat dst = new Mat();
        double degree = calDegree(src, dst);
        if (degree == -1) {
            rotateImg(src, dst, degree);
        } else {
            dst = src.clone();
        }
        saveFile(rotateDir, dst);
    }

    // 逆时针旋转图像degree角度
    private static void rotateImg(Mat src, Mat imgRotate, double degree) {
        // 旋转中心为图像中心
        Point center = new Point(src.cols() / 2.0, src.rows() / 2.0);
        int len = (int) Math.sqrt(src.cols() * src.cols() + src.rows() * src.rows());
        // 计算二维旋转的仿射变换矩阵
        Mat mat = Imgproc.getRotationMatrix2D(center, degree, 1);
        Imgproc.warpAffine(src, imgRotate, mat, new Size(len, len), 1, 0, new Scalar(255, 255, 255));
    }

    // 通过霍夫变换计算角度
    private static double calDegree(Mat src, Mat dst) {
        Mat midImg = new Mat();
        Mat dstImg = new Mat();
        Imgproc.Canny(src, midImg, 50, 200, 3, false);
        Imgproc.cvtColor(midImg, dstImg, Imgproc.COLOR_GRAY2BGR);
        // 通过霍夫变换检测直线
        Mat lines = new Mat();
        Imgproc.HoughLines(midImg, lines, 1, Math.PI / 180, 300);
        if (lines.rows() == 0) {
            Imgproc.HoughLines(midImg, lines, 1, Math.PI / 180, 200);
        }
        if (lines.rows() == 0) {
            Imgproc.HoughLines(midImg, lines, 1, Math.PI / 180, 150);
        }
        if (lines.rows() == 0) {
            System.out.println("没有检测到直线！");
            return -1;
        }
        double sum = 0;
        for (int i = 0; i < lines.rows(); i++) {
            double[] vec = lines.get(i, 0);
            double rho = vec[0];
            double theta = vec[1];
            Point pt1 = new Point();
            Point pt2 = new Point();
            double a = Math.cos(theta);
            double b = Math.sin(theta);
            double x0 = a * rho;
            double y0 = b * rho;
            pt1.x = Math.round(x0 + 1000 * (-b));
            pt1.y = Math.round(y0 + 1000 * (a));
            pt2.x = Math.round(x0 - 1000 * (-b));
            pt2.y = Math.round(y0 - 1000 * (a));
            sum += theta;
            Imgproc.line(dstImg, pt1, pt2, new Scalar(55, 100, 195), 1, Imgproc.LINE_AA, 0);
        }
        double average = sum / lines.rows();
        double angle = average / Math.PI * 180 - 90;
        rotateImg(dstImg, dst, angle);
        return angle;
    }

    private static void saveFile(String filename, Mat img) {
        File file = new File(filename);
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        Imgcodecs.imwrite(filename, img);
    }

}
