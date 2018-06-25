var lcag = lcag || {};

String.prototype.replaceAll = function(search, replacement) {
    var target = this;
    return target.split(search).join(replacement);
};

lcag.Common = lcag.Common || {
    urlPrefix: "",
    alertSuccess: function() {
        toastr.success("Updated successfully");
    },
    alertError: function(message) {
        if (message != null && message != "") {
            toastr.error(message);
        } else {
            toastr.error("An error occurred");
        }
    }
}