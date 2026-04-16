package com.pinebook.api.repository;

import com.pinebook.api.model.Chunk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChunkRepository extends JpaRepository<Chunk, Long> {

    List<Chunk> findByVideoId(String videoId);

    void deleteByVideoId(String videoId);
}
