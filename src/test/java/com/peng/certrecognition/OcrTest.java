package com.peng.certrecognition;

import com.peng.certrecognition.configuration.Constants;
import com.peng.certrecognition.ocr.HorizontalCorrection;
import com.peng.certrecognition.ocr.Recognition;
import com.peng.certrecognition.ocr.Segmentation;
import com.peng.certrecognition.service.RecognitionService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest
public class OcrTest {

    @Autowired
    private RecognitionService recognitionService;

    @Test
    public void test1() {
        String imgName = "08.jpg";
        HorizontalCorrection.contoursBase(Constants.PHOTO_PATH, imgName);
        Map<String, String> result = Segmentation.segment(imgName);
        if (result != null) {
            List<Map<String, Object>> data = Recognition.recognize(imgName, result);
            recognitionService.addRecognition(imgName, "", data);
        }
    }

}
