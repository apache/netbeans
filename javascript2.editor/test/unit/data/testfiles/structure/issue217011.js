var Player217011 = function(){
    this.name = "better player";
    this.b = new Bar();
   
    
    function Bar(){
      this.tp =1;  
    };
    
    this.getinfo = function() {
        return this.name;
    }
   
    this.factor = function(){ this.dirtFactor=10; } 
}

var a217011 = new Player217011();
 
formatter.println(a217011.tp);
formatter.println(a217011.b.tp);
formatter.println((a217011.getinfo()));
a217011.factor();
formatter.println("dirtFactor:" + a217011.dirtFactor)

var Hrac217011 = {}
Hrac217011.jedna = null;
//Hrac217011.dva = null;
Hrac217011.init = function () {
    this.jedna = "jedna";
    this.dva = "dva";
}
 
formatter.println(Hrac217011.jedna);
formatter.println(Hrac217011.dva);
Hrac217011.init();
formatter.println(Hrac217011.jedna);
formatter.println(Hrac217011.dva);