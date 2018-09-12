var ondra = {
    name: "ondra",
    address: { // in ondra
        city: "chrudim",
        state: "CR"
    },
    house :{
      address: { // in house
          street: "Piseckeho"
      },
      number: 30
    }
};

 
with (ondra) {
    console.log(address);
    with (address) {
        console.log(state);
    }
    with (house) {
        with(ondra) { // second
            with(address) { // from ondra
                console.log(state);
            } 
        }
        with(address){ // from house
            console.log(street);
        }
    }
}