package com.ct.security.backend.dto.request

enum class EventType {
    /**
     * Only store into a data base
     */
    DB_STORE,
    /**
     * Only store into a TXT file
     */
    FILE_STORE,
    /**
     * Store into a data base and a TXT file
     */
    FULL_STORE,
    /**
     * No store the event
     */
    NON_STORE
}
