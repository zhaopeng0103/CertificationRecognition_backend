package com.peng.certrecognition.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class PythonUtils {

    public static String execPythonFile(String python_path, String photoPath, String photoName) {
        String line;
        try {
            String[] args = new String[]{"python", python_path, photoPath, photoName};
            Process process = Runtime.getRuntime().exec(args);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            line = bufferedReader.readLine().trim();
            bufferedReader.close();
            process.waitFor();
        } catch (Exception e) {
            line = String.valueOf("0");
            e.printStackTrace();
        }
        return line;
    }

    public static void main(String[] args) {
        String python_path = "D:/360Downloads/pythonWorkplace/CertificationRecognition_recognition/Application.py";
        String photoPath = "F:/CertificationRecognition_backend/photo/424107420@qq.com/";
        String photoName = "3bed7f24-57d5-4869-9293-f83c7609c122.png";
        System.out.println(execPythonFile(python_path, photoPath, photoName));
    }

}
