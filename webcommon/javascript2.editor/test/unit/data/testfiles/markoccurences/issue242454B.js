function Root() {
  function f1() {
  } 

  function X() {
     this.y1 = f1;  
     this.y2 = f2;  

    function f2() {
    } 
  }
}