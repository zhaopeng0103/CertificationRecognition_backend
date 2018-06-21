package com.peng.certrecognition.controller;

import com.peng.certrecognition.configuration.Constants;
import com.peng.certrecognition.domain.Photo;
import com.peng.certrecognition.domain.Recognition;
import com.peng.certrecognition.domain.User;
import com.peng.certrecognition.ocr.HorizontalCorrection;
import com.peng.certrecognition.ocr.Segmentation;
import com.peng.certrecognition.service.PhotoService;
import com.peng.certrecognition.service.RecognitionService;
import com.peng.certrecognition.util.PythonUtils;
import com.peng.certrecognition.util.UUIDUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CrossOrigin
@RestController
public class PhotoController extends BaseController {

    @Autowired
    private PhotoService photoService;

    @Autowired
    private RecognitionService recognitionService;

    @RequestMapping(value = "/photo/upload", method = RequestMethod.POST)
    public ResponseEntity<?> photoUpload(@RequestParam(value = "file") MultipartFile file) {
        String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
        User user = getCurrentUser();
        if (user != null) {
            if (file.isEmpty()) {
                logger.info("PhotoController::photoUpload:failed:{}", "图片不存在！");
                return responseError(methodName, "图片不存在！");
            }
            String originalFileName = file.getOriginalFilename();
            String type = originalFileName.substring(originalFileName.lastIndexOf("."));
            String newFileName = String.format("%s%s", UUIDUtils.genUUID(), ".jpg");
            File dest = new File(Constants.PHOTO_PATH.concat(user.getEmail()).concat(File.separator), newFileName);
            if (!dest.getParentFile().exists()) {
                dest.getParentFile().mkdirs();
            }
            try {
                file.transferTo(dest);
                Photo photo = photoService.addPhoto(user, file, newFileName, type, dest.getPath());
                Map<String, Object> data = photo.toMapFromUpload();
                logger.info("PhotoController::photoUpload:success:{}", data);
                return responseSuccess(methodName, data, "图片上传完成");
            } catch (IOException e) {
                logger.error("PhotoController::photoUpload:failed:{}", "图片上传失败！", e.getMessage());
                return responseError(methodName, "图片上传失败！");
            }
        } else {
            logger.info("PhotoController::photoUpload:failed:{}", "用户不存在！");
            return responseError(methodName, "用户不存在！");
        }
    }

    @RequestMapping(value = "/photo/list", method = RequestMethod.GET)
    public ResponseEntity<?> photoList() {
        String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
        try {
            List<Map<String, Object>> data = new ArrayList<>();
            for (Photo photo : photoService.getPhotoList(getCurrentUser())) {
                data.add(photo.toMapFromList());
            }
            logger.info("PhotoController::photoList:finished:{}", data.size());
            return responseSuccess(methodName, data, "获取成功！");
        } catch (Exception e) {
            logger.error("PhotoController::photoList:failed: {}", e.getMessage());
            return responseError(methodName, "获取图片列表失败！");
        }
    }

    @RequestMapping(value = "/photo/download/{photoName}", method = RequestMethod.GET)
    public ResponseEntity<?> photoDownload(@PathVariable("photoName") String photoName) {
        String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
        try {
            Photo photo = photoService.getPhotoByName(photoName.concat(".jpg"));
            InputStream inputStream = new FileInputStream(new File(photo.getPath()));
            InputStreamResource inputStreamResource = new InputStreamResource(inputStream);

            HttpHeaders respHeaders = new HttpHeaders();
            respHeaders.setContentDispositionFormData("attachment", photo.getFilename());

            logger.info("PhotoController::photoDownload:finished:{}", photo.getFilename());
            return new ResponseEntity<>(inputStreamResource, respHeaders, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("PhotoController::photoDownload:failed:{}", e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.EXPECTATION_FAILED);
        }
    }

    @RequestMapping(value = "/photo/view", method = RequestMethod.POST)
    public ResponseEntity<?> photoView(@RequestParam("photoName") String photoName) {
        String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
        try {
            Recognition recognition = recognitionService.getRecognitionByName(photoName);
            Map<String, Object> data = recognition.toMap();
            logger.info("PhotoController::photoView:finished:{}", data);
            return responseSuccess(methodName, data, "获取结果成功！");
        } catch (Exception e) {
            logger.error("PhotoController::photoView:failed: {}", e.getMessage());
            return responseError(methodName, "获取结果失败！");
        }
    }

    @RequestMapping(value = "/photo/delete", method = RequestMethod.POST)
    public ResponseEntity<?> photoDelete(@RequestParam("photoName") String photoName) {
        String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
        User user = getCurrentUser();
        if (user != null) {
            try {
                photoService.deletePhotoByName(photoName);
                recognitionService.deleteRecognitionByName(photoName);
                File file = new File(Constants.PHOTO_PATH.concat(user.getEmail()).concat(File.separator), photoName);
                if (file.exists()) {
                    boolean result = file.delete();
                }
                Map<String, Object> data = new HashMap<>();
                data.put("photoName", photoName);
                logger.info("PhotoController::photoDelete:finished:{}", data);
                return responseSuccess(methodName, data, "删除成功！");
            } catch (Exception e) {
                logger.error("PhotoController::photoDelete:failed:{} --> {}", photoName, e.getMessage());
                return responseError(methodName, "删除失败！");
            }
        } else {
            logger.info("PhotoController::photoRecognition:failed:{}", "用户不存在！");
            return responseError(methodName, "用户不存在！");
        }
    }

    @RequestMapping(value = "/photo/recognition", method = RequestMethod.POST)
    public ResponseEntity<?> photoRecognition(@RequestParam("photoName") String photoName) {
        String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
        User user = getCurrentUser();
        if (user != null) {
            try {
                String photo_path = Constants.PHOTO_PATH.concat(user.getEmail()).concat(File.separator);
                String recognitionRes = PythonUtils.execPythonFile(Constants.PYTHON_PATH, photo_path, photoName);
                if (recognitionRes.equals("1")) {
                    Recognition recognition = recognitionService.getRecognitionByName(photoName);
                    Map<String, Object> data = recognition.toMap();
                    logger.info("PhotoController::photoRecognition:finished:{}", data);
                    return responseSuccess(methodName, data, "识别成功！");
                } else {
                    logger.error("PhotoController::photoRecognition:failed:{} --> {}", photoName);
                    return responseError(methodName, "识别失败！");
                }
            } catch (Exception e) {
                logger.error("PhotoController::photoRecognition:failed:{} --> {}", photoName, e.getMessage());
                return responseError(methodName, "识别出错！");
            }
        } else {
            logger.info("PhotoController::photoRecognition:failed:{}", "用户不存在！");
            return responseError(methodName, "用户不存在！");
        }
    }

    @RequestMapping(value = "/photo/recognitionOnline", method = RequestMethod.POST)
    public ResponseEntity<?> photoRecognitionOnline(@RequestParam("photoName") String photoName) {
        String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
        User user = getCurrentUser();
        if (user != null) {
            try {
                String photo_path = Constants.PHOTO_PATH.concat(user.getEmail()).concat(File.separator);
                HorizontalCorrection.contoursBase(photo_path, photoName);
                Map<String, String> tables = Segmentation.segment(photoName);
                if (tables != null && tables.size() > 0) {
                    List<Map<String, Object>> data = com.peng.certrecognition.ocr.Recognition.recognize(photoName, tables);
                    Recognition recognition = recognitionService.addRecognition(photoName, photo_path, data);
                    Map<String, Object> result = recognition.toMap();
                    logger.info("PhotoController::photoRecognition:finished:{}", result);
                    return responseSuccess(methodName, result, "识别成功！");
                } else {
                    logger.error("PhotoController::photoRecognition:failed:{} --> {}", photoName);
                    return responseError(methodName, "识别无结果！");
                }
            } catch (Exception e) {
                logger.error("PhotoController::photoRecognition:failed:{} --> {}", photoName, e.getMessage());
                return responseError(methodName, "识别出错！");
            }
        } else {
            logger.info("PhotoController::photoRecognition:failed:{}", "用户不存在！");
            return responseError(methodName, "用户不存在！");
        }
    }

}
