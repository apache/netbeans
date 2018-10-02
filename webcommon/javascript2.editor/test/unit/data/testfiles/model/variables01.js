var MyApp = {};

function Address (town, state, number) {
    var city;
    var zip;
    
    this.street = "Ulice";

    city = town;
    country = state;
    zip = number;

    MyApp.country = state; 

    this.print = function() {
        if (zip == 16000) {
            zip = 15000;
        }
        this.id = 123;
        var result = city + " "  + this.street + " " + zip + " "  + country;
        return result;
    }
}


var address = new Address("Prague", "Czech Republic", 15000)
formatter.println(address.print());

telefon = "5648965";
formatter.println("Telefon: " + telefon);
formatter.println("Global Country: " + country);
formatter.println("MyApp.country: " + MyApp.country);
formatter.println("address id: " + address.id);
