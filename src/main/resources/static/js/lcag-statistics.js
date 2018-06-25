var lcag = lcag || {};

lcag.Statistics = lcag.Statistics || {
	refresh: function() {
        $.ajax({
          method: "GET",
          url: lcag.Common.urlPrefix + "/statistics"
        }).done(function(result) {
            $("#totalContributions").html("Total £" + result.totalContributions);
            $("#totalContributors").html(result.totalContributors + " Contributors");
            $("#numberOfRegisteredMembers").html(result.numberOfRegisteredMembers + " Members");
            $("#numberOfGuests").html(result.numberOfGuests + " Guests");
            $("#totalUsers").html(result.totalUsers + " Total");

            $( "#emailAddressesModal" ).on('shown.bs.modal', function(e) {
                var sourceButton = $(e.relatedTarget).attr('id');
                var groupParameter = "";

                if (sourceButton == 'numberOfRegisteredMembers') {
                    groupParameter = "?group=Registered";
                } else if (sourceButton == 'numberOfGuests') {
                    groupParameter = "?group=LCAG Guests";
                }

                $.ajax({
                    method: "get",
                    url: "/member/emailAddresses" + groupParameter
                }).done(function(data) {
                    var title = "";

                    if (sourceButton == "numberOfRegisteredMembers") {
                       title = "Registered Member Email Addresses";
                    } else if (sourceButton == "numberOfGuests") {
                        title = "Guest Email Addresses";
                    } else if (sourceButton == "totalUsers") {
                        title = "All Email Addresses";
                    }

                    $("#emailAddressesTitle").text(title);
                    $("#emailAddressesTarget").text(JSON.stringify(data).replaceAll(",", "; ").slice(1, -1).replaceAll("\"", ""));
                });
            });

            $( "#emailAddressesModal" ).on('hidden.bs.modal', function(e) {
                $("#emailAddressesTarget").text("");
            });
        });
	}
}