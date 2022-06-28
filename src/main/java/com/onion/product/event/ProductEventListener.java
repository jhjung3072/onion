package com.onion.product.event;


import com.onion.domain.Notification;
import com.onion.domain.User;
import com.onion.domain.product.Product;
import com.onion.notification.NotificationRepository;
import com.onion.notification.NotificationType;
import com.onion.product.ProductRepository;
import com.onion.user.UserPredicates;
import com.onion.user.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;


@Slf4j
@Async
@Component
@Transactional
@RequiredArgsConstructor
public class ProductEventListener {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;

    @EventListener
    public void handleProductCreatedEvent(ProductCreatedEvent productCreatedEvent) {
        Product product = productRepository.findProductWithTagsAndLocationById(productCreatedEvent.getProduct().getId());
        Iterable<User> users = userRepository.findAll(UserPredicates.findByTagsAndLocation(product.getTags(), product.getLocation()));
        users.forEach(user -> {
            if (user.isProductPostedByWeb()) {
                createNotification(product, user, product.getShortDescription(), NotificationType.PRODUCT_CREATED);
            }
        });
    }


    private void createNotification(Product product, User user, String message, NotificationType notificationType) {
        Notification notification = new Notification();
        notification.setTitle(product.getName());
        notification.setLink("/p/" + product.getId());
        notification.setChecked(false);
        notification.setCreatedDateTime(LocalDateTime.now());
        notification.setMessage(message);
        notification.setUser(user);
        notification.setNotificationType(notificationType);
        notificationRepository.save(notification);
    }


}
