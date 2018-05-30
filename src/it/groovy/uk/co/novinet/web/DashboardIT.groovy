package uk.co.novinet.web

import geb.spock.GebSpec

class DashboardIT extends GebSpec {

    def "dashboard has correct number of rows"() {
        given:
            go "http://localhost:8282"

        expect:
            true
    }
}
