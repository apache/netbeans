$.nette.ext('unique', {
   start: function(xhr) {
       if (this.xhr){
           this.xhr.abort();
       }    
       this.xhr = xhr;
   } 
});