package com.example.heydibe.user.repository;

import com.example.heydibe.user.entity.SocialAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface SocialAccountRepository extends JpaRepository<SocialAccount, Long> {

    Optional<SocialAccount> findByProviderAndProviderUserId(String provider, String providerUserId);

    List<SocialAccount> findByUserId(Long userId);

    Optional<SocialAccount> findByUserIdAndProvider(Long userId, String provider);

    @Modifying
    @Transactional
    @Query("DELETE FROM SocialAccount sa WHERE sa.userId = :userId")
    void deleteAllByUserId(@Param("userId") Long userId);
}
