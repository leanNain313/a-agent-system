package com.ai.service;

import com.ai.common.ResultPage;
import com.ai.model.dto.user.*;
import com.ai.model.entity.User;
import com.ai.model.vo.user.UserVO;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.servlet.http.HttpServletRequest;


/**
* @author 惘念
* @description 针对表【user】的数据库操作Service
* @createDate 2025-07-16 20:36:48
*/
public interface UserService extends IService<User> {

    void register(RegisterRequest registerRequest);

    UserVO doLogin(LoginRequest loginRequest , HttpServletRequest request);

    void loginOut(HttpServletRequest request);

    UserVO getCurrentUser(HttpServletRequest request);

    void updateUserByAdmin(UpdateUserRequest updateUserRequest, HttpServletRequest request);

    void updateUserByUser(UpdateUserRequest updateUserRequest, HttpServletRequest request);

    void removeUserById(Long id, HttpServletRequest request);

    ResultPage<UserVO> getUserList(UserPageRequest userPageRequest);

    void addUser(AddUserRequest addUserRequest);

    void disabledUser(AccountDisableRequest accountDisableRequest);

}
