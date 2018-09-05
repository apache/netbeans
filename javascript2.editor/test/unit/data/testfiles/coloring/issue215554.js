var Bug = Backbone.Model.extend({
    defaults: {
        url: "http://netbeans.org/bugzilla/show_bug.cgi?id="
    }
});


//define directory collection
var BugList = Backbone.Collection.extend({
    model: Bug
});   