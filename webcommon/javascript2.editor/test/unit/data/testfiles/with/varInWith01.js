var MyContext = {};
MyContext.okno = {
    truhlik: {
        typ: "kvetinac",
        kolik: 10
    },
    material:"sklo"
};


with (MyContext.okno) {
    console.log(truhlik);
    var myDataVarInWith = truhlik;
}
  
console.log(myDataVarInWith);
console.log(myDataVarInWith.kolik);
