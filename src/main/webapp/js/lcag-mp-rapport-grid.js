var lcag = lcag || {};

lcag.MpRapportGrid = lcag.MpRapportGrid || {
    grid: {},
    initialise: function() {
        $("#mp-rapport-grid").jqGrid({
            colModel: [
                { name: "id", label: "ID", hidden: true },
                { name: "mpName", label: "MP Name", width: 150, template: "string" },
                { name: "rapportVolunteer", label: "Volunteer", width: 150, template: "string", formatter: lcag.MpRapportGrid.formatters.rapportVolunteer },
                { name: "rapportContact", label: "Contact", width: 150, template: "string", formatter: lcag.MpRapportGrid.formatters.rapportContact },
                { name: "rapportNotes", label: "Notes", width: 300, height: 200, template: "string", formatter: lcag.MpRapportGrid.formatters.rapportNotes },
                { name: "rapportTags", label: "Tags", width: 180, height: 200, template: "string", formatter: lcag.MpRapportGrid.formatters.rapportTags },
                { name: "action", label: "", width: 90, formatter: lcag.MpRapportGrid.formatters.action, search: false },
            ],
            datatype: function(postData) {
                    jQuery.ajax({
                        url: lcag.Common.urlPrefix + '/mp',
                        data: postData,
                        dataType: "json",
                        complete: function(response, status) {
                            if (status == "success") {
                                lcag.MpRapportGrid.grid = $("#mp-rapport-grid");
                                console.log("jsondata:", response.responseJSON);
                                lcag.MpRapportGrid.grid[0].addJSONData(response.responseJSON);
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
                $("#mp-rapport-grid").find(".update-mp-row-btn").on("click", function(e) {
                    var rowContext = this;
                    $.ajax({
                          type: "POST",
                          url: lcag.Common.urlPrefix + "/mp/updateRapport",
                          data: (function() {
                              var id = $(rowContext).data("row-id");
                              lcag.Common.alertPleaseWait();
                              return {
                                  "id": id,
                                  "rapportVolunteer": $("#rapportVolunteer_" + id).val(),
                                  "rapportContact": $("#rapportContact_" + id).val(),
                                  "rapportNotes": $("#rapportNotes_" + id).val(),
                                  "rapportTags": $("#rapportTags_" + id).val()
                            };
                          })(),
                          success: function(e) {
                            lcag.Common.alertSuccess();
                            lcag.MpRapportGrid.grid.trigger("reloadGrid");
                            //lcag.VerificationGrid.grid.trigger("reloadGrid");
                          },
                          error: function(e) {
                            lcag.Common.alertError();
                            lcag.MpRapportGrid.grid.trigger("reloadGrid");
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
        $("#mp-rapport-grid").keyup(function (e) {
            if (e.keyCode === 27) {
                $("#mp-rapport-grid")[0].clearToolbar();
                return false;
            }
        });
        $(window).bind('resize', function() {
            $("#mp-rapport-grid").width($(window).width() -10);
            $("#mp-rapport-grid").setGridWidth($(window).width() -10);
            $("#mp-rapport-grid").setGridHeight($(window).height()-200);
        }).trigger('resize');

    },
	formatters: {

        "rapportVolunteer": function(cellvalue, options, row) {
            return '<div class="input-group"><input ' + (row.status == 3 ? 'disabled="disabled"' : '') + ' id="rapportVolunteer_' + row.id + '" type="text" class="form-control" value="' + row.rapportVolunteer + '"></div>';
        },
        "rapportContact": function(cellvalue, options, row) {
            return '<div class="input-group"><input ' + (row.status == 3 ? 'disabled="disabled"' : '') + ' id="rapportContact_' + row.id + '" type="text" class="form-control" value="' + row.rapportContact + '"></div>';
        },


        "rapportNotes": function(cellvalue, options, row) {
            return '<div class="input-group"><textarea id="rapportNotes_' + row.id + '" type="textarea" rows="12" cols="200" class="form-control">' + row.rapportNotes + '</textarea></div>';
        },

        "rapportTags": function(cellvalue, options, row) {
            return '<div class="input-group"><textarea id="rapportTags_' + row.id + '" type="textarea" rows="12" cols="180" class="form-control">' + row.rapportTags + '</textarea></div>';
        },
        "action": function(cellvalue, options, row) {
            if (row.status != 3) {
                return '<button type="button" class="btn btn-default update-mp-row-btn" data-row-id="' + row.id + '"><span class="fa fa-check fa-lg" aria-hidden="true"></span>&nbsp;Update</button>';
            }
            return "";
        }
    }
}
