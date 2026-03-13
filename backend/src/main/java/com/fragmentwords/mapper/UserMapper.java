package com.fragmentwords.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fragmentwords.model.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * User Mapper
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
}
