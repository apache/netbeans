window.store = {
    wines: {},
    populate: function() {
    },
    address: {
        street: "Ulice",
        city: "Praha"
    }
};

store.populate();  
window.store.populate();
console.log(store.address.street);
console.log(window.store.address.street);
