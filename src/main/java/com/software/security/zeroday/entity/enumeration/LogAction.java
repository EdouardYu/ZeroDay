package com.software.security.zeroday.entity.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum LogAction {

    SIGN_IN,
    SIGN_OUT,
    MODIFY_PROFILE,
    MODIFY_PASSWORD,
    DELETE_ACCOUNT,
    CREATE_POST,
    UPDATE_POST,
    DELETE_POST,
}
