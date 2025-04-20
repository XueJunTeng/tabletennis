// UserInterestMapper.java
package com.example.tabletennis.mapper;

import com.example.tabletennis.entity.UserInterest;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface UserInterestMapper {
    List<UserInterest> selectByUserId(Long userId);
    void upsert(UserInterest userInterest);
}