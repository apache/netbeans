function Test234375() {
    this.den = new Date();
    with (this.den) {
        g // cc doesn't offer Date functions
        console.log(getDate());
    }
    
    console.log(this.den.getDate());
    
    var car = new Car234375();
    with(car){
        r // this works
        console.log(run());
    }
    
    var arr = [];
    with(arr){
        push(1);
        p // cc doesn't offer e.g. pull() or push()
    }
    
    window.console.log(arr);
}

function Car234375(){
    this.topSpeed = 200;
    this.run = function (){console.log("vrr");};
    this.rypadlo = false;
}


new Test234375();