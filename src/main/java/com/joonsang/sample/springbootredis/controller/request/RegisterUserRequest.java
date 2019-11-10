package com.joonsang.sample.springbootredis.controller.request;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Register User Request
 * @author debugrammer
 * @version 1.0
 * @since 2019-11-10
 */
@NoArgsConstructor
@Data
public class RegisterUserRequest {

    private String username;
}
