package com.son.SpringAPI.accounts;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

public class AccountAdapter extends User {

    /*
     권한에 따른 링크 추가 과정에서
     - 이벤트 조회시 인증정보 존재 확인 -> security context에 User 정보가 있는지만 확인하면 됨
     - 이벤트 생성시 이벤트에 manager정보 주입 -> User정보가 아닌 Account 형태의 정보로의 변환이 필요하다

       이 클래스를 UserDetails의 loadByUsername 에 적용하여 기존의 User을 리턴하는 코드에 적용한다
     */

    private Account account;

    public AccountAdapter(Account account) {
        super(account.getEmail(), account.getPassword(), authorities(account.getRoles()));
        this.account = account;
    }

    private static Collection<? extends GrantedAuthority> authorities(Set<AccountRole> roles) {
        return roles.stream()
                .map(r -> new SimpleGrantedAuthority("ROLE_" + r.name()))
                .collect(Collectors.toSet());
    }

    public Account getAccount() {
        return account;
    }

}
