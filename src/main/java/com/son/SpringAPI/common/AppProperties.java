package com.son.SpringAPI.common;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotEmpty;

@Component
@ConfigurationProperties(prefix = "my-app")
@Getter @Setter
public class AppProperties {

    /*
     Dependency 설정을 한 후에 프로젝트를 실행 시키면
     Spring Boot에서 properties에 있는 정보를 주입 시켜준다
     */

    @NotEmpty
    private String adminUsername;

    @NotEmpty
    private String adminPassword;

    @NotEmpty
    private String userUsername;

    @NotEmpty
    private String userPassword;

    @NotEmpty
    private String testUsername;

    @NotEmpty
    private String testPassword;

    @NotEmpty
    private String clientId;

    @NotEmpty
    private String clientSecret;

}
