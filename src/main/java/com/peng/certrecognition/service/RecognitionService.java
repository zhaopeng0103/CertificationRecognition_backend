package com.peng.certrecognition.service;

import com.peng.certrecognition.domain.Recognition;
import com.peng.certrecognition.repository.RecognitionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RecognitionService extends BaseService {

    @Autowired
    private RecognitionRepository recognitionRepository;

    /**
     * 查询识别结果
     *
     * @param photoName 图片名
     * @return Recognition
     */
    public Recognition getRecognitionByName(String photoName) {
        return recognitionRepository.findByFilename(photoName);
    }

    /**
     * 删除识别结果
     *
     * @param photoName 图片名
     */
    public void deleteRecognitionByName(String photoName) {
        recognitionRepository.deleteByFilename(photoName);
    }

}
