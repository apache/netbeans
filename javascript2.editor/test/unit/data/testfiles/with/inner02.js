var man02 = {
    firstName: "Pepa",
    secondName: "Vyskoc",
    address: {
        street: "Kolesa",
        city: "Kladruby"
    }
};


with (man02) {
    console.log(firstName);
    with (address) {
        console.log(street);
    }
}