package com.JSR.PharmaFlow.Controllers;
import com.JSR.PharmaFlow.Utils.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;

@Slf4j

@RestController
public class OAuth2Controller {

    @Autowired
    private JwtUtil jwtUtil;



    @GetMapping("/oauth2/state")
    public ResponseEntity<String> generateStateToken() {
        String stateToken = UUID.randomUUID().toString();
        return ResponseEntity.ok(stateToken); // Return plain state (React stores it)
    }




    @GetMapping("/oauth2/callback")
    public void oauth2Callback(
            @RequestParam String token,
            HttpServletResponse response
    ) throws IOException {
        // Return HTML that sends token to opener
        String html = """
            <html>
              <body>
                <script>
                  window.opener.postMessage(
                    { type: 'OAUTH_SUCCESS', token: '%s' },
                    'http://localhost:5173'
                  );
                  window.close();
                </script>
                <p>Login successful! You can close this window.</p>
              </body>
            </html>
            """.formatted(token);

        response.setContentType("text/html");
        response.getWriter().write(html);
    }


}

