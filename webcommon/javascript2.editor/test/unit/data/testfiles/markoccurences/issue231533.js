it("case1: multiple namespaces", function() {
            var beasties = {};

            beasties.Animal = function() {};
            beasties.Animal.prototype.roar = function() {return 'rrrr';};

            var animal = new beasties.Animal();

            expect(animal.roar()).toEqual('rrrr'); // ctr+click does not work on roar
        });