var man01 = {
    firstName : "Pepa",
    secondName: "Vyskoc"
};

var address01 = {
    street: "Kolesa",
    city: "Kladruby"
};

with (man01) {
    console.log(firstName);
    with(address01) {
        console.log(street);
    }
}