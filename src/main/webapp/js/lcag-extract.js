var lcag = lcag || {};

lcag.ExtractPage = lcag.ExtractPage || {
    grid: {},
    initialise: function() {
        $("*.extractMember").hide();
        $("*.extractSpecial").hide();
        $("#extractCopy").click(function() {
            $("#extractResults").select();

            try {
                var successful = document.execCommand( 'copy' );
                var msg = successful ? 'successful' : 'unsuccessful';
                console.log('Copying text command was ' + msg);
             } catch (err) {
                console.log('Oops, unable to copy');
             }
        });
        $("#extractGo").click(function() {
            $.ajax({
                  //type: "POST",
                  url: lcag.Common.urlPrefix + "/extract",
                  dataType: "html",
                  data: (function() {
                      lcag.Common.alertPleaseWait();
                      //alert( $("#extractMpName").val());
                      return {
                            "extractType": $("#extractType").val(),
                            "extractSpecial": $("#extractSpecial").val(),
                            "extractColumns": $("#extractColumns").val(),
                            "extractMpName": $("#extractMpName").val(),
                            "extractMpConstituency": $("#extractMpConstituency").val(),
                            "extractMpParty": $("#extractMpParty").val(),
                            "extractMpTags": $("#extractMpTags").val(),
                            "extractAdminSig": $("#extractAdminSig").val(),
                            "extractName": $("#extractName").val(),
                            "extractEmail": $("#extractEmail").val(),
                            "extractUsername": $("#extractUsername").val(),
                            "extractTags": $("#extractTags").val(),
                            "extractField1": $("#extractField1").val(),
                            "extractValue1": $("#extractValue1").val(),
                            "extractField2": $("#extractField2").val(),
                            "extractValue2": $("#extractValue2").val(),
                    };
                  })(),
                  success: function(response) {
                    $("#extractResults").html(response);
                    lcag.Common.alertSuccess();
                  },
                  error: function(e) {
                    lcag.Common.alertError();
                  }
                });
        });
        $("#extractType").change(function() {
            if( $("#extractType").val() === "MP" ) {
                $("*.extractMember").hide();
                $("*.extractSpecial").hide();
            }
            if( $("#extractType").val() === "Member" ) {
                $("*.extractMember").show();
                $("*.extractSpecial").hide();
            }
            if( $("#extractType").val() === "Special" ) {
                $("*.extractMember").show();
                $("*.extractSpecial").show();
            }
         })
    },
}