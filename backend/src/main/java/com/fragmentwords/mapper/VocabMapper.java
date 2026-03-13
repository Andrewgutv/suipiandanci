package com.fragmentwords.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fragmentwords.model.entity.Vocab;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface VocabMapper extends BaseMapper<Vocab> {
}