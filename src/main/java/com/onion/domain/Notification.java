package com.onion.domain;

import com.onion.notification.NotificationType;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter @Setter @EqualsAndHashCode(of = "id")
public class Notification {

    @Id @GeneratedValue
    private Long id;

    private String title;

    private String link;

    private String message;

    private boolean checked;

    @ManyToOne
    private User user;

    private LocalDateTime createdDateTime;

    @Enumerated(EnumType.STRING)
    private NotificationType notificationType;

}
