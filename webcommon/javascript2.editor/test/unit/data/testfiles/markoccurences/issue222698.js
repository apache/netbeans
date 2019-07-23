var json = "data=" + angular.toJson($scope.servos);
$http({
    method: 'POST',
    url: 'setServo',
    data: json,
    headers: {'Content-Type': 'multipart/form-data, boundary=' + json.length + '; charset=UTF-8'}
});