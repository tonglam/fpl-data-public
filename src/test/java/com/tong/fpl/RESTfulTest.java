package com.tong.fpl;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

/**
 * Create by tong on 2020/8/3
 */
@SpringBootTest()
@AutoConfigureMockMvc
public class RESTfulTest {

    @Autowired
    private MockMvc mockMvc;

    MvcResult mockResult(String url, MultiValueMap<String, String> params) throws Exception {
        return this.mockMvc
                .perform(MockMvcRequestBuilders.get(url)
                        .contentType(MediaType.ALL).params(params))
                .andExpect(MockMvcResultMatchers.status().isOk())
//                .andDo(MockMvcResultHandlers.print())
                .andReturn();
    }

    @ParameterizedTest
    @CsvSource({"/entry/upsertEntryInfo, 1713"})
    void qryEntryEventResult(String url, String entry) throws Exception {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("entry", entry);
        MvcResult mvcResult = this.mockResult(url, params);
        System.out.println(1);
    }

}
