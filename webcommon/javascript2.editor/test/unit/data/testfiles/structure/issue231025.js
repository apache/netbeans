function hello($scope) {
    $scope.cancel = function() {
        window.history.back();
    };
    window.console.log("A");
}

function greetings() {
    if (true) {
        return;
    }
    window.console.log("B");
}