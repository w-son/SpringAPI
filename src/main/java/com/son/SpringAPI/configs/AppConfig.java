package com.son.SpringAPI.configs;

import com.son.SpringAPI.accounts.Account;
import com.son.SpringAPI.accounts.AccountRepository;
import com.son.SpringAPI.accounts.AccountRole;
import com.son.SpringAPI.accounts.AccountService;
import com.son.SpringAPI.common.AppProperties;
import com.son.SpringAPI.events.Event;
import com.son.SpringAPI.events.EventRepository;
import com.son.SpringAPI.events.EventStatus;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Set;

@Configuration
public class AppConfig {

    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public ApplicationRunner applicationRunner() {
        return new ApplicationRunner() {

            @Autowired
            AccountService accountService;
            @Autowired
            AppProperties appProperties;
            @Autowired
            EventRepository eventRepository;

            @Override
            public void run(ApplicationArguments args) throws Exception {
                Account admin = Account.builder()
                        .email(appProperties.getAdminUsername())
                        .password(appProperties.getAdminPassword())
                        .roles(Set.of(AccountRole.ADMIN))
                        .build();
                accountService.saveAccount(admin);

                Account user = Account.builder()
                        .email(appProperties.getUserUsername())
                        .password(appProperties.getUserPassword())
                        .roles(Set.of(AccountRole.USER))
                        .build();
                accountService.saveAccount(user);
                for(int i=0; i<10; i++) {
                    generateEvent(i, user);
                }

            }

            private Event generateEvent(int i, Account account) {
                Event event = buildEvent(i);
                event.setManager(account);
                return this.eventRepository.save(event);
            }

            private Event buildEvent(int i) {
                return Event.builder()
                        .name("event " + i)
                        .description("REST API Development with Spring")
                        .beginEnrollmentDateTime(LocalDateTime.of(2020, 5, 4, 18, 32))
                        .closeEnrollmentDateTime(LocalDateTime.of(2020, 5, 4, 23, 59))
                        .beginEventDateTime(LocalDateTime.of(2020, 5, 4, 18, 32))
                        .endEventDateTime(LocalDateTime.of(2020, 5, 4, 23, 59))
                        .basePrice(100)
                        .maxPrice(200)
                        .limitOfEnrollment(100)
                        .location("강남의 D2 스타트업 팩토리")
                        .free(false)
                        .offline(true)
                        .eventStatus(EventStatus.DRAFT)
                        .build();
            }

        };
    }

}
