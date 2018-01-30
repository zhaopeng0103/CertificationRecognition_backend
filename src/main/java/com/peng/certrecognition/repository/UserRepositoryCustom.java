package com.peng.certrecognition.repository;

import com.peng.certrecognition.domain.User;

public interface UserRepositoryCustom {

    User refreshOwnCloudToken(User user);

}
