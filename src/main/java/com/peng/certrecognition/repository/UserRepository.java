package com.peng.certrecognition.repository;

import com.peng.certrecognition.domain.User;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserRepository extends MongoRepository<User, String>, UserRepositoryCustom {

    User findByEmail(String email);

    User findById(String id);

}
