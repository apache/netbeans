with(cls1) {
    console.log(firstName);
    
    with(cls2) {
        console.log(foo);
        firstName;
        secondName;
    }
} 