!(function() {
    function Test($scope) {
        function loadLabels() {
            labelsFct.getAll($scope, function(data) {
                $scope.labels = data;
            }, function(data) {
            });
        }

    }
})();