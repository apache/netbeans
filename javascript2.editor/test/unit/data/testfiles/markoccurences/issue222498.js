// Since dojo.store.Memory doesn't have various store methods we need, we have to add them manually
store.getChildren = function(object) {
    // Add a getChildren() method to store for the data model where
    // children objects point to their parent (aka relational model)
    return this.query({parent: this.getIdentity(object)});
};
  