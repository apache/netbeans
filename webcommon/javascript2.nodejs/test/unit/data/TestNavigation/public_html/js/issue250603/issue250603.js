"use strict";

var http = {
    BadRequest: function (response, msg) {
        response.writeHead(400, {
            "Content-Type": "text/plain"
        });
        response.write(msg);
        response.end();
    },
   
    respond : function(errorType, msg, res){
        if (this.)
    }
};

module.exports = http;