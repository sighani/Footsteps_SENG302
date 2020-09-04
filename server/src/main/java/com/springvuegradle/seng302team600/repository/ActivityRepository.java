package com.springvuegradle.seng302team600.repository;

import com.springvuegradle.seng302team600.model.Activity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.springvuegradle.seng302team600.model.ActivityType;
import com.springvuegradle.seng302team600.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;

@RepositoryRestResource
public interface ActivityRepository extends JpaRepository<Activity, Long> {

    Activity findByActivityId(Long id);

    // Finds both activities created by and participated by the given user
    @Query(value =
            "SELECT DISTINCT A.* " +
            "FROM activity AS A LEFT JOIN activity_participant AS AP " +
            "ON A.activity_id = AP.activity_id " +
            "WHERE creator_user_id = :userId OR user_id = :userId " +
            "ORDER BY A.activity_id ASC", nativeQuery = true)
    List<Activity> findAllByUserId(@Param("userId") Long userId);

    @Query(value =
            "SELECT DISTINCT A.* " +
            "FROM activity AS A LEFT JOIN activity_participant AS AP " +
            "ON A.activity_id = AP.activity_id " +
            "WHERE is_continuous = true AND (creator_user_id = :userId OR user_id = :userId) " +
            "ORDER BY A.activity_id ASC", nativeQuery = true)
    List<Activity> findAllContinuousByUserId(@Param("userId") Long userId);

    @Query(value =
            "SELECT DISTINCT A.* " +
            "FROM activity AS A LEFT JOIN activity_participant AS AP " +
            "ON A.activity_id = AP.activity_id " +
            "WHERE is_continuous = false AND (creator_user_id = :userId OR user_id = :userId) " +
            "ORDER BY A.activity_id ASC", nativeQuery = true)
    List<Activity> findAllDurationByUserId(@Param("userId") Long userId);

    @Query(value="SELECT * FROM activity WHERE activity_id in ?1", nativeQuery=true)
    List<Activity> getActivitiesByIds(@Param("userIds") List<Long> activityIds);

    @Query(value =
            "SELECT * FROM activity WHERE activity_name LIKE ?1", nativeQuery = true)
    Page<Activity> findAllByKeyword(@Param("keyword") String keyword, Pageable pageable);
}
