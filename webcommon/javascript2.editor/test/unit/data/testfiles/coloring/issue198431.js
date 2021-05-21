function test(){

    function doit(){
    }

    doit();// Becomes green if statement below is added
    this.doitPublic = doit;

}