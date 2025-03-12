import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.wavefront.WavefrontProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = WavefrontProperties.Application.class)
@AutoConfigureMockMvc
public class CsrfProtectionTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser
    public void testCsrfProtectionBlock() throws Exception {
        // Attempt to perform a POST request without CSRF token
        mockMvc.perform(MockMvcRequestBuilders.post("/done"))
                .andExpect(status().isForbidden());
        mockMvc.perform(MockMvcRequestBuilders.post("/register"))
                .andExpect(status().isForbidden());
        mockMvc.perform(MockMvcRequestBuilders.post("/login")).andExpect(status().isForbidden());
        mockMvc.perform(MockMvcRequestBuilders.post("/create")).andExpect(status().isForbidden());
        mockMvc.perform(MockMvcRequestBuilders.post("/extra/import")).andExpect(status().isForbidden());
        mockMvc.perform(MockMvcRequestBuilders.post("/comment")).andExpect(status().isForbidden());

    }

}