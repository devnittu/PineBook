package com.pinebook.api.repository;

import com.pinebook.api.model.Video;
import com.pinebook.api.model.VideoStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface VideoRepository extends JpaRepository<Video, Long> {

    Optional<Video> findByVideoId(String videoId);

    boolean existsByVideoId(String videoId);

    @Modifying
    @Transactional
    @Query("UPDATE Video v SET v.status = :status WHERE v.videoId = :videoId")
    int updateStatusByVideoId(@Param("status") VideoStatus status, @Param("videoId") String videoId);
}
