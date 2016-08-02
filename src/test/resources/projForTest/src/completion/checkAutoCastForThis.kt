package completion

interface Monster {
    
    public fun test() {
        if (this is Beholder) {
            this.<caret>
        }
    }
    
}

class Beholder : Monster {
    
    fun destroy(){}
    
}