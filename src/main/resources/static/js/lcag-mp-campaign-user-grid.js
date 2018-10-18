var lcag = lcag || {};

lcag.MpCampaignUserGrid = lcag.MpCampaignUserGrid || {
    grid: {},
    initialise: function() {
        $("#mp-campaign-user-grid").jqGrid({
            colModel: [
                { name: "id", label: "ID", hidden: true },
                { name: "administratorName", label: "Administrator", width: 150, template: "string" },
                { name: "mpName", label: "MP Name", width: 150, template: "string" },
                { name: "name", label: "Name", width: 150, template: "string" },
                { name: "allowEmailShareStatus", label: "Share e-mail", width: 150, formatter: lcag.MpCampaignUserGrid.formatters.allowEmailShareStatus },
                { name: "sentInitialEmail", label: "Initial e-mail", width: 150, formatter: lcag.MpCampaignUserGrid.formatters.sentInitialEmail },
                { name: "campaignNotes", label: "Notes", width: 300, height: 200, template: "string", formatter: lcag.MpCampaignUserGrid.formatters.campaignNotes },
                { name: "action", label: "", width: 250, formatter: lcag.MpCampaignUserGrid.formatters.action, search: false },
//                { name: "meetingNext", label: "Next meeting", width: 150, template: "string", formatter: lcag.MpCampaignUserGrid.formatters.meetingNext },
                { name: "meetingNext", label: "Next Meeting", width: 150, align: "center", sorttype: "date", formatter: lcag.MemberGrid.formatters.meetingDate },

//                { name: "meetingNext", label: "Next meeting", search: true, searchoptions: {
//                        sopt: ['eq'],
//                        dataInit: function(e) {
//                            $(e).datetimepicker({
//                                dateFormat: 'yyyymmdd '
//                            })
//                                .change(function() {
//                                    $("#mp-campaign-user-grid")[0].triggerToolbar();
//                               });
//                        }
//                    } },
                { name: "meetingCount", label: "Meetings", width: 60, template: "string", formatter: lcag.MpCampaignUserGrid.formatters.meetingCount },
                { name: "telephoneCount", label: "Telephone", width: 60, template: "string", formatter: lcag.MpCampaignUserGrid.formatters.telephoneCount },
                { name: "writtenCount", label: "Written", width: 60, template: "string", formatter: lcag.MpCampaignUserGrid.formatters.writtenCount },
                { name: "involved", label: "Involved", width: 60, template: "string", formatter: lcag.MpCampaignUserGrid.formatters.involved }

            ],
            datatype: function(postData) {
                    jQuery.ajax({
                        url: lcag.Common.urlPrefix + '/membermpcampaign',
                        data: postData,
                        dataType: "json",
                        complete: function(response, status) {
                            if (status == "success") {
                                lcag.MpCampaignUserGrid.grid = $("#mp-campaign-user-grid");
                                console.log("jsondata:", response.responseJSON);
                                lcag.MpCampaignUserGrid.grid[0].addJSONData(response.responseJSON);
                            }
                        }
                    });
            },
            iconSet: "fontAwesome",
            editurl: 'clientArray',
            sortname: "id",
            sortorder: "desc",
            threeStateSort: false,
            cmTemplate: { autoResizable: true },
            autoResizing: { compact: true },
            autoresizeOnLoad: true,
            headertitles: true,
            pager: true,
            rowNum: 25,
            //width: "2500", // 8500px
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
                //$('#mp-campaign-user-grid').jqGrid("editCell", 0, 0, false);
                //lcag.Statistics.refresh();
                $("#mp-campaign-user-grid").find(".update-membermpcampaign-row-btn").on("click", function(e) {
                    var rowContext = this;
                    $.ajax({
                          type: "POST",
                          url: lcag.Common.urlPrefix + "/membermpcampaign/update",
                          data: (function() {
                              var rowid = $(rowContext).data("row-id");
                              $("#mp-campaign-user-grid").saveRow(rowid, false, 'clientArray');
                              lcag.Common.alertPleaseWait();
                              return {
                                  "id": rowid,
                                  "allowEmailShareStatus": $("#allowEmailShareStatus_"+rowid).val(),
                                  "sentInitialEmail": $("#sentInitialEmail_"+rowid).val(),
                                  "campaignNotes": $("#campaignNotes_" + rowid).val(),
                                  "meetingNext": $("#meetingNext_" + rowid).val(),
                                  "meetingCount": $("#meetingCount_" + rowid).val(),
                                  "telephoneCount": $("#telephoneCount_" + rowid).val(),
                                  "writtenCount": $("#writtenCount_" + rowid).val(),
                                  "involved": $("#involved_" + rowid).val()
                            };
                          })(),
                          success: function(e) {
                            lcag.Common.alertSuccess();
                            lcag.MpCampaignUserGrid.grid.trigger("reloadGrid");
                          },
                          error: function(e) {
                            lcag.Common.alertError();
                            lcag.MpCampaignUserGrid.grid.trigger("reloadGrid");
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
        "campaignNotes": function(cellvalue, options, row) {
            return '<div class="input-group"><textarea id="campaignNotes_' + row.id + '" rows="12" cols="200" class="form-control">' + row.campaignNotes + '</textarea></div>';
        },

        "allowEmailShareStatus": function(cellvalue, options, row) {
            return '<div class="input-group">'
            + '<select id="allowEmailShareStatus_' + row.id + '" class="form-control" >'
            + '<option value="Not Asked"' + (row.allowEmailShareStatus == "Not Asked" ? 'selected="selected"' : '') + '>Not Asked</option>'
            + '<option value="Awaiting Reply" ' + (row.allowEmailShareStatus == "Awaiting Reply" ? 'selected="selected"' : '') + ' disabled=""">Awaiting Reply</option>'
            + '<option value="To Be Shared" ' + (row.allowEmailShareStatus == "To Be Shared" ? 'selected="selected"' : '') + ' >To Be Shared</option>'
            + '<option value="Shared" ' + (row.allowEmailShareStatus == "Shared" ? 'selected="selected"' : '') + ' disabled="">Shared</option>'
            + '<option value="Private" ' + (row.allowEmailShareStatus == "Private" ? 'selected="selected"' : '') + '>Private</option>'
            + '<option value="Exclude" ' + (row.allowEmailShareStatus == "Exclude" ? 'selected="selected"' : '') + '>Exclude</option>'
            + '</select>'
            + '</div>';
        },
        "sentInitialEmail": function(cellvalue, options, row) {
            return '<div class="input-group">'
                + '<select id="sentInitialEmail_' + row.id + '" class="form-control" >'
                + '<option value="N"' + (row.sentInitialEmail == 'N' ? 'selected="selected"' : '') + '>No</option>'
              + '<option value="Y" ' + (row.sentInitialEmail == 'Y' ? 'selected="selected"' : '') + '>Yes</option></select>'
                + '</div>';
        },
        "meetingNext": function(cellvalue, options, row) {
//            return '<div class="input-group"><input id="meetingNext_' + row.id + '" type="text" class="form-control input-large" value="' + row.meetingNext + '"></div>';
            var dateString = row.meetingNext == null ? "" : moment(row.meetingNext).format("DD/MM/YYYY");
            return '<div class="input-group date"><div class="input-group-addon"><i class="fa fa-calendar"></i></div><input ' + ' id="meetingNext_' + row.id + '" type="text" class="form-control" value="' + dateString + '"></div>';

        },
        "meetingCount": function(cellvalue, options, row) {
            return '<div class="input-group"><input id="meetingCount_' + row.id + '" type="text" class="form-control input-large" value="' + row.meetingCount + '"></div>';
        },
        "telephoneCount": function(cellvalue, options, row) {
            return '<div class="input-group"><input id="telephoneCount_' + row.id + '" type="text" class="form-control input-large" value="' + row.telephoneCount + '"></div>';
        },
        "writtenCount": function(cellvalue, options, row) {
            return '<div class="input-group"><input id="writtenCount_' + row.id + '" type="text" class="form-control input-large" value="' + row.writtenCount + '"></div>';
        },
        "involved": function(cellvalue, options, row) {
            return '<div class="input-group"><input id="involved_' + row.id + '" type="text" class="form-control input-large" value="' + row.involved + '"></div>';
        },
        "action": function(cellvalue, options, row) {
            return '<table>'
                + '<tr><th>Name</th><td>'+row.name +'</td></tr>'
                + '<tr><th>Username</th><td>'+row.username +'  (' + row.usergroup+ ')</td></tr>'
                + '<tr><th>Lobby Day</th><td>'+row.lobbyingDayAttending+'</td></tr>'
                + '<tr><th>Posts</th><td>'+row.postnum +'</td></tr>'
                + '<tr><th>Threads</th><td>'+row.threadnum +'</td></tr>'
                + '<tr><th>Last Visit</th><td>'+row.lastvisit +'</td></tr>'
                + '<tr><th>Schemes</th><td>'+row.schemes +'</td></tr>'
                + '<tr><th>MP</th><td>'+row.mpName+'</td></tr>'
                + '<tr><th>EDM URL</th><td><a href="'+row.edmUrl+'" target="_blank">'+row.edmUrl +'</a></td></tr>'
                + '<tr><th>e-mail</th><td><a href="mailto:' + row.email +'">'+row.email +'</a></td></tr>'
                + '<tr><th colspan="2"><button type="button" class="btn btn-default update-membermpcampaign-row-btn" data-row-id="' + row.id + '"><span class="fa fa-check fa-lg" aria-hidden="true"></span>&nbsp;Update</button></th></tr>'
                + '</table>'
        }
    }
};