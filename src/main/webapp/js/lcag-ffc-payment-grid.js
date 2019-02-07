var lcag = lcag || {};

lcag.FfcPaymentGrid = lcag.FfcPaymentGrid || {
    grid: {},
    initialise: function() {
        $("#ffc-payment-grid").jqGrid({
            colModel: [
                { name: "id", label: "ID", hidden: true },
                { name: "username", label: "Username", width: 150, template: "string" },
                { name: "firstName", label: "First Name", width: 150, template: "string" },
                { name: "lastName", label: "Last Name", width: 150, template: "string" },
                { name: "email", label: "email", width: 200, template: "string" },
                { name: "hasProvidedSignature", label: "Signed", width: 150, formatter: lcag.FfcPaymentGrid.formatters.hasProvidedSignature, stype: "select", searchoptions: { sopt: ["eq", "ne"], value: ":Any;1:Yes;0:No" } },
                { name: "esig", label: "e-sig", width: 230, formatter: lcag.FfcPaymentGrid.formatters.esig },
                { name: "paymentType", label: "Type", width: 160, template: "string" },
                { name: "paymentMethod", label: "Method", width: 50, template: "string" },
                { name: "status", label: "Status", width: 100, template: "string" },
                { name: "reference", label: "Reference", width: 100, template: "string" },
                { name: "errorDescription", label: "Error", width: 200, template: "string" },
            ],
            datatype: function(postData) {
                    jQuery.ajax({
                        url: lcag.Common.urlPrefix + '/ffcpayment',
                        data: postData,
                        dataType: "json",
                        complete: function(response, status) {
                            if (status == "success") {
                                lcag.FfcPaymentGrid.grid = $("#ffc-payment-grid");
                                console.log("jsondata:", response.responseJSON);
                                lcag.FfcPaymentGrid.grid[0].addJSONData(response.responseJSON);
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
        }).jqGrid("filterToolbar", {
            searchOnEnter: false
        });
        $("#ffc-payment-grid").keyup(function (e) {
            if (e.keyCode === 27) {
                $("#ffc-payments-grid")[0].clearToolbar();
                return false;
            }
        });
        $(window).bind('resize', function() {
            $("#ffc-payment-grid").width($(window).width() -10);
            $("#ffc-payment-grid").setGridWidth($(window).width() -10);
            $("#ffc-payment-grid").setGridHeight($(window).height()-200);
        }).trigger('resize');

    },
	formatters: {
        "hasProvidedSignature": function(cellvalue, options, row) {
            return '<input disabled="disabled" id="hasProvidedSignature_' + row.id + '" type="checkbox" ' + (row.hasProvidedSignature ? ' checked="checked"' : '') + '/>';
        },
        "esig": function(cellvalue, options, row) {
            return '<a href="https://litigation.hmrcloancharge.info/signContributionAgreement?guid=' + row.esig + '" target="_blank">' + row.esig + '</a>';
        },
    }


}
