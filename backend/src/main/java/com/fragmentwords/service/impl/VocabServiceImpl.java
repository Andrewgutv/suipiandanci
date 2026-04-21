package com.fragmentwords.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fragmentwords.mapper.DevicePreferenceMapper;
import com.fragmentwords.mapper.UserMapper;
import com.fragmentwords.mapper.VocabMapper;
import com.fragmentwords.model.entity.DevicePreference;
import com.fragmentwords.model.entity.User;
import com.fragmentwords.model.entity.Vocab;
import com.fragmentwords.service.VocabService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class VocabServiceImpl extends ServiceImpl<VocabMapper, Vocab> implements VocabService {

    // 模拟设备-词库映射（实际可存入数据库表device_vocab）
    private String currentDeviceVocab = "";
    private Long currentVocabId = 1L; // 默认四级词库

    @Autowired
    private DevicePreferenceMapper devicePreferenceMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    public List<Vocab> getAllVocabs() {
        return this.list();
    }

    @Override
    public Vocab getSelectedVocab(String deviceId) {
        if (deviceId == null || deviceId.isBlank()) {
            return this.getById(1L);
        }

        LambdaQueryWrapper<DevicePreference> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DevicePreference::getDeviceId, deviceId);
        DevicePreference preference = devicePreferenceMapper.selectOne(wrapper);
        if (preference == null || preference.getVocabId() == null) {
            return this.getById(1L);
        }

        Vocab vocab = this.getById(preference.getVocabId());
        return vocab != null ? vocab : this.getById(1L);
    }

    @Override
    public void selectVocab(String deviceId, Long vocabId) {
        if (deviceId == null || deviceId.isBlank() || vocabId == null) {
            return;
        }

        LambdaQueryWrapper<DevicePreference> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DevicePreference::getDeviceId, deviceId);
        DevicePreference preference = devicePreferenceMapper.selectOne(wrapper);

        if (preference == null) {
            preference = new DevicePreference();
            preference.setDeviceId(deviceId);
            preference.setDailyGoal(50);
            preference.setNotificationEnabled(true);
            preference.setSoundEnabled(true);
            preference.setCreateTime(new Date());
        }

        preference.setVocabId(vocabId);
        preference.setUpdateTime(new Date());

        if (preference.getId() == null) {
            devicePreferenceMapper.insert(preference);
        } else {
            devicePreferenceMapper.updateById(preference);
        }

        LambdaQueryWrapper<User> userWrapper = new LambdaQueryWrapper<>();
        userWrapper.eq(User::getDeviceId, deviceId);
        User user = userMapper.selectOne(userWrapper);
        if (user != null) {
            user.setUpdateTime(new Date());
            userMapper.updateById(user);
        }
    }
}
