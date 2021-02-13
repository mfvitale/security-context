package me.mfvitale.securitycontext;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class SecurityContextDefaultStrategy {

    @Test
    void singleThreadAllWorks() {

        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("user", "pass"));

        assertThat(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).isEqualTo("user");
    }

    @Test
    void multiThreadFail() throws InterruptedException {

        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("user", "pass"));

        AtomicReference<Object> principal = new AtomicReference<>();
        Thread user = new Thread(() -> {
            // do some stuff
            principal.set(SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        });

        user.start();
        user.join();

        assertThat(principal.get()).isEqualTo("user");
    }
}
