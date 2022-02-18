/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ilr.providerallocation

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.querydsl.QuerydslPredicateExecutor

interface ProviderAllocationRepository : JpaRepository<ProviderAllocation, ProviderAllocationPK>, QuerydslPredicateExecutor<ProviderAllocation> {
    @Query(value = "delete from provider_allocation where year = ?1", nativeQuery = true)
    @Modifying
    fun deleteAllByYear(year: Int?)

    @Query(value = "select distinct year from provider_allocation order by year desc", nativeQuery = true)
    fun findDistinctAcademicYears(): List<Int>

    @JvmDefault
    fun findAll(ukprns: Set<Int?>?, academicYear: Int?, pageable: Pageable): Page<ProviderAllocation> {
        val predicate = ProviderAllocationQueryBuilder()
                .withUkprns(ukprns)
                .withAcademicYear(academicYear)
                .predicate
        return if (predicate == null) {
            findAll(pageable)
        } else {
            findAll(predicate, pageable)
        }
    }
}

