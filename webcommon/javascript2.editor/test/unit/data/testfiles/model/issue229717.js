function ExpensesModel() {
        var self = this;

        self.types = ["ahoj", "cau"];

        self.addType = function(type) {
            self.types.push(type);
            this.types;
        };
        
        function privateFun() {};
}  
     
ExpensesModel.prototype.publicFnc = function () {};

var test = new ExpensesModel();

test.types;
