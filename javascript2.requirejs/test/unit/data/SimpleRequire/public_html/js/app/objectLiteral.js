/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


define({
    nickname: "Pepa",
    getSomeDate: function() {
        return new Date();
    },
    getLiteral: function() {
        return {
            "propX": 1,
            "propY": {
                "a": new Date(),
                "ab": 1
            }
        };
    },
    "test": {
        ale: {
            "aa": 1,
            "ac": 1
        },
        ale2: new Date()
    },
    name: "ZDepa",
    yell: function() {
        window.console.log("I am " + this.nickname);
    }
});
