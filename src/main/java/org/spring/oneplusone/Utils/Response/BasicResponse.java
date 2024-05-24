package org.spring.oneplusone.Utils.Response;

public interface BasicResponse<T> {
    boolean isSuccess();
    T getResult();
    void setSuccess(boolean success);
    void setResult(T result);
}