package com.JSR.PharmaFlow.Controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.*;

import java.util.*;

@RestController
@RequestMapping ( "/api/chat-gpt" )
@CrossOrigin ( "*" )
public class AIController {

    private static final Logger logger = LoggerFactory.getLogger( AIController.class );


    private final OllamaChatModel ollamaChatModel;


    @Autowired
    public AIController( OllamaChatModel ollamaChatModel ) {
        this.ollamaChatModel = ollamaChatModel;
    }


    @PostMapping ( "/chat" )
    public ResponseEntity < Map < String, String > > chatWithOllama( @RequestBody Map < String, String > request ) {
        try {

            String prompt = request.get( "prompt" );
            String response = ollamaChatModel.call( prompt );
            return ResponseEntity.ok( Map.of( "response" , response ) );

        } catch (Exception ex) {
            logger.error( "Ollama API error" , ex );
            return ResponseEntity.status( HttpStatus.SERVICE_UNAVAILABLE )
                    .body( Map.of( "response" , "Ollama error: " + ex.getMessage() ) );
        }
    }
}

