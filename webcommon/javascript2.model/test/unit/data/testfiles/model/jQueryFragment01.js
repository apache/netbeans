var globalProperty = "Some global property";
(function() {
    
var functionVar = "155";
var jQuery = {};
jQuery.event = {
    name: "testovaci object",
    customEvent: {
        "getData": true,
        "setData": true,
        "changeData": true,
        "class" : {
            name : "fdsadfdsa"
        },
        martin: {
            address: "martinova adresa"
        },
        petr: 10,
        test : function () {
            formatter.println("");
            formatter.println("What is vissible inside function jQuery.event.customEvent.test()");
            formatter.addIndent(4);
            formatter.println("this.martin.address: " + this.martin.address);
            formatter.println("this.petr: " + this.petr);
            formatter.println("this.name: " + this.name);
            formatter.println("jQuery.event.name: " + jQuery.event.name);
            formatter.println("globalPropery: " + globalProperty);
            formatter.println("functionVar: " + functionVar);
            formatter.removeIndent(4);
            
        }
    }
}

formatter.println(jQuery.event.name); 
jQuery.event.customEvent.test();

}());

jQuery.ajaxStart().addClass();

