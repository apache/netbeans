/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

"use strict";

angular.module("messagesApp", []).controller("MessagesCtrl", ["$scope", "$http", function MessagesCtrl($scope, $http) {
        $scope.messages = [];
        $scope.currentMessage = "";
        $scope.sendText = function () {
            $http.get("api/string/" + encodeURIComponent($scope.currentMessage)).then(function (result) {
                $scope.messages.push(result.data);
            }, function (e) {
                window.console.error(e);
            });
        };

    }]).directive("messages", [function () {
        return {
            restrict: "E",
            template: "<table><thead><tr><th>Original message</th><th>Response</th></tr></thead><tbody><tr data-ng-repeat='m in messages'><td>{{m.original}}</td><td>{{m.reverted}}</td></tr></tbody></table>"
        };
    }]).directive("mytitle", [function () {
        return {
            restrict: "E",
            scope: {
                msg: "="
            },
            template: "<h2>{{msg}}</h2>"
        };
    }]);