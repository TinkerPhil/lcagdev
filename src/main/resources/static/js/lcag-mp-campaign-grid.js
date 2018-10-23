var lcag = lcag || {};

lcag.MpCampaignGrid = lcag.MpCampaignGrid || {
    grid: {},
    initialise: function() {
        $("#mp-campaign-grid").jqGrid({
            colModel: [
                { name: "id", label: "ID", hidden: true },
                { name: "mpName", label: "MP Name", width: 150, template: "string" },
                { name: "administratorName", label: "Administrator", width: 150, template: "string" },
                { name: "campaignNotes", label: "Notes", width: 300, height: 200, template: "string", formatter: lcag.MpCampaignGrid.formatters.campaignNotes },
                { name: "other", label: "", width: 600, formatter: lcag.MpCampaignGrid.formatters.other, search: false },
                { name: "tags", label: "Tags", width: 150, template: "string", formatter: lcag.MpCampaignUserGrid.formatters.tags },
                { name: "emails", label: "", width: 400, formatter: lcag.MpCampaignGrid.formatters.emails, search: false }
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
            iconSet: "fontAwesome",
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
    },
	formatters: {
        "campaignNotes": function(cellvalue, options, row) {
            return '<div class="input-group"><textarea id="campaignNotes_' + row.id + '" type="textarea" rows="12" cols="200" class="form-control">' + row.campaignNotes + '</textarea></div>';
        },

        "other": function(cellvalue, options, row) {
            var edm = row.edmStatus.substring(0,2).toUpperCase();
            return '<table>'
                + '<tr><th>EDM</th><td width="100">'
                + '<select id="edmStatus_' + row.id + '" class="form-control" >'
                + '<option value="UNSET"' + (edm === 'UN ' ? 'selected="selected"' : '') + '>Unset</option>'
                + '<option value="Signed" ' + (edm === 'SI' ? 'selected="selected"' : '') + '>Signed</option>'
                + '<option value="Refused" ' + (edm === 'RE' ? 'selected="selected"' : '') + '>Refused</option>'
                + '<option value="No Point" ' + (edm === 'NO' ? 'selected="selected"' : '') + '>No Point</option>'
                + '<option value="Sympathetic" ' + (edm === 'SY' ? 'selected="selected"' : '') + '>Sympathetic</option></select>'
                + '</td></tr>'
                + '<tr><th>EDM URL</th><td><a href="'+row.edmUrl+'" target="_blank">'+row.edmUrl +'</a></td></tr>'
                + '<tr><th>Party</th><td>'+row.party +'</td></tr>'
                + '<tr><th>Ministerial</th><td>'+row.ministerialStatus +'</td></tr>'
                + '<tr><th>Twitter</th><td>'+row.twitter +'</td></tr>'
                + '<tr><th>Constituency</th><td>'+row.constituency+'</td></tr>'
                + '<tr><th>Address</th><td>'+row.constituencyAddress+'</td></tr>'
                //+ '<tr><th>pCon</th><td>'+row.pCon+'</td></tr>'
                + '<tr><th>mpGroupNo</th><td>'+row.mpGroupNo+'</td></tr>'
                + '<tr><th>Majority</th><td>'+row.majority+'</td></tr>'
                + '<tr><th>Telephone</th><td>'+row.telNo +'</td></tr>'
                + '<tr><th>e-mail</th><td><a href="mailto:' + row.email +'">'+row.email +'</a></td></tr>'
                + '<tr><th>URL</th><td><a href="'+row.url+'" target="_blank">'+row.url +'</a></td></tr>'
                + '<tr></tr>'
                + '<tr></tr>'
                + '<tr><td colspan="2" align="center"><button type="button" class="btn btn-default update-mp-row-btn" data-row-id="' + row.id + '"><span class="fa fa-check fa-lg" aria-hidden="true"></span>&nbsp;Update</button></td></tr>'
                + '</table>';
        },
        "tags": function(cellvalue, options, row) {
            return '<div class="input-group"><input id="tags_' + row.id + '" type="text" class="form-control input-small" value="' + row.tags + '"></div>';
        },
        "emails": function(cellvalue, options, row) {
            return '<table>'
                + '<tr><th>Shared</th><td>'+row.sharedCampaignEmails+'</td></tr>'
                + '<tr><th>Private</th><td>'+row.privateCampaignEmails+'</td></tr>'
                + '<tr><th>Mail</th><td>'
                + '<a href="mailto:'+row.sharedCampaignEmails+'">Shared</a><br>'
                + '<a href="mailto:?bcc='+row.privateCampaignEmails+'">Private</a><br>'
                + '<a href="mailto:'+row.sharedCampaignEmails+'?bcc='+row.privateCampaignEmails+'">Both</a>'
                + '</td></tr>'
                + '</table>';
        },
    }
}
