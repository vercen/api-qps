package com.ksyun.train.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author vercen
 * @version 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RespBean {
    private String message;

    public static RespBean success(String message){
        return new RespBean(message);
    }

}
