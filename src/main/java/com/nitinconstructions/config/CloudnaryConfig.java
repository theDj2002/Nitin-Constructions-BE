package com.nitinconstructions.config;

import com.cloudinary.Cloudinary;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;
@Configuration
public class CloudnaryConfig {
    @Bean
    public Cloudinary getCloudinary(){
        Map config =new HashMap<>();
        config.put("cloud_name","dwkvlhuyv");
        config.put("api_key","324458971992869");
        config.put("api_secret","_n8gSEZNe-kt4Y7pHVv7Vr47px4");
        config.put("secure",true);
        return new Cloudinary(config);
    }
}
