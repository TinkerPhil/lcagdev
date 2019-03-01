package uk.co.novinet.auth

import spock.lang.Specification

class MyBbPasswordEncoderTest extends Specification {

    MyBbPasswordEncoder testObj

    def setup() {
        testObj = new MyBbPasswordEncoder()
    }

    def "can encode password with salt"() {
        println testObj.hashPassword("lcag", "salt")
        expect:
        "f28aec15073d8fda5aecbfff1e89289a" == testObj.hashPassword("hm7cL04nch4rGe", "4McxEuH8")
        "34fd4df51ada6f1305ef89196b37bf17" == testObj.hashPassword("2019l0anCharg3", "TXpv6XE4")
    }
}
