(function() {
    var Animal = function() {
    };
    Animal.prototype.roar = function() {
        return 'rrrr';
    };

    var animal = new Animal();
    var grrr = animal.roar(); // here it works            

    var Cat = function() {
    };

    Cat.prototype = new Animal();
    Cat.prototype.mieow = function() {
        return 'mieow';
    };
  
    var cat = new Cat(); 
    expect(cat.roar()).toEqual('rrrr'); // ctr+click does not work on roar
})();