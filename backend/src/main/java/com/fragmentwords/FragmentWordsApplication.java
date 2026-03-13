package com.fragmentwords;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
//@MapperScan("com.fragmentwords.mapper") // 扫描Mapper包
public class FragmentWordsApplication {

    public static void main(String[] args) {
        SpringApplication.run(FragmentWordsApplication.class, args);
    }

}
