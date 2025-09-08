package com.ye.mapper;

import com.ye.model.entity.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author 惘念
* @description 针对表【user】的数据库操作Mapper
* @createDate 2025-07-16 20:36:48
* @Entity entity.model.com.ye.User
*/
@Mapper
public interface UserMapper extends BaseMapper<User> {

}




