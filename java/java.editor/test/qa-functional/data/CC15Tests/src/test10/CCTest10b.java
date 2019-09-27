package test10;

import test9.CCTest9a;

public class CCTest10b {
    
    public static void main(String[] args) {
        CCTest9a e; //Check that CCTest9a is in the CC
	
	e = CCTest9a.A; //Check the CC provided after the dot
        
	CCTest9a x = e.A; //Check the CC provided after the dot
    }
    
}
