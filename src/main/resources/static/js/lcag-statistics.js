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
        });
	}
}