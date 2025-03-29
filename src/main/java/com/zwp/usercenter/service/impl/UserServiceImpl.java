package com.zwp.usercenter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zwp.usercenter.model.domain.User;
import com.zwp.usercenter.service.UserService;
import com.zwp.usercenter.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.zwp.usercenter.constant.UserConstant.USER_LOGIN_STATE;

/**
 * 用户服务实现类
* @author zwp
* @description 针对表【user(用户)】的数据库操作Service实现
* @createDate 2025-03-29 12:57:56
*/
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService{

    @Autowired
    private UserMapper userMapper;

    final String SALT = "zwp";


    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword) {
        // 1.校验
        if (StringUtils.isAnyEmpty(userAccount, userPassword, checkPassword)) {
            return -1;
        }
        if (userAccount.length() < 4) {
            return -1;
        }
        if (userPassword.length() < 8 || checkPassword.length() < 8) {
            return -1;
        }
        // 账户不能包含特殊字符
        String validPattern = "[`~!@#$%^&*()+=|{}':;',\\\\[\\\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        /* Pattern.compile(validPattern): 将 validPattern 字符串编译成一个 Pattern 对象。 Pattern 对象是正则表达式的编译表示形式，用于高效地进行匹配操作。
        .matcher(userAccount): 使用编译后的 Pattern 对象创建一个 Matcher 对象。 Matcher 对象负责在输入的 userAccount 字符串中查找与 Pattern 匹配的子序列*/
        if (matcher.find()) {
            return -1;
        }
        // 密码和校验密码相同
        if (!userPassword.equals(checkPassword)) {
            return -1;
        }
        // 账户不能重复
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.eq("userAccount", userAccount);
        Long count = userMapper.selectCount(userQueryWrapper);
        if (count > 0) {
            return -1;
        }
        // 2.加密
        String encryPassword = DigestUtils.md5DigestAsHex((SALT+userPassword).getBytes());
        /*定义一个固定的盐值 SALT 为 "zwp"。 (请注意，这只是一个示例，实际应用中应该使用更安全的盐值生成和管理方式)。
        将盐值 SALT 和用户输入的原始密码 userPassword 拼接在一起。将连接后的字符串转换为字节数组，以便进行哈希处理
        使用 MD5 算法对字节数组进行哈希处理，并将结果转换为十六进制字符串格式。
        将十六进制的哈希密码存储在 encryptPassword 变量中*/
        // 3.插入数据
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryPassword);
        boolean saveResult = this.save(user);
        /* UserServiceImpl 类继承了 ServiceImpl<UserMapper, User>。 这是 MyBatis-Plus 框架提供的一个 通用 Service 实现类 。
        ServiceImpl 已经默认实现了一些基本的增删改查 (CRUD) 操作，包括 save(), updateById(), removeById(), getById() 等方法
        调用 this.save(user) 时，实际上是在调用 ServiceImpl 父类中提供的 save() 方法
        同时利用了 ServiceImpl 提供的默认功能 (例如主键生成) */
        if (!saveResult) {
            return -1;
        }
        return user.getId();
    }

    @Override
    public User userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        // 1.校验
        if (StringUtils.isAnyEmpty(userAccount, userPassword)) {
            return null;
        }
        if (userAccount.length() < 4) {
            return null;
        }
        if (userPassword.length() < 8) {
            return null;
        }
        // 账户不能包含特殊字符
        String validPattern = "[`~!@#$%^&*()+=|{}':;',\\\\[\\\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if (matcher.find()) {
            return null;
        }
        // 2.加密
        String encryPassword = DigestUtils.md5DigestAsHex((SALT+userPassword).getBytes());
        // 查询用户是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        queryWrapper.eq("userPassword", encryPassword);
        User user = userMapper.selectOne(queryWrapper);
        // 用户不存在
        if (user == null) {
            log.info("user login failed,userAccount cannot match userPassword");
            return null;
        }
        // 3.用户信息脱敏
        User safetyUser = getSafetyUser(user);
        // 记录用户的登录态
        request.getSession().setAttribute(USER_LOGIN_STATE, safetyUser);
        return safetyUser;
    }

    /**
     * 用户脱敏
     * @param originUser
     * @return
     */
    @Override
    public User getSafetyUser(User originUser) {
        User safetyUser = new User();
        safetyUser.setId(originUser.getId());
        safetyUser.setUsername(originUser.getUsername());
        safetyUser.setUserAccount(originUser.getUserAccount());
        safetyUser.setAvatarUrl(originUser.getAvatarUrl());
        safetyUser.setGender(originUser.getGender());
        safetyUser.setPhone(originUser.getPhone());
        safetyUser.setEmail(originUser.getEmail());
        safetyUser.setUserRole(originUser.getUserRole());
        safetyUser.setUserStatus(originUser.getUserStatus());
        safetyUser.setCreateTime(originUser.getCreateTime());
        return safetyUser;
    }
}




