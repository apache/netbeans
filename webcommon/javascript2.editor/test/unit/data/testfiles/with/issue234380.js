function Player234380() {
    this.den = new Date();
    with (this.den) {
        window.console.log(getDate());
    }
} 

new Player234380();
