var lcag = lcag || {};

lcag.MpCampaignUserGrid = lcag.MpCampaignUserGrid || {
    grid: {},
    initialise: function() {
        $("#mp-campaign-user-grid").jqGrid({
            colModel: [
                { name: "id", label: "ID", hidden: true },
                { name: "email", label: "email", width: 150, template: "string" },
                { name: "name", label: "User", width: 300, formatter: lcag.MpCampaignUserGrid.formatters.userdet},//, search: false },
                { name: "mpName", label: "MP", width: 300, formatter: lcag.MpCampaignUserGrid.formatters.mpdet},//, search: false },
                { name: "campaignNotes", label: "Notes", width: 300, height: 200, template: "string", formatter: lcag.MpCampaignUserGrid.formatters.campaignNotes },
                { name: "meetingNext", label: "Next meeting", width: 150, align: "center", sorttype: "date", formatter: lcag.MpCampaignUserGrid.formatters.meetingNext },
                { name: "meetingCount", label: "Meets", width: 50, template: "string", formatter: lcag.MpCampaignUserGrid.formatters.meetingCount },
                { name: "telephoneCount", label: "Tel", width: 50, template: "string", formatter: lcag.MpCampaignUserGrid.formatters.telephoneCount },
                { name: "writtenCount", label: "Writ", width: 50, template: "string", formatter: lcag.MpCampaignUserGrid.formatters.writtenCount },
                { name: "involved", label: "Involved", width: 60, template: "string", formatter: lcag.MpCampaignUserGrid.formatters.involved },
                { name: "tags", label: "Tags", width: 150, template: "string", formatter: lcag.MpCampaignUserGrid.formatters.tags },
//                { name: "name", label: "Name", width: 150, template: "string" },
//                { name: "allowEmailShareStatus", label: "Share e-mail", width: 100, formatter: lcag.MpCampaignUserGrid.formatters.allowEmailShareStatus },
                { name: "allowEmailShareStatus", label: "Share e-mail", width: 100, template: "string" },
                { name: "username", label: "Username", width: 150, template: "string" },
                { name: "administratorName", label: "Administrator", width: 150, template: "string" },
                { name: "mpConstituency", label: "Constituency", width: 150, template: "string" },
//                { name: "mpName", label: "MP Name", width: 150, template: "string" }
                { name: "extra", label: "Extra", width: 300, formatter: lcag.MpCampaignUserGrid.formatters.extra, search: false }


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
                                  "campaignNotes": $("#campaignNotes_" + rowid).val(),
                                  "telNo": $("#telNo_" + rowid).val(),
                                  "tags": $("#tags_" + rowid).val(),
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
                    //format: "LT"
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
            var val = cellvalue.substring(0,1).toUpperCase();
            return '<div class="input-group">'
            + '<select id="allowEmailShareStatus_' + row.id + '" class="form-control">'
                + '<option value="Not Asked"' + (val === "N" ? ' selected="selected"' : '') + '>Not Asked</option>'
                + '<option value="To Be Shared"' + (val === "T" ? ' selected="selected"' : '') + ' >To Be Shared</option>'
                + '<option value="Private"' + (val === "P" ? ' selected="selected"' : '') + '>Private</option>'
                + '<option value="Exclude"' + (val === "E" ? ' selected="selected"' : '') + '>Exclude</option>'
                + '<option value="Bad Email"' + (val === "B" ? ' selected="selected"' : '') + '>Bad Email</option>'
                + '<option value="Awaiting Reply"' + (val === "A" ? ' selected="selected"' : '') + '>***Awaiting Reply***</option>'
                + '<option value="Shared"' + (val ==="S" ? ' selected="selected"' : '') + ' >***Shared***</option>'
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
            var dateString = row.meetingNext == null ? "" : moment(row.meetingNext).format("DD/MM/YYYY");
            return '<div class="input-group date"><div class="input-group-addon"><i class="fa fa-calendar"></i></div><input  id="meetingNext_' + row.id + '" type="text" class="form-control" value="' + dateString + '"></div>';

        },
        "meetingCount": function(cellvalue, options, row) {
            return '<div class="input-group"><input id="meetingCount_' + row.id + '" type="text" class="form-control" value="' + row.meetingCount + '"></div>';
        },
        "telephoneCount": function(cellvalue, options, row) {
            return '<div class="input-group"><input id="telephoneCount_' + row.id + '" type="text" class="form-control input-small" value="' + row.telephoneCount + '"></div>';
        },
        "writtenCount": function(cellvalue, options, row) {
            return '<div class="input-group"><input id="writtenCount_' + row.id + '" type="text" class="form-control input-small" value="' + row.writtenCount + '"></div>';
        },
        "involved": function(cellvalue, options, row) {
            return '<div class="input-group"><input id="involved_' + row.id + '" type="text" class="form-control input-small" value="' + row.involved + '"></div>';
        },
        "tags": function(cellvalue, options, row) {
            return '<div class="input-group"><textarea id="tags_' + row.id + '" rows="3" cols="100" class="form-control">' + row.tags + '</textarea></div>';
//            return '<div class="input-group"><input id="tags_' + row.id + '" type="text" class="form-control input-small" value="' + row.tags + '"></div>';
        },
        "userdet": function(cellvalue, options, row) {
            var val = row.allowEmailShareStatus.substring(0,1).toUpperCase();
            if( val == null) { val="N";}
            return '<table>'
                + '<tr><th>Name</th><td>'+row.name +'</td></tr>'
                + '<tr><th>Username</th><td>'+row.username +'  (' + row.usergroup+ ')</td></tr>'
                + '<tr><th>Lobby Day</th><td>'+row.lobbyingDayAttending+'</td></tr>'
                + '<tr><th>Schemes</th><td>'+row.schemes +'</td></tr>'
                + '<tr><th>Tel No</th><td>'
                + '<div class="input-group"><input id="telNo_' + row.id + '" width="150" type="text" class="form-control" value="' + row.telNo + '"></div>'
                + '</td></tr>'
                + '<tr><th>Administrator</th><td>'+row.administratorName+'</td></tr>'
                + '<tr><th>Email Share</th><td>'
                    + '<select id="allowEmailShareStatus_' + row.id + '" class="form-control">'
                        + '<option value="Not Asked"' + (val === "N" ? ' selected="selected"' : '') + '>Not Asked</option>'
                        + '<option value="To Be Shared"' + (val === "T" ? ' selected="selected"' : '') + ' >To Be Shared</option>'
                        + '<option value="Private"' + (val === "P" ? ' selected="selected"' : '') + '>Private</option>'
                        + '<option value="Exclude"' + (val === "E" ? ' selected="selected"' : '') + '>Exclude</option>'
                        + '<option value="Bad Email"' + (val === "B" ? ' selected="selected"' : '') + '>Bad Email</option>'
                        + '<option value="Awaiting Reply"' + (val === "A" ? ' selected="selected"' : '') + '>***Awaiting Reply***</option>'
                        + '<option value="Shared"' + (val ==="S" ? ' selected="selected"' : '') + ' >***Shared***</option>'
                    + '</select>'
                + '</td></tr>'
                + '<tr><th align="right" colspan="2"><button type="button" class="btn btn-default update-membermpcampaign-row-btn" data-row-id="' + row.id + '"><span class="fa fa-check fa-lg" aria-hidden="true"></span>&nbsp;Update</button></th></tr>'
                + '</table>';
        },
        "mpdet": function(cellvalue, options, row) {
            var shared = row.sharedCampaignEmails;
            var private = row.privateCampaignEmails;
            var privateCount;
            var sharedCount;
            if( shared == null) {
                shared="";
                sharedCount=0;
            }
            else {
                sharedCount = shared.split(';').length;
            }
            var sharedCsv = shared.replaceAll(';',',');
            if( private == null) {
                private="";
                privateCount = 0;
            }
            else {
                privateCount = private.split(';').length;
            }
            var privateCsv = private.replaceAll(';',',');

            return '<table>'
                    + '<tr><th>MP</th><td>'+row.mpName+'&nbsp;&nbsp('+row.party+')</td></tr>'
                    + '<tr><th>Constituency</th><td>'+row.mpConstituency+'&nbsp;&nbsp('+row.majority+')</td></tr>'
                    + '<tr><th>EDM Status</th><td>'+row.edmStatus +'</td></tr>'
                    + '<tr><th>EDM URL</th><td><a href="'+row.edmUrl+'" target="_blank">'+row.edmUrl +'</a></td></tr>'
                    + '<tr><th>MP Tel</th><td>'+row.mpTelNo+'</td></tr>'
                    + '<tr><th>MP Twitter</th><td><a href="https://twitter.com/' + row.mpTwitter +'" target="_blank">'+row.mpTwitter +'</a></td></tr>'
                    + '<tr><th>MP email</th><td><a href="mailto:' + row.mpEmail +'">'+row.mpEmail +'</a></td></tr>'
                + '</table>'
//                + '<br>'
                + '<table style="border-spacing:5px; border-collapse:separate;">'
                    + '<tr title="">'
                        + '<th>Mail</th>'
                        + '<th><a href="mailto:'+shared+'">Shared</a></th>'
                        + '<th><a href="mailto:?bcc='+private+'">Private</a></th>'
                        + '<th><a href="mailto:'+shared+'?bcc='+private+'">Both</a></th>'
                    + '</tr>'
                    + '<tr>'
                        + '<th>Count</th>'
                        + '<td>'+sharedCount+'</td>'
                        + '<td>'+privateCount+'</td>'
                        + '<td>'+(sharedCount+privateCount)+'</td>'
                    + '</tr>'
                    + '<tr>'
                        + '<th>Semicolon</th>'
                        + '<td style="max-width:50px;overflow:hidden;white-space:nowrap;">'+shared+'</td>'
                        + '<td style="max-width:50px;overflow:hidden;white-space:nowrap;">'+private+'</td>'
                        + '<td></th>'
                    + '</tr>'
                    + '<tr>'
                        + '<th>Comma</th>'
                        + '<td style="max-width:50px;overflow:hidden;white-space:nowrap;">'+sharedCsv+'</td>'
                        + '<td style="max-width:50px;overflow:hidden;white-space:nowrap;">'+privateCsv+'</td>'
                        + '<td></th>'
                    + '</tr>'
//                    + '<tr title="'+shared+'"><th>Shared</th><td style="max-width:50px;overflow:hidden;white-space:nowrap;">'+shared+'</td><td style="max-width:50px;overflow:hidden;white-space:nowrap;">'+sharedCsv+'</td></tr>'
//                    + '<tr title="'+private+'"><th>Private</th><td style="max-width:50px;overflow:hidden;white-space:nowrap;">'+private+'</td><td style="max-width:50px;overflow:hidden;white-space:nowrap;">'+privateCsv+'</td></tr>'
                + '</table>';
        },
        "extra": function(cellvalue, options, row) {
            var val = row.allowEmailShareStatus.substring(0,1).toUpperCase();
            if( val == null) { val="N";}
            return '<table style="border-spacing:5px; border-collapse:separate;">'
                + '<tr><th>Parliamentary e-mail</th><td>'+row.parliamentaryEmail +'</td></tr>'
                + '<tr><th>Constituency e-mail</th><td>'+row.constituencyEmail +'</td></tr>'
                + '<tr><th>Proper Name</th><td>'+row.properName+'</td></tr>'
                + '</table>';
        }
    }
};