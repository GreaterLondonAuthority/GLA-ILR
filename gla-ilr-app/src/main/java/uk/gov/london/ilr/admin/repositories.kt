package uk.gov.london.ilr.admin

import org.springframework.data.jpa.repository.JpaRepository

interface MessageRepository : JpaRepository<Message, String> {
    fun findByCode(code: String): Message;
}
