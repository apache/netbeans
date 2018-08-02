function SettingsController_243888($scope) {
    $scope.name = "John Smith";
    $scope.cssColor = "blue";
    $scope.contacts = [
        {type: 'phone', value: '408 555 1212'},
        {type: 'email', value: 'john.smith@example.org'}];

    $scope.greet = function() {
        alert(this.name);
    };

    $scope.addContact = function() {
        this.contacts.push({type: 'email', value: 'yourname@example.org'});
    };

    $scope.removeContact = function(contactToRemove) {
        var index = this.contacts.indexOf(contactToRemove);
        this.contacts.splice(index, 1);
    };

    $scope.clearContact = function(contact) {
        contact.type = 'phone';
        contact.value = '';
    };
    $scope.foo = 1;
}

function Ctrl_243888($scope) {
    $scope.page = 1;
    $scope.printAttempt = {
        number: 1,
        total: 2
    };
    $scope.day = new Date();
    $scope.print = function() {
    };
}
function Ctrl2_243888($scope) {
    $scope.foo = 1;
}