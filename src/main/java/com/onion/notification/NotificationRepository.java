package com.onion.notification;

import com.onion.domain.Notification;
import com.onion.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional(readOnly = true)
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    long countByUserAndChecked(User user, boolean checked);

    @Transactional
    List<Notification> findByUserAndCheckedOrderByCreatedDateTimeDesc(User user, boolean checked);

    @Transactional
    void deleteByUserAndChecked(User user, boolean checked);

    @Query("SELECT n FROM Notification n WHERE n.title = ?1")
    boolean getByTitle(String title);
}
