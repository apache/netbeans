function GlobalUriHelper(myPattern) {
	var _self = this;
	this.parse = function (systemUri) {
		var address = {baseUrl:'', id:0};
		systemUri.replace(myPattern, function(a, protocol, server, id) {
			address.baseUrl = protocol + server;
			address.id = parseInt(id);
		});
		return address;
	};
}