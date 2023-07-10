package com.dayone.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ErrorCode {
    INTERNAL_SERVER_ERROR("서버 내부 오류가 발생 했습니다."),
    INVALID_REQUEST("잘못된 요청 입니다."),
    ACCESS_DENIED("접근권한 없습니다"),
    FAIL_TO_CONNECT_REDIS_SERVER("레디시 서버에 접속을 실패 하였습니다"),
    COMPANY_NOT_FOUND("존재하지 않는 회사 입니다"),
    COMPANY_ALREADY_SAVED("이미 저장된 회사입니다"),
    ALREADY_EXIST_USER("이미 사용 중인 아이디 입니다"),
    ID_NOT_FOUND("존재하지 않는 아이디 입니다"),
    PASSWORD_UN_MATCH("비밀번호가 일치하지 않습니다"),
    INVALID_TICKER("ticker is empty");
    ;

    private String description;
}
