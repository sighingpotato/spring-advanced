package org.example.expert.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
class PasswordEncoderTest {

    @InjectMocks
    private PasswordEncoder passwordEncoder;

    @Test
    void matches_메서드가_정상적으로_동작한다() {
        // given : 원본 비밀번호, 암호화 비밀번호 준비
        String rawPassword = "testPassword";
        String encodedPassword = passwordEncoder.encode(rawPassword);

        // when : 원본과 암호화를 비교. 인자 순서를 원본, 암호화 순서로 맞춘다.
        boolean matches = passwordEncoder.matches(rawPassword, encodedPassword);

        // then : 비교 결과가 true여야 한다.
        assertTrue(matches);
    }
}
