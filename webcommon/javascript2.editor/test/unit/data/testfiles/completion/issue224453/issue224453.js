/**
 * 
 * @param {Number} $scope
 * @returns {undefined}
 */
function SpecificationCtrl($scope) {

    $scope.viewMode = function(view) {
        $location.path("specification/" +
                $scope.specification.id + "/v/" + view);
    };
    $scope.v
}

function  Issue224453 ( ){
    this.type = "type";
};

Issue224453.prototype.getType = function () {
    return "this.type";
};

var issue224453A = new Issue224453();

issue224453A.getType();