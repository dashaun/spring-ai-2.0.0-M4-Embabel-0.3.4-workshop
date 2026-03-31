package com.workshop.lab3;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration test for the Lab 3 code review agent.
 *
 * <p>Requires Ollama running locally at http://localhost:11434 with the
 * {@code qwen2.5-coder:1.5b} model pulled ({@code ollama pull qwen2.5-coder:1.5b}).</p>
 *
 * <p>Uses the Spring Boot 4 idiom: {@code @SpringBootTest} (MOCK web env) +
 * {@code @AutoConfigureMockMvc} from {@code spring-boot-starter-webmvc-test}.
 * The new package is {@code org.springframework.boot.webmvc.test.autoconfigure}
 * — moved from the old {@code ...web.servlet} location during Spring Boot 4
 * modularisation.</p>
 *
 * <p>The source code under review intentionally contains two common Java bugs:
 * <ul>
 *   <li>String identity comparison ({@code ==}) instead of {@code .equals()}</li>
 *   <li>Returning {@code null} instead of {@code Optional<User>}</li>
 * </ul>
 * The assertions confirm that the Embabel GOAP plan ran to completion and
 * produced a structured, non-empty review report.</p>
 */
@SpringBootTest
@AutoConfigureMockMvc
class AgentConfigIntegrationTest {

    // Inline JSON avoids any Jackson 2/3 ObjectMapper ambiguity in the test layer.
    // The content matches the curl / HTTPie example exactly.
    static final String REQUEST_JSON = """
            {
              "sourceCode": "public class UserService {\\n  private List<User> users = new ArrayList<>();\\n\\n  public User findById(String id) {\\n    for (User u : users) {\\n      if (u.getId() == id) return u;\\n    }\\n    return null;\\n  }\\n}",
              "language": "Java",
              "context": "Service layer in a Spring Boot REST API"
            }
            """;

    static final Set<String> VALID_VERDICTS =
            Set.of("APPROVE", "REQUEST_CHANGES", "NEEDS_DISCUSSION");

    @Autowired
    MockMvc mockMvc;

    @Test
    void reviewCode_returnsStructuredReport() throws Exception {
        MvcResult result = mockMvc.perform(
                        post("/review")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(REQUEST_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").isNotEmpty())
                .andExpect(jsonPath("$.body").isNotEmpty())
                .andExpect(jsonPath("$.verdict").isNotEmpty())
                .andReturn();

        String responseJson = result.getResponse().getContentAsString();

        String title   = JsonPath.read(responseJson, "$.title");
        String body    = JsonPath.read(responseJson, "$.body");
        String verdict = JsonPath.read(responseJson, "$.verdict");

        assertThat(title).isNotBlank();
        assertThat(body).isNotBlank();
        assertThat(verdict).isIn(VALID_VERDICTS);

        // Buggy code with == string comparison and null return should not be approved outright
        assertThat(verdict).isNotEqualTo("APPROVE");

        System.out.println("\n=== Code Review Report ===");
        System.out.println("Title  : " + title);
        System.out.println("Verdict: " + verdict);
        System.out.println("Body   :\n" + body);
    }
}
