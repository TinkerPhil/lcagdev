package uk.co.novinet.web

import geb.Page

class DashboardPage extends Page {
    static url = "http://${dashboardHost()}:${dashboardPort()}"

    static at = { title == "Loan Charge Action Group Membership Dashboard" }

    static content = {
        memberTab { $(".nav.nav-tabs li a")[0] }
        guestsAwaitingVerificationTab { $(".nav.nav-tabs li a")[1] }
        paymentsTab { $(".nav.nav-tabs li a")[2] }
        paymentsGrid { $("#payments-grid") }
        paymentsGridUpdateButtons { $("#payments-grid .update-row-btn") }
        paymentsGridRows { paymentsGrid.find("tr") }
        memberGrid { $("#member-grid") }
        memberGridRows { $("#member-grid tr") }
        verificationGrid { $("#verification-grid") }
        verificationGridRows { $("#verification-grid tr") }
        gridVerificationButtons { verificationGrid.find("td[aria-describedby=verification-grid_action] .update-row-btn") }
        toastError { $("#toast-container div.toast.toast-error div.toast-message") }
        toastSuccess { $("#toast-container div.toast.toast-success div.toast-message") }

        // document verification modal
        documentVerificationModal { $("#documentVerificationModal") }
        documentVerificationTarget { $("#documentVerificationTarget") }
        documentLinksToVerify { documentVerificationTarget.find("a.document-download-link") }
        verifiedByInput { $("#verifiedBy") }
        confirmVerifyButton { $("#verify-confirm-btn") }
        notesInput { $("#notes") }
        addNoteButton { $("#verify-add-note-btn") }

        // verification grid
        verificationGridNotesTds { verificationGrid.find("td[aria-describedby=verification-grid_notes]") }

        // member grid
        memberGridNameTds { memberGrid.find("td[aria-describedby=member-grid_name]") }
        memberGridHmrcLetterCheckedTds { memberGrid.find("td[aria-describedby=member-grid_hmrcLetterChecked]") }
        memberGridIdentityCheckedTds { memberGrid.find("td[aria-describedby=member-grid_identificationChecked]") }
        memberGridVerifiedByTds { memberGrid.find("td[aria-describedby=member-grid_verifiedBy]") }
        memberGridVerifiedOnTds { memberGrid.find("td[aria-describedby=member-grid_verifiedOn]") }
        memberGridContributionAmountTds { memberGrid.find("td[aria-describedby=member-grid_contributionAmount]") }
        memberGridGroupSelectTds { memberGrid.find("td[aria-describedby=member-grid_group]") }
        memberGridAdditionalGroupsSelectTds { memberGrid.find("td[aria-describedby=member-grid_additionalGroups]") }
        memberGridActionButtonTds { memberGrid.find("td[aria-describedby=member-grid_action]") }

        // payments grid
        paymentsGridMemberTds { paymentsGrid.find("td[aria-describedby=payments-grid_userId]") }
        select2FirstMemberChoice { $("li.select2-results__option.select2-results__option--highlighted") }

    }
}
