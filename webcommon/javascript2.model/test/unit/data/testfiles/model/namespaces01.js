function addProperties(arg) {
    if (arg == 10) {
        MyContext.User.session = 80;
        MyContext.test = "yes";
    }
}


var MyContext = {};

MyContext.id = 10;
MyContext.User = {};
MyContext.User.firstName = 'David';
MyContext.User.lastName = 'Strupl';
MyContext.User.Address = {};
MyContext.User.Address.street = 'Dejvicka';
MyContext.User.Address.town = 'Prague';

formatter.println("Context: " + MyContext)
formatter.println("Context id: " + MyContext.id)
formatter.println("User: " + MyContext.User);
formatter.println("First Name: " + MyContext.User.firstName);
formatter.println("Second Name: " + MyContext.User.lastName);

formatter.println("Street: " + MyContext.User.Address.street);
formatter.println("Town: " + MyContext.User.Address.town);

Ns1.Ns2.Ns3.fix = 'true';
formatter.println('fix: ' + Ns1.Ns2.Ns3.fix);

