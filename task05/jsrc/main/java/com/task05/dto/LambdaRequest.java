package com.task05.dto;

import com.amazonaws.services.dynamodbv2.xspec.S;

public class LambdaRequest {
    private int principalId;
    private Object content;

    public int getPrincipalId() {
        return principalId;
    }

    public void setPrincipalId(int principalId) {
        this.principalId = principalId;
    }

    public Object getContent() {
        return content;
    }

    public void setContent(Object content) {
        this.content = content;
    }
}
