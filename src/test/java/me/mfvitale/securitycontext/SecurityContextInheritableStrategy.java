package me.mfvitale.securitycontext;

import me.mfvitale.securitycontext.helpers.SecurityContextHelper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.concurrent.DelegatingSecurityContextExecutor;
import org.springframework.security.concurrent.DelegatingSecurityContextExecutorService;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class SecurityContextInheritableStrategy {


    @Test
    void singleThreadAllWorks() {
        SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_INHERITABLETHREADLOCAL);

        SecurityContextHolder.setContext(SecurityContextHelper.createContext("user", "pass"));

        assertThat(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).isEqualTo("user");
    }

    @Test
    void multiThreadAllWorksNow() throws InterruptedException {

        SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_INHERITABLETHREADLOCAL);

        SecurityContextHolder.setContext(SecurityContextHelper.createContext("user", "pass"));

        AtomicReference<Object> principal = new AtomicReference<>();
        Thread user = new Thread(() -> {
            // do some stuff
            principal.set(SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        });

        user.start();
        user.join();

        assertThat(principal.get()).isEqualTo("user");
    }

    @Test
    void threadPoolStillFail() throws InterruptedException {

        SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_INHERITABLETHREADLOCAL);

        SecurityContextHolder.setContext(SecurityContextHelper.createContext("user1", "pass"));

        ExecutorService executor = Executors.newSingleThreadExecutor();

        AtomicReference<Object> principal = new AtomicReference<>();
        print(SecurityContextHolder.getContext());
        executor.execute(() -> {
            // do some stuff
            print(SecurityContextHolder.getContext());
            principal.set(SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        });
        executor.awaitTermination(100, TimeUnit.MILLISECONDS);

        assertThat(principal.get()).isEqualTo("user1");

        SecurityContextHolder.setContext(SecurityContextHelper.createContext("user2", "pass"));
        print(SecurityContextHolder.getContext());
        executor.execute(() -> {
            // do some stuff
            print(SecurityContextHolder.getContext());
            principal.set(SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        });
        executor.awaitTermination(100, TimeUnit.MILLISECONDS);

        assertThat(principal.get()).isEqualTo("user2");
    }


    @Test
    void threadPoolAllWorks() throws InterruptedException {

        SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_INHERITABLETHREADLOCAL);

        SecurityContextHolder.setContext(SecurityContextHelper.createContext("user1", "pass"));

        ExecutorService originalExecutor = Executors.newSingleThreadExecutor();
        DelegatingSecurityContextExecutorService executor = new DelegatingSecurityContextExecutorService(originalExecutor);

        AtomicReference<Object> principal = new AtomicReference<>();
        print(SecurityContextHolder.getContext());
        executor.execute(() -> {
            // do some stuff
            print(SecurityContextHolder.getContext());
            principal.set(SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        });
        executor.awaitTermination(100, TimeUnit.MILLISECONDS);

        assertThat(principal.get()).isEqualTo("user1");

        SecurityContextHolder.setContext(SecurityContextHelper.createContext("user2", "pass"));
        print(SecurityContextHolder.getContext());
        executor.execute(() -> {
            // do some stuff
            print(SecurityContextHolder.getContext());
            principal.set(SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        });
        executor.awaitTermination(100, TimeUnit.MILLISECONDS);

        assertThat(principal.get()).isEqualTo("user2");
    }

    private static void print(SecurityContext securityContext) {
        System.out.println(Thread.currentThread().getName() + "="+ securityContext.getAuthentication().getPrincipal());
    }
}
