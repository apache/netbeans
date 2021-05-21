/**
 * @constructor
 * @returns {Woman}
 */
function Woman (fName, sName) {
    var firstName = fName;
    var secondName = sName;
    
    this.getFirstName = function () {
        return firstName;
    }
    
    this.getSecondName = function () {
        return secondName;
    }
    
    this.address = {
        street: "unknown street",
        city: "some town",
        zip: "15000",
        print : function () {
            return "Address: " + this.street + ", " + this.city + ", " + this.zip; 
        }
    };
    
    
}

var martin = new Woman("Martin", "Chloupek");

with(martin) {
    console.log(getFirstName());
    address.city = "Pribram";
    with (address) {
        console.log(city);
        city = "Podlesi";
    }
}

console.log(martin.address.city);
