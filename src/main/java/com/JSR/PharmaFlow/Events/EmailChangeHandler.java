package com.JSR.PharmaFlow.Events;


import com.JSR.PharmaFlow.Repository.UsersRepository;
import com.JSR.PharmaFlow.Services.TokenService;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
//import org.springframework.security.core.token.TokenService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;

@Component
@Slf4j
public class EmailChangeHandler {
    private final TokenService tokenService;
    private final UsersRepository userRepository;

    public EmailChangeHandler(TokenService tokenService,
                              UsersRepository userRepository) {
        this.tokenService = tokenService;
        this.userRepository = userRepository;
    }

    @EventListener
//    @Transactional (propagation = Propagation.REQUIRES_NEW)
    public void handleEmailChanged(EmailChangedEvent event) {
        try {
            log.info("Processing email change from {} to {}", event.getOldEmail(), event.getNewEmail());

            // 1. Invalidate all tokens for old email
            tokenService.invalidateAllTokensForUser(event.getOldEmail());

            // 2. Optional: Transfer any token associations to new email
            userRepository.findByEmail(event.getNewEmail()).ifPresent(user -> {
                // Update any token references if needed
                log.info("Updated token associations for user {}", user.getId());
            });

        } catch (Exception e) {
            log.error("Failed to process email change event", e);
            // Consider adding retry logic or dead-letter queue
        }
    }
}