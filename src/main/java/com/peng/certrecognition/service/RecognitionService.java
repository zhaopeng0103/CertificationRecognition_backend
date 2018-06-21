package com.peng.certrecognition.service;

import com.peng.certrecognition.domain.Recognition;
import com.peng.certrecognition.repository.RecognitionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

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

    /**
     * 插入识别结果
     * @param filename 文件名
     * @param path 路径
     * @param data 数据信息
     * @return Recognition
     */
    public Recognition addRecognition(String filename, String path, List<Map<String, Object>> data) {
        Recognition recognition = new Recognition();
        recognition.setFilename(filename);
        recognition.setPath(path);
        recognition.setData(data);
        recognition.setError_code(0);
        recognition.setError_msg("");
        return recognitionRepository.save(recognition);
    }

}
