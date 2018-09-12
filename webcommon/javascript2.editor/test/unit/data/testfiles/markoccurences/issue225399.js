function AdminRunsCtrl($http) {
    this.get = function($scope, version, onSuccess, onFail) {
        version = (version.length > 0) ? {"version": version} : {petr : 10};
        $http.get($scope.SYNERGY.server.buildURL("specifications", version)).success(onSuccess).error(onFail);
        version.version = 10;
    };   
} 
