with(MySpace.cls1) {
    console.log(firstName);
    
    with(MySpace.cls2) {
        console.log(foo);
        firstName;
        secondName;
    }
} 