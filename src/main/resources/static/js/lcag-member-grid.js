var lcag = lcag || {};

lcag.MemberGrid = lcag.MemberGrid || {
    grid: {},
    initialise: function() {
        $("#member-grid").jqGrid({
            colModel: [
                { name: "id", label: "ID", hidden: true },
                { name: "registrationDate", label: "Registration Date", width: 90, align: "center", sorttype: "date", formatter: "date", formatoptions: { newformat: "d-M-Y" }, formatter: lcag.MemberGrid.formatters.registrationDate },
                { name: "name", label: "Name", width: 150, template: "string", formatter: lcag.MemberGrid.formatters.name },
                { name: "username", label: "Username", width: 150, template: "string" },
                { name: "emailAddress", label: "Email Address", width: 150, template: "string" },
                { name: "hmrcLetterChecked", label: "HMRC Letter Received", width: 59, formatter: lcag.MemberGrid.formatters.hmrcLetterChecked, stype: "select", searchoptions: { sopt: ["eq", "ne"], value: ":Any;1:Yes;0:No" } },
                { name: "identificationChecked", label: "Identification Checked", width: 59, formatter: lcag.MemberGrid.formatters.identificationChecked, stype: "select", searchoptions: { sopt: ["eq", "ne"], value: ":Any;1:Yes;0:No" } },
                { name: "contributionAmount", label: "Contribution Amount", width: 90, align: "center", formatter: lcag.MemberGrid.formatters.contributionAmount },
                { name: "contributionDate", label: "Contribution Date", width: 110, align: "center", sorttype: "date", formatter: lcag.MemberGrid.formatters.contributionDate},
                { name: "agreedToContributeButNotPaid", label: "Agreed To Contribute But Not Paid", width: 59, formatter: lcag.MemberGrid.formatters.agreedToContributeButNotPaid, stype: "select", searchoptions: { sopt: ["eq", "ne"], value: ":Any;1:Yes;0:No" } },
                { name: "mpName", label: "MP Name", width: 90, formatter: lcag.MemberGrid.formatters.mpName },
                { name: "mpParty", label: "MP Party", width: 90, formatter: lcag.MemberGrid.formatters.mpParty },
                { name: "mpConstituency", label: "MP Constituency", width: 90, formatter: lcag.MemberGrid.formatters.mpConstituency },
                { name: "mpEngaged", label: "MP Engaged", width: 59, formatter: lcag.MemberGrid.formatters.mpEngaged, stype: "select", searchoptions: { sopt: ["eq", "ne"], value: ":Any;1:Yes;0:No" } },
                { name: "mpSympathetic", label: "MP Sympathetic", width: 59, formatter: lcag.MemberGrid.formatters.mpSympathetic, stype: "select", searchoptions: { sopt: ["eq", "ne"], value: ":Any;1:Yes;0:No" } },
                { name: "schemes", label: "Schemes", width: 250, formatter: lcag.MemberGrid.formatters.schemes },
                { name: "industry", label: "Industry", width: 250, formatter: lcag.MemberGrid.formatters.industry },
                { name: "notes", label: "Notes", width: 250, formatter: lcag.MemberGrid.formatters.notes },
                { name: "group", label: "Group", width: 90, formatter: lcag.MemberGrid.formatters.group, stype: "select", searchoptions: { sopt: ["eq", "ne"], value: ":Any;LCAG Guests:LCAG Guests;Registered:Registered;Moderators:Moderators;Administrators:Administrators" } },
                { name: "action", label: "", width: 65, formatter: lcag.MemberGrid.formatters.action, search: false }
            ],
            datatype: function(postData) {
                    jQuery.ajax({
                        url: lcag.Common.urlPrefix + '/member',
                        data: postData,
                        dataType: "json",
                        complete: function(response, status) {
                            if (status == "success") {
                                lcag.MemberGrid.grid = $("#member-grid")[0];
                                lcag.MemberGrid.grid.addJSONData(response.responseJSON);
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
            width: "3000px",
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
                            console.log("rowContext", rowContext);
                            var id = $(rowContext).data("row-id");
                            var contributionDate = $("#contributionDate_" + id).val();
                            var formattedContributionDate = null;

                            if (contributionDate != null && contributionDate != "") {
                                formattedContributionDate = moment(contributionDate, "DD/MM/YYYY").format();
                            }

                            console.log("id", id);

                            return {
                                "id": id,
                                "identificationChecked": $("#identificationChecked_" + id).prop("checked"),
                                "hmrcLetterChecked": $("#hmrcLetterChecked_" + id).prop("checked"),
                                "contributionAmount": $("#contributionAmount_" + id).val(),
                                "contributionDate": formattedContributionDate,
                                "agreedToContributeButNotPaid": $("#agreedToContributeButNotPaid_" + id).prop("checked"),
                                "mpName": $("#mpName_" + id).val(),
                                "mpParty": $("#mpParty_" + id).val(),
                                "mpConstituency": $("#mpConstituency_" + id).val(),
                                "mpEngaged": $("#mpEngaged_" + id).prop("checked"),
                                "mpSympathetic": $("#mpSympathetic_" + id).prop("checked"),
                                "schemes": $("#schemes_" + id).val(),
                                "industry": $("#industry_" + id).val(),
                                "notes": $("#notes_" + id).val(),
                                "group": $("#group_" + id).val()
                            };
                          })(),
                          success: function(e) {
                            $("#grid").trigger("reloadGrid");
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
    },
	formatters: {
        "registrationDate": function(cellvalue, options, row) {
            return moment(row.registrationDate).format("DD/MM/YYYY HH:mm");
        },
        "identificationChecked": function(cellvalue, options, row) {
            return '<input ' + (row.status == 3 ? 'disabled="disabled"' : '') + ' id="identificationChecked_' + row.id + '" type="checkbox" ' + (row.identificationChecked ? ' checked="checked"' : '') + '" data-row-id="' + row.id + '" />';
        },
        "hmrcLetterChecked": function(cellvalue, options, row) {
            return '<input ' + (row.status == 3 ? 'disabled="disabled"' : '') + ' id="hmrcLetterChecked_' + row.id + '" type="checkbox" ' + (row.hmrcLetterChecked ? ' checked="checked"' : '') + '" data-row-id="' + row.id + '" />';
        },
        "contributionAmount": function(cellvalue, options, row) {
            return '<div class="input-group"><div class="input-group"><div class="input-group-addon">£</div><input ' + (row.status == 3 ? 'disabled="disabled"' : '') + ' id="contributionAmount_' + row.id + '" type="text" value="' + row.contributionAmount + '" class="form-control"></div></div>';
        },
        "contributionDate": function(cellvalue, options, row) {
            var dateString = row.contributionDate == null ? "" : moment(row.contributionDate).format("DD/MM/YYYY");
            return '<div class="input-group date"><div class="input-group-addon"><i class="fa fa-calendar"></i></div><input ' + (row.status == 3 ? 'disabled="disabled"' : '') + ' id="contributionDate_' + row.id + '" type="text" class="form-control" value="' + dateString + '"></div>';
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
        "industry": function(cellvalue, options, row) {
            return '<div class="input-group"><input ' + (row.status == 3 ? 'disabled="disabled"' : '') + ' id="industry_' + row.id + '" type="text" class="form-control input-large" value="' + row.industry + '"></div>';
        },
        "group": function(cellvalue, options, row) {
            if (row.group == "LCAG Guests" || row.group == "Registered" || row.group == "Moderators") {
                return '<select id="group_' + row.id + '" class="form-control"><option ' + (row.group == 'LCAG Guests' ? 'selected="selected"' : '') + '>LCAG Guests</option><option ' + (row.group == 'Registered' ? 'selected="selected"' : '') + '>Registered</option><option ' + (row.group == 'Moderators' ? 'selected="selected"' : '') + '>Moderators</option></select>';
            }

            return row.group;
        },
        "action": function(cellvalue, options, row) {
            if (row.status != 3) {
                return '<button type="button" class="btn btn-default update-row-btn" data-row-id="' + row.id + '"><span class="fa fa-check fa-lg" aria-hidden="true"></span>&nbsp;Update</button>';
            }

            return "";
        }
    }
}


