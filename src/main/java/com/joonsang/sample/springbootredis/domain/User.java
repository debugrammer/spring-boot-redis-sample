package com.joonsang.sample.springbootredis.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * User Domain
 * @author debugrammer
 * @version 1.0
 * @since 2019-11-10
 */
@NoArgsConstructor
@Setter
public class User implements Serializable {

    @Getter
    private String username;

    @Getter
    @JsonProperty("created_at")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime createdAt;
}
