package com.peng.certrecognition.service;

import com.peng.certrecognition.domain.Photo;
import com.peng.certrecognition.domain.User;
import com.peng.certrecognition.repository.PhotoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.List;

@Service
public class PhotoService extends BaseService {

    @Autowired
    private PhotoRepository photoRepository;

    /**
     * 上传图片
     * @param user
     * @param file
     * @param newFileName
     * @param type
     * @param localPath
     * @return
     */
    public Photo addPhoto(User user, MultipartFile file, String newFileName, String type, String localPath) {
        Photo photo = new Photo();
        photo.setUserid(user.getId());
        photo.setOriginalname(file.getOriginalFilename());
        photo.setSize(file.getSize());
        photo.setFilename(newFileName);
        photo.setType(type);
        photo.setPath(localPath);
        photo.setLabel(null);
        photo.setCreateTime(new Date());
        return photoRepository.save(photo);
    }

    /**
     * 获得图片列表
     * @param user
     * @return
     */
    public List<Photo> getPhotoList(User user) {
        return photoRepository.findByUserid(user.getId());
    }

    /**
     * 获得单个图片
     * @param photoId
     * @return
     */
    public Photo getPhoto(String photoId) {
        return photoRepository.findById(photoId);
    }

    /**
     * 删除单张图片
     * @param photoId
     */
    public void deletePhoto(String photoId) {
        photoRepository.deleteById(photoId);
    }

}
