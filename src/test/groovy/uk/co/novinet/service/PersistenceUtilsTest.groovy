package uk.co.novinet.service

import spock.lang.Specification

class PersistenceUtilsTest extends Specification {
    def "can parse iso8601 date"() {
        expect:
        1555928933L == PersistenceUtils.removeOffset("2019-04-22T10:28:53Z")
    }
}
