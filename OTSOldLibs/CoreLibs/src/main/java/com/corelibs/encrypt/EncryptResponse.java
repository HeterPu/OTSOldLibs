package com.corelibs.encrypt;

import java.io.Serializable;

/**
 * Created by admin on 2016/8/4.
 */
public class EncryptResponse implements Serializable {

    public String string;
    public String sign;
    public String random;


    public EncryptResponse(String random, String sign, String string) {
        this.random = random;
        this.sign = sign;
        this.string = string;
    }


    public EncryptResponse() {
    }

    public String getRandom() {

        return random;
    }

    public void setRandom(String random) {
        this.random = random;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public String getString() {
        return string;
    }

    public void setString(String string) {
        this.string = string;
    }

    @Override
    public String toString() {
        return "EncryptResponse{" +
                "random='" + random + '\'' +
                ", string='" + string + '\'' +
                ", sign='" + sign + '\'' +
                '}';
    }
}
