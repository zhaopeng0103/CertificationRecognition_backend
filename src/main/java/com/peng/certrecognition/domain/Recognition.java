package com.peng.certrecognition.domain;

import com.peng.certrecognition.configuration.Constants;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.HashMap;
import java.util.Map;

@Document(collection = "recognition")
public class Recognition {

    @Id
    private String id;

    @Field("filename")
    private String filename;

    @Field("path")
    private String path;

    @Field("data")
    private Map<String, Map<String, Object>> data;

    @Field("error_code")
    private int error_code;

    @Field("error_msg")
    private String error_msg;

    public Recognition() {
    }

    public Recognition(String id, String filename, String path, Map<String, Map<String, Object>> data, int error_code, String error_msg) {
        this.id = id;
        this.filename = filename;
        this.path = path;
        this.data = data;
        this.error_code = error_code;
        this.error_msg = error_msg;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Map<String, Map<String, Object>> getData() {
        return data;
    }

    public void setData(Map<String, Map<String, Object>> data) {
        this.data = data;
    }

    public int getError_code() {
        return error_code;
    }

    public void setError_code(int error_code) {
        this.error_code = error_code;
    }

    public String getError_msg() {
        return error_msg;
    }

    public void setError_msg(String error_msg) {
        this.error_msg = error_msg;
    }

    @Override
    public String toString() {
        return "Recognition{" +
                "id='" + id + '\'' +
                ", filename='" + filename + '\'' +
                ", path='" + path + '\'' +
                ", data=" + data +
                ", error_code=" + error_code +
                ", error_msg='" + error_msg + '\'' +
                '}';
    }

    public Map<String, Object> toMap() {
        Map<String, Object> result = new HashMap<>();
        Map<String, Map<String, Object>> map = getData();
        result.put(String.valueOf(map.get(Constants.RECOG_TITLE).get(Constants.WORD_NAME)), map.get(Constants.RECOG_TITLE).get(Constants.WORD));
        result.put(String.valueOf(map.get(Constants.RECOG_NAME).get(Constants.WORD_NAME)), map.get(Constants.RECOG_NAME).get(Constants.WORD));
        result.put(String.valueOf(map.get(Constants.RECOG_SITUATION).get(Constants.WORD_NAME)), map.get(Constants.RECOG_SITUATION).get(Constants.WORD));
        result.put(String.valueOf(map.get(Constants.RECOG_LOCATION).get(Constants.WORD_NAME)), map.get(Constants.RECOG_LOCATION).get(Constants.WORD));
        result.put(String.valueOf(map.get(Constants.RECOG_NUMBER).get(Constants.WORD_NAME)), map.get(Constants.RECOG_NUMBER).get(Constants.WORD));
        result.put(String.valueOf(map.get(Constants.RECOG_RIGHTTYPE).get(Constants.WORD_NAME)), map.get(Constants.RECOG_RIGHTTYPE).get(Constants.WORD));
        result.put(String.valueOf(map.get(Constants.RECOG_RIGHTNATURE).get(Constants.WORD_NAME)), map.get(Constants.RECOG_RIGHTNATURE).get(Constants.WORD));
        result.put(String.valueOf(map.get(Constants.RECOG_USING).get(Constants.WORD_NAME)), map.get(Constants.RECOG_USING).get(Constants.WORD));
        result.put(String.valueOf(map.get(Constants.RECOG_AREA).get(Constants.WORD_NAME)), map.get(Constants.RECOG_AREA).get(Constants.WORD));
        result.put(String.valueOf(map.get(Constants.RECOG_TIMELIMIT).get(Constants.WORD_NAME)), map.get(Constants.RECOG_TIMELIMIT).get(Constants.WORD));
        result.put(String.valueOf(map.get(Constants.RECOG_OTHER).get(Constants.WORD_NAME)), map.get(Constants.RECOG_OTHER).get(Constants.WORD));
        result.put(String.valueOf(map.get(Constants.RECOG_ADDIN).get(Constants.WORD_NAME)), map.get(Constants.RECOG_ADDIN).get(Constants.WORD));
        return result;
    }

}
