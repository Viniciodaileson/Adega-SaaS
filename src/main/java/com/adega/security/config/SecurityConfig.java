package com.adega.security.config;

import com.adega.security.audit.AuditFilter;
import com.adega.security.auth.LimiteTentativasFilter;
import com.adega.security.jwt.JwtAuthenticationFilter;
import com.adega.security.jwt.JwtService;
import com.adega.security.user.UsuarioRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   JwtService jwtService,
                                                   UsuarioRepository usuarios,
                                                   ApplicationEventPublisher publicador,
                                                   LimiteTentativasFilter filtroLimite,
                                                   CorsConfigurationSource fonteCors,
                                                   @Value("${spring.h2.console.enabled:false}")
                                                   boolean h2ConsoleHabilitado) throws Exception {

        JwtAuthenticationFilter filtroJwt = new JwtAuthenticationFilter(jwtService, usuarios);
        AuditFilter filtroAuditoria = new AuditFilter(publicador);

        return http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(fonteCors))
                .sessionManagement(sessao -> sessao.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .headers(cabecalhos -> {
                    // O console do H2 usa frames; fora do ambiente de desenvolvimento,
                    // o cabeçalho mais restritivo é aplicado.
                    if (h2ConsoleHabilitado) {
                        cabecalhos.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin);
                    } else {
                        cabecalhos.frameOptions(HeadersConfigurer.FrameOptionsConfig::deny);
                    }

                    cabecalhos
                            .contentTypeOptions(Customizer.withDefaults())
                            .referrerPolicy(referencia -> referencia.policy(
                                    ReferrerPolicyHeaderWriter.ReferrerPolicy.NO_REFERRER));
                })
                .authorizeHttpRequests(autorizacao -> {
                    autorizacao
                            .requestMatchers(HttpMethod.POST,
                                    "/api/auth/login",
                                    "/api/auth/refresh",
                                    "/api/auth/logout")
                            .permitAll()
                            .requestMatchers(HttpMethod.GET, "/.well-known/jwks.json")
                            .permitAll()
                            .requestMatchers("/error")
                            .permitAll();

                    if (h2ConsoleHabilitado) {
                        autorizacao.requestMatchers("/h2-console/**").permitAll();
                    }

                    autorizacao.anyRequest().authenticated();
                })
                .exceptionHandling(excecoes -> excecoes
                        .authenticationEntryPoint(this::responderNaoAutenticado)
                        .accessDeniedHandler(this::responderAcessoNegado))
                .addFilterBefore(filtroJwt, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(filtroAuditoria, JwtAuthenticationFilter.class)
                .addFilterAfter(filtroLimite, AuditFilter.class)
                .build();
    }

    @Bean
    public CorsConfigurationSource fonteCors(
            @Value("${adega.cors.origens:http://localhost:5173,http://127.0.0.1:5173}")
            String origensPermitidas) {

        CorsConfiguration configuracao = new CorsConfiguration();

        configuracao.setAllowedOrigins(Arrays.stream(origensPermitidas.split(","))
                .map(String::trim)
                .filter(origem -> !origem.isEmpty())
                .toList());
        configuracao.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuracao.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        configuracao.setExposedHeaders(List.of("Authorization"));
        configuracao.setAllowCredentials(false);
        configuracao.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource fonte = new UrlBasedCorsConfigurationSource();
        fonte.registerCorsConfiguration("/**", configuracao);

        return fonte;
    }

    @Bean
    public FilterRegistrationBean<LimiteTentativasFilter> registroLimiteDesativado(
            LimiteTentativasFilter filtro) {

        FilterRegistrationBean<LimiteTentativasFilter> registro = new FilterRegistrationBean<>(filtro);
        registro.setEnabled(false);

        return registro;
    }

    private void responderNaoAutenticado(HttpServletRequest requisicao,
                                         HttpServletResponse resposta,
                                         AuthenticationException excecao) throws IOException {
        escreverErro(resposta, HttpStatus.UNAUTHORIZED, "Não autenticado",
                "Informe um token de acesso válido.");
    }

    private void responderAcessoNegado(HttpServletRequest requisicao,
                                       HttpServletResponse resposta,
                                       AccessDeniedException excecao) throws IOException {
        escreverErro(resposta, HttpStatus.FORBIDDEN, "Acesso negado",
                "Você não tem permissão para acessar este recurso.");
    }

    private void escreverErro(HttpServletResponse resposta,
                              HttpStatus status,
                              String erro,
                              String detalhe) throws IOException {

        resposta.setStatus(status.value());
        resposta.setContentType(MediaType.APPLICATION_JSON_VALUE);
        resposta.setCharacterEncoding(StandardCharsets.UTF_8.name());

        resposta.getWriter().write("{\"status\":" + status.value()
                + ",\"erro\":\"" + erro + "\""
                + ",\"detalhes\":[\"" + detalhe + "\"]}");
    }
}
