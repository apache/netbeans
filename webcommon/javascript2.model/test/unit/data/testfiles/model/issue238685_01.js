HudsonReader.utils.DB = function(callback, logger) { 
    /**
     * @param {Job} job
     * @param {Function} callback callback function
     */
     this.store = function(job, callback) {
        console.log("store running");
     };
    
    this.store();
    
}
  
var a = new HudsonReader.utils.DB();
a.store();