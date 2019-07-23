function AdDetailCtrl($scope, $routeParams, $http) {
    $http.get($scope.backendUri + 'example/' + $routeParams.id).success(function(data, status) {
        $scope.ad = data;
    });
}

function AdDetailCtrl1($scope, $routeParams, $http) {
    $http.get($scope.backendUri + 'example/').success(function(data, status) {
        $scope.ad = data;
    });
}

function AdDetailCtrl2($scope, $routeParams, $http) {
    $http.get($scope.backendUri + 'example/' + "bar").success(function(data, status) {
        $scope.ad = data;
    });
}