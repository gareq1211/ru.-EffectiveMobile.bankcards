//package ru.effectivemobile.bankcards.controller;
//
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.context.annotation.Import;
//import org.springframework.security.test.context.support.WithMockUser;
//import org.springframework.test.web.servlet.MockMvc;
//import ru.effectivemobile.bankcards.config.SecurityConfig;
//import ru.effectivemobile.bankcards.service.CardService;
//
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//
//@WebMvcTest(CardController.class)
//@Import(SecurityConfig.class)
//class CardControllerMyTest {
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @MockBean
//    private CardService cardService;
//
//    @Test
//    @WithMockUser(username = "test@example.com", roles = "USER")
//    void shouldReturnOk_WhenAccessingMyCards() throws Exception {
//        mockMvc.perform(get("/cards/my"))
//                .andExpect(status().isOk());
//    }
//
//    @Test
//    void shouldReturnUnauthorized_WhenNotAuthenticated() throws Exception {
//        mockMvc.perform(get("/cards/my"))
//                .andExpect(status().isUnauthorized());
//    }
//}