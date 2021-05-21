function Test($scope) {
    function getLabelId(stringLabel) {
        for (var i = 0, max = $scope.labels.length; i < max; i++) {
            if ($scope.labels[i].label === stringLabel) {
                return $scope.labels[i].id;
            }
        }
        return -1;
    }
} 

function Test2($scope) {
    var caseId = 1;
    for (var i = 0, max = $scope.assignment.specificationData.testSuites.length; i < max; i += 1) {
        for (var j = 0, max2 = $scope.assignment.specificationData.testSuites[i].testCases.length; j < max2; j += 1) {
            if (caseId === parseInt($scope.assignment.specificationData.testSuites[i].testCases[j].id)) {
                $scope.currentCase = {
                    "title": $scope.assignment.specificationData.testSuites[i].testCases[j].title
                };
                return;
            }
        }
    }
}