function highlight() {
         document.getElementById(location.hash.replace(/#/, "")).className = "highlight";
    }
  
(function(){
prettyPrint(); highlight();
});
