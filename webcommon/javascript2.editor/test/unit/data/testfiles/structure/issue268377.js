class Editor {
    constructor () {
        console.log("Editor");
        this.field = 1;
    }
}

class Base {
    constructor () {
        console.log("Base");
        this.field = 1;
    }
}  

class Child extends Base {
    constructor() {
        super();
        this.field = 3;
    }
}