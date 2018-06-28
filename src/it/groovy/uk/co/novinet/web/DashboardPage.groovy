package uk.co.novinet.web

import geb.Page

class DashboardPage extends Page {
    static url = "http://localhost:8282"

    static at = { title == "Loan Charge Action Group Membership Dashboard" }

    static content = {
        memberTab { $(".nav.nav-tabs li a")[0] }
        guestsAwaitingVerificationTab { $(".nav.nav-tabs li a")[1] }
        paymentsTab { $(".nav.nav-tabs li a")[2] }
        memberGrid { $("#member-grid") }
        memberGridRows { $("#member-grid tr") }
        verificationGrid { $("#verification-grid") }
        verificationGridRows { $("#verification-grid tr") }
        gridVerificationButtons { verificationGrid.find("td[aria-describedby=verification-grid_action] .update-row-btn") }
        documentVerificationModal { $("#documentVerificationModal") }
        documentVerificationTarget { $("#documentVerificationTarget") }
        verifiedByInput { $("#verifiedBy") }
        confirmVerifyButton { $("#verify-confirm-btn") }
        toastError { $("#toast-container div.toast.toast-error div.toast-message") }
        toastSuccess { $("#toast-container div.toast.toast-success div.toast-message") }

        memberGridNameTds { memberGrid.find("td[aria-describedby=member-grid_name]") }
        memberGridHmrcLetterCheckedTds { memberGrid.find("td[aria-describedby=member-grid_hmrcLetterChecked]") }
        memberGridIdentityCheckedTds { memberGrid.find("td[aria-describedby=member-grid_identificationChecked]") }
        memberGridVerifiedByTds { memberGrid.find("td[aria-describedby=member-grid_verifiedBy]") }
        memberGridVerifiedOnTds { memberGrid.find("td[aria-describedby=member-grid_verifiedOn]") }

    }
}
