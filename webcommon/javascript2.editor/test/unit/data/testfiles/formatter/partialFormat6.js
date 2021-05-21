var url = require('url');

function logErr(err) {
    if (err) {
        /*FORMAT_START*/console.log(err);
            if (err.stack) {
            console.log(err.stack);
        }/*FORMAT_END*/
    }
}

a = -1;


