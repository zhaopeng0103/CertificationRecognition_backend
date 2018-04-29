package com.peng.certrecognition.repository;

import com.peng.certrecognition.domain.Recognition;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface RecognitionRepository extends MongoRepository<Recognition, String>, RecognitionRepositoryCustom {

    Recognition findByFilename(String filename);

    void deleteByFilename(String filename);

}
