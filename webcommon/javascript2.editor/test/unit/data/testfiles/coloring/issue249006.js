factory(    function () {
    var LoginService = {};

        AuthHttp.getSession().then(function (data) {
            LoginService.authProvider = "google";
            LoginService.test = "google";
        });

        return LoginService;

    });
      