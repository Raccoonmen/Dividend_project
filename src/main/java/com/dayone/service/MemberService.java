package com.dayone.service;

import com.dayone.exception.ScrapException;
import com.dayone.model.Auth;
import com.dayone.persist.MemberRepository;
import com.dayone.persist.entity.MemberEntity;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import static com.dayone.type.ErrorCode.*;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Slf4j
@Service
@AllArgsConstructor
public class MemberService implements UserDetailsService {

    private final PasswordEncoder passwordEncoder;
    private final MemberRepository memberRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("Loading user by username: {}", username);
        return this.memberRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("couldn't find user -> " + username));
    }

    public MemberEntity register(Auth.SignUp member) {
        log.info("Registering new member with username: {}", member.getUsername());
        // 아이디가 존재하는 경우 exception 발생
        boolean exists = this.memberRepository.existsByUsername(member.getUsername());
        if (exists) {
            throw new ScrapException(ALREADY_EXIST_USER, BAD_REQUEST);
        }

        // ID 생성 가능한 경우, 멤버 테이블에 저장
        // 비밀번호는 암호화 되어서 저장되어야함
        member.setPassword(this.passwordEncoder.encode(member.getPassword()));
        var result = this.memberRepository.save(member.toEntity());
        log.info("New member registered successfully with username: {}", result.getUsername());

        return result;
    }

    public MemberEntity authenticate(Auth.SignIn member) {
        log.info("Authenticating member with username: {}", member.getUsername());
        // id 로 멤버 조회
        var user = this.memberRepository.findByUsername(member.getUsername())
                .orElseThrow(() -> new ScrapException(ID_NOT_FOUND, BAD_REQUEST));
        // 패스워드 일치 여부 확인
        //      - 일치하지 않는 경우 400 status 코드와 적합한 에러 메시지 반환
        //      - 일치하는 경우, 해당 멤버 엔티티 반환
        if (!this.passwordEncoder.matches(member.getPassword(), user.getPassword())) {
            log.error("Password does not match for member with username: {}", member.getUsername());
            throw new ScrapException(PASSWORD_UN_MATCH, BAD_REQUEST);
        }

        return user;
    }
}
