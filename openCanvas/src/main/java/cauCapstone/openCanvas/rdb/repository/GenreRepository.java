package cauCapstone.openCanvas.rdb.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import cauCapstone.openCanvas.rdb.entity.Genre;

public interface GenreRepository extends JpaRepository<Genre, Long>{
    Optional<Genre> findByName(String name);
}
