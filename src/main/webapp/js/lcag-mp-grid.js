var lcag = lcag || {};

lcag.MpGrid = lcag.MpGrid || {
    grid: {},
    initialise: function() {
        $("#mp-grid").jqGrid({
            colModel: [
                { name: "id", label: "ID", hidden: true },
                { name: "lastName", label: "Last Name", width: 150, template: "string", formatter: lcag.MpGrid.formatters.lastName },
                { name: "firstName", label: "First Name", width: 150, template: "string", formatter: lcag.MpGrid.formatters.firstName },
                //{ name: "edmStatus", label: "EDM Status", width: 100, template: "string"/*, formatter: lcag.MpGrid.formatters.edmStatus */},

                { name: "party", label: "Party", width: 100, template: "string", formatter: lcag.MpGrid.formatters.party},
                { name: "twitter", label: "Twitter", width: 150, template: "string", formatter: lcag.MpGrid.formatters.twitter},
                { name: "email", label: "e-mail", width: 250, template: "string", formatter: lcag.MpGrid.formatters.email},
                { name: "constituency", label: "Constituency", width: 200, template: "string", formatter: lcag.MpGrid.formatters.constituency },
                { name: "constituencyAddress", label: "Constituency Address", width: 400, template: "string", formatter: lcag.MpGrid.formatters.constituencyAddress},
                { name: "ministerialStatus", label: "Minister", width: 200, template: "string", formatter: lcag.MpGrid.formatters.ministerialStatus, stype: "select", searchoptions: { sopt: ["eq", "ne" ], value: ":Any;1:Yes;0:No" } },
                { name: "url", label: "Url", width: 400, template: "string", formatter: lcag.MpGrid.formatters.url },
                { name: "majority", label: "Majority", width: 90, template: "string", formatter: lcag.MpGrid.formatters.majority},
                { name: "telNo", label: "Tel", width: 120, template: "string", formatter: lcag.MpGrid.formatters.telNo },
                { name: "action", label: "", width: 90, formatter: lcag.MpGrid.formatters.action, search: false },

            ],
            datatype: function(postData) {
                    jQuery.ajax({
                        url: lcag.Common.urlPrefix + '/mp',
                        data: postData,
                        dataType: "json",
                        complete: function(response, status) {
                            if (status == "success") {
                                lcag.MpGrid.grid = $("#mp-grid");
                                console.log("jsondata:", response.responseJSON);
                                lcag.MpGrid.grid[0].addJSONData(response.responseJSON);
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
            cmTemplate: { autoResizable: true },
            //autoResizing: { compact: true },
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
                $("#mp-grid").find(".update-mp-row-btn").on("click", function(e) {
                    var rowContext = this;
                    $.ajax({
                          type: "POST",
                          url: lcag.Common.urlPrefix + "/mp/update",
                          data: (function() {
                              var id = $(rowContext).data("row-id");
                              lcag.Common.alertPleaseWait();
                              return {
                                "id": id,
                                "lastName": $("#lastName_" + id).val(),
                                "firstName": $("#firstName_" + id).val(),
                                "party": $("#party_" + id).val(),
                                "twitter": $("#twitter_" + id).val(),
                                "email": $("#email_" + id).val(),
                                "constituency": $("#constituency_" + id).val(),
                                "constituencyAddress": $("#constituencyAddress_" + id).val(),
                                "ministerialStatus": $("#ministerialStatus_" + id).val(),
                                "url": $("#url_" + id).val(),
                                "majority": $("#majority_" + id).val(),
                                "telNo": $("#telNo_" + id).val()
                            };
                          })(),
                          success: function(e) {
                            lcag.Common.alertSuccess();
                            lcag.MpGrid.grid.trigger("reloadGrid");
                          },
                          error: function(e) {
                            lcag.Common.alertError();
                            lcag.MpGrid.grid.trigger("reloadGrid");
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

        $("#mp-grid").keyup(function (e) {
            if (e.keyCode === 27) {
                $("#mp-grid")[0].clearToolbar();
                return false;
            }
        });

        $(window).bind('resize', function() {
            $("#mp-grid").width($(window).width() -10);
            $("#mp-grid").setGridWidth($(window).width() -10);
            $("#mp-grid").setGridHeight($(window).height()-200);
        }).trigger('resize');

        $("#mp-grid").jqGrid('setGroupHeaders', {
            useColSpanStyle: false,
            groupHeaders: [
                { startColumnName: 'MP', numberOfColumns: 2, titleText: 'MP' },
            ]
        });
    },

    formatters: {
        "lastName": function(cellvalue, options, row) {
            return '<div class="input-group"><input ' + (row.status == 3 ? 'disabled="disabled"' : '') + ' id="lastName_' + row.id + '" type="text" class="form-control" value="' + row.lastName + '"></div>';
        },
        "firstName": function(cellvalue, options, row) {
            return '<div class="input-group"><input ' + (row.status == 3 ? 'disabled="disabled"' : '') + ' id="firstName_' + row.id + '" type="text" class="form-control" value="' + row.firstName + '"></div>';
        },
        "party": function(cellvalue, options, row) {
            return '<div class="input-group"><input ' + (row.status == 3 ? 'disabled="disabled"' : '') + ' id="party_' + row.id + '" type="text" class="form-control" value="' + row.party + '"></div>';
        },
        "twitter": function(cellvalue, options, row) {
            return '<div class="input-group"><input ' + (row.status == 3 ? 'disabled="disabled"' : '') + ' id="twitter_' + row.id + '" type="text" class="form-control" value="' + row.twitter + '"></div>';
        },
        "email": function(cellvalue, options, row) {
            return '<div class="input-group"><input ' + (row.status == 3 ? 'disabled="disabled"' : '') + ' id="email_' + row.id + '" type="text" class="form-control" value="' + row.email + '"></div>';
        },
        "constituency": function(cellvalue, options, row) {
            return '<div class="input-group"><input ' + (row.status == 3 ? 'disabled="disabled"' : '') + ' id="constituency_' + row.id + '" type="text" class="form-control" value="' + row.constituency + '"></div>';
        },
        "constituencyAddress": function(cellvalue, options, row) {
            return '<div class="input-group"><input ' + (row.status == 3 ? 'disabled="disabled"' : '') + ' id="constituencyAddress_' + row.id + '" type="text" class="form-control" value="' + row.constituencyAddress + '"></div>';
        },
        "ministerialStatus": function(cellvalue, options, row) {
            var disabled = !(row.group == "LCAG Guests" || row.group == "Registered" || row.group == "Moderators");
            console.log("row.group", row.group);
            return '<select id="ministerialStatus_' + row.id + '" class="form-control" ' + (disabled ? 'disabled="disabled" ' : '') + '><option value="UNSET"' + (row.ministerialStatus == 'UNSET ' ? 'selected="selected"' : '') + '>Unset</option><option value="YES" ' + (row.ministerialStatus == 'YES' ? 'selected="selected"' : '') + '>Yes</option><option value="NO" ' + (row.ministerialStatus == 'NO' ? 'selected="selected"' : '') + '>No</option></select>';
        },

        "url": function(cellvalue, options, row) {
            return '<div class="input-group"><input ' + (row.status == 3 ? 'disabled="disabled"' : '') + ' id="url_' + row.id + '" type="text" class="form-control" value="' + row.url + '"></div>';
        },
        "majority": function(cellvalue, options, row) {
            return '<div class="input-group"><input ' + (row.status == 3 ? 'disabled="disabled"' : '') + ' id="majority_' + row.id + '" type="text" class="form-control" value="' + row.majority + '"></div>';
        },
        "telNo": function(cellvalue, options, row) {
            return '<div class="input-group"><input ' + (row.status == 3 ? 'disabled="disabled"' : '') + ' id="telNo_' + row.id + '" type="text" class="form-control" value="' + row.telNo + '"></div>';
        },

        "action": function(cellvalue, options, row) {
            if (row.status != 3) {
                return '<button type="button" class="btn btn-default update-mp-row-btn" data-row-id="' + row.id + '"><span class="fa fa-check fa-lg" aria-hidden="true"></span>&nbsp;Update</button>';
            }
            return "";
        }
    }
}
