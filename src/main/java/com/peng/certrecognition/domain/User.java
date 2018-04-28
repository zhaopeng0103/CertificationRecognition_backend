package com.peng.certrecognition.domain;

import com.peng.certrecognition.util.DateUtils;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Document(collection = "user")
public class User {

    @Id
    private String id;

    @Field("username")
    private String username;

    @Field("avatar")
    private String avatar;

    @Field("email")
    @Indexed(unique = true)
    private String email;

    @Field("password")
    private String password;

    @Field("phone")
    private String phone;

    @Field("sex")
    private String sex;

    @Field("birthday")
    private String birthday;

    @Field("active")
    private boolean active;

    @Field("create_time")
    @CreatedDate
    private Date createTime;

    @Field("update_time")
    @LastModifiedDate
    private Date updateTime;

    public User() {
    }

    public User(String username, String avatar, String email, String password, String phone, String sex, String birthday, boolean active, Date createTime, Date updateTime) {
        this.username = username;
        this.avatar = avatar;
        this.email = email;
        this.password = password;
        this.phone = phone;
        this.sex = sex;
        this.birthday = birthday;
        this.active = true;
        this.createTime = createTime;
        this.updateTime = updateTime;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
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
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", avatar='" + avatar + '\'' +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", phone='" + phone + '\'' +
                ", sex='" + sex + '\'' +
                ", active=" + active +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                '}';
    }

    public Map<String, Object> toMapFromRegister() {
        Map<String, Object> map = new HashMap<>();
        map.put("userId", id);
        map.put("username", username);
        map.put("email", email);
        map.put("create_time", DateUtils.getTransformDate(createTime));
        return map;
    }

    public Map<String, Object> toMapFromInfo() {
        Map<String, Object> map = new HashMap<>();
        map.put("userId", id);
        map.put("username", username);
        map.put("avatar", avatar);
        map.put("email", email);
        map.put("phone", phone);
        map.put("sex", sex);
        map.put("birthday", birthday);
        map.put("create_time", DateUtils.getTransformDate(createTime));
        return map;
    }

    public Map<String, Object> toMapFromUpdate() {
        Map<String, Object> map = new HashMap<>();
        map.put("userId", id);
        map.put("email", email);
        map.put("username", username);
        map.put("avatar", avatar);
        map.put("phone", phone);
        map.put("sex", sex);
        map.put("birthday", birthday);
        map.put("update_time", DateUtils.getTransformDate(updateTime));
        return map;
    }

}
