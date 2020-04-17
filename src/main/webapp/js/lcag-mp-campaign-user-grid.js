var lcag = lcag || {};

lcag.MpCampaignUserGrid = lcag.MpCampaignUserGrid || {
    grid: {},
    initialise: function() {
        $("#mp-campaign-user-grid").jqGrid({
            colModel: [
                { name: "u_id", label: "ID", width: 0, hidden: true },
                { name: "u_email", label: "email", width: 150, formatter: lcag.MpCampaignUserGrid.formatters.u_email},
                { name: "u_name", label: "Member", width: 300, formatter: lcag.MpCampaignUserGrid.formatters.u_member_det},
                { name: "u_mpName", label: "MP", width: 300, formatter: lcag.MpCampaignUserGrid.formatters.u_mp_det},
                { name: "u_campaignNotes", label: "Notes", width: 300, height: 200, template: "string", formatter: lcag.MpCampaignUserGrid.formatters.u_campaignNotes },
                { name: "u_meetingNext", label: "Next meeting", width: 150, align: "center", search: false, formatter: lcag.MpCampaignUserGrid.formatters.u_meetingNext },
                { name: "u_meetingCount", label: "Meets", width: 50, template: "string", formatter: lcag.MpCampaignUserGrid.formatters.u_meetingCount },
                { name: "u_telephoneCount", label: "Tel", width: 50, template: "string", formatter: lcag.MpCampaignUserGrid.formatters.u_telephoneCount },
                { name: "u_writtenCount", label: "Writ", width: 50, template: "string", formatter: lcag.MpCampaignUserGrid.formatters.u_writtenCount },
                { name: "u_involved", label: "Involved", width: 60, template: "string", formatter: lcag.MpCampaignUserGrid.formatters.u_involved },
                { name: "u_tags", label: "Tags", width: 150, template: "string", formatter: lcag.MpCampaignUserGrid.formatters.u_tags },
                { name: "u_allowEmailShareStatus", label: "Share e-mail", width: 100, template: "string" },
                { name: "u_username", label: "Username", width: 150, template: "string" },
                { name: "u_adminSig", label: "Admin Sig", width: 100, template: "string" },
                { name: "u_constituency", label: "Constituency", width: 150, template: "string", formatter: lcag.MpCampaignUserGrid.formatters.u_constituency },
                { name: "u_extra", label: "Extra", width: 300, formatter: lcag.MpCampaignUserGrid.formatters.u_extra, search: false }
            ],
            datatype: function(postData) {
                    jQuery.ajax({
                        url: lcag.Common.urlPrefix + '/membermpcampaign',
                        data: postData,
                        dataType: "json",
                        complete: function(response, status) {
                            if (status == "success") {
                                lcag.MpCampaignUserGrid.grid = $("#mp-campaign-user-grid");
                                //console.log("jsondata:", response.responseJSON);
                                lcag.MpCampaignUserGrid.grid[0].addJSONData(response.responseJSON);
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
            viewrecords: true,
            rowattr: function (row) {
                if (row.group == "Registered") {
                    return { "class": "success" };
                } else if (row.group == "Administrators") {
                    return { "class": "danger" };
                } else if (row.group == "Moderators") {
                    return { "class": "info" };
                }
            },
            gridComplete: function() {
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
                                  "allowEmailShareStatus": $("#u_allowEmailShareStatus_"+rowid).val(),
                                  "campaignNotes": $("#u_campaignNotes_" + rowid).val(),
                                  "telNo": $("#u_telNo_" + rowid).val(),
                                  "tags": $("#u_tags_" + rowid).val(),
                                  "meetingNext": $("#u_meetingNext_" + rowid).val(),
                                  "meetingCount": $("#u_meetingCount_" + rowid).val(),
                                  "telephoneCount": $("#u_telephoneCount_" + rowid).val(),
                                  "writtenCount": $("#u_writtenCount_" + rowid).val(),
                                  "involved": $("#u_involved_" + rowid).val()
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

        $("#mp-campaign-user-grid").keyup(function (e) {
            if (e.keyCode === 27) {
                $("#mp-campaign-user-grid")[0].clearToolbar();
                return false;
            }
        });

        $(window).bind('resize', function() {
            $("#mp-campaign-user-grid").width($(window).width() -10);
            $("#mp-campaign-user-grid").setGridWidth($(window).width() -10);
            $("#mp-campaign-user-grid").setGridHeight($(window).height()-200);
        }).trigger('resize');
    },

	formatters: {
        "u_email": function(cellvalue, options, row) {
            var shared = row.sharedCampaignEmails;
            var private = row.privateCampaignEmails;
            if( shared == null) {
                shared="";
            }
            if( private == null) {
                private="";
            }
            var shared = shared.replaceAll(row.email,'');
            var private = private.replaceAll(row.email,'');
            return '<div><table>'
                    + '<tr title="'+row.email+'"><th>'+row.email +'</th></tr>'
                    + '<tr title=""><td>&nbsp;</td></tr>'
                    + '<tr title="'+row.email+'"><td><a href="mailto:'+row.email+'">Member</a></td></tr>'
                    + '<tr title=""><td>&nbsp;</td></tr>'
                    + '<tr title="'+row.email+';'+shared+'"><td><a href="mailto:'+row.email+'?cc='+shared+'">Member+CC</a></td></tr>'
                    + '<tr title=""><td>&nbsp;</td></tr>'
                    + '<tr title="'+row.email+';'+shared+';'+private+'"><td><a href="mailto:'+row.email+'?cc='+shared+'&bcc='+private+'">Member+CC+BCC</a></td></tr>'
                + '</table></div>';
        },
        "u_campaignNotes": function(cellvalue, options, row) {
            return '<div class="input-group"><textarea id="u_campaignNotes_' + row.id + '" rows="12" cols="200" class="form-control">' + row.campaignNotes + '</textarea></div>';
        },

        "u_allowEmailShareStatus": function(cellvalue, options, row) {
            var val = cellvalue.substring(0,1).toUpperCase();
            return '<div class="input-group">'
            + '<select id="u_allowEmailShareStatus_' + row.id + '" class="form-control">'
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
        "u_sentInitialEmail": function(cellvalue, options, row) {
            return '<div class="input-group">'
                + '<select id="u_sentInitialEmail_' + row.id + '" class="form-control" >'
                + '<option value="N"' + (row.sentInitialEmail == 'N' ? 'selected="selected"' : '') + '>No</option>'
              + '<option value="Y" ' + (row.sentInitialEmail == 'Y' ? 'selected="selected"' : '') + '>Yes</option></select>'
                + '</div>';
        },
        "u_meetingNext": function(cellvalue, options, row) {
            var dateString = row.meetingNext == null ? "" : moment(row.meetingNext).format("DD/MM/YYYY");
            return '<table>'
                    + '<tr title="'+dateString+'"><td><div class="input-group date"><div class="input-group-addon"><i class="fa fa-calendar"></i></div><input  id="u_meetingNext_' + row.id + '" type="text" class="form-control" value="' + dateString + '"></div></td></tr>'
                    + '<tr><td><br></td></tr>'
                    + '<tr><th title="" align="right"><button type="button" class="btn btn-default update-membermpcampaign-row-btn" data-row-id="' + row.id + '"><span class="fa fa-check fa-lg" aria-hidden="true"></span>&nbsp;Update</button></th></tr>'
                    + '</table>';
        },
        "u_meetingCount": function(cellvalue, options, row) {
            return '<div class="input-group"><input id="u_meetingCount_' + row.id + '" type="text" class="form-control" value="' + row.meetingCount + '"></div>';
        },
        "u_telephoneCount": function(cellvalue, options, row) {
            return '<div class="input-group"><input id="u_telephoneCount_' + row.id + '" type="text" class="form-control input-small" value="' + row.telephoneCount + '"></div>';
        },
        "u_constituency": function(cellvalue, options, row) {
            return '<a target="_blank" href="https://www.bbc.co.uk/news/politics/constituencies/'+row.pCon+ '">'+ row.constituency + '</a>';
        },
        "u_writtenCount": function(cellvalue, options, row) {
            return '<div class="input-group"><input id="u_writtenCount_' + row.id + '" type="text" class="form-control input-small" value="' + row.writtenCount + '"></div>';
        },
        "u_involved": function(cellvalue, options, row) {
            return '<div class="input-group"><input id="u_involved_' + row.id + '" type="text" class="form-control input-small" value="' + row.involved + '"></div>';
        },
        "u_tags": function(cellvalue, options, row) {
            return '<div class="input-group"><textarea id="u_tags_' + row.id + '" rows="3" cols="100" class="form-control">' + row.tags + '</textarea></div>';
        },
        "u_member_det": function(cellvalue, options, row) {
            var val = row.allowEmailShareStatus;
            if( val == null) { val="Not Asked";}
            return '<table>'
                + '<tr title="'+row.name+'"><th>Name</th><td>'+row.name +'</td></tr>'
                + '<tr title="'+row.username+' ('+row.usergroup+')"><th>Username</th><td>'+row.username +'  (' + row.usergroup+ ')</td></tr>'
                + '<tr title="'+row.bigGroupUsername+'"><th>WTT Username</th><td>'+row.bigGroupUsername +'</td></tr>'
                //+ '<tr title="'+row.lobbyingDayAttending+'"><th>Lobby Day</th><td>'+row.lobbyingDayAttending+'</td></tr>'
                + '<tr title="'+row.schemes+'"><th>Schemes</th><td>'+row.schemes +'</td></tr>'
                + '<tr title="'+row.telNo+'"><th>Tel No</th><td>'
                + '<div class="input-group"><input id="u_telNo_' + row.id + '" width="150" type="text" class="form-control" value="' + row.telNo + '"></div>'
                + '</td></tr>'
                + '<tr title="'+row.adminName+' - '+row.adminUsername+'"><th>Administrator</th><td>'+row.adminSig+'</td></tr>'
                + '<tr><th>Email Share</th><td>'
                    + '<select id="u_allowEmailShareStatus_' + row.id + '" class="form-control">'
                        + '<option value="Not Asked"' + (val === "Not Asked" ? ' selected="selected"' : '') + '>***Not Asked***</option>'
                        + '<option value="To Be Shared"' + (val === "To Be Shared" ? ' selected="selected"' : '') + ' >To Be Shared</option>'
                        + '<option value="Private"' + (val === "Private" ? ' selected="selected"' : '') + '>Private</option>'
                        + '<option value="Exclude"' + (val === "Exclude" ? ' selected="selected"' : '') + '>Exclude</option>'
                        + '<option value="Bad Email"' + (val === "Bad Email" ? ' selected="selected"' : '') + '>Bad Email</option>'
                        + '<option value="Awaiting Reply"' + (val === "Awaiting Reply" ? ' selected="selected"' : '') + '>***Awaiting Reply***</option>'
                        + '<option value="Shared"' + (val ==="Shared" ? ' selected="selected"' : '') + ' >***Shared***</option>'
                        + '<option value="Never Replied"' + (val ==="Never Replied" ? ' selected="selected"' : '') + ' >***Never Replied***</option>'
                    + '</select>'
                + '</td></tr>'
                + '</table>';
        },
        "u_mp_det": function(cellvalue, options, row) {
            var shared = row.sharedCampaignEmails;
            var private = row.privateCampaignEmails;
            var privateCount;
            var sharedCount;
            if( shared == null || shared === "") {
                shared="";
                sharedCount=0;
            }
            else {
                sharedCount = shared.split(';').length;
            }
            shared = shared.replaceAll(row.email,'');
            var sharedCsv = shared.replaceAll(';',',');

            if( private == null || private === "") {
                private="";
                privateCount = 0;
            }
            else {
                privateCount = private.split(';').length;
            }
            private = private.replaceAll(row.email,'');
            var privateCsv = private.replaceAll(';',',');

            return '<table>'
                    + '<tr title="'+row.mpName+'"><th>MP</th><td>'+row.mpName+'&nbsp;&nbsp('+row.party+')</td></tr>'
                    + '<tr title="'+row.constituency+'&nbsp;&nbsp('+row.majority+')"><th>Constituency</th><td><a target="_blank" href="https://www.bbc.co.uk/news/politics/constituencies/'+row.pCon+'">'+row.constituency+'</a>&nbsp;&nbsp('+row.majority+')</td></tr>'
                    + '<tr title="'+row.edmStatus+'&nbsp;&nbsp('+row.ministerialStatus+')"><th>EDM/Minis Status</th><td>'+row.edmStatus+'&nbsp;&nbsp('+row.ministerialStatus+')</td></tr>'
                    + '<tr title="'+row.edmUrl+'"><th>EDM URL</th><td><a href="'+row.edmUrl+'" target="_blank">'+row.edmUrl +'</a></td></tr>'
                    + '<tr title="'+row.mpTelNo+'"><th>MP Tel</th><td>'+row.mpTelNo+'</td></tr>'
                    + '<tr title="'+row.mpTwitter+'"><th>MP Twitter</th><td><a href="https://twitter.com/' + row.mpTwitter +'" target="_blank">'+row.mpTwitter +'</a></td></tr>'
                    + '<tr title="'+row.mpEmail+'"><th>MP email</th><td><a href="mailto:' + row.mpEmail +'">'+row.mpEmail +'</a></td></tr>'
                + '</table>'
                + '<table style="border-spacing:5px; border-collapse:separate;">'
                    + '<tr title="">'
                        + '<th title="">Mail</th>'
                        + '<th title="'+shared+'"><a href="mailto:'+shared+'">Shared</a></th>'
                        + '<th title="'+private+'"><a href="mailto:?bcc='+private+'">Private</a></th>'
                        + '<th title="'+shared+';'+private+'"><a href="mailto:'+shared+'?bcc='+private+'">Both</a></th>'
                    + '</tr>'
                    + '<tr title="">'
                        + '<th title="">Count</th>'
                        + '<td title="">'+sharedCount+'</td>'
                        + '<td title="">'+privateCount+'</td>'
                        + '<td title="">'+(sharedCount+privateCount)+'</td>'
                    + '</tr>'
                    + '<tr title="">'
                        + '<th title="">Semicolon</th>'
                        + '<td title="'+shared+'" style="max-width:50px;overflow:hidden;white-space:nowrap;">'+shared+'</td>'
                        + '<td title="'+private+'" style="max-width:50px;overflow:hidden;white-space:nowrap;">'+private+'</td>'
                        + '<td></th>'
                    + '</tr>'
                    + '<tr title="">'
                        + '<th title="">Comma</th>'
                        + '<td title="'+sharedCsv+'" style="max-width:50px;overflow:hidden;white-space:nowrap;">'+sharedCsv+'</td>'
                        + '<td title="'+privateCsv+'" style="max-width:50px;overflow:hidden;white-space:nowrap;">'+privateCsv+'</td>'
                        + '<td></td>'
                    + '</tr>'
                + '</table>';
        },
        "u_extra": function(cellvalue, options, row) {
            return '<table style="border-spacing:5px; border-collapse:separate;">'
                + '<tr title="'+row.parliamentaryEmail +'"><th>Parliamentary e-mail</th><td>'+row.parliamentaryEmail +'</td></tr>'
                + '<tr title="'+row.constituencyEmail +'"><th>Constituency e-mail</th><td>'+row.constituencyEmail +'</td></tr>'
                + '<tr title="'+row.properName+'"><th>Proper Name</th><td>'+row.properName+'</td></tr>'
                + '</table>';
        }
    }
};