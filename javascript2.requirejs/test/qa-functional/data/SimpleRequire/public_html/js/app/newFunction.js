/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


define(["app/function"], function(funct) {

    window.console.log(funct());

    function Alien() {

        this.spaceShip = true;
        this.origin = "Alpha Centauri";

        this.birth = new Date(0);
        /**
         * Some weird stuff here
         */
        this.conquer = function() {
            return [];
        };

        this.getBirthDate = function() {
            return this.birth;
        };
        /**
         * 
         * @returns {Date}
         */
        this.getWannabeDate = function() {

        };

        this.getStuff = function() {
            return {
                a: 1,
                b: new Date(),
                c: function() {
                    return {x: 1};
                }
            };
        };

        this.anatomy = {
            legs: 11,
            eyes: function() {
            },
            heads: {
                leftOnes: 1,
                rightOnes: 2
            }
        };

    }

    var a = new Alien();

    return a;


});