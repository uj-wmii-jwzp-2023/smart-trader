package uj.jwzp.smarttrader.model;

import java.util.List;

public class OrderValidationResponse {
    private boolean valid;
    private List<String> errors;

    public OrderValidationResponse() {

    }

    public OrderValidationResponse(boolean valid, List<String> errors) {
        this.valid = valid;
        this.errors = errors;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public List<String> getErrors() {
        return errors;
    }

    public void setErrors(List<String> errors) {
        this.errors = errors;
    }
}
