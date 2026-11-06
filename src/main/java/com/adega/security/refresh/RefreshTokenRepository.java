package com.adega.security.refresh;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByHashToken(String hashToken);

    List<RefreshToken> findByUsuarioId(Long usuarioId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select token from RefreshToken token where token.hashToken = :hashToken")
    Optional<RefreshToken> buscarPorHashTokenComBloqueio(@Param("hashToken") String hashToken);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update RefreshToken token set token.revogado = true where token.usuarioId = :usuarioId and token.revogado = false")
    int revogarTodosDoUsuario(@Param("usuarioId") Long usuarioId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            delete from RefreshToken token
            where token.expiraEm < :agora
               or (token.revogado = true and token.criadoEm < :limite)
            """)
    int limparTokensAntigos(@Param("agora") Instant agora, @Param("limite") Instant limite);
}
