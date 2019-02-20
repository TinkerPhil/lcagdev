<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="fmt" uri = "http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri='http://java.sun.com/jsp/jstl/core' %>
<!DOCTYPE html>
<html>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <script src="https://code.jquery.com/jquery-3.3.1.min.js" integrity="sha256-FgpCb/KJQlLNfOu91ta32o/NMZxltwRo8QtmkMRdAu8=" crossorigin="anonymous"></script>
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css" integrity="sha384-BVYiiSIFeK1dGmJRAkycuHAHRg32OmUcww7on3RYdg4Va+PmSTsz/K68vbdEjh4u" crossorigin="anonymous">
    <script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/js/bootstrap.min.js" integrity="sha384-Tc5IQib027qvyjSMfHjOMaLkfuWVxZxUPnCJA7l2mCWNIpG9mGCD8wGNIcPD7Txa" crossorigin="anonymous"></script>
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap-theme.min.css" integrity="sha384-rHyoN1iRsVXV4nD0JutlnGaslCJuC7uwjduW9SVrLvRYooPp2bWYgmgJQIXwl/Sp" crossorigin="anonymous">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/free-jqgrid/4.15.4/css/ui.jqgrid.min.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/4.7.0/css/font-awesome.min.css">
    <script src="https://cdnjs.cloudflare.com/ajax/libs/free-jqgrid/4.15.4/jquery.jqgrid.min.js"></script>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/free-jqgrid/4.15.4/i18n/grid.locale-en.js"></script>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/bootstrap-datepicker/1.8.0/css/bootstrap-datepicker.min.css">
    <script src="https://cdnjs.cloudflare.com/ajax/libs/bootstrap-datepicker/1.8.0/js/bootstrap-datepicker.min.js"></script>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/bootstrap-datetimepicker/4.17.47/js/bootstrap-datetimepicker.min.js"></script>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/moment.js/2.22.1/moment.min.js"></script>
    <link href="https://cdnjs.cloudflare.com/ajax/libs/select2/4.0.6-rc.0/css/select2.min.css" rel="stylesheet" />
    <script src="https://cdnjs.cloudflare.com/ajax/libs/select2/4.0.6-rc.0/js/select2.min.js"></script>
    <link href="https://cdnjs.cloudflare.com/ajax/libs/dropzone/5.4.0/min/dropzone.min.css" rel="stylesheet" />
    <script src="https://cdnjs.cloudflare.com/ajax/libs/dropzone/5.4.0/min/dropzone.min.js"></script>
    <link href="https://cdnjs.cloudflare.com/ajax/libs/toastr.js/latest/css/toastr.min.css" rel="stylesheet" />
    <script src="https:////cdnjs.cloudflare.com/ajax/libs/toastr.js/latest/toastr.min.js"></script>
    <script src="https://cdn.rawgit.com/google/code-prettify/master/loader/run_prettify.js"></script>
    <link rel="stylesheet" href="/css/lcag.css">
    <script src="/js/lcag-common.js"></script>
    <script src="/js/lcag-statistics.js"></script>
    <script src="/js/lcag-member-grid.js"></script>
    <script src="/js/lcag-payments-grid.js"></script>
    <script src="/js/lcag-mp-grid.js"></script>
    <script src="/js/lcag-mp-campaign-grid.js"></script>
    <script src="/js/lcag-mp-campaign-user-grid.js"></script>
    <script src="/js/lcag-verification-grid.js"></script>
    <script src="/js/lcag-ffc-payment-grid.js"></script>
    <title>Loan Charge Action Group Membership Dashboard</title>
</head>
    <body>
        <!-- Modal -->
        <div class="modal fade" id="emailAddressesModal" role="dialog">
            <div class="modal-dialog modal-lg">
                <div class="modal-content">
                    <div class="modal-header">
                        <button type="button" class="close" data-dismiss="modal">&times;</button>
                        <h4 class="modal-title" id="emailAddressesTitle">Email Addresses</h4>
                    </div>
                    <div class="modal-body">
                        <pre class="prettyprint" id="emailAddressesTarget" style="white-space: pre-wrap; word-break: keep-all;"></pre>
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-default" data-dismiss="modal">Close</button>
                    </div>
                </div>
            </div>
        </div>

        <!-- Modal -->
        <div class="modal fade" id="documentVerificationModal" role="dialog">
            <div class="modal-dialog modal-lg">
                <div class="modal-content">
                    <div class="modal-header">
                        <button type="button" class="close" data-dismiss="modal">&times;</button>
                        <h4 class="modal-title" id="documentVerificationTitle">Documents</h4>
                    </div>
                    <div class="modal-body">
                        <div id="documentVerificationTarget">
                        </div>
                    </div>
                    <div class="modal-footer">
                        <div class="input-group">
                            <div class="input-group-addon"><i class="fa fa-quote-right" aria-hidden="true"></i></div>
                            <input type="text" name="notes" id="notes" class="form-control" placeholder="Notes">
                            <span class="input-group-btn"><button id="verify-add-note-btn" class="btn btn-default add-note" type="button">Add note</button></span>
                        </div>
                        <br/>
                        <div class="input-group">
                            <div class="input-group-addon"><i class="fa fa-check" aria-hidden="true"></i></div>
                            <input type="text" name="verifiedBy" id="verifiedBy" class="form-control" placeholder="Verified By">
                            <span class="input-group-btn"><button id="verify-confirm-btn" class="btn btn-default verify-confirm" type="button">Verify and send confirmation email</button></span>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <nav class="navbar navbar-default">
            <div class="container-fluid">
                <div class="navbar-header">
                    <a href="#" class="navbar-brand">
                        <img alt="Brand" src="/images/lcag_logo.jpg" width="60">
                    </a>
                </div>
                <div class="collapse navbar-collapse">
                    <p class="navbar-text">
                        <span id="totalContributions" class="label label-default">Total £0</span>
                        <span id="totalContributors" class="label label-primary">0 Contributors</span>
                        <span id="numberOfRegisteredMembers" class="label label-success action" data-toggle="modal" data-target="#emailAddressesModal">0 Members</span>
                        <span id="numberOfGuests" class="label label-info action" data-toggle="modal" data-target="#emailAddressesModal">0 Guests</span>
                        <span id="totalUsers" class="label label-danger action" data-toggle="modal" data-target="#emailAddressesModal">0 Total</span>
                    </p>
                    <ul class="nav navbar-nav">
                        <li>
                            <form action="/paymentUpload" class="dropzone" id="dropzone-form">
                                <p class="dz-message">Drop bank export txt file here</p>
                            </form>
                        </li>
                    </ul>
                </div>
            </div>
        </nav>

        <div>
            <ul class="nav nav-tabs" role="tablist">
                <li role="presentation" class="active"><a href="#members" aria-controls="members" role="tab" data-toggle="tab">Members</a></li>
                <li role="presentation"><a id="guestsAwaitingVerificationTabHeader" href="#guestsAwaitingVerification" aria-controls="guestsAwaitingVerification" role="tab" data-toggle="tab">Guests Awaiting Verification</a></li>
                <li role="presentation"><a href="#payments" aria-controls="payments" role="tab" data-toggle="tab">Payments</a></li>
                <li role="presentation"><a href="#mp" aria-controls="mp" role="tab" data-toggle="tab">MP</a></li>
                <li role="presentation"><a href="#mpcampaign" aria-controls="mpcampaign" role="tab" data-toggle="tab">MP Campaign</a></li>
                <li role="presentation"><a href="#mpcampaignuser" aria-controls="mpcampaignuser" role="tab" data-toggle="tab">MP Campaign-user</a></li>
                <li role="presentation"><a href="#ffcpayment" aria-controls="ffcpayment" role="tab" data-toggle="tab">FFC payments</a></li>
            </ul>

            <div class="tab-content">
                <div role="tabpanel" class="tab-pane active" id="members">
                    <div> <!-- class="horizontal-scroll">-->
                        <table id="member-grid">
                        </table>
                    </div>
                </div>
                <div role="tabpanel" class="tab-pane" id="guestsAwaitingVerification">
                    <div> <!-- class="horizontal-scroll">-->
                        <table id="verification-grid">
                        </table>
                    </div>
                </div>
                <div role="tabpanel" class="tab-pane" id="payments">
                    <div> <!-- class="horizontal-scroll">-->
                        <table id="payments-grid">
                        </table>
                    </div>
                </div>
                <div role="tabpanel" class="tab-pane" id="mp">
                    <div> <!-- class="horizontal-scroll">-->
                        <table id="mp-grid">
                        </table>
                    </div>
                </div>
                <div role="tabpanel" class="tab-pane" id="mpcampaign">
                    <div> <!-- class="horizontal-scroll">-->
                        <table id="mp-campaign-grid">
                        </table>
                    </div>
                </div>
                <div role="tabpanel" class="tab-pane" id="mpcampaignuser">
                    <div> <!-- class="horizontal-scroll">-->
                        <table id="mp-campaign-user-grid">
                        </table>
                    </div>
                </div>
                <div role="tabpanel" class="tab-pane" id="ffcpayment">
                    <div> <!-- class="horizontal-scroll">-->
                        <table id="ffc-payment-grid">
                        </table>
                    </div>
                </div>
            </div>
        </div>
    </body>
    <script type="text/javascript">
        Dropzone.autoDiscover = false;
        $(function () {
            lcag.MemberGrid.initialise();
            lcag.PaymentsGrid.initialise();
            lcag.MpGrid.initialise();
            lcag.MpCampaignGrid.initialise();
            lcag.MpCampaignUserGrid.initialise();
            lcag.VerificationGrid.initialise();
            lcag.FfcPaymentGrid.initialise();
            $("#dropzone-form").dropzone({
                maxFiles: 2000,
                url: "/paymentUpload",
                success: function (file, response) {
                    lcag.Common.alertSuccess();
                    lcag.PaymentsGrid.grid.trigger("reloadGrid");
                    lcag.MemberGrid.grid.trigger("reloadGrid");
                    lcag.VerificationGrid.grid.trigger("reloadGrid");
                }
            });

            $(document).ajaxStart(function() {
                lcag.Common.alertPleaseWait();
            }).ajaxStop(function() {
                lcag.Common.hidePleaseWait();
            });
        });
    </script>
</html>
