/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ilr.referencedata;

import com.querydsl.core.types.Predicate;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface ReferenceDataRepository extends JpaRepository<RefDataMapping, Integer>,
        QuerydslPredicateExecutor<RefDataMapping> {

    default Page<RefDataMapping> findAll(Integer year, String attribute, Pageable pageable) {
        Predicate predicate = new RefDataMappingQueryBuilder()
                .withAttribute(attribute)
                .withYear(year)
                .getPredicate();
        if (predicate == null) {
            return findAll(pageable);
        } else {
            return findAll(predicate, pageable);
        }
    }

    @Query(value = "select distinct year from ref_data_mapping "
            + "where year is not null order by year desc", nativeQuery = true)
    List<Integer> findDistinctAcademicYears();

    @Query(value = "select distinct attribute from ref_data_mapping "
            + "where attribute is not null order by attribute asc", nativeQuery = true)
    List<String> findDistinctAttributes();

    @Query(value = "delete from ref_data_mapping where year = ?1", nativeQuery = true)
    @Modifying
    void deleteAllByIdYear(Integer year);

}
