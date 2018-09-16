'use strict';

/* Controllers */

angular.module('myApp.controllers', []).
        controller('MyCtrl1', [function () {
                this.test = 1;
                this.hello = function () {
                };
                this.ironman = new Runner();
                this.dog = {name: "Jerry"};
            }])
        .controller('MyCtrl2', function () {
            this.test2 = 1;
            this.hello2 = function () {
            };
            this.ironman2 = new Runner();
            this.dog2 = {name: "Jerry"};
            this.day = new Date();
        })
        .controller('MyCtrl3', [function () {
                this.test3 = 1;
                this.hello3 = function () {
                };
                this.ironman3 = new Runner();
                this.dog3 = {name: "Jerry"};
            }]);


function Runner() {
    this.iteration = 1;
    this.date = new Date();
    this.exec = function () {
    };
    this.config = {
        url: "a",
        http: "1"
    };
}