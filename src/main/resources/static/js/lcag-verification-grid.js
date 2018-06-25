var lcag = lcag || {};

lcag.VerificationGrid = lcag.VerificationGrid || {
    grid: {},
    initialise: function() {
        $("#verification-grid").jqGrid({
            colModel: [
                { name: "id", label: "ID", hidden: true },
                { name: "registrationDate", label: "Registration Date", width: 90, align: "center", sorttype: "date", formatter: "date", formatoptions: { newformat: "d-M-Y" }, formatter: lcag.VerificationGrid.formatters.registrationDate },
                { name: "name", label: "Name", width: 150, template: "string", formatter: lcag.VerificationGrid.formatters.name },
                { name: "username", label: "Username", width: 150, template: "string" },
                { name: "emailAddress", label: "Email Address", width: 150, template: "string" },
                { name: "memberOfBigGroup", label: "Member of Big Group", width: 59, formatter: lcag.VerificationGrid.formatters.memberOfBigGroup, stype: "select", searchoptions: { sopt: ["eq", "ne"], value: ":Any;1:Yes;0:No" } },
                { name: "bigGroupUsername", label: "Big Group Username", width: 90, template: "string" },
                { name: "verifiedBy", label: "Verified By", width: 100, formatter: lcag.VerificationGrid.formatters.verifiedBy },
                { name: "action", label: "", width: 150, formatter: lcag.VerificationGrid.formatters.action, search: false }
            ],
            datatype: function(postData) {
                    jQuery.ajax({
                        url: lcag.Common.urlPrefix + '/member?hasCompletedMembershipForm=true&verifiedBy=<NULL>',
                        data: postData,
                        dataType: "json",
                        complete: function(response, status) {
                            lcag.VerificationGrid.grid = $("#verification-grid")
                            if (status == "success") {
                                lcag.VerificationGrid.grid[0].addJSONData(response.responseJSON);
                                if (lcag.VerificationGrid.grid.getGridParam("reccount") > 0) {
                                    $("#guestsAwaitingVerificationTabHeader").text("Guests Awaiting Verification *");
                                    $("#guestsAwaitingVerificationTabHeader").addClass("has-items");
                                }

                            }
                        }
                    });
            },
            iconSet: "fontAwesome",
            sortname: "id",
            sortorder: "desc",
            threeStateSort: false,
            headertitles: true,
            pager: true,
            rowNum: 25,
            width: "1500px",
            altRows: true,
            viewrecords: true,
            gridComplete: function() {
                lcag.Statistics.refresh();

                $( "#documentVerificationModal" ).on('shown.bs.modal', function(e) {
                    var memberId = $(e.relatedTarget).attr('data-row-id');

                    $.ajax({
                        method: "get",
                        url: "/member/documents?memberId=" + memberId
                    }).done(function(data) {
                        if (data.length > 1) {
                            var tableHtml = "";
                            tableHtml += '<table class="table"><thead><tr><th>Filename</th><th>Upload Date</th></tr></thead><tbody>';
                            for (var i = 0; i < data.length; i++) {
                                tableHtml +=
                                    '<tr>' +
                                        '<td><a href="/member/document/download?path=' + data[i].path + '" class="document-download-link" data-sftp-path="' + data[i].path + '">' + data[i].filename + '</a></td>' +
                                        '<td>' + moment(data[i].uploadDate).format("DD/MM/YYYY HH:mm") + '</td>' +
                                    '</tr>';
                            }
                            tableHtml += '</tbody></table>';
                            $("#documentVerificationTarget").append(tableHtml);
                            /*
                            $(".document-download-link").on("click", function(e) {
                                var me = this;
                                e.preventDefault();
                                $.ajax({
                                    method: "get",
                                    url: "/member/document/download?path=" + $(me).attr("data-sftp-path")
                                }).done(function(data) {
                                    console.log("Download complete");
                                });
                            });
                            */
                        }
                    });
                });

                $("#documentVerificationModal" ).on('hidden.bs.modal', function(e) {
                    $("#documentVerificationTarget").text("");
                });

//                $("#verification-grid").find(".update-row-btn").on("click", function(e) {
//                    var rowContext = this;
//                    var id = $(rowContext).data("row-id");
//
//                    var verifiedBy = $("#verifiedBy_" + id).val();
//
//                    console.log("verifiedBy", verifiedBy);
//
//                    if (verifiedBy == null || verifiedBy == "") {
//                       var jElement = $("#verifiedBy_" + id);
//                       jElement.addClass('highlight-error');
//                       setTimeout(
//                           function() { jElement.removeClass('highlight-error'); },
//                           2000
//                       );
//                        lcag.Common.alertError("Please enter a value for 'Verified By'");
//                        return;
//                    }
//
//                    $.ajax({
//                          type: "POST",
//                          url: lcag.Common.urlPrefix + "/member/verify",
//                          data: (function() {
//                            console.log("rowContext", rowContext);
//                            var id = $(rowContext).data("row-id");
//
//                            return {
//                                "id": id,
//                                "verifiedBy": verifiedBy
//                            };
//                          })(),
//                          success: function(e) {
//                            lcag.Common.alertSuccess();
//                            lcag.VerificationGrid.grid.trigger("reloadGrid");
//                            lcag.MemberGrid.grid.trigger("reloadGrid");
//                          },
//                          error: function(e) {
//                            lcag.Common.alertError();
//                            lcag.VerificationGrid.grid.trigger("reloadGrid");
//                            lcag.MemberGrid.grid.trigger("reloadGrid");
//                          }
//                        });
//                });

                $('.date').datepicker({
                    autoclose: true,
                    format: "dd/mm/yyyy"
                });
            }
        }).jqGrid("filterToolbar", {
            searchOnEnter: false
        });
    },
	formatters: {
        "registrationDate": function(cellvalue, options, row) {
            return moment(row.registrationDate).format("DD/MM/YYYY HH:mm");
        },
        "memberOfBigGroup": function(cellvalue, options, row) {
            return '<input disabled="disabled" id="memberOfBigGroup_' + row.id + '" type="checkbox" ' + (row.memberOfBigGroup ? ' checked="checked"' : '') + '" data-row-id="' + row.id + '" />';
        },
        "verifiedBy": function(cellvalue, options, row) {
            return '<div class="input-group"><input id="verifiedBy_' + row.id + '" type="text" class="form-control" value="' + row.verifiedBy + '"></div>';
        },
        "action": function(cellvalue, options, row) {
          //  return '<button type="button" class="btn btn-default update-row-btn" data-row-id="' + row.id + '"><span class="fa fa-check fa-lg" aria-hidden="true"></span>&nbsp;Verify ID and Scheme Documents</button>';
          return '<button type="button" class="btn btn-default update-row-btn" data-toggle="modal" data-target="#documentVerificationModal" data-row-id="' + row.id + '"><span class="fa fa-check fa-lg" aria-hidden="true"></span>&nbsp;Verify ID and Scheme Documents</button>';
        }
    }
}


