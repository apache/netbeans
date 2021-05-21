'use strict';


// Declare app level module which depends on filters, and services
angular.module('myApp', [
  'ngRoute',
  'myApp.filters',
  'myApp.services',
  'myApp.directives',
  'myApp.controllers'
]).
config(['$routeProvider', function($routeProvider) {
  $routeProvider.when('/view1', {templateUrl: 'partials/partial1.html', controller: MyCtrl1, controllerAs: "ct1"});
  $routeProvider.when('/view2', {templateUrl: 'partials/partial2.html', controller: MyCtrl2, controllerAs: "ct2"});
  $routeProvider.when('/view3', {templateUrl: 'partials/partial3.html', controller: MyCtrl3, controllerAs: "ct3"});
  $routeProvider.otherwise({redirectTo: '/view1'});
}]);
