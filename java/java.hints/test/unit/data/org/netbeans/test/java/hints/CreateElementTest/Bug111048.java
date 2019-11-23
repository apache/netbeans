package org.netbeans.test.java.hints;

import java.util.HashMap;
import java.util.Map;

public class Bug111048 {

    public Bug111048() {
    }

    public void t() {
	Map m = new HashMap();
	if (m.contains("")) {
	}
	
	if (this.contains("")) {
	}
	
	if (m.fieldOrClass.equals("")) {
	}
	
	if (this.fieldOrClass.equals("")) {
	    
	}
    }
}
