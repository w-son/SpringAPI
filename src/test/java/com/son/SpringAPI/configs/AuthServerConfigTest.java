package com.son.SpringAPI.configs;

import com.son.SpringAPI.accounts.Account;
import com.son.SpringAPI.accounts.AccountRepository;
import com.son.SpringAPI.accounts.AccountRole;
import com.son.SpringAPI.accounts.AccountService;
import com.son.SpringAPI.common.AppProperties;
import com.son.SpringAPI.common.BaseControllerTest;
import com.son.SpringAPI.common.TestDescription;
import com.son.SpringAPI.events.EventRepository;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Set;

import static org.junit.Assert.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class AuthServerConfigTest extends BaseControllerTest {

    @Autowired
    AccountService accountService;
    @Autowired
    EventRepository eventRepository;
    @Autowired
    AccountRepository accountRepository;
    @Autowired
    AppProperties appProperties;

    @Before
    public void setUp() {
        // 테스트를 일괄적으로 실행할 경우 메모리에서 데이터들이 공유가 되기때문에 id 중복 생성 문제가 발생할 수 있다
        eventRepository.deleteAll();
        accountRepository.deleteAll();
    }

    @Test
    @TestDescription("인증 토큰을 발급 받는 테스트")
    public void getAuthToken() throws Exception {
        // Account 정보를 가지고 있는 서버에 대해 인증하는 방법

        String username = appProperties.getTestUsername();
        String password = appProperties.getTestPassword();
        Account account = Account.builder()
                .email(username)
                .password(password)
                .roles(Set.of(AccountRole.ADMIN, AccountRole.USER))
                .build();
        accountService.saveAccount(account);

        String clientId = appProperties.getClientId();
        String clientSecret = appProperties.getClientSecret();

        mockMvc.perform(post("/oauth/token")
                        .with(httpBasic(clientId, clientSecret))
                        .param("username", username)
                        .param("password", password)
                        .param("grant_type", "password"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("access_token").exists());
    }

}