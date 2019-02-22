var lcag = lcag || {};

lcag.MemberGrid = lcag.MemberGrid || {
    grid: {},
    initialise: function() {
        $("#member-grid").jqGrid({
            colModel: [
                { name: "id", label: "ID", hidden: true },
                { name: "name", label: "Name", width: 150, template: "string", formatter: lcag.MemberGrid.formatters.name, frozen: true },
                { name: "username", label: "Username", width: 150, template: "string", frozen: true },
                { name: "emailAddress", label: "Email Address", width: 200, template: "string" },

                { name: "action", label: "", width: 90, formatter: lcag.MemberGrid.formatters.action, search: false },
                { name: "hmrcLetterChecked", label: "HMRC Letter Checked", width: 59, formatter: lcag.MemberGrid.formatters.hmrcLetterChecked, stype: "select", searchoptions: { sopt: ["eq", "ne"], value: ":Any;1:Yes;0:No" } },
                { name: "identificationChecked", label: "Identification Checked", width: 90, formatter: lcag.MemberGrid.formatters.identificationChecked, stype: "select", searchoptions: { sopt: ["eq", "ne"], value: ":Any;1:Yes;0:No" } },
                { name: "verifiedOn", label: "Verified On Date", width: 150, align: "center", sorttype: "date", formatter: lcag.MemberGrid.formatters.verifiedOn },
                { name: "verifiedBy", label: "Verified By", width: 100, formatter: lcag.MemberGrid.formatters.verifiedBy },
                { name: "agreedToContributeButNotPaid", label: "Agreed To Contribute But Not Paid", width: 59, formatter: lcag.MemberGrid.formatters.agreedToContributeButNotPaid, stype: "select", searchoptions: { sopt: ["eq", "ne"], value: ":Any;1:Yes;0:No" } },
                { name: "contributionAmount", label: "Contribution Amount", width: 120, align: "center", formatter: lcag.MemberGrid.formatters.contributionAmount },
                { name: "group", label: "Group", width: 100, formatter: lcag.MemberGrid.formatters.group, stype: "select", searchoptions: { sopt: ["eq", "ne"], value: ":Any;LCAG Guests:LCAG Guests;Registered:Registered;Moderators:Moderators;Administrators:Administrators;Suspended:Suspended" } },
                { name: "country", label: "Country", width: 120, formatter: lcag.MemberGrid.formatters.country },
                { name: "memberOfBigGroup", label: "Member of Big Group", width: 59, formatter: lcag.MemberGrid.formatters.memberOfBigGroup, stype: "select", searchoptions: { sopt: ["eq", "ne"], value: ":Any;1:Yes;0:No" } },
                { name: "bigGroupUsername", label: "Big Group Username", width: 90, formatter: lcag.MemberGrid.formatters.bigGroupUsername },
                { name: "registrationDate", label: "Registration Date", width: 150, align: "center", sorttype: "date", formatter: "date", formatoptions: { newformat: "d-M-Y" }, formatter: lcag.MemberGrid.formatters.registrationDate },

                { name: "action", label: "", width: 100, formatter: lcag.MemberGrid.formatters.action, search: false },
                { name: "mpName", label: "MP Name", width: 90, formatter: lcag.MemberGrid.formatters.mpName },
                { name: "mpParty", label: "MP Party", width: 90, formatter: lcag.MemberGrid.formatters.mpParty },
                { name: "mpConstituency", label: "MP Constituency", width: 120, formatter: lcag.MemberGrid.formatters.mpConstituency },
                { name: "mpEngaged", label: "MP Engaged", width: 59, formatter: lcag.MemberGrid.formatters.mpEngaged, stype: "select", searchoptions: { sopt: ["eq", "ne"], value: ":Any;1:Yes;0:No" } },
                { name: "mpSympathetic", label: "MP Sympathetic", width: 59, formatter: lcag.MemberGrid.formatters.mpSympathetic, stype: "select", searchoptions: { sopt: ["eq", "ne"], value: ":Any;1:Yes;0:No" } },
                { name: "action", label: "", width: 90, formatter: lcag.MemberGrid.formatters.action, search: false },
                { name: "schemes", label: "Schemes", width: 280, formatter: lcag.MemberGrid.formatters.schemes },
                { name: "industry", label: "Industry", width: 280, formatter: lcag.MemberGrid.formatters.industry },
                { name: "notes", label: "Notes", width: 280, formatter: lcag.MemberGrid.formatters.notes },
                { name: "howDidYouHearAboutLcag", label: "How Did You Hear About LCAG", width: 280, formatter: lcag.MemberGrid.formatters.howDidYouHearAboutLcag },
                { name: "hasCompletedMembershipForm", label: "Completed Membership Form", width: 59, formatter: lcag.MemberGrid.formatters.hasCompletedMembershipForm, stype: "select", searchoptions: { sopt: ["eq", "ne"], value: ":Any;1:Yes;0:No" } },
                { name: "action", label: "", width: 90, formatter: lcag.MemberGrid.formatters.action, search: false },
                { name: "token", label: "Membership Token", width: 150, template: "string" },
                { name: "claimToken", label: "Claim Token", width: 150, template: "string", classes: "claim first" },
                { name: "registeredForClaim", label: "Registered For Claim", width: 59, formatter: lcag.MemberGrid.formatters.registeredForClaim, stype: "select", searchoptions: { sopt: ["eq", "ne"], value: ":Any;1:Yes;0:No" }, classes: "claim" },
                { name: "hasCompletedClaimParticipantForm", label: "Completed Claim Participant Form", width: 59, formatter: lcag.MemberGrid.formatters.hasCompletedClaimParticipantForm, stype: "select", searchoptions: { sopt: ["eq", "ne"], value: ":Any;1:Yes;0:No" }, classes: "claim" },
                { name: "hasBeenSentClaimConfirmationEmail", label: "Has Been Sent Claim Confirmation Email", width: 59, formatter: lcag.MemberGrid.formatters.hasBeenSentClaimConfirmationEmail, stype: "select", searchoptions: { sopt: ["eq", "ne"], value: ":Any;1:Yes;0:No" }, classes: "claim" },
                { name: "hasOptedOutOfClaim", label: "Has Opted Out Of Claim", width: 59, formatter: lcag.MemberGrid.formatters.hasOptedOutOfClaim, stype: "select", searchoptions: { sopt: ["eq", "ne"], value: ":Any;1:Yes;0:No" }, classes: "claim last" },
//                { name: "hasBeenSentInitialMassLobbyingEmail", label: "Has Been Sent Initial Email", width: 59, formatter: lcag.MemberGrid.formatters.hasBeenSentInitialMassLobbyingEmail, stype: "select", searchoptions: { sopt: ["eq", "ne"], value: ":Any;1:Yes;0:No" }, classes: "mass-lobbying-day first" },
//                { name: "lobbyingDayAttending", label: "Attending", width: 59, formatter: lcag.MemberGrid.formatters.lobbyingDayAttending, classes: "mass-lobbying-day ", stype: "select", searchoptions: { sopt: ["eq", "ne"], value: ":Any;UNSET:Unset;YES:Yes;NO:No;MAYBE:Maybe" } },
//                { name: "lobbyingDayHasBeenSentMpTemplate", label: "Has Been Sent MP Template Letter", width: 59, formatter: lcag.MemberGrid.formatters.lobbyingDayHasBeenSentMpTemplate, stype: "select", searchoptions: { sopt: ["eq", "ne"], value: ":Any;1:Yes;0:No" }, classes: "mass-lobbying-day" },
//                { name: "lobbyingDayHasSentMpTemplateLetter", label: "Has Sent Template Letter To MP", width: 59, formatter: lcag.MemberGrid.formatters.lobbyingDayHasSentMpTemplateLetter, stype: "select", searchoptions: { sopt: ["eq", "ne"], value: ":Any;1:Yes;0:No" }, classes: "mass-lobbying-day" },
//                { name: "lobbyingDayHasReceivedMpResponse", label: "Has Received MP Response", width: 59, formatter: lcag.MemberGrid.formatters.lobbyingDayHasReceivedMpResponse, stype: "select", searchoptions: { sopt: ["eq", "ne"], value: ":Any;1:Yes;0:No" }, classes: "mass-lobbying-day" },
//                { name: "lobbyingDayMpHasConfirmedAttendance", label: "MP Has Confirmed Attendance", width: 59, formatter: lcag.MemberGrid.formatters.lobbyingDayMpHasConfirmedAttendance, stype: "select", searchoptions: { sopt: ["eq", "ne"], value: ":Any;1:Yes;0:No" }, classes: "mass-lobbying-day" },
//                { name: "lobbyingDayMpIsMinister", label: "MP Is Minister", width: 90, formatter: lcag.MemberGrid.formatters.lobbyingDayMpIsMinister, stype: "select", searchoptions: { sopt: ["eq", "ne"], value: ":Any;1:Yes;0:No" }, classes: "mass-lobbying-day" },
//                { name: "lobbyingDayNotes", label: "Notes", width: 200, formatter: lcag.MemberGrid.formatters.lobbyingDayNotes, classes: "mass-lobbying-day last" },
                { name: "action", label: "", width: 90, formatter: lcag.MemberGrid.formatters.action, search: false }
            ],
            datatype: function(postData) {
                    jQuery.ajax({
                        url: lcag.Common.urlPrefix + '/member',
                        data: postData,
                        dataType: "json",
                        complete: function(response, status) {
                            if (status == "success") {
                                lcag.MemberGrid.grid = $("#member-grid");
                                console.log("jsondata:", response.responseJSON);
                                lcag.MemberGrid.grid[0].addJSONData(response.responseJSON);
                            }
                        }
                    });
            },
            shrinkToFit:false,
            width: $(window).width() - 10,
            autoresizeOnLoad: true,

            iconSet: "fontAwesome",
            sortname: "id",
            sortorder: "desc",
            threeStateSort: false,
            headertitles: true,
            pager: true,
            rowNum: 25,
            altRows: true,
            rowattr: function (row) {
                if (row.group == "Registered") {
                    return { "class": "success" };
                } else if (row.group == "Administrators") {
                    return { "class": "danger" };
                } else if (row.group == "Moderators") {
                    return { "class": "info" };
                }
            },
            viewrecords: true,
            gridComplete: function() {
                lcag.Statistics.refresh();
                $("#member-grid").find(".update-row-btn").on("click", function(e) {
                    var rowContext = this;
                    $.ajax({
                          type: "POST",
                          url: lcag.Common.urlPrefix + "/member/update",
                          data: (function() {
                            var id = $(rowContext).data("row-id");

                            return {
                                "id": id,
                                "name": $("#name_" + id).val(),
                                "identificationChecked": $("#identificationChecked_" + id).prop("checked"),
                                "hmrcLetterChecked": $("#hmrcLetterChecked_" + id).prop("checked"),
                                "agreedToContributeButNotPaid": $("#agreedToContributeButNotPaid_" + id).prop("checked"),
                                "mpName": $("#mpName_" + id).val(),
                                "mpParty": $("#mpParty_" + id).val(),
                                "mpConstituency": $("#mpConstituency_" + id).val(),
                                "mpEngaged": $("#mpEngaged_" + id).prop("checked"),
                                "mpSympathetic": $("#mpSympathetic_" + id).prop("checked"),
                                "schemes": $("#schemes_" + id).val(),
                                "industry": $("#industry_" + id).val(),
                                "notes": $("#notes_" + id).val(),
                                "group": $("#group_" + id).val(),
                                "hasCompletedMembershipForm": $("#hasCompletedMembershipForm_" + id).prop("checked"),
                                "memberOfBigGroup": $("#memberOfBigGroup_" + id).prop("checked"),
                                "bigGroupUsername": $("#bigGroupUsername_" + id).val(),
                                "verifiedOn": $("#verifiedOn_" + id).val(),
                                "verifiedBy": $("#verifiedBy_" + id).val(),
                                "howDidYouHearAboutLcag": $("#howDidYouHearAboutLcag_" + id).val(),
                                "registeredForClaim": $("#registeredForClaim_" + id).prop("checked"),
                                "hasCompletedClaimParticipantForm": $("#hasCompletedClaimParticipantForm_" + id).prop("checked"),
                                "hasBeenSentClaimConfirmationEmail": $("#hasBeenSentClaimConfirmationEmail_" + id).prop("checked"),
                                "hasOptedOutOfClaim": $("#hasOptedOutOfClaim_" + id).prop("checked"),
//                                "hasBeenSentInitialMassLobbyingEmail": $("#hasBeenSentInitialMassLobbyingEmail_" + id).prop("checked"),
//                                "lobbyingDayHasBeenSentMpTemplate": $("#lobbyingDayHasBeenSentMpTemplate_" + id).prop("checked"),
//                                "lobbyingDayAttending": $("#lobbyingDayAttending_" + id).val(),
//                                "lobbyingDayHasSentMpTemplateLetter": $("#lobbyingDayHasSentMpTemplateLetter_" + id).prop("checked"),
//                                "lobbyingDayHasReceivedMpResponse": $("#lobbyingDayHasReceivedMpResponse_" + id).prop("checked"),
//                                "lobbyingDayMpHasConfirmedAttendance": $("#lobbyingDayMpHasConfirmedAttendance_" + id).prop("checked"),
//                                "lobbyingDayMpIsMinister": $("#lobbyingDayMpIsMinister_" + id).prop("checked"),
//                                "lobbyingDayNotes": $("#lobbyingDayNotes_" + id).val(),
                                "country": $("#country_"+id).val()
                            };
                          })(),
                          success: function(e) {
                            lcag.Common.alertSuccess();
                            lcag.MemberGrid.grid.trigger("reloadGrid");
                            lcag.VerificationGrid.grid.trigger("reloadGrid");
                          },
                          error: function(e) {
                            lcag.Common.alertError();
                            lcag.MemberGrid.grid.trigger("reloadGrid");
                            lcag.VerificationGrid.grid.trigger("reloadGrid");
                          }
                        });
                });
                $('.date').datepicker({
                    autoclose: true,
                    format: "dd/mm/yyyy"
                });
            }
        }).jqGrid("filterToolbar", {
            searchOnEnter: false
        });

        $("#member-grid").keyup(function (e) {
            if (e.keyCode === 27) {
                $("#member-grid")[0].clearToolbar();
                return false;
            }
        });

        $("#member-grid").jqGrid('setGroupHeaders', {
            useColSpanStyle: false,
            groupHeaders: [
                //{ startColumnName: 'hasBeenSentInitialMassLobbyingEmail', numberOfColumns: 8, titleText: 'Mass Lobbying Day' },
                { startColumnName: 'claimToken', numberOfColumns: 5, titleText: 'Claim' }
            ]
        });

        $("#member-grid").setFrozenColumns();

        $(window).bind('resize', function() {
            $("#member-grid").width($(window).width() -10);
            $("#member-grid").setGridWidth($(window).width() -10);
            $("#member-grid").setGridHeight($(window).height()-270);
        }).trigger('resize');

    },
	formatters: {
        "name": function(cellvalue, options, row) {
            return '<div class="input-group"><input ' + (row.status == 3 ? 'disabled="disabled"' : '') + ' id="name_' + row.id + '" type="text" class="form-control" value="' + row.name + '"></div>';
        },
        "registrationDate": function(cellvalue, options, row) {
            return moment(row.registrationDate).format("DD/MM/YYYY HH:mm");
        },
        "verifiedOn": function(cellvalue, options, row) {
            var dateString = row.verifiedOn == null ? "" : moment(row.verifiedOn).format("DD/MM/YYYY");
            return '<div class="input-group date"><div class="input-group-addon"><i class="fa fa-calendar"></i></div><input ' + (row.status == 3 ? 'disabled="disabled"' : '') + ' id="verifiedOn_' + row.id + '" type="text" class="form-control" value="' + dateString + '"></div>';
        },
        "verifiedBy": function(cellvalue, options, row) {
            return '<div class="input-group"><input ' + (row.status == 3 ? 'disabled="disabled"' : '') + ' id="verifiedBy_' + row.id + '" type="text" class="form-control" value="' + row.verifiedBy + '"></div>';
        },
        "identificationChecked": function(cellvalue, options, row) {
            return '<input ' + (row.status == 3 ? 'disabled="disabled"' : '') + ' id="identificationChecked_' + row.id + '" type="checkbox" ' + (row.identificationChecked ? ' checked="checked"' : '') + '" data-row-id="' + row.id + '" />';
        },
        "hmrcLetterChecked": function(cellvalue, options, row) {
            return '<input ' + (row.status == 3 ? 'disabled="disabled"' : '') + ' id="hmrcLetterChecked_' + row.id + '" type="checkbox" ' + (row.hmrcLetterChecked ? ' checked="checked"' : '') + '" data-row-id="' + row.id + '" />';
        },
        "contributionAmount": function(cellvalue, options, row) {
            return '<div class="input-group"><div class="input-group"><div class="input-group-addon">£</div><input disabled="disabled" id="contributionAmount_' + row.id + '" type="text" value="' + (row.contributionAmount == null ? "0.00" : parseFloat(Math.round(row.contributionAmount * 100) / 100).toFixed(2)) + '" class="form-control"></div></div>';
        },
        "agreedToContributeButNotPaid": function(cellvalue, options, row) {
            return '<input ' + (row.status == 3 ? 'disabled="disabled"' : '') + ' id="agreedToContributeButNotPaid_' + row.id + '" type="checkbox" ' + (row.agreedToContributeButNotPaid ? ' checked="checked"' : '') + '" data-row-id="' + row.id + '" />';
        },
        "mpName": function(cellvalue, options, row) {
            return '<div class="input-group"><input ' + (row.status == 3 ? 'disabled="disabled"' : '') + ' id="mpName_' + row.id + '" type="text" class="form-control" value="' + row.mpName + '"></div>';
        },
        "mpParty": function(cellvalue, options, row) {
            return '<div class="input-group"><input ' + (row.status == 3 ? 'disabled="disabled"' : '') + ' id="mpParty_' + row.id + '" type="text" class="form-control" value="' + row.mpParty + '"></div>';
        },
        "mpConstituency": function(cellvalue, options, row) {
            return '<div class="input-group"><input ' + (row.status == 3 ? 'disabled="disabled"' : '') + ' id="mpConstituency_' + row.id + '" type="text" class="form-control" value="' + row.mpConstituency + '"></div>';
        },
        "mpEngaged": function(cellvalue, options, row) {
            return '<input ' + (row.status == 3 ? 'disabled="disabled"' : '') + ' id="mpEngaged_' + row.id + '" type="checkbox" ' + (row.mpEngaged ? ' checked="checked"' : '') + '" data-row-id="' + row.id + '" />';
        },
        "mpSympathetic": function(cellvalue, options, row) {
            return '<input ' + (row.status == 3 ? 'disabled="disabled"' : '') + ' id="mpSympathetic_' + row.id + '" type="checkbox" ' + (row.mpSympathetic ? ' checked="checked"' : '') + '" data-row-id="' + row.id + '" />';
        },
        "schemes": function(cellvalue, options, row) {
            return '<div class="input-group"><input ' + (row.status == 3 ? 'disabled="disabled"' : '') + ' id="schemes_' + row.id + '" type="text" class="form-control input-large" value="' + row.schemes + '"></div>';
        },
        "notes": function(cellvalue, options, row) {
            return '<div class="input-group"><input ' + (row.status == 3 ? 'disabled="disabled"' : '') + ' id="notes_' + row.id + '" type="text" class="form-control input-large" value="' + row.notes + '"></div>';
        },
        "howDidYouHearAboutLcag": function(cellvalue, options, row) {
            return '<div class="input-group"><input ' + (row.status == 3 ? 'disabled="disabled"' : '') + ' id="howDidYouHearAboutLcag_' + row.id + '" type="text" class="form-control input-large" value="' + row.howDidYouHearAboutLcag + '"></div>';
        },
        "memberOfBigGroup": function(cellvalue, options, row) {
            return '<input ' + (row.status == 3 ? 'disabled="disabled"' : '') + ' id="memberOfBigGroup_' + row.id + '" type="checkbox" ' + (row.memberOfBigGroup ? ' checked="checked"' : '') + '" data-row-id="' + row.id + '" />';
        },
        "bigGroupUsername": function(cellvalue, options, row) {
            return '<div class="input-group"><input ' + (row.status == 3 ? 'disabled="disabled"' : '') + ' id="bigGroupUsername_' + row.id + '" type="text" class="form-control" value="' + row.bigGroupUsername + '"></div>';
        },
        "industry": function(cellvalue, options, row) {
            return '<div class="input-group"><input ' + (row.status == 3 ? 'disabled="disabled"' : '') + ' id="industry_' + row.id + '" type="text" class="form-control input-large" value="' + row.industry + '"></div>';
        },
        "group": function(cellvalue, options, row) {
            if (row.group == "LCAG Guests" || row.group == "Registered" || row.group == "Moderators" || row.group == 'Suspended') {
                return '<select id="group_' + row.id + '" class="form-control">'
                        + '<option ' + (row.group == 'LCAG Guests' ? 'selected="selected"' : '') + '>LCAG Guests</option>'
                        + '<option ' + (row.group == 'Registered' ? 'selected="selected"' : '') + '>Registered</option>'
                        + '<option ' + (row.group == 'Moderators' ? 'selected="selected"' : '') + '>Moderators</option>'
                        + '<option ' + (row.group == 'Suspended' ? 'selected="selected"' : '') + '>Suspended</option>'
                    + '</select>';
            }

            return row.group;
        },
/*
        "lobbyingDayAttending": function(cellvalue, options, row) {
            var disabled = !(row.group == "LCAG Guests" || row.group == "Registered" || row.group == "Moderators");
            console.log("row.group", row.group);
            return '<select id="lobbyingDayAttending_' + row.id + '" class="form-control" ' + (disabled ? 'disabled="disabled" ' : '') + '><option value="UNSET"' + (row.lobbyingDayAttending == 'UNSET ' ? 'selected="selected"' : '') + '>Unset</option><option value="YES" ' + (row.lobbyingDayAttending == 'YES' ? 'selected="selected"' : '') + '>Yes</option><option value="NO" ' + (row.lobbyingDayAttending == 'NO' ? 'selected="selected"' : '') + '>No</option><option value="MAYBE" ' + (row.lobbyingDayAttending == 'MAYBE' ? 'selected="selected"' : '') + '>Maybe</option></select>';
        },
*/
        "hasCompletedMembershipForm": function(cellvalue, options, row) {
            return '<input id="hasCompletedMembershipForm_' + row.id + '" type="checkbox" ' + (row.hasCompletedMembershipForm ? ' checked="checked"' : '') + '" data-row-id="' + row.id + '" />';
        },
        "registeredForClaim": function(cellvalue, options, row) {
            return '<input id="registeredForClaim_' + row.id + '" type="checkbox" ' + (row.registeredForClaim ? ' checked="checked"' : '') + '" data-row-id="' + row.id + '" />';
        },
        "hasCompletedClaimParticipantForm": function(cellvalue, options, row) {
            return '<input id="hasCompletedClaimParticipantForm_' + row.id + '" type="checkbox" ' + (row.hasCompletedClaimParticipantForm ? ' checked="checked"' : '') + '" data-row-id="' + row.id + '" />';
        },
        "hasBeenSentClaimConfirmationEmail": function(cellvalue, options, row) {
            return '<input id="hasBeenSentClaimConfirmationEmail_' + row.id + '" type="checkbox" ' + (row.hasBeenSentClaimConfirmationEmail ? ' checked="checked"' : '') + '" data-row-id="' + row.id + '" />';
        },
        "hasOptedOutOfClaim": function(cellvalue, options, row) {
            return '<input id="hasOptedOutOfClaim_' + row.id + '" type="checkbox" ' + (row.hasOptedOutOfClaim ? ' checked="checked"' : '') + '" data-row-id="' + row.id + '" />';
        },
/*
        "lobbyingDayMpIsMinister": function(cellvalue, options, row) {
            return '<input id="lobbyingDayMpIsMinister_' + row.id + '" type="checkbox" ' + (row.lobbyingDayMpIsMinister ? ' checked="checked"' : '') + '" data-row-id="' + row.id + '" />';
        },
        "lobbyingDayMpHasConfirmedAttendance": function(cellvalue, options, row) {
            return '<input id="lobbyingDayMpHasConfirmedAttendance_' + row.id + '" type="checkbox" ' + (row.lobbyingDayMpHasConfirmedAttendance ? ' checked="checked"' : '') + '" data-row-id="' + row.id + '" />';
        },
        "lobbyingDayHasReceivedMpResponse": function(cellvalue, options, row) {
            return '<input id="lobbyingDayHasReceivedMpResponse_' + row.id + '" type="checkbox" ' + (row.lobbyingDayHasReceivedMpResponse ? ' checked="checked"' : '') + '" data-row-id="' + row.id + '" />';
        },
        "lobbyingDayHasBeenSentMpTemplate": function(cellvalue, options, row) {
            return '<input id="lobbyingDayHasBeenSentMpTemplate_' + row.id + '" type="checkbox" ' + (row.lobbyingDayHasBeenSentMpTemplate ? ' checked="checked"' : '') + '" data-row-id="' + row.id + '" />';
        },
        "lobbyingDayHasSentMpTemplateLetter": function(cellvalue, options, row) {
            return '<input id="lobbyingDayHasSentMpTemplateLetter_' + row.id + '" type="checkbox" ' + (row.lobbyingDayHasSentMpTemplateLetter ? ' checked="checked"' : '') + '" data-row-id="' + row.id + '" />';
        },
        "hasBeenSentInitialMassLobbyingEmail": function(cellvalue, options, row) {
            return '<input id="hasBeenSentInitialMassLobbyingEmail_' + row.id + '" type="checkbox" ' + (row.hasBeenSentInitialMassLobbyingEmail ? ' checked="checked"' : '') + '" data-row-id="' + row.id + '" />';
        },
        "lobbyingDayNotes": function(cellvalue, options, row) {
            return '<div class="input-group"><input ' + (row.status == 3 ? 'disabled="disabled"' : '') + ' id="lobbyingDayNotes_' + row.id + '" type="text" class="form-control input-large" value="' + row.lobbyingDayNotes + '"></div>';
        },
*/
        "country": function(cellvalue, options, row) {
            return '<div class="input-group"><input ' + (row.status == 3 ? 'disabled="disabled"' : '') + ' id="country_' + row.id + '" type="text" class="form-control input-large" value="' + row.country + '"></div>';
        },
        "action": function(cellvalue, options, row) {
            if (row.status != 3) {
                return '<button type="button" class="btn btn-default update-row-btn" data-row-id="' + row.id + '"><span class="fa fa-check fa-lg" aria-hidden="true"></span>&nbsp;Update</button>';
            }
            return "";
        }
    }
}






