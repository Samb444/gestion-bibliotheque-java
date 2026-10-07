package sn.codesamb.gestionsallesport.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sn.codesamb.gestionsallesport.entity.Cours;

/**
 * Repository Spring Data JPA pour la gestion des cours.
 */
@Repository
public interface CoursRepository extends JpaRepository<Cours, Long> {
}
