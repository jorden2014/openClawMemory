package com.mailbatch.mailbatchsystem.security;

import com.mailbatch.mailbatchsystem.entity.User;
import com.mailbatch.mailbatchsystem.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * 自定义 UserDetailsService 实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("用户不存在: " + username));
        
        // 临时：返回明文密码（开发环境）
        log.info("加载用户 {}，密码（明文）: {}", username, user.getPassword());
        
        Collection<? extends GrantedAuthority> authorities = 
            java.util.Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_" + user.getRole().name())
            );

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPassword())  // 明文密码
                .authorities(authorities)
                .build();
    }
}
