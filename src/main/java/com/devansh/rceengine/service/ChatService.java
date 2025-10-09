package com.devansh.rceengine.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class ChatService {
    public final ChatClient chatClient;

    public ChatService(ChatClient.Builder chatClientBuilder){
        this.chatClient = chatClientBuilder.build();
    }

    public String complexityAnalysis(String userCode, String userInput){
        String prompt = """
                For the given code -
                %s
                I have done this complexity Analysis-
                %s
                Task: Verify and correct (if wrong) the complexity of the code.
                Response format-
                1. Verdict (right or wrong)
                2. Correct Time and Space Complexity
                3. Reason
                
                While calculating the time complexity ignore the drivers code, and focus on the main
                Algorithm.
                """.formatted(userCode, userInput);

        String response = chatClient.prompt()
                .user(prompt)
                .call()
                .content()
                .trim();

        return response;
    }
}
