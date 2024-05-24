package org.spring.oneplusone.Service;

import com.sun.jdi.request.DuplicateRequestException;
import org.spring.oneplusone.Utils.Error.CustomException;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

@Service
public class RequestManagementService {
    //중복요청되지 않아야 하는 Request를 관리
    private final ConcurrentHashMap<String, Boolean> requsetMap = new ConcurrentHashMap<>();

    public boolean startRequest(String requestId){
        if (requsetMap.putIfAbsent(requestId, Boolean.TRUE)!=null){
//            throw new CustomException();
        }
        return true;
    }
    public void finishRequest(String requestId){
        requsetMap.remove(requestId);
    }
}
