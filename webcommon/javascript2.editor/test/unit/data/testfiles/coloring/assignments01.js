var prom = "Simple text";

var Address2 = {
    street: 'K lesiku',
    city : 'Prague',
    zip : '16000',
    toString : function () {
        var text = this.street + ", " + this.city + " " + this.toString();
    }
}

var test = prom;
formatter.println(test.length);

test = Address2;
formatter.println(test.street);

var test2 = 10;  
formatter.println(test2.street);

function change() {
    test2 = prom;
    return test2;
}       
formatter.println(test2.length);


