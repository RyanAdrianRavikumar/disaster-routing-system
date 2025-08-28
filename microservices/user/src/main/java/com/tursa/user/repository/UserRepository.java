package com.tursa.user.repository;

import com.tursa.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    @Query("SELECT u FROM User u WHERE u.rfid = :rfid")
    Optional<User> findByRfid(@Param("rfid") String rfid);

    @Query("SELECT u FROM User u WHERE u.status = :status ORDER BY u.rescuePriority DESC")
    List<User> findByStatusOrderByPriorityDesc(@Param("status") User.UserStatus status);
}
