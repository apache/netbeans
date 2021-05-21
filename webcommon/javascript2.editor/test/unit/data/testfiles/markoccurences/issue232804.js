(function () {
var testWith02 = {
    app : {
        name: "some name",
        description: "some description",
        getUsages : function() { return 0;}
        
    }
    
}
   
with (testWith02) {
    app.description = "new description";
    console.log(app.description);
}
})();
