var lcag = lcag || {};

lcag.PaymentsGrid = lcag.PaymentsGrid || {
    grid: {},
    initialise: function() {
        $("#payments-grid").jqGrid({
            colModel: [
                { name: "id", label: "ID", hidden: true },
                { name: "userId", label: "Member", width: 150, template: "string", formatter: lcag.PaymentsGrid.formatters.userId },
                { name: "date", label: "Transaction Date", width: 40, template: "string", formatter: lcag.PaymentsGrid.formatters.date, search: false },
                { name: "transactionIndexOnDay", label: "Index", width: 40, template: "string" },
                { name: "description", label: "Description", width: 180, template: "string" },
                { name: "amount", label: "Amount", width: 40, template: "string", formatter: lcag.PaymentsGrid.formatters.amount },
                { name: "runningBalance", label: "Running Balance", width: 40, template: "string", formatter: lcag.PaymentsGrid.formatters.runningBalance  },
                { name: "counterParty", label: "Counter Party", width: 70, template: "string" },
                { name: "reference", label: "Reference", width: 50, template: "string" },
                { name: "paymentSource", label: "Payment Source", width: 50, template: "string" }
            ],
            datatype: function(postData) {
                    jQuery.ajax({
                        url: lcag.Common.urlPrefix + '/payments',
                        data: postData,
                        dataType: "json",
                        complete: function(response, status) {
                            if (status == "success") {
                                lcag.PaymentsGrid.grid = $("#payments-grid")[0];
                                lcag.PaymentsGrid.grid.addJSONData(response.responseJSON);
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
            width: "1800px",
            altRows: true,
            viewrecords: true,
            gridComplete: function() {
                lcag.Statistics.refresh();
                $('.userIdSelect').select2({
                  width: "resolve",
                  ajax: {
                    url: '/member',
                    data: function (params) {
                      return { username: params.term }
                    },
                    dataType: 'json',
                    processResults: function (data) {
                        var arr = []
                        $.each(data.rows, function (index, row) {
                            arr.push({
                                id: row.id,
                                text: row.username + " (" + row.emailAddress + ")"
                            })
                        })
                        return {
                            results: arr
                        };
                    }
                  }
                }).on('select2:select', function (e) {
                    var memberId = $(this).val();
                    var paymentId = $(this).attr("id").split("_")[1];
                    $.ajax({
                      method: "POST",
                      url: lcag.Common.urlPrefix + "/assignToMember",
                      data: { "memberId": memberId, "paymentId": paymentId }
                    }).done(function(result) {
                        lcag.Common.alertSuccess();
                        lcag.PaymentsGrid.grid.trigger("reloadGrid");
                    });
                });
            }
        }).jqGrid("filterToolbar", {
            searchOnEnter: false
        });
    },
	formatters: {
        "amount": function(cellvalue, options, row) {
            return '<div class="input-group"><div class="input-group"><div class="input-group-addon">£</div><input disabled="disabled" id="amount_' + row.id + '" type="text" value="' + row.amount + '" class="form-control"></div></div>';
        },
        "runningBalance": function(cellvalue, options, row) {
            return '<div class="input-group"><div class="input-group"><div class="input-group-addon">£</div><input disabled="disabled" id="amount_' + row.id + '" type="text" value="' + row.runningBalance + '" class="form-control"></div></div>';
        },
        "date": function(cellvalue, options, row) {
            return moment(row.date).format("DD/MM/YYYY");
        },
        "userId": function(cellvalue, options, row) {
            if (row.userId == null) {
                return '<select style="width: 100%;" id="userId_' + row.id + '" class="userIdSelect"></select>';
            }
            return '<select style="width: 100%;" id="userId_' + row.id + '" class="userIdSelect"><option selected value="' + row.userId + '">' + row.username + ' (' + row.emailAddress + ')</option></select>';
        }
    }
}


