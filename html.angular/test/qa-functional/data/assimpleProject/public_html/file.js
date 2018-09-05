function SettingsController() {
    this.name = "John Smith";
    this.cssColor = "blue";
    this.contacts = [
        {type: 'phone', value: '408 555 1212'},
        {type: 'email', value: 'john.smith@example.org'}];
    this.own = 1;
    this.greet = function() {
        alert(this.name);
    };

    this.addContact = function() {
        this.contacts.push({type: 'email', value: 'yourname@example.org'});
    };

    this.removeContact = function(contactToRemove) {
        var index = this.contacts.indexOf(contactToRemove);
        this.contacts.splice(index, 1);
    };

    this.clearContact = function(contact) {
        contact.type = 'phone';
        contact.value = '';
    };
    this.foo = 1;
}

function ACtrl() {
    this.page = 1;
    this.printAttempt = {
        number: 1,
        total: 2
    };
    this.day = new Date();
    this.print = function() {
    };
}
function ACtrl2() {
    this.foo = 1;
}