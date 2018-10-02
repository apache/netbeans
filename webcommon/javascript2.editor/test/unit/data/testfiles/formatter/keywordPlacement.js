if (x < 0) {
    alert("x<0");
}
                    else if (x === 0) {
        alert("x==0");
    }
else {
        alert("x>0");
    }

while(true) {
    if (cond) {
        break;
    } else {
        do {
            try {
                console.log("in loop");
            }
            catch (e) {
                alert("exception!");
            }                                               finally {
                // just a comment
            }
        }
while (canContinue);
    }
}