package com.fragmentwords.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fragmentwords.model.entity.UnknownWord;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UnknownWordMapper extends BaseMapper<UnknownWord> {
}