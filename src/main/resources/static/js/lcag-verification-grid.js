var lcag = lcag || {};

lcag.VerificationGrid = lcag.VerificationGrid || {
    grid: {},
    clearCurrentVerificationState: function() {
        console.log("Clearing current verification state");
        $("#documentVerificationTarget").children().remove();
        $('#verifiedBy').val(null);
        $('#notes').val(null);
    },
    currentMemberId: {},
    initialise: function() {
        $("#verification-grid").jqGrid({
            colModel: [
                { name: "id", label: "ID", width: 0, hidden: true },
                { name: "registrationDate", label: "Registration Date", width: 150, align: "center", sorttype: "date", formatter: "date", formatoptions: { newformat: "d-M-Y" }, formatter: lcag.VerificationGrid.formatters.registrationDate },
                { name: "name", label: "Name", width: 200, template: "string", formatter: lcag.VerificationGrid.formatters.name },
                { name: "username", label: "Username", width: 150, template: "string" },
                { name: "emailAddress", label: "Email Address", width: 200, template: "string" },
                { name: "memberOfBigGroup", label: "Member of Big Group", width: 59, formatter: lcag.VerificationGrid.formatters.memberOfBigGroup, stype: "select", searchoptions: { sopt: ["eq", "ne"], value: ":Any;1:Yes;0:No" } },
                { name: "bigGroupUsername", label: "Big Group Username", width: 150, template: "string" },
                { name: "notes", label: "Notes", width: 500, template: "string" },
                { name: "action", label: "", width: 160, formatter: lcag.VerificationGrid.formatters.action, search: false }
            ],
            datatype: function(postData) {
                    jQuery.ajax({
                        url: lcag.Common.urlPrefix + '/member?hasCompletedMembershipForm=true&verifiedBy=<NULL>&group=LCAG%20Guests',
                        data: postData,
                        dataType: "json",
                        complete: function(response, status) {
                            lcag.VerificationGrid.grid = $("#verification-grid")
                            if (status == "success") {
                                lcag.VerificationGrid.grid[0].addJSONData(response.responseJSON);
                                if (lcag.VerificationGrid.grid.getGridParam("reccount") > 0) {
                                    $("#guestsAwaitingVerificationTabHeader").text("Guests Awaiting Verification *");
                                    $("#guestsAwaitingVerificationTabHeader").addClass("has-items");
                                } else {
                                    $("#guestsAwaitingVerificationTabHeader").text("Guests Awaiting Verification");
                                    $("#guestsAwaitingVerificationTabHeader").removeClass("has-items");
                                }
                            }
                        }
                    });
            },
            shrinkToFit:false,
            width: $(window).width() - 10, //"2500", // 8500px
            autoresizeOnLoad: true,

            iconSet: "fontAwesome",
            sortname: "id",
            sortorder: "desc",
            threeStateSort: false,
            headertitles: true,
            pager: true,
            rowNum: 25,
            //width: "1500px",
            altRows: true,
            viewrecords: true,
            gridComplete: function() {
                lcag.Statistics.refresh();
                $('.date').datepicker({
                    autoclose: true,
                    format: "dd/mm/yyyy"
                });
            }
        }).jqGrid("filterToolbar", {
            searchOnEnter: false
        });

        $("#verification-grid").setGridWidth($(window).width() -10);

        $(window).bind('resize', function() {
            $("#verification-grid").width($(window).width() -10);
            $("#verification-grid").setGridWidth($(window).width() -10);
            $("#verification-grid").setGridHeight($(window).height()-220);
        }).trigger('resize');

        $( "#documentVerificationModal" ).on('shown.bs.modal', function(e) {
            $("#dynamicVerificationTable").remove();
            var memberId = $(e.relatedTarget).attr('data-row-id');

            var existingNotes = $(e.relatedTarget).parents("tr").find("td[aria-describedby=verification-grid_notes]").text();

            if (existingNotes.trim().length > 0) {
                $("#notes").val(existingNotes);
            }

            lcag.VerificationGrid.currentMemberId = memberId;

            $.ajax({
                method: "get",
                url: "/member/documents?memberId=" + memberId
            }).done(function(data) {
                if (data != null && data.documents != null && data.documents.length > 1) {
                    var tableHtml = "";
                    tableHtml += '<table class="table"><thead><tr><th>Filename</th><th>Upload Date</th></tr></thead><tbody>';
                    for (var i = 0; i < data.documents.length; i++) {
                        tableHtml +=
                            '<tr>' +
                                '<td><a href="/member/document/download?path=' + encodeURIComponent(data.documents[i].path) + '" class="document-download-link" data-sftp-path="' + data.documents[i].path + '">' + data.documents[i].filename + '</a></td>' +
                                '<td>' + moment(data.documents[i].uploadDate).format("DD/MM/YYYY HH:mm") + '</td>' +
                            '</tr>';
                    }
                    tableHtml += '</tbody></table>';
                    $("#documentVerificationTarget").append(tableHtml);
                } else {
                    $("#documentVerificationTarget").append('<p>No documents found for this member</p>');
                }
            });
        });

        $("#documentVerificationModal" ).on('hidden.bs.modal', function(e) {
            lcag.VerificationGrid.clearCurrentVerificationState();
        });

        $("#verify-add-note-btn").on("click", function(e) {
            var rowContext = this;
            var id = lcag.VerificationGrid.currentMemberId;

            var notes = $("#notes").val();

            console.log("notes", notes);

            if (notes == null || notes == "") {
                var jElement = $("#notes");
                jElement.addClass('highlight-error');
                setTimeout(
                    function() { jElement.removeClass('highlight-error'); },
                    2000
                );
                lcag.Common.alertError("Please enter a note");
                return;
            }

            $.ajax({
                  type: "POST",
                  url: lcag.Common.urlPrefix + "/member/addNote",
                  data: (function() {
                    console.log("rowContext", rowContext);

                    return {
                        "id": lcag.VerificationGrid.currentMemberId,
                        "notes": notes
                    };
                  })(),
                  success: function(e) {
                    lcag.VerificationGrid.clearCurrentVerificationState();
                    $('#documentVerificationModal').modal('toggle');
                    lcag.VerificationGrid.currentMemberId = null;
                    lcag.Common.alertSuccess();
                    lcag.VerificationGrid.grid.trigger("reloadGrid");
                    lcag.MemberGrid.grid.trigger("reloadGrid");
                  },
                  error: function(e) {
                    lcag.Common.alertError();
                    lcag.VerificationGrid.grid.trigger("reloadGrid");
                    lcag.MemberGrid.grid.trigger("reloadGrid");
                  }
            });
        });

        $("#verify-confirm-btn").on("click", function(e) {
            var rowContext = this;
            var id = lcag.VerificationGrid.currentMemberId;

            var verifiedBy = $("#verifiedBy").val();

            console.log("verifiedBy", verifiedBy);

            if (verifiedBy == null || verifiedBy == "") {
                var jElement = $("#verifiedBy");
                jElement.addClass('highlight-error');
                setTimeout(
                    function() { jElement.removeClass('highlight-error'); },
                    2000
                );
                lcag.Common.alertError("Please enter your initials in the 'Verified By' input field");
                return;
            }

            $.ajax({
                  type: "POST",
                  url: lcag.Common.urlPrefix + "/member/verify",
                  data: (function() {
                    console.log("rowContext", rowContext);

                    return {
                        "id": lcag.VerificationGrid.currentMemberId,
                        "verifiedBy": verifiedBy
                    };
                  })(),
                  success: function(e) {
                    lcag.VerificationGrid.clearCurrentVerificationState();
                    $('#documentVerificationModal').modal('toggle');
                    lcag.VerificationGrid.currentMemberId = null;
                    lcag.Common.alertSuccess();
                    lcag.VerificationGrid.grid.trigger("reloadGrid");
                    lcag.MemberGrid.grid.trigger("reloadGrid");
                  },
                  error: function(e) {
                    lcag.Common.alertError();
                    lcag.VerificationGrid.grid.trigger("reloadGrid");
                    lcag.MemberGrid.grid.trigger("reloadGrid");
                  }
            });
        });
    },
	formatters: {
        "registrationDate": function(cellvalue, options, row) {
            return moment(row.registrationDate).format("DD/MM/YYYY HH:mm");
        },
        "memberOfBigGroup": function(cellvalue, options, row) {
            return '<input disabled="disabled" id="memberOfBigGroup_' + row.id + '" type="checkbox" ' + (row.memberOfBigGroup ? ' checked="checked"' : '') + '" data-row-id="' + row.id + '" />';
        },
        "action": function(cellvalue, options, row) {
          return '<button type="button" class="btn btn-default update-row-btn" data-toggle="modal" data-target="#documentVerificationModal" data-row-id="' + row.id + '"><span class="fa fa-check fa-lg" aria-hidden="true"></span>&nbsp;Verify ID and Scheme Documents</button>';
        }
    }
}


