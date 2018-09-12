/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

define(["app/newFunction"], function(alien) {

    var counter = 0;

    function getRandomDate() {
        counter++;
        return new Date();
    }

    var Logger = {
        log: function() {
            return {a: 1, b: [], c: new Date()};
        }
    };


    return {
        getSomeDate: function() {
            return getRandomDate();
        },
        dummy : alien.conquer,
        dummy2 : alien.anatomy,
        logIt: Logger.log()
    };

});
