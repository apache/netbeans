function AppViewModel() {
    var self = this;
 
    self.people = ko.observableArray([
        { name: 'Bert' },
        { name: 'Charles' },
        { name: 'Denise' }
    ]);
 
    self.addPerson = function() {
        self.people.push({ name: "New at " + new Date() });
    };
 
    self.removePerson = function() {
        self.people.remove(this);
    }
    self.cremovePerson = ko.computed(function() {
      return self.people.remove(this);
    })
}
 
ko.applyBindings(new AppViewModel());