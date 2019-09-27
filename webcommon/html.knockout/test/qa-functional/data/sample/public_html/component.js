ko.components.register('message-editor', {
    viewModel: function (params) {
        this.text = ko.observable(params.initialText || '');
    },
    template: 'Message: <input data-bind="value: text" /> '
});

ko.applyBindings();

(function () {

    function test() {
        ko.components.register('message-container', {
            viewModel: function (params) {
                this.text = ko.observable(params.initialText || '');
            },
            template: 'Message: <input data-bind="value: text" /> '
        });
        ko.components.register('myMessage', {
            viewModel: function (params) {
                this.text = ko.observable(params.initialText || '');
            },
            template: 'Message: <input data-bind="value: text" /> '
        });
        ko.components.register('unrelated-message', {
            viewModel: function (params) {
                this.text = ko.observable(params.initialText || '');
            },
            template: 'Message: <input data-bind="value: text" /> '
        });
    }
    test();
    
    
    ko.components.register("yetanother", {
        viewModel : function(params){
            console.log(params.par1);
            console.log(params.test);
            console.log(params.par);
        },
        template : ""
    });
    
    
    ko.applyBindings();
})();