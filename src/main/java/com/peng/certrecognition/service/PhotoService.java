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
     *
     * @param user 用户
     * @param file 文件
     * @param newFileName 新文件名
     * @param type 类型
     * @param path 路径
     * @return Photo
     */
    public Photo addPhoto(User user, MultipartFile file, String newFileName, String type, String path) {
        Photo photo = new Photo();
        photo.setUserid(user.getId());
        photo.setOriginalname(file.getOriginalFilename());
        photo.setSize(file.getSize());
        photo.setFilename(newFileName);
        photo.setType(type);
        photo.setPath(path);
        photo.setLabel(null);
        photo.setCreateTime(new Date());
        return photoRepository.save(photo);
    }

    /**
     * 获得图片列表
     *
     * @param user 用户
     * @return List
     */
    public List<Photo> getPhotoList(User user) {
        return photoRepository.findByUserid(user.getId());
    }

    /**
     * 获得单个图片
     *
     * @param photoName 图片名
     * @return Photo
     */
    public Photo getPhotoByName(String photoName) {
        return photoRepository.findByFilename(photoName);
    }

    /**
     * 删除单张图片
     *
     * @param photoName 图片名
     */
    public void deletePhotoByName(String photoName) {
        photoRepository.deleteByFilename(photoName);
    }

}
