define(function(require) {
    var Editor = Backbone.View.extend({});

    Editor.prototype.openEdition = function (edition) {
        this.pager.on('empty', this.welcome, this);
        this.pager.on('selected', this.openPage, this);
        $('.project-drawer-inner').append(this.pager.el);
    };

    Editor.prototype.welcome = function () {
        this.text = "Welcome";
    };
});