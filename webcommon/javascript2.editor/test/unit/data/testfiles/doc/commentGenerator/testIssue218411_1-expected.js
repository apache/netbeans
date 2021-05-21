function SpecPoolCtrl($scope, $http) {
    $scope.specs = [];

    /**
     * 
     * @returns {undefined}^
     */
    $scope.filter = function() {
        alert($scope.version);
    };

    $scope.fetch();
}
