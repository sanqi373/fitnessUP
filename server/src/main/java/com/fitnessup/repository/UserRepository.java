package com.fitnessup.repository;

import com.fitnessup.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    @Transactional
    @Modifying
    @Query("update User u set u.calorieGoal = :goal where u.id = :id and (:onlyIfUnset = false or u.calorieGoal is null)")
    int setCalorieGoal(Long id, Long goal, boolean onlyIfUnset);

    Optional<User> findByPhone(String phone);
    boolean existsByPhone(String phone);
}
