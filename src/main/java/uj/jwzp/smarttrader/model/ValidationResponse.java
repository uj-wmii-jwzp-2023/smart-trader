package uj.jwzp.smarttrader.model;

import java.util.ArrayList;
import java.util.List;

public class ValidationResponse {
    private List<String> messages;

    public ValidationResponse() {
        messages = new ArrayList<>();
    }

    public ValidationResponse(List<String> messages) {
        this.messages = messages;
    }

    public boolean isValid() {
        return messages.isEmpty();
    }

    public void addMessages(List<String> messages) {
        this.messages.addAll(messages);
    }

    public void addMessage(String message) {
        messages.add(message);
    }

    public List<String> getMessages() {
        return messages;
    }

    public void setMessages(List<String> messages) {
        this.messages = messages;
    }
}
