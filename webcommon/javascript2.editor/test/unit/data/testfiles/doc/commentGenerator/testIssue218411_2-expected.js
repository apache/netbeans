function SpecPoolCtrl($scope, $http) {
    $scope.specs = [];

    /**
     * 
     * @param {type} param1
     * @param {type} param2
     * @returns {undefined}^
     */
    $scope.filter = function(param1, param2) {
        alert($scope.version);
    };

    $scope.fetch();
}
