package com.onion.tag;

import com.onion.domain.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

public interface TagRepository extends JpaRepository<Tag, Long> {
    Tag findByTitle(String title);
}
