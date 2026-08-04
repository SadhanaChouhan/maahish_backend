package com.maahish.notification.repository;

import com.maahish.notification.entity.Notification;
import com.maahish.user.entity.User;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Page<Notification> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);

    long countByUserAndReadFalse(User user);

    @Modifying
    @Query("UPDATE Notification n SET n.read = true WHERE n.user.id = :userId AND n.read = false")
    int markAllReadForUser(@Param("userId") Long userId);

    void deleteByUser(User user);
}
