MyGameListViewModel = function(games)
{
	var self = this;
        self.game = 1;
	self.pureGamesCount = ko.pureComputed(function()
	{
		return self.gamesToPlay().length + " games found.";
	});
	self.foo = ko.pureComputed(function()
	{
		return "foo";
	});
	self.foo2 = ko.pureComputed(function()
	{
		return "foo";
	});
        
        self.names = ko.observableArray([
        { name: 'Bert' },
        { name: 'Charles' },
        { name: 'Denise' }
    ]);
};
ko.applyBindings(new MyGameListViewModel());

function SimpleMode(){
    var self = this;
    this.purePrintName = ko.pureComputed(function(){
        return "<b>"+self.name+" "+lastName+"</b>";
    });
    this.pureName = ko.pureComputed(function(){
        return "<b>"+self.name+" "+lastName+"</b>";
    });

}
ko.applyBindings(new SimpleMode());