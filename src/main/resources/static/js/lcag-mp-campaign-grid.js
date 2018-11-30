var lcag = lcag || {};

lcag.MpCampaignGrid = lcag.MpCampaignGrid || {
    grid: {},
    initialise: function() {
        $("#mp-campaign-grid").jqGrid({
            colModel: [
                { name: "id", label: "ID", hidden: true },
                { name: "mpName", label: "MP Name", width: 150, template: "string" },
                { name: "adminSig", label: "Administrator", width: 150, template: "string" },
                { name: "campaignNotes", label: "Notes", width: 300, height: 200, template: "string", formatter: lcag.MpCampaignGrid.formatters.campaignNotes },
                { name: "other", label: "", width: 500, formatter: lcag.MpCampaignGrid.formatters.other, search: false },
                { name: "tags", label: "Tags", width: 150, template: "string", formatter: lcag.MpCampaignUserGrid.formatters.tags },
                { name: "emails", label: "", width: 400, formatter: lcag.MpCampaignGrid.formatters.emails, search: false },
                { name: "constituency", label: "Constituency", width: 150, template: "string" },
            ],
            datatype: function(postData) {
                    jQuery.ajax({
                        url: lcag.Common.urlPrefix + '/mp',
                        data: postData,
                        dataType: "json",
                        complete: function(response, status) {
                            if (status == "success") {
                                lcag.MpCampaignGrid.grid = $("#mp-campaign-grid");
                                console.log("jsondata:", response.responseJSON);
                                lcag.MpCampaignGrid.grid[0].addJSONData(response.responseJSON);
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
                //lcag.Statistics.refresh();
                $("#mp-campaign-grid").find(".update-mp-row-btn").on("click", function(e) {
                    var rowContext = this;
                    $.ajax({
                          type: "POST",
                          url: lcag.Common.urlPrefix + "/mp/updateCampaign",
                          data: (function() {
                              var id = $(rowContext).data("row-id");
                              lcag.Common.alertPleaseWait();
                              return {
                                  "id": id,
                                  "edmStatus": $("#edmStatus_" + id).val(),
                                  "tags": $("#tags_" + id).val(),
                                  "campaignNotes": $("#campaignNotes_" + id).val()
                            };
                          })(),
                          success: function(e) {
                            lcag.Common.alertSuccess();
                            lcag.MpCampaignGrid.grid.trigger("reloadGrid");
                            //lcag.VerificationGrid.grid.trigger("reloadGrid");
                          },
                          error: function(e) {
                            lcag.Common.alertError();
                            lcag.MpCampaignGrid.grid.trigger("reloadGrid");
                            //lcag.VerificationGrid.grid.trigger("reloadGrid");
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
        $("#mp-campaign-grid").keyup(function (e) {
            if (e.keyCode === 27) {
                $("#mp-campaign-grid")[0].clearToolbar();
                return false;
            }
        });
        $(window).bind('resize', function() {
            $("#mp-campaign-grid").width($(window).width() -10);
            $("#mp-campaign-grid").setGridWidth($(window).width() -10);
            $("#mp-campaign-grid").setGridHeight($(window).height()-200);
        }).trigger('resize');

    },
	formatters: {
        "campaignNotes": function(cellvalue, options, row) {
            return '<div class="input-group"><textarea id="campaignNotes_' + row.id + '" type="textarea" rows="12" cols="200" class="form-control">' + row.campaignNotes + '</textarea></div>';
        },

        "other": function(cellvalue, options, row) {
            var edm = row.edmStatus.substring(0,3).toUpperCase();
            return '<table>'
                + '<tr title="'+row.edmStatus+'"><th>EDM</th><td style="width: 100px;">'
                + '<select id="edmStatus_' + row.id + '" class="form-control" >'
                + '<option value="Not Asked"' + (edm === 'NOT' ? 'selected="selected"' : '') + '>Not Asked</option>'
                + '<option value="Signed" ' + (edm === 'SIG' ? 'selected="selected"' : '') + '>Signed</option>'
                + '<option value="Refused" ' + (edm === 'REF' ? 'selected="selected"' : '') + '>Refused</option>'
                + '<option value="No Point" ' + (edm === 'NO ' ? 'selected="selected"' : '') + '>No Point</option>'
                + '<option value="Sympathetic" ' + (edm === 'SYM' ? 'selected="selected"' : '') + '>Sympathetic</option></select>'
                + '</td></tr>'
                + '<tr title="'+row.edmUrl+'"><th>EDM URL</th><td><a href="'+row.edmUrl+'" target="_blank">'+row.edmUrl +'</a></td></tr>'
                + '<tr title="'+row.party+'"><th>Party</th><td>'+row.party +'</td></tr>'
                + '<tr title="'+row.ministerialStatus+'"><th>Ministerial</th><td>'+row.ministerialStatus +'</td></tr>'
                + '<tr title="'+row.twitter+'"><th>Twitter</th><td><a href="https://twitter.com/'+row.twitter+'" target="_blank">'+row.twitter +'</a></td></tr>'
                + '<tr title="'+row.constituency+'"><th>Constituency</th><td>'+row.constituency+'</td></tr>'
                + '<tr title="'+row.constituencyAddress+'"><th>Address</th><td>'+row.constituencyAddress+'</td></tr>'
                + '<tr title="'+row.majority+'"><th>Majority</th><td>'+row.majority+'</td></tr>'
                + '<tr title="'+row.telNo+'"><th>Telephone</th><td>'+row.telNo +'</td></tr>'
                + '<tr title="'+row.email+'"><th>e-mail</th><td><a href="mailto:' + row.email +'">'+row.email +'</a></td></tr>'
                + '<tr title="'+row.url+'"><th>URL</th><td><a href="'+row.url+'" target="_blank">'+row.url +'</a></td></tr>'
                + '<tr><td colspan="2">&nbsp;</td> </tr>'
                + '<tr><td colspan="2">&nbsp;</td> </tr>'
                + '<tr><td colspan="2" align="center"><button type="button" class="btn btn-default update-mp-row-btn" data-row-id="' + row.id + '"><span class="fa fa-check fa-lg" aria-hidden="true"></span>&nbsp;Update</button></td></tr>'
                + '</table>';
        },
        "tags": function(cellvalue, options, row) {
            return '<div class="input-group"><textarea id="tags_' + row.id + '" type="textarea" rows="3" cols="100" class="form-control">' + row.tags + '</textarea></div>';
//            return '<div class="input-group"><input id="tags_' + row.id + '" type="text" class="form-control input-small" value="' + row.tags + '"></div>';
        },
        "emails": function(cellvalue, options, row) {
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
            var sharedCsv = shared.replaceAll(';',',');

            if( private == null || private === "") {
                private="";
                privateCount = 0;
            }
            else {
                privateCount = private.split(';').length;
            }
            var privateCsv = private.replaceAll(';',',');

            return '<table style="border-spacing:5px; border-collapse:separate;">'
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
/*
        "emails": function(cellvalue, options, row) {

            var shared = row.sharedCampaignEmails;
            var private = row.privateCampaignEmails;
            if( shared == null) { shared="";}
            var sharedCsv = shared.replaceAll(';',',');
            if( private == null) { private="";}
            var privateCsv = private.replaceAll(';',',');
            return '<table>'
                + '<tr title="'+shared+'"><th>Shared (;)</th><td>&nbsp;</td><td>'+shared+'</td></tr>'
                + '<tr title="'+private+'"><th>Private (;)</th><td>&nbsp;</td><td>'+private+'</td></tr>'
                + '<tr title=""><td colspan="3">&nbsp;</td> </tr>'
                + '<tr title="'+sharedCsv+'"><th>Shared (,)</th><td>&nbsp;</td><td>'+sharedCsv+'</td></tr>'
                + '<tr title="'+privateCsv+'"><th>Private (,)</th><td>&nbsp;</td><td>'+privateCsv+'</td></tr>'
                + '<tr title=""><td colspan="3">&nbsp;</td> </tr>'
                + '<tr title=""><th>Mail</th><td>&nbsp;</td><td>'
                + '<a href="mailto:'+shared+'">Shared</a><br>'
                + '<a href="mailto:?bcc='+private+'">Private</a><br>'
                + '<a href="mailto:'+shared+'?bcc='+private+'">Both</a>'
                + '</td></tr>'
                + '</table>';
        },
*/
    }
}
