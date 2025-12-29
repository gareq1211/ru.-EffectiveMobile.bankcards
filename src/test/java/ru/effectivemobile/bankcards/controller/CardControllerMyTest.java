//package ru.effectivemobile.bankcards.controller;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.http.MediaType;
//import org.springframework.security.test.context.support.WithMockUser;
//import org.springframework.test.web.servlet.MockMvc;
//
//import ru.effectivemobile.bankcards.dto.CardDto;
//import ru.effectivemobile.bankcards.service.CardService;
//
//import java.math.BigDecimal;
//import java.util.List;
//
//import static org.mockito.Mockito.when;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//
//@SpringBootTest
//@AutoConfigureMockMvc
//class CardControllerMyTest {
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @MockBean
//    private CardService cardService;
//
//    @Autowired
//    private ObjectMapper objectMapper;
//
//    @Test
//    @WithMockUser(username = "test@example.com", roles = "USER")
//    void shouldReturnMyCards_WhenAuthenticated() throws Exception {
//        CardDto cardDto = new CardDto(
//                1L, "**** **** **** 3456", "John Doe", "12/28", "ACTIVE", new BigDecimal("1000.00")
//        );
//
//        when(cardService.getMyCards()).thenReturn(List.of(cardDto));
//
//        mockMvc.perform(get("/cards/my")
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$").isArray())
//                .andExpect(jsonPath("$.length()").value(1))
//                .andExpect(jsonPath("$[0].maskedPan").value("**** **** **** 3456"));
//    }
//}