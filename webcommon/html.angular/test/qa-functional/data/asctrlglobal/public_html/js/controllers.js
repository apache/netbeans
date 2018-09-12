'use strict';



function MyCtrl1() {
    this.test = 1;
    this.hello = function() {
    };
    this.ironman = new Runner();
    this.dog = {name: "Jerry"};
}

function MyCtrl2() {
    this.test2 = 1;
    this.hello2 = function() {
    };
    this.ironman2 = new Runner();
    this.dog2 = {name: "Jerry"};
    this.day = new Date();
}

function MyCtrl3() {
    this.test3 = 1;
    this.hello3 = function() {
    };
    this.ironman3 = new Runner();
    this.dog3 = {name: "Jerry"};
}


function Runner() {
    this.iteration = 1;
    this.date = new Date();
    this.exec = function() {
    };
    this.config = {
        url: "a",
        http: "1"
    };
}