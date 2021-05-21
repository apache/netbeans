angular.module("tripPlanner.tripDay")
        .controller("tp.tripDay.TripDayFormCtrl", ["$scope", "tp.trip.TripModel", 
            function TripDayFormCtrl($scope, TripModel, TripHandler) { // lineA
                $scope.trip = trip ? trip : new TripModel("km");  // line B
            }])