/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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

