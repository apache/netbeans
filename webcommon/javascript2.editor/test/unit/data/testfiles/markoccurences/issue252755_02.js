$("#foo").on("click", ".foo", function() {
    var coord;
    coord[0] = 1; //OK
    $("#foo2").appendTo("#foo3").show(400, function() {
        //Create map target div when completely open (or it doesn't work)
        coord[0] = 1; //marked as not declared!
        coord = 1; //OK
    });
});



