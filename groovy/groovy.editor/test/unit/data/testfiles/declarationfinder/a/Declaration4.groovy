package a

class Declaration4 {
	
    void execute () {
        def builder = new TestBuilder()
        
        def clon = builder.clone(builder)
        
        clon.getInfo()
    }  
}

