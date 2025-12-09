package com.JSR.PharmaFlow.Repository;

import com.JSR.PharmaFlow.Enums.OAuthProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.JSR.PharmaFlow.Entity.Users;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsersRepository extends JpaRepository<Users, Long> {

    boolean existsByEmail(String email);

    boolean existsByFullName(String fullName);

    Optional<Users> findByFullName(String fullName);

    Optional<Users>findByEmail(String email);

    void  deleteByFullName(String username);


    Optional<Users> findByEmailAndAuthProvider(String email, OAuthProvider oAuthProvider);

    Optional<Users> findByUsername(String username);
}
