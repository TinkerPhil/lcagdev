var lcag = lcag || {};

String.prototype.replaceAll = function(search, replacement) {
    var target = this;
    return target.split(search).join(replacement);
};

lcag.Common = lcag.Common || {
    urlPrefix: "",
    alertPleaseWait: function() {
         toastr.info("Please wait...", {
            "timeOut": "0",
            "extendedTImeout": "0",
            "maxOpened": "1"
        });
    },
    hidePleaseWait: function() {
        $("div.toast.toast-info").remove();
    },
    alertSuccess: function() {
        toastr.success("Updated successfully", {
            "maxOpened": "1"
        });
    },
    alertError: function(message) {
        if (message != null && message != "") {
            toastr.error(message);
        } else {
            toastr.error("An error occurred");
        }
    }
}