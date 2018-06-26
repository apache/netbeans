function level0() {
    var first = "defined in level0";// Try rename refactor from here
    function level1_1(){
        return first;
    }
    function level1_2(){
        var first = "defined in level1_2";// This hidden declaration causes the damage.
        return first;
    }
    function level1_3(){
        first = "value assigned in level1_3";// hidden - cannot be refactored from level0
        return first;
    }
    function level1_4(){
        this.level2_1 = function(){
            first = "value assigned in level2_1";// hidden - cannot be refactored from level0
            return first;
        }
    } 
    
    formatter.println("Execution of level0()");
    formatter.addIndent(4);
    formatter.println("calling level1_1(): " + level1_1());
    formatter.println("calling level1_2(): " + level1_2());
    formatter.println("calling level1_3(): " + level1_3());
    formatter.println("calling level1_1(): " + level1_1());
    formatter.println("value in lovel0: " + first);
    var level4 = new level1_4();
    formatter.println("calling level4.level2_1(): " + level4.level2_1());
    formatter.println("calling level1_1(): " + level1_1());
    formatter.println("value in lovel0: " + first); 
    formatter.removeIndent(4);
    formatter.println("");
    return first;
    
}

formatter.println("calling level0(): " + level0());



