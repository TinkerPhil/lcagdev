var lcag = lcag || {};

/*
    private Map<String, String> FIELD_TO_COLUMN_TRANSLATIONS = new HashMap<String, String>() {{
        put("id", "bt.id");
        put("userId", "bt.user_id");
        put("date", "bt.date");
        put("description", "bt.description");
        put("amount", "bt.amount");
        put("runningBalance", "bt.running_balance");
        put("counterParty", "bt.counter_party");
        put("reference", "bt.reference");
    }};
*/

lcag.PaymentsGrid = lcag.PaymentsGrid || {
    grid: {},
    initialise: function() {
        $("#payments-grid").jqGrid({
            colModel: [
                { name: "id", label: "ID", hidden: true },
                { name: "userId", label: "Member ID", width: 60, template: "string" },
                { name: "date", label: "Transaction Date", width: 100, template: "string" },
                { name: "description", label: "Description", width: 200, template: "string" },
                { name: "amount", label: "Amount", width: 60, template: "string" },
                { name: "runningBalance", label: "Running Balance", width: 60, template: "string" },
                { name: "counterParty", label: "Counter Party", width: 90, template: "string" },
                { name: "reference", label: "Reference", width: 90, template: "string" }
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
            width: "2000px",
            altRows: true,
            viewrecords: true,
            gridComplete: function() {
                lcag.Statistics.refresh();
            }
        }).jqGrid("filterToolbar", {
            searchOnEnter: false
        });
    },
	formatters: {
    }
}


