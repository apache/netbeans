$scope.init = function() {
    var session = window.SYNERGY.util.getCookie("user");
    if (typeof session !== "undefined") {
        session = JSON.parse(session);
        try {
            var diff = new Date().getTime() - parseInt(session.created);
            if (diff < window.SYNERGY.defaultCookiesExpiration * 24 * 60 * 60 * 1000) {
                window.SYNERGY.session.hideLoginForm();
                window.SYNERGY.session.showUserMenu();
            } else {
                window.SYNERGY.util.deleteCookie("user");
            }
        } catch (e) {
        }
    }
}