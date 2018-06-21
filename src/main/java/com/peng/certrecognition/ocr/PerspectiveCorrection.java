package com.peng.certrecognition.ocr;

import com.peng.certrecognition.configuration.Constants;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.util.*;

public class PerspectiveCorrection {
    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    private static Point center = new Point(0, 0);

    private static boolean sort_corners(List<Point> corners) {
        List<Point> top = new ArrayList<>();
        List<Point> bot = new ArrayList<>();
        Point tmp_pt = new Point();
        List<Point> oldData = corners;

        if (corners.size() != 4) {
            return false;
        }
        for (Point point1 : corners) {
            for (Point point2 : corners) {
                if (point1.y < point2.y) {
                    tmp_pt.x = point1.x;
                    tmp_pt.y = point1.y;
                    point1.x = point2.x;
                    point1.y = point2.y;
                    point2.x = tmp_pt.x;
                    point2.y = tmp_pt.y;
                }
            }
        }
        top.add(corners.get(0));
        top.add(corners.get(1));
        bot.add(corners.get(2));
        bot.add(corners.get(3));
        if (top.size() == 2 && bot.size() == 2) {
            corners.clear();
            Point tl = top.get(0).x > top.get(1).x ? top.get(1) : top.get(0);
            Point tr = top.get(0).x > top.get(1).x ? top.get(0) : top.get(1);
            Point bl = bot.get(0).x > bot.get(1).x ? bot.get(1) : bot.get(0);
            Point br = bot.get(0).x > bot.get(1).x ? bot.get(0) : bot.get(1);
            corners.add(tl);
            corners.add(tr);
            corners.add(br);
            corners.add(bl);
            return true;
        } else {
            corners = oldData;
            return false;
        }
    }

    private static Point computeIntersect() {
        return null;
    }

    private static boolean isBadLine(int a, int b) {
        return a * a + b * b < 100;
    }

    private static void sortCorners(List<Point> corners, Point center) {
        List<Point> top = new ArrayList<>();
        List<Point> bot = new ArrayList<>();
        List<Point> backup = corners;
        Collections.sort(corners, new Comparator<Point>() {
            @Override
            public int compare(Point o1, Point o2) {
                return (int) (o1.x - o2.x);
            }
        });
        for (Point point : corners) {
            if (point.y < center.y && top.size() < 2) {
                top.add(point);
            } else {
                bot.add(point);
            }
        }
        corners.clear();
        if (top.size() == 2 && bot.size() == 2) {
            Point tl = top.get(0).x > top.get(1).x ? top.get(1) : top.get(0);
            Point tr = top.get(0).x > top.get(1).x ? top.get(0) : top.get(1);
            Point bl = bot.get(0).x > bot.get(1).x ? bot.get(1) : bot.get(0);
            Point br = bot.get(0).x > bot.get(1).x ? bot.get(0) : bot.get(1);
            corners.add(tl);
            corners.add(tr);
            corners.add(br);
            corners.add(bl);
        } else {
            corners = backup;
        }
    }

    private static void calDstSize(List<Point> corners) {
        int h1 = (int) Math.sqrt((corners.get(0).x - corners.get(3).x) * (corners.get(0).x - corners.get(3).x) + (corners.get(0).y - corners.get(3).y) * (corners.get(0).y - corners.get(3).y));
        int h2 = (int) Math.sqrt((corners.get(1).x - corners.get(2).x) * (corners.get(1).x - corners.get(2).x) + (corners.get(1).y - corners.get(2).y) * (corners.get(1).y - corners.get(2).y));
        int g_dst_hight = Math.max(h1, h2);

        int w1 = (int) Math.sqrt((corners.get(0).x - corners.get(1).x) * (corners.get(0).x - corners.get(1).x) + (corners.get(0).y - corners.get(1).y) * (corners.get(0).y - corners.get(1).y));
        int w2 = (int) Math.sqrt((corners.get(2).x - corners.get(3).x) * (corners.get(2).x - corners.get(3).x) + (corners.get(2).y - corners.get(3).y) * (corners.get(2).y - corners.get(3).y));
        int g_dst_width = Math.max(w1, w2);
    }

    public static void correct(String imgName) {
        String horCorrectionDir = String.format("%s%s%s%s", Constants.OCR_PATH, Constants.IMG_HOR_CORRECTION, File.separator, imgName);
        Mat src = Imgcodecs.imread(horCorrectionDir);
        Mat source = src.clone();
        Mat bkup = src.clone();
        Mat img = src.clone();
        Imgproc.cvtColor(img, img, Imgproc.COLOR_BGR2GRAY);
        Imgproc.GaussianBlur(img, img, new Size(5, 5), 0, 0);
        // 获取自定义核
        Mat element = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3, 3)); // MORPH_RECT表示矩形的卷积核
        // 膨胀操作
        Imgproc.dilate(img, img, element);
        // 边缘提取
        Imgproc.Canny(img, img, 30, 120, 3, false);

        List<MatOfPoint> contours = new ArrayList<>();
        List<MatOfPoint> f_contours = new ArrayList<>();
        List<Point> approx2 = new ArrayList<>();
        Imgproc.findContours(img, f_contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_NONE);

        // 求出面积最大的轮廓
        double max_area = 0;
        int index = 0;
        for (int i = 0; i < f_contours.size(); i++) {
            double tmparea = Math.abs(Imgproc.contourArea(f_contours.get(i)));
            if (tmparea > max_area) {
                index = i;
                max_area = tmparea;
            }
        }
        contours.add(f_contours.get(index));

        Point[] tmp = contours.get(0).toArray();
        for (int line_type = 1; line_type <= 3; line_type++) {
            Mat black = img.clone();
            black.setTo(new Scalar(0));
            Imgproc.drawContours(black, contours, 0, new Scalar(255), line_type);

            MatOfInt4 lines = new MatOfInt4();
            List<Point> corners = new ArrayList<>();
            List<Point> approx = new ArrayList<>();

            int para = 10;
            int flag = 0;
            int round = 0;
            for (; para < 300; para++) {
                corners.clear();
                approx.clear();
                center = new Point(0, 0);
                Imgproc.HoughLinesP(black, lines, 1, Math.PI / 180, para, 30, 10);

                //过滤距离太近的直线
                Set<Integer> ErasePt = new HashSet<>();
                for (int i = 0; i < lines.rows(); i++) {
                    for (int j = i + 1; j < lines.rows(); j++) {
//                        if (isBadLine(Math.abs(lines.get(i, 0) - lines.get(j, 0)), Math.abs(lines[i][1] - lines[j][1])) && (isBadLine(Math.abs(lines[i][2] - lines[j][2]), Math.abs(lines[i][3] - lines[j][3])))) {
//                            ErasePt.add(j);//将该坏线加入集合
//                        }
                    }
                }
            }
        }
    }

}
