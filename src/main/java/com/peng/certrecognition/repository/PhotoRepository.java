package com.peng.certrecognition.repository;

import com.peng.certrecognition.domain.Photo;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface PhotoRepository extends MongoRepository<Photo, String>, PhotoRepositoryCustom {

    Photo findByFilename(String filename);

    List<Photo> findByUserid(String userid);

    void deleteByFilename(String filename);

}
