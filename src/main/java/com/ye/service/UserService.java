package com.ye.service;

import com.ye.common.ResultPage;
import com.ye.model.dto.user.*;
import com.ye.model.entity.User;
import com.ye.model.vo.user.UserVO;
import com.baomidou.mybatisplus.extension.service.IService;


/**
* @author 惘念
* @description 针对表【user】的数据库操作Service
* @createDate 2025-07-16 20:36:48
*/
public interface UserService extends IService<User> {

    void register(RegisterRequest registerRequest);

    UserVO doLogin(LoginRequest loginRequest);

    void resetPassword(RegisterRequest registerRequest);

    void loginOut();

    void updateUserByAdmin(UpdateUserRequest updateUserRequest);

    void updateUserByUser(UpdateUserRequest updateUserRequest);

    void removeUserById(Long id);

    ResultPage<UserVO> getUserList(UserPageRequest userPageRequest);

    void addUser(AddUserRequest addUserRequest);

    void disabledUser(AccountDisableRequest accountDisableRequest);

    UserVO getUserDetailById(Long id);

    void disabledHandle(long disableTime);

    void AuthLevelToTwo(String code);
}
