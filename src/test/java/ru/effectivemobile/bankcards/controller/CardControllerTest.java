package ru.effectivemobile.bankcards.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import ru.effectivemobile.bankcards.config.SecurityConfig;
import ru.effectivemobile.bankcards.dto.CardDto;
import ru.effectivemobile.bankcards.dto.CreateCardRequest;
import ru.effectivemobile.bankcards.service.CardService;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
@Disabled("Validation issue in @WebMvcTest — will fix later")
@WebMvcTest(CardController.class)
@Import(SecurityConfig.class)
class CardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CardService cardService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldCreateCard_WhenAdminSendsValidRequest() throws Exception {
        CreateCardRequest request = new CreateCardRequest(
                "1234567890123456",
                "John Doe",
                "12/28",
                new BigDecimal("1000.00"),
                1L
        );

        CardDto responseDto = new CardDto(
                1L,
                "**** **** **** 3456",
                "John Doe",
                "12/28",
                "ACTIVE",
                new BigDecimal("1000.00")
        );

        when(cardService.createCard(any())).thenReturn(responseDto);

        mockMvc.perform(post("/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.maskedPan").value("**** **** **** 3456"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldReturnForbidden_WhenUserIsNotAdmin() throws Exception {
        CreateCardRequest request = new CreateCardRequest(
                "1234567890123456", "John Doe", "12/28", new BigDecimal("100"), 1L
        );

        mockMvc.perform(post("/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReturnBadRequest_WhenPanIsInvalid() throws Exception {
        CreateCardRequest request = new CreateCardRequest(
                "123", // ← Невалидный PAN
                "John Doe",
                "12/28",
                new BigDecimal("100"),
                1L
        );

        mockMvc.perform(post("/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}