package test8;

public class CCTest8iii {
    
    public static void main(String[] args) {
        InnerEnum e; //Check that InnerEnum is in the CC
	
	e = InnerEnum.A; //Check the CC provided after the dot
        

    }
    
    private static enum InnerEnum {
        A, B, C
    }
}
