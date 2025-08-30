package com.tursa.notification.repository;

import com.tursa.notification.model.Notification;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByUserRfidOrderBySentAtDesc(String userRfid);

    @Query("SELECT n FROM Notification n WHERE n.isRead = false ORDER BY n.priority DESC, n.sentAt DESC")
    List<Notification> findUnreadNotificationsOrderByPriority();

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.userRfid = :userRfid AND n.isRead = false")
    int markAllAsReadForUser(@Param("userRfid") String userRfid);

    @Query("SELECT COUNT(n) FROM Notification n WHERE n.userRfid = :userRfid AND n.isRead = false")
    long countUnreadByUserRfid(@Param("userRfid") String userRfid);
}
