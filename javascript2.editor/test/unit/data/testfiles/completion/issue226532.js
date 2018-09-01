function Test(){

  this.run = 1;
  this.fail = function(){};
  this.ok = function(){
    var request = new XMLHttpRequest();
    var that = this;
    request.onreadystatechange = function() {
                that.ppp();

    };
  };
}