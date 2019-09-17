package top.lconcise.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.BeanIds;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import top.lconcise.security.MyAuthenticationFailureHandler;
import top.lconcise.security.MyAuthenticationSuccessHandler;

/**
 * Created by liusj on 2019/7/23
 */
@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {

//    @Autowired
//    private MyAuthenticationSuccessHandler authenticationSuccessHandler;
//    @Autowired
//    private MyAuthenticationFailureHandler authenticationFailureHandler;
//
//    @Bean
//    public PasswordEncoder passwordEncoder() {
//        return new BCryptPasswordEncoder();
//    }


    @Bean(name = BeanIds.AUTHENTICATION_MANAGER)
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

//    @Override
//    protected void configure(HttpSecurity http) throws Exception {
//        http.formLogin() // 表单登录
//                .loginPage("/login.html")       // 登录跳转url
////                .loginPage("/authentication/require")
//                .loginProcessingUrl("/login")   // 处理表单登录url
//                .successHandler(authenticationSuccessHandler)
//                .failureHandler(authenticationFailureHandler)
//                .and()
//                .authorizeRequests()            // 授权配置
//                .antMatchers("/login.html", "/css/**", "/authentication/require").permitAll()  // 无需认证
//                .anyRequest()                   // 所有请求
//                .authenticated()                // 都需要认证
//                .and().csrf().disable();
//
//    }
}