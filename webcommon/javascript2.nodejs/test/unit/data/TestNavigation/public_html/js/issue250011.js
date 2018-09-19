var http = {
    BadRequest: function (response, msg) {
        response.writeHead(400, {
            "Content-Type": "text/plain"
        });
        response.write(msg);
        response.end();
    },
   
    respond : function(errorType, msg25, msg26, message){
        var ee= 1;
        msg26;
        // cc here
    }
};



module.exports = http;

