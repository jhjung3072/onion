package com.onion.user;

import com.onion.domain.User;
import com.onion.paging.SearchRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface UserRepository extends SearchRepository<User, Integer> {

    // 이메일로 유저 찾기
    @Query("SELECT u FROM User u WHERE u.email = ?1")
    User findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByNickname(String nickname);

    //유저 검색
    @Query("SELECT u FROM User u WHERE u.email LIKE %?1%")
    Page<User> findAll(String keyword, Pageable pageable);

    // 유저 id와 이름만 반환하기 위해 new사용
    @Query("SELECT NEW User(u.id, u.nickname) FROM User u ORDER BY u.nickname ASC")
    List<User> findAll();

    Integer countById(Integer id);

    // 유저 활성화 업데이트
    @Query("UPDATE User u SET u.enabled = ?2 WHERE u.id = ?1")
    @Modifying
    void updateEnabledStatus(Integer id, boolean enabled);
}
