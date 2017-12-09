package com.example.oauth2springtest.controller;

import com.example.oauth2springtest.Application;
import com.example.oauth2springtest.SecurityConfig;
import com.google.gson.JsonParser;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@WebAppConfiguration
@SpringBootTest(classes = Application.class)
public class ProductControllerTest {
    private static final String CONTENT_TYPE = "application/json;charset=UTF-8";

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private FilterChainProxy filterChainProxy;

    private MockMvc mockMvc;

    @Before
    public void setUp() throws Exception {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .addFilter(filterChainProxy)
                .build();
    }

    @Test
    public void verifyAuthentication() throws Exception {
        String accessToken = obtainAccessToken(SecurityConfig.USERNAME, SecurityConfig.PASSWORD);
        assertNotNull(accessToken);

        mockMvc.perform(get("/oauth/check_token?token=" + accessToken)
                .with(httpBasic("clientIdPassword", "secret")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user_name", is(SecurityConfig.USERNAME)));
    }

    private String obtainAccessToken(String username, String password) throws Exception {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "password");
        params.add("client_id", "clientIdPassword");
        params.add("username", username);
        params.add("password", password);

        ResultActions result = mockMvc.perform(post("/oauth/token")
                .params(params)
                .with(httpBasic("clientIdPassword", "secret"))
                .accept(CONTENT_TYPE))
                .andExpect(status().isOk())
                .andExpect(content().contentType(CONTENT_TYPE));

        String resultString = result.andReturn().getResponse().getContentAsString();

        return new JsonParser().parse(resultString).getAsJsonObject().get("access_token").getAsString();
    }
}