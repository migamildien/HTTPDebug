package com.example.imrahn.httpdebug;

/**
 * Created by Imrahn on 2015-02-25.
 */
public class SubmitResponse {



    public int getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }

    private int responseCode;

    public String getResponseBody() {
        return responseBody;
    }

    public void setResponseBody(String responseBody) {
        this.responseBody = responseBody;
    }

    private String responseBody;
}
