package com.onion.notification;

import com.onion.ControllerHelper;
import com.onion.domain.Notification;
import com.onion.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationRepository repository;
    private final ControllerHelper controllerHelper;

    private final NotificationService service;

    @GetMapping("/notifications")
    public String getNotifications(HttpServletRequest request, Model model) {
        User user = controllerHelper.getAuthenticatedUser(request);
        List<Notification> notifications = repository.findByUserAndCheckedOrderByCreatedDateTimeDesc(user, false);
        long numberOfChecked = repository.countByUserAndChecked(user, true);
        putCategorizedNotifications(model, notifications, numberOfChecked, notifications.size());
        model.addAttribute("isNew", true);
        service.markAsRead(notifications);
        return "notification/list";
    }

    @GetMapping("/notifications/old")
    public String getOldNotifications(HttpServletRequest request, Model model) {
        User user = controllerHelper.getAuthenticatedUser(request);
        List<Notification> notifications = repository.findByUserAndCheckedOrderByCreatedDateTimeDesc(user, true);
        long numberOfNotChecked = repository.countByUserAndChecked(user, false);
        putCategorizedNotifications(model, notifications, notifications.size(), numberOfNotChecked);
        model.addAttribute("isNew", false);
        return "notification/list";
    }

    @DeleteMapping("/notifications")
    public String deleteNotifications(HttpServletRequest request) {
        User user = controllerHelper.getAuthenticatedUser(request);
        repository.deleteByUserAndChecked(user, true);
        return "redirect:/notifications";
    }

    private void putCategorizedNotifications(Model model, List<Notification> notifications,
                                             long numberOfChecked, long numberOfNotChecked) {
        List<Notification> newProductNotification = new ArrayList<>();
        for (var notification : notifications) {
            switch (notification.getNotificationType()) {
                case PRODUCT_CREATED: newProductNotification.add(notification); break;
            }
        }

        model.addAttribute("numberOfNotChecked", numberOfNotChecked);
        model.addAttribute("numberOfChecked", numberOfChecked);
        model.addAttribute("notifications", notifications);
        model.addAttribute("newProductNotification", newProductNotification);
    }

}
