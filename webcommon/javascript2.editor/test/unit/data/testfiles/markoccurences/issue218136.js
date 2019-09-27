function Player(url, name){
    var static_int = 0;
    Player.prototype.setStatic = function(v){ static_int = v; };
}

var p1 = new Player();
p1.setStatic(100);
