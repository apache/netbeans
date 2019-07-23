var ironman = {
    name: "Hejlik",
    date: new Date()
}

with (ironman) {
    date.getMonth();
    !(function( ) {
        date.getYear();
        ironman.date.getTimezoneOffset();
    })();
}