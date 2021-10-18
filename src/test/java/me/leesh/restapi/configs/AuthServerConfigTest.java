package me.leesh.restapi.configs;

import me.leesh.restapi.accounts.Account;
import me.leesh.restapi.accounts.AccountRole;
import me.leesh.restapi.accounts.AccountService;
import me.leesh.restapi.common.AppProperties;
import me.leesh.restapi.common.BaseControllerTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthServerConfigTest extends BaseControllerTest {

    @Autowired
    AccountService accountService;

    @Autowired
    AppProperties appProperties;

    /**
     * 본 프로젝트에서는 스프링 시큐리티가 지원하는 6가지 Grant Type 중 Password, Refresh Token 2가지 방법을 지원한다.
     * 최초에는 Password Grant Type 으로 발급을 받는다.
     * Password Grant Token 은 요청-응답이 1 Hop 이다.
     * YouTube, FaceBook 등에서 사용하는 인증 방법이다.
     */
    @Test
    @DisplayName("인증 토큰을 발급받는 테스트")
    public void getAuthToken() throws Exception {

        // given
//        Account user = Account.builder()
//                .email(appProperties.getUserUsername())
//                .password(appProperties.getUserPassword())
//                .roles(Set.of(AccountRole.ADMIN, AccountRole.USER))
//                .build();
//
//        String clientId = "myApp";
//        String clientSecret = "pass";

        this.mockMvc.perform(post("/oauth/token")
                        .with(httpBasic(appProperties.getClientId(), appProperties.getClientSecret()))
                        .param("username", appProperties.getUserUsername())
                        .param("password", appProperties.getUserPassword())
                        .param("grant_type", "password"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("access_token").exists())


        ;


    }


}