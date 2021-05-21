(function() {
    $scope = FrameworksController.$scope;
    for (var gg in $scope.frameworks) {
        gg.language;
    }
    with ($scope) {
        for (var fw in frameworks) {
            fw.language;
        }
    }
});