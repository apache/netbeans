var Test = {
    Verstion: 10,
    Run: (function(){
        return {
            m1: true,
            m2: false
        }
    })(),
    Specify: {
        isSupported: (function() {

            return false;
        })
    }
}

Test.Specify.isSupported = false;


