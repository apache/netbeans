function init() {

    function Expense(description, type, price) {
        var self = this;
        self.date = 0; //TODO
        self.description = description;
        self.type = ko.observable(type);
        self.price = ko.observable(price);

        self.priceStatus = ko.observable("");
        self.attemptedPrice = ko.computed({
            read: self.price,
            write: function(value) {
                if (isNaN(value)) {
                    self.priceStatus("numberBad");
                } else {
                    self.priceStatus("numberOK");
                    self.price(value); // Write to underlying storage
                }
            }
        });

    }

    function ExpenseType(name) {
        var self = this;
        self.name = ko.observable(name);
    }

    //main model
    function ExpensesModel() {
        var self = this;

        //TODO - remove hardcoded data, load from a server
        self.types = ko.observableArray([
            new ExpenseType("Lunch"),
            new ExpenseType("Dinner"),
            new ExpenseType("Drinks")
        ]);

        this.addType = function() {
            self.types.push(new ExpenseType(""));
        };

        // TODO - load from server
        self.expenses = ko.observableArray([
            new Expense("a shark", self.types[0], 10),
            new Expense("beers", self.types[2], 300)
        ]);

        self.addExpense = function() {
            self.expenses.push(new Expense("", self.types[0], 0));
        };

        self.removeExpense = function(expense) {
            self.expenses.remove(expense);
        };

        self.totalPrice = ko.computed(function() {
            var total = 0;
            for (var i = 0; i < self.expenses().length; i++)
                //how to get the numeric value from the observable so I do not have to use parseInt()???
                total += parseInt(self.expenses()[i].price());
            return total;
        });
    }

    ko.applyBindings(new ExpensesModel());
}