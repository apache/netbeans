"use strict";     
var onreadystatechange;

var a = {
    test: function () {
        if (true || false) {
            onreadystatechange = function() {
                if (true) {
                    onreadystatechange = function () {console.log("true");};
                } else {
                    onreadystatechange = function () {console.log("false");};
                }
            };
        }     

    } 
} 

a.test();
onreadystatechange();
onreadystatechange();