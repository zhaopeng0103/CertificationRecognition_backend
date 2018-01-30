package com.peng.certrecognition.domain;

import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.util.StringUtils;

@Document(collection = "user")
public class User {

    @Field("username")
    private String userName;

    @Field("avatar")
    private String avatar;

    @Field("email")
    @Indexed(unique = true)
    private String email;

    @Field("password")
    private String password;

    @Field("phone")
    private String phone;

    @Field("company")
    private String company;

    @Field("active")
    private boolean active;

    public User() {
    }

    public User(String email) {
        this.avatar = "";
        this.userName = "";
        this.email = email;
        this.phone = "";
        this.company = "";
        this.password = "";
        this.active = true;
    }

    public User(String avatar, String userName, String email, String password,
                String phone, String company, String ownCloudPassword, String ownCloudToken, boolean active) {
        this.avatar = avatar;
        this.userName = userName;
        this.email = email;
        this.phone = phone;
        this.company = company;
        this.password = password;
        this.active = active;
    }

    private String genPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    public boolean isValidPassword(String password) {
        if (StringUtils.isEmpty(password.trim())) {
            return false;
        }
        return BCrypt.checkpw(password, this.password);
    }

    public void setPassword(String password) {
        this.password = genPassword(password);
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        // TODO email format verify
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public String toString() {
        return "User{" +
                "active=" + active +
                ", company='" + company + '\'' +
                ", phone='" + phone + '\'' +
                ", email='" + email + '\'' +
                ", userName='" + userName + '\'' +
                ", avatar='" + avatar + '\'' +
                '}';
    }

}
