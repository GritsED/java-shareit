package ru.practicum.shareit.exception;

import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ErrorHandlerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        // Тестовый контроллер
        @RestController
        class TestController {
            @GetMapping("/notfound")
            void notFound() {
                throw new NotFoundException("Not found message");
            }

            @GetMapping("/conflict")
            void conflict() {
                throw new ConflictException("Conflict occurred");
            }

            @GetMapping("/validate")
            void validate() {
                throw new ValidationException("Validation failed");
            }

            @PostMapping("/method-arg")
            void methodArg(@Valid @RequestBody ErrorHandlerTest.Dto dto) {
            }

            @GetMapping("/throwable")
            void throwable() {
                throw new RuntimeException("oops");
            }

            @GetMapping("/constraint")
            void constraint() {
                throw new ConstraintViolationException("violation", null);
            }

            @GetMapping("/data-integrity")
            void dataIntegrity() {
                throw new DataIntegrityViolationException("db error");
            }
        }

        mockMvc = MockMvcBuilders
                .standaloneSetup(new TestController())
                .setControllerAdvice(new ErrorHandler())
                .build();
    }

    @Test
    void handleNotFound() throws Exception {
        mockMvc.perform(get("/notfound"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("Not found message"));
    }

    @Test
    void handleConflict() throws Exception {
        mockMvc.perform(get("/conflict"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Conflict occurred"));
    }

    @Test
    void handleValidationException() throws Exception {
        mockMvc.perform(get("/validate"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation failed"));
    }

    @Test
    void handleMethodArgumentNotValid() throws Exception {
        // Пустой email не проходит @Email + @NotBlank
        String json = "{\"email\":\"\"}";
        mockMvc.perform(post("/method-arg")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("must not be blank"));
    }

    @Test
    void handleThrowable() throws Exception {
        mockMvc.perform(get("/throwable"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("An unexpected error occurred."));
    }

    @Test
    void handleConstraintViolation() throws Exception {
        mockMvc.perform(get("/constraint"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Почта уже используется другим пользователем."));
    }

    @Test
    void handleDataIntegrityViolation() throws Exception {
        mockMvc.perform(get("/data-integrity"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Ошибка сохранения данных."));
    }

    private static class Dto {
        @NotBlank(message = "must not be blank")
        private String name;
        @Email(message = "must be a well-formed email address")
        private String email;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }
    }
}