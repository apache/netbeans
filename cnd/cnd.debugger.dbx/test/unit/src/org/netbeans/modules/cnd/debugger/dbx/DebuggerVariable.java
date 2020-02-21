package org.netbeans.modules.cnd.debugger.dbx;

import javax.swing.Action;

import org.netbeans.modules.cnd.debugger.common2.debugger.Variable;
import org.netbeans.modules.cnd.debugger.common2.debugger.ModelChangeDelegator;
import org.netbeans.modules.cnd.debugger.common2.debugger.NativeDebugger;

public class DebuggerVariable extends Variable {

    public DebuggerVariable (ModelChangeDelegator updater, Variable parent, 
			    String name, String type, String value) {
	super(updater, parent, name, type, value);
    }

    public String getDebugInfo() {return null;}
    public NativeDebugger getDebugger(){return null;}
    public void removeAllDescendantFromOpenList(boolean isLocal){};

    public Action[] getActions(boolean isWatch){return null;}
    public boolean getDelta(){return true;}
    public void setVariableValue(String newValue){};

    // Can we use the array browser on this variable?
    public boolean isArrayBrowsable() {return true;}

    public void noteExpanded(boolean isWatch) {};
    public void noteCollapsed(boolean isWatch) {};

    public void postFormat(String format) {};
    public String getFormat(){return null;}

    public void createWatch() {}
}
