package com.JSR.PharmaFlow.Services;



import com.JSR.PharmaFlow.Entity.Users;
import com.JSR.PharmaFlow.Repository.UsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Service
public class UserLoginService {

    private  final  UsersRepository usersRepository;

    private final KafkaTemplate<String, Object> kafkaTemplate;


    @Autowired
    public UserLoginService(UsersRepository usersRepository, KafkaTemplate<String, Object> kafkaTemplate) {
        this.usersRepository = usersRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    public boolean isFirstLogin(String email){
        Users users = usersRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("user not found with the : "+ email));

        return !users.isHasLoggedIn();
    }


    public void markAsLoggedIn(String email){

        Users users = usersRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("user not found"));
        users.setHasLoggedIn(true);
        usersRepository.save(users);
    }

    public void sendWelcomeEmail(String email , String username){
        Map<String , Object> welcomeEvent = new HashMap<>();
        welcomeEvent.put("email", email);
        welcomeEvent.put("username", username);
        welcomeEvent.put("subject", "Welcome to PharmFlow!");
        welcomeEvent.put("template", "Welcome-email");
        welcomeEvent.put("timestamp", Instant.now().toString());


        // This is your PRODUCER
        // I will send messages to topic "user-welcome-email"
//        kafkaTemplate.send("TOPIC_NAME", KEY, MESSAGE);
        // PUBLISH to Kafka topic
        kafkaTemplate.send("user-welcome-email", email , welcomeEvent);
        // Message is now stored in Kafka!
    }
}
