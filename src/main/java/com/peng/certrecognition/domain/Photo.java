package com.peng.certrecognition.domain;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Document(collection = "photo")
public class Photo {

    @Id
    private String id;

    @Field("userid")
    private String userid;

    @Field("original_name")
    private String originalname;

    @Field("filename")
    private String filename;

    @Field("size")
    private long size;

    @Field("type")
    private String type;

    @Field("path")
    private String path;

    @Field("label")
    private Map<String, Object> label;

    @Field("create_time")
    @CreatedDate
    private Date createTime;

    @Field("update_time")
    @LastModifiedDate
    private Date updateTime;

    public Photo() {
    }

    public Photo(String userid, String originalname, String filename, long size, String type, String path, Map<String, Object> label, Date createTime, Date updateTime) {
        this.userid = userid;
        this.originalname = originalname;
        this.filename = filename;
        this.size = size;
        this.type = type;
        this.path = path;
        this.label = label;
        this.createTime = createTime;
        this.updateTime = updateTime;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getOriginalname() {
        return originalname;
    }

    public void setOriginalname(String originalname) {
        this.originalname = originalname;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Map<String, Object> getLabel() {
        return label;
    }

    public void setLabel(Map<String, Object> label) {
        this.label = label;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    @Override
    public String toString() {
        return "Photo{" +
                "id='" + id + '\'' +
                ", userid='" + userid + '\'' +
                ", originalname='" + originalname + '\'' +
                ", filename='" + filename + '\'' +
                ", size=" + size +
                ", type='" + type + '\'' +
                ", path='" + path + '\'' +
                ", label=" + label +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                '}';
    }

    public Map<String, Object> toMapFromUpload() {
        Map<String, Object> map = new HashMap<>();
        map.put("filename", filename);
        return map;
    }

    public Map<String, Object> toMapFromList() {
        Map<String, Object> map = new HashMap<>();
        map.put("filename", filename);
        map.put("createTime", createTime);
        return map;
    }

    public Map<String, Object> toMapFromView() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("userid", userid);
        map.put("originalname", originalname);
        map.put("filename", filename);
        map.put("size", size);
        map.put("type", type);
        map.put("path", path);
        map.put("label", label);
        map.put("createTime", createTime);
        return map;
    }

    public Map<String, Object> toMapFromRecognition() {
        Map<String, Object> map = new HashMap<>();
        map.put("originalname", originalname);
        map.put("filename", filename);
        map.put("label", label);
        map.put("createTime", createTime);
        return map;
    }

}
