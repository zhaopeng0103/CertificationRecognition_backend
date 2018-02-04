package com.peng.certrecognition.controller;

import com.peng.certrecognition.domain.Photo;
import com.peng.certrecognition.service.PhotoService;
import com.peng.certrecognition.util.UUIDUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
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

    @RequestMapping(value = "/photo/upload", method = RequestMethod.POST)
    public ResponseEntity<?> upload(HttpServletRequest request) {
        List<MultipartFile> files = ((MultipartHttpServletRequest) request).getFiles("files");
        List<Map<String, Object>> photoList = new ArrayList<>();
        for (MultipartFile file : files) {
            if (!file.isEmpty()) {
                String originalFileName = file.getOriginalFilename();
                String type = originalFileName.substring(originalFileName.lastIndexOf("."));
                String newFileName = UUIDUtils.genUUID() + type;
                File localPath = new File(request.getServletContext().getRealPath("//photos//") + newFileName);
                if (!localPath.exists()) {
                    localPath.mkdirs();
                }
                try {
                    file.transferTo(localPath);
                    Photo photo = photoService.addPhoto(getCurrentUser(), file, newFileName, type, localPath.getPath());
                    photoList.add(photo.toMap());
                    logger.info("PhotoController::photoUpload:finished:{}", originalFileName);
                } catch (IOException e) {
                    logger.error("PhotoController::photoUpload:failed:{}", originalFileName);
                    e.printStackTrace();
                }
            }
        }
        return responseSuccess(photoList, "图片上传完成");
    }

    @RequestMapping(value = "/photo/download", method = RequestMethod.GET)
    public ResponseEntity<?> download(@RequestParam("photoId") String photoId) {
        try {
            Photo photo = photoService.getPhoto(photoId);
            InputStream inputStream = new FileInputStream(new File(photo.getPath()));
            InputStreamResource inputStreamResource = new InputStreamResource(inputStream);

            HttpHeaders respHeaders = new HttpHeaders();
            respHeaders.setContentDispositionFormData("attachment", photo.getOriginalname());

            logger.info("PhotoController::photoDownload:finished:{}", inputStreamResource);
            return new ResponseEntity<>(inputStreamResource, respHeaders, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("PhotoController::photoDownload:failed:{}", e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.EXPECTATION_FAILED);
        }
    }

    @RequestMapping(value = "/photo/delete", method = RequestMethod.POST)
    public ResponseEntity<?> delete(@RequestParam("photoId") String photoId) {
        try {
            photoService.deletePhoto(photoId);
            Map<String, Object> data = new HashMap<>();
            data.put("photoId", photoId);
            logger.info("PhotoController::photoDelete:finished:{}", data);
            return responseSuccess(data, "删除成功！");
        } catch (Exception e) {
            logger.error("PhotoController::photoDelete:failed:{} --> {}", photoId, e.getMessage());
            return responseError("删除失败！");
        }
    }

    @RequestMapping(value = "/photo/list", method = RequestMethod.POST)
    public ResponseEntity<?> list() {
        try {
            List<Map<String, Object>> data = new ArrayList<>();
            for (Photo photo : photoService.getPhotoList(getCurrentUser())) {
                data.add(photo.toMap());
            }
            logger.info("PhotoController::photoList:finished:{}", data);
            return responseSuccess(data, "获取成功！");
        } catch (Exception e) {
            logger.error("PhotoController::photoList:failed: {}", e.getMessage());
            return responseError("获取图片列表失败！");
        }
    }

    @RequestMapping(value = "/photo/view", method = RequestMethod.POST)
    public ResponseEntity<?> view(@RequestParam("photoId") String photoId) {
        try {
            Photo photo = photoService.getPhoto(photoId);
            logger.info("PhotoController::photoView:finished:{}", photo);
            return responseSuccess(photo.toMap(), "获取成功！");
        } catch (Exception e) {
            logger.error("PhotoController::photoView:failed: {}", e.getMessage());
            return responseError("获取图片失败！");
        }
    }


}
