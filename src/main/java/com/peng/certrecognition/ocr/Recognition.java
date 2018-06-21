package com.peng.certrecognition.ocr;

import com.baidu.aip.ocr.AipOcr;
import com.peng.certrecognition.configuration.Constants;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Recognition {

    public static List<Map<String, Object>> recognize(String imgName, Map<String, String> result) {
        String preImgName = imgName.split("\\.")[0];
        String cellsDir = String.format("%s%s%s%s%s", Constants.OCR_PATH, Constants.IMG_CELLS, File.separator, preImgName, File.separator);
        List<Map<String, Object>> data = new ArrayList<>();
        List<String> tempList = new ArrayList<>();

        AipOcr client = new AipOcr(Constants.APP_ID, Constants.API_KEY, Constants.SECRET_KEY);
        client.setConnectionTimeoutInMillis(2000);
        client.setSocketTimeoutInMillis(60000);

        for (Map.Entry<String, String> entry : result.entrySet()) {
            int roi = Integer.parseInt(entry.getKey().split("_")[1]);
            int row = Integer.parseInt(entry.getValue().split("_")[0]);
            int col = Integer.parseInt(entry.getValue().split("_")[1]);
            for (int i = 0; i < row; i++) {
                tempList.clear();
                for (int j = 0; j < col; j++) {
                    String tempText = "";
                    JSONObject jsonObject = client.basicGeneral(cellsDir.concat(String.format("%d_%d_%d.png", roi, i, j)), new HashMap<>());
                    if (jsonObject.has("words_result")) {
                        JSONArray words_result = (JSONArray) jsonObject.get("words_result");
                        for (int k = 0; k < words_result.length(); k++) {
                            tempText = tempText.concat(((JSONObject) words_result.get(k)).get("words").toString());
                        }
                    }
                    tempList.add(tempText);
                }
                if (tempList.size() == 1) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("word_name", "附记");
                    map.put("word", tempList.get(0));
                    data.add(map);
                } else if (tempList.size() % 2 == 0) {
                    for (int k = 0; k < tempList.size() / 2; k++) {
                        Map<String, Object> map = new HashMap<>();
                        map.put("word_name", tempList.get(k * 2));
                        map.put("word", tempList.get(k * 2 + 1));
                        data.add(map);
                    }
                }
            }
        }
        return data;
    }

}
