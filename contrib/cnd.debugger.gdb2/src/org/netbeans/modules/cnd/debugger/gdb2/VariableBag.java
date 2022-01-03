/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.cnd.debugger.gdb2;

import java.util.Vector;
import org.netbeans.editor.ObjectArrayUtilities;
// OLD import java.util.Iterator;

import org.netbeans.modules.cnd.debugger.common2.debugger.ModelChangeDelegator;
import org.netbeans.modules.cnd.debugger.common2.debugger.NativeDebugger;

import org.netbeans.modules.cnd.debugger.common2.debugger.Variable;

/*
 * Helps manage MI vars.
 */
class VariableBag {

    private final Vector<GdbVariable> variables = new Vector<GdbVariable>();
    private final Vector<GdbVariable> watchvariables = new Vector<GdbVariable>();
    private final Vector<GdbVariable> localvariables = new Vector<GdbVariable>();

    public final static int FROM_LOCALS = 1;
    public final static int FROM_WATCHES = 2;
    public final static int FROM_BOTH = 3;

    private ModelChangeDelegator updater;
    private NativeDebugger debugger;	// ... to which we're bound

    public VariableBag() {
    }

    boolean hasVariable(String miName) {
        if (miName == null || miName.isEmpty()) {
            return false;
        }
        //checks if variable with the miName presented in the bag
        for (GdbVariable var : variables) {
            if (miName.equals(var.getMIName())) {
                return true;
            }
        }
        for (GdbVariable var : localvariables) {
            if (miName.equals(var.getMIName())) {
                return true;
            }
        }
        for (GdbVariable var : watchvariables) {
            if (miName.equals(var.getMIName())) {
                return true;
            }
        }
        return false;
    }

    private boolean isBound() {
	return debugger != null;
    }

    public void bindDebugger(NativeDebugger debugger) {
        this.debugger = debugger;
    }

    public void setUpdater(ModelChangeDelegator updater) {
        this.updater = updater;
    }

    public ModelChangeDelegator variablesUpdater() {
        return updater;
    }

    public Variable[] getVars() {
	Variable [] vars = new Variable[variables.size()];
	return variables.toArray(vars);
    } 

    public Variable[] getWatchVars() {
	Variable [] vars = new Variable[watchvariables.size()];
	return watchvariables.toArray(vars);
    } 

    public Variable[] getLocalVars() {
	Variable [] vars = new Variable[localvariables.size()];
	return localvariables.toArray(vars);
    } 

    public void add(GdbVariable newVar) {
	//System.out.println("VariableBag add " + ((GdbVariable)newVar).getMIName());
	variables.add(newVar);
	if (newVar.isWatch())
	    watchvariables.add(newVar);
	else
	    localvariables.add(newVar);
    }

    public GdbVariable byAddr(String exp, String addr, int from) {
        Variable[] iter = getVars();
	if (from == FROM_LOCALS)
	    iter = getLocalVars();
	else if (from == FROM_WATCHES)
	    iter = getWatchVars();

	for (Variable v : iter) {
            if (v instanceof GdbVariable) {
                GdbVariable gv = (GdbVariable)v;
                if (exp.equals(gv.getVariableName()) && addr.equals(gv.getAsText()))
                    return (GdbVariable)v;
            }
        }
        return null;
    }

    public GdbVariable get(String exp, boolean with_MIname, int from) {
	/* OLD
	Iterator iter = variables.iterator();
	while (iter.hasNext()) {
	    Variable v = (Variable) iter.next();
	    //System.out.println("VariableBag get " + ((GdbVariable)v).getMIName());
	    //System.out.println("VariableBag get " + ((GdbVariable)v).getMIName());
	    if (v instanceof GdbVariable) {
		GdbVariable gv = (GdbVariable)v;
		if (with_MIname) {
	            if (exp.equals(gv.getMIName()))
		        return v;
	        } else {
		    if (exp.equals(gv.getVariableName()))
		        return v;
	        }
	    }
	}
	*/

        Variable[] iter = getVars();
	if (from == FROM_LOCALS)
	    iter = getLocalVars();
	else if (from == FROM_WATCHES)
	    iter = getWatchVars();

	for (Variable v : iter) {
	    if (v instanceof GdbVariable) {
		GdbVariable gv = (GdbVariable)v;
		if (with_MIname) {
	            if (exp.equals(gv.getMIName()))
		        return (GdbVariable)v;
	        } else {
		    if (exp.equals(gv.getVariableName()))
		        return (GdbVariable)v;
	        }
	    }
	}
	return null;
    }

    public int remove_count = 0;

    public void remove(Variable oldVar) {
	Variable[] children = oldVar.getChildren();
	variables.remove(oldVar);
	remove_count++;
	int size = children.length;
	if (size != 0) {
	    for (int vx=0; vx < size; vx++) {
		if (children[vx] != null)
		    remove(children[vx]);
	    }
	}
	if (((GdbVariable)oldVar).isWatch()) {
	    watchvariables.remove(oldVar);
        } else {
	    localvariables.remove(oldVar);
        }
    }

    public void removeWatch(Variable oldVar) {
	watchvariables.remove(oldVar);
    }

    public void removeLocal(Variable oldVar) {
	localvariables.remove(oldVar);
    }

    public void removeAll() {
	/* OLD
	Iterator iter = variables.iterator();
	while (iter.hasNext()) {
		iter.remove();
	}
	*/
	variables.clear();
    }

    public void removeAllWatches() {
	/* OLD
	Iterator iter = watchvariables.iterator();
	while (iter.hasNext()) {
		iter.remove();
	}
	*/
	watchvariables.clear();
    }

    public void removeAllLocals() {
	/* OLD
	Iterator iter = localvariables.iterator();
	while (iter.hasNext()) {
		iter.remove();
	}
	*/
	localvariables.clear();
    }
}

