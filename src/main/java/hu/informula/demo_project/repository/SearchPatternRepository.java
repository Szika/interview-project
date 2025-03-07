package hu.informula.demo_project.repository;

import hu.informula.demo_project.entity.SearchPattern;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SearchPatternRepository extends JpaRepository<SearchPattern, Long> {

}
