define([], function() {

    var counter = 0;

    function getRandomDate() {
        counter++;
        return new Date();
    }

    var Logger = {
        log: function() {
            return {a: 1, b: [], c: new Date()};
        }
    };


    return {
        getSomeDate: function() {
            return getRandomDate();
        },
        logIt: Logger.log()
    };

});