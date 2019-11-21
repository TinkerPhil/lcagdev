var lcag = lcag || {};

lcag.ExtractPage = lcag.ExtractPage || {
    grid: {},
    initialise: function() {
        $("#extract-page").html('<table width="100%"><tr><td><table>'
                                + '  <tr>'
                                + '    <th>Type</th><td><select id="extractType"><option>MP</option><option>Member</option><option>Special</option></select></td>'
                                + '    <td class="extractSpecial"><input type="text" id="extractSpecial"></td>'
                                + '    <td>&nbsp;</td>'
                                + '  </tr>'
                                + '  <tr><td colspan="4"><table id="pjh"><tr><th>test</th></tr></table></td></tr>'
                                + '  <tr><td colspan="4">&nbsp;</td></tr>'
                                + '  <tr><th>Columns</th><td colspan="3"><input type="text" id="extractColumns" value="*"></td></tr>'
                                + '  <tr><td colspan="4">&nbsp;</td></tr>'
                                + '  <tr><th colspan="2">MP</th><th class="extractMember" colspan="2">Member</th></tr>'
                                + '  <tr>'
                                + '    <td>Name</td><td><input type="text" id="extractMpName"></td>'
                                + '    <td class="extractMember">Name</td><td class="extractMember"><input type="text" id="extractName"></td>'
                                + '  </tr>'
                                + '  <tr>'
                                + '    <td>Constituency</td><td><input type="text" id="extractMpConstituency"></td>'
                                + '    <td class="extractMember">e-mail</td><td class="extractMember"><input type="text" id="extractEmail"></td>'
                                + '  </tr>'
                                + '  <tr>'
                                + '    <td>Party</td><td><input type="text" id="extractMpParty"></td>'
                                + '    <td class="extractMember">Username</td><td class="extractMember"><input type="text" id="extractUsername"></td>'
                                + '  </tr>'
                                + '  <tr>'
                                + '    <td>Tags</td><td><input type="text" id="extractMpTags"></td>'
                                + '    <td class="extractMember">Tags</td><td class="extractMember"><input type="text" id="extractTags"></td>'
                                + '  </tr>'
                                + '  <tr><td colspan="4">&nbsp;</td></tr>'
                                + '  <tr>'
                                + '    <th>Extra Col 1</th><td>value</td>'
                                + '    <th>Extra Col 2</th><td>value</td>'
                                + '  </tr>'
                                + '  <tr>'
                                + '    <td><input type="text" id="extractField1"></td><td><input type="text" id="extractValue1"></td>'
                                + '    <td><input type="text" id="extractField2"></td><td><input type="text" id="extractValue2"></td>'
                                + '  </tr>'
                                + '  <tr><td colspan="2">&nbsp;</td></tr>'
                                + '  <tr><td align="center" colspan="2"><input type="button" value="Go" id="extractGo"></td></tr>'
                                + '</table>'
                                + '<br>'
                                + '<table width="100%">'
                                + '  <tr><th>Results</th><td><input type="button" value="Copy" id="extractCopy"></td></tr>'
                                + '  <tr><td colspan="2"><textarea style="-webkit-box-sizing: border-box; -moz-box-sizing: border-box; box-sizing: border-box; width: 100%; height: 150px;" id="extractResults">Hit Go to populate</textarea></td></tr>'
                                + '</table></td></tr></table>'
        );

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