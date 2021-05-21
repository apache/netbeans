function SpecPoolCtrl($scope, $rootScope, $http, $location, $routeParams, text)
{
    $scope.specs = [];
    $scope.version = $routeParams.id || '';
    $scope.loads = 0;
    $scope.orderProp = 'title';
    $scope.currentAction = "";
    $scope.currentActionId = -1;
    $scope.rights = 0;

    $scope.     // here

}