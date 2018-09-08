"use strict";

angular.module("tripPlanner.auth", []).factory("tp.auth.LoginService", ["tp.auth.Profile", function (Profile) {

        (function initAuth() {
            hello.init({
                google: '1005520707529-tpb3ajv7plllarh40ber2qi52t8bca4q.apps.googleusercontent.com'
            }, {redirect_uri: 'http://localhost:8383/TripPlanner/redirect.html', scope: 'email'});
        })();

//      store session tokens
        var tokens = {
            "google": null
        };

        var LoginService = {
            login: function (serviceName) {
                hello(serviceName).login().then(function () {
                    LoginService.authProvider = serviceName;
                    tokens[serviceName] = hello(serviceName).getAuthResponse().access_token;
                });
            },
            authProvider: null
        };

        hello.on('auth.login', function (r) {
            LoginService.authProvider = r.network;
            Profile.setAuthProvider(r.network);

            hello(LoginService.authProvider).api('me').then(function (profile) {
                console.log(Profile.getDisplayName(profile));
            });
        });

        return LoginService;

    }]).factory("tp.auth.Profile", [function () {

        var googleFormatter = {
            getDisplayName: function (me) {
                return me.displayName;
            }
        };
        
        var formatters ={
            "google" : googleFormatter
        };
        
        var Profile = {
            authProvider : null,
            setAuthProvider: function (providerName) {
                this.authProvider = providerName;
            },
            getDisplayName: function (me) {
                return formatters[this.authProvider].getDisplayName(me);
            }
        };

        return Profile;

    }]);


"use strict";

/* Controllers */

angular.module("tripPlanner.controllers",
        ["tripPlanner.map", "tripPlanner.trip", "tripPlanner.user", "tripPlanner.core", "tripPlanner.logger", "tripPlanner.utils", "tripPlanner.auth"])
        .controller("TripPlannerCtrl", ["$scope", "tp.logger", "tp.core.Session", "tp.user.UserModel", "tp.auth.LoginService",
            function TripPlannerCtrl($scope, logger, sessionFct, User, LoginService) {

                sessionFct.setUser(new User("lada", "lada@lada", 1), 1);
                $scope.currentUser = sessionFct.getUser().username;

                $scope.debug = false;
                $scope.preferredMapProvider = "google";

                $scope.$on("busyMode", function (event, args) {
                    if (args) {
                        window.document.body.style.cursor = "wait";
                    } else {
                        window.document.body.style.cursor = "default";
                    }
                });

                $scope.handleGenericError = function (msg) {
                    logger.log("Action failed", msg, "INFO", "alert-error");
                };

                $scope.ngRefresh = function () {
                    if (!$scope.$$phase) {
                        $scope.$digest();
                    }
                };

                $scope.login = function (serviceName) {
                    LoginService.login(serviceName);
                };

            }])
        .controller("HomeCtrl", ["$scope", function HomeCtrl($scope) {
                $scope.test = 1;
            }])
        .controller("NewTripCtrl", ["$scope", "tp.trip.TripModel", "tp.trip.TripHandler", "$location", "tp.trip.TripCache",
            /**
             * 
             * @param {type} $scope
             * @param {mapProvider} mapProvider
             * @param {type} TripModel
             * @param {type} TripDay
             * @returns {undefined}
             */
            function NewTripCtrl($scope, TripModel, TripHandler, $location, TripCache) {
                $scope.trip = null;
                $scope.openedDatePicker = false;

                function init() {
                    $scope.trip = new TripModel("km");
                }

                $scope.tripIsValid = function () {
                    return $scope.debug || $scope.trip.isValid();
                };

                $scope.createTrip = function () {
                    new TripHandler().createTrip($scope.trip).then(function (newTrip) {
                        TripCache.set(newTrip);
                        $location.path("trip/" + newTrip.id);
                    }, function (msg) {
                        $scope.handleGenericError(msg);
                    }).then(function () {
                        $scope.$apply();
                    });
                };

                $scope.openDatePicker = function ($event) {
                    $event.preventDefault();
                    $event.stopPropagation();
                    $scope.openedDatePicker = true;
                };

                init();

            }])
        .controller("ViewTripCtrl", ["$scope", "tp.trip.TripCache", "tp.trip.TripHttp", "$routeParams", "tp.TimeDateConvertor",
            function TripCtrl($scope, TripCache, TripHttp, $routeParams, TimeDateConvertor) {
                $scope.trip = null;
                $scope.tripId = $routeParams.id || -1;

                function init() {
                    if (TripCache.get()) {
                        $scope.trip = TripCache.get();
                        $scope.trip.date = TimeDateConvertor.UTCToDate($scope.trip.date);
                    } else {
                        TripHttp.get($scope.tripId).then(function (data) {
                            $scope.trip = data;
                            $scope.trip.date = TimeDateConvertor.UTCToDate($scope.trip.date);
                        }).then(function () {
                            $scope.$apply();
                        });
                    }
                }

                init();

            }])
        .controller("NewTripDayCtrl", ["$scope", "tp.map.MapProvider", "tp.trip.TripModel", "tp.tripDay.TripDayModel", "tp.trip.TripHandler", "tp.core.Session",
            /**
             * 
             * @param {type} $scope
             * @param {mapProvider} mapProvider
             * @param {type} Trip
             * @param {type} TripDay
             * @returns {undefined}
             */
            function NewTripDayCtrl($scope, mapProvider, Trip, TripDay, TripHandler, se) {

                $scope.currentStep = "step1";
                /** @type MapProvider */
                var map = mapProvider.getMapProvider($scope.preferredMapProvider);
                /**
                 * 
                 * @type TripModel
                 */
                $scope.trip = null;
                $scope.directions = [];
                $scope.selectedDirection = 0;
                var rawDirectionResult = null;
                $scope.openedDatePicker = false;


                function init() {
                    var _t = new Trip("km");
                    _t.tripDays.push(new TripDay({"highways": false, "tolls": false}));
                    $scope.trip = _t;
                }

                function placeSelected(place, identifier) {
                    $scope.trip.tripDays[0].setPlace(place, identifier);
                    $scope.ngRefresh();
                }

                $scope.back = function () {
                    $scope.currentStep = "step1";
                };

                $scope.tripIsValid = function () {
                    return $scope.debug || $scope.trip.isValid();
                };

                $scope.createTrip = function () {
                    $scope.directions = [];
                    if ($scope.debug) {
                        $scope.trip.tripDays[0].setPlace(map.mocks.places[0], "from");
                        $scope.trip.tripDays[0].setPlace(map.mocks.places[1], "to");
                    }

                    $scope.currentStep = "step2";

                    if ($scope.debug) {
                        map.getDebugDirections(displayDirections);
                    } else {
                        map.getSimpleDirections($scope.trip.tripDays[0].from, $scope.trip.tripDays[0].to, $scope.trip.units, $scope.trip.tripDays[0].avoids, displayDirections);
                    }

                };
                function displayDirections(err, data) {
                    if (err) {
                        window.console.error(err);
                        return;
                    }

                    $scope.directions = data.routes;
                    rawDirectionResult = data;
                    $scope.selectDirection(0);
                    $scope.ngRefresh();
                }

                $scope.selectDirection = function (index) {
                    $scope.selectedDirection = index;
                    map.displayRoute(rawDirectionResult, "tripPreview", index, "directions-panel");
                    $scope.ngRefresh();
                };

                $scope.openDatePicker = function ($event) {
                    $event.preventDefault();
                    $event.stopPropagation();
                    $scope.openedDatePicker = true;
                };

                $scope.create = function () {
                    $scope.trip.tripDays[0].rawRouteData = map.getFinalRouteObject(rawDirectionResult, $scope.selectedDirection);
                    $scope.trip.tripDays[0].distance = map.getDistance($scope.trip.tripDays[0].rawRouteData, 0);
                    $scope.trip.tripDays[0].duration = map.getDuration($scope.trip.tripDays[0].rawRouteData, 0);
                    new TripHandler().createTrip($scope.trip).then(function (e) {
                        window.console.log("trip created " + e);
                    }, function (msg) {
                        $scope.handleGenericError(msg);
                    });
                };

                init();

            }]);


