package com.son.SpringAPI.accounts;

import lombok.*;

import javax.persistence.*;
import java.util.Set;

@Entity
@Builder
@Getter @Setter @EqualsAndHashCode(of = "id")
@NoArgsConstructor
@AllArgsConstructor
public class Account {

    @Id @GeneratedValue
    private Integer id;

    @Column(unique = true)
    private String email;

    private String password;

    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(value = EnumType.STRING)
    private Set<AccountRole> roles;

}
