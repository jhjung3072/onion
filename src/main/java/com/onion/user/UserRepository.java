package com.onion.user;

import com.onion.paging.SearchRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

import java.util.List;

public interface UserRepository extends SearchRepository<User, Integer> , QuerydslPredicateExecutor<User> {

    // 이메일로 유저 찾기
    @Query("SELECT u FROM User u WHERE u.email = ?1")
    User findByEmail(String email);

    @Query("SELECT u FROM User u WHERE u.nickname = ?1")
    User findByNickname(String nickname);

    boolean existsByEmail(String email);

    boolean existsByNickname(String nickname);

    
    // 유저 키워드 검색(id, 이메일, 닉네임) by 관리자
    @Query("SELECT u FROM User u WHERE CONCAT(u.id, u.email, u.nickname) LIKE %?1%")
    Page<User> findAll(String keyword, Pageable pageable);

    // 유저 id와 이름만 반환하기 위해 new사용
    @Query("SELECT NEW User(u.id, u.nickname) FROM User u ORDER BY u.nickname ASC")
    List<User> findAll();

    Integer countById(Integer id);

    // 유저 활성화 업데이트 by 관리자
    @Query("UPDATE User u SET u.enabled = ?2 WHERE u.id = ?1")
    @Modifying
    void updateEnabledStatus(Integer id, boolean enabled);

    // 알림 상태 변경 by 유저
    @Query("UPDATE User u SET u.productPostedByWeb = ?2 where u.id=?1")
    @Modifying
    void updateNotificationStatus(Integer id, boolean enabled);

    // 유저의 인증상태(로그인방법)업데이트
    @Query("UPDATE User u SET u.authenticationType = ?2 WHERE u.id = ?1")
    @Modifying
    void updateAuthenticationType(Integer userId, AuthenticationType type);
}
