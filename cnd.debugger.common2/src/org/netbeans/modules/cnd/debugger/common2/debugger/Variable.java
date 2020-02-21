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


package org.netbeans.modules.cnd.debugger.common2.debugger;

import javax.swing.Action;

public abstract class Variable {

    // "permanent" attributes

    protected final Variable parent;
    protected final ModelChangeDelegator updater;	// effectively a
							// pointer to our
							// owning model
    private String name = "<unset name>"; // NOI18N
    protected String type = "";
    protected String value = "";

    protected boolean isLeaf = true;
    protected boolean isPtr = false;
    protected boolean literal = false;
    private boolean isExpanded = false;
    protected boolean waitingForDebugger;
    protected Variable[] children = null;
    private boolean stat;
    
    protected boolean hasMore = false;

    protected Variable(ModelChangeDelegator updater, Variable parent,
				String name, String type, String value) {
	this.parent = parent;
	this.updater = updater;

	if (name != null) {
	     this.name = name;
	}
	if (type != null)
	    this.type = type;
	if (value != null)
	    this.value = value;

	if (Log.Variable.debug)
	    System.out.printf("Variable.<init>(%s)\n", name); // NOI18N
     }

    public ModelChangeDelegator getUpdater() {
	return updater;
    }
    
    /*
     * Cause the view to pull all variables.
     */
    protected void update() {
	if (Log.Variable.debug) {
	    System.out.printf("Variable.update()\n"); // NOI18N
	}
	updater.treeNodeChanged((Object)this);
    }

    // AbstractVariable ........................................................

    /**
     * Return the name of this variable.
     *
     * @return the name of this variable.
     */
    public String getVariableName () {
	return name;
    }

    public void setVariableName(String name) {
	this.name = name;
    }

    public String getFullName() {
        StringBuilder res = new StringBuilder(name);
        Variable p = parent;
        while (p != null) {
            // LATER: need to insert -> or . based on type
            res.insert(0, p.getVariableName() + '.');
            p = p.parent;
        }
        return res.toString();
    }
    
    public boolean isEditable() {
        return (isLeaf() && !literal) || isPtr();
    }

    /**
     * Getter for textual representation of the value. It converts
     * the value to a string representation. So if the variable represents
     * null reference, the returned string will be for example "null".
     * That is why null can be returned when the watch is not valid
     *
     * @return the value of this watch or null if the watch is not in the scope
     */
    public String getAsText () {
	return value;
    }

    public void setAsText(String value) {
	// This is intended for use by user actions, not setting by the engine
	// the way I'm currently usingit.
	Object ovalue = this.value;
	// 6536351, 6520382
	if (value == null)
	    this.value = "<null>"; // NOI18N
	else
	    this.value = value.trim();
    }

    public void setExpanded(boolean e) {
	if (Log.Variable.expanded) {
	    System.out.printf("Variable[%s].setExpanded(%s)\n", // NOI18N
		getVariableName(), e);
	}
        this.isExpanded = e;
    }

    public boolean isExpanded() {
	if (Log.Variable.expanded) {
	    System.out.printf("Variable[%s].isExpanded() -> %s\n", // NOI18N
		getVariableName(), isExpanded);
	}
        return isExpanded;
    }

    /**
     * Return string representation of type of this variable.
     *
     * @return string representation of type of this variable
     */
    public String getType () {
	return type;
    }

    public void setType(String type) {
	this.type = type;
    }

    
    public boolean isRoot() {
	return parent == null;
    }

    public void setStatic(boolean stat) {
	this.stat = stat;
    }

    public boolean isStatic() {
	return stat;
    }

    public boolean isLeaf() {
	if (Log.Variable.leaf) {
	    System.out.printf("Variable[%s].isLeaf() -> %s\n", // NOI18N
		getVariableName(), isLeaf);
	}
	return isLeaf;
    } 

    public void setLeaf(boolean isLeaf) {
	if (Log.Variable.leaf) {
	    System.out.printf("Variable[%s].setLeaf(%s)\n", // NOI18N
		getVariableName(), isLeaf);
	}
	this.isLeaf = isLeaf;
	update();
    }

    public void setLiteral(boolean lit) {
	literal = lit;
    }
    
    public boolean isLiteral() {
	return literal;
    }


    public void setPtr(boolean e) {
	isPtr = e;
    }
    
    public boolean isPtr() {
	return isPtr;
    }
    
    public boolean hasMore(){
        return hasMore;
    }
    
    public void setHasMore(boolean hasMore){
        this.hasMore = hasMore;
    }

    public void setChildren(Variable[] children, boolean andUpdate) {
	if (Log.Variable.debug) {
	    int nc = children == null? -1: children.length;
	    System.out.printf("Variable[%s].setChildren(%d, %s)\n", // NOI18N
		name, nc, andUpdate);
	}

	this.children = children;

	waitingForDebugger = false;	// set in 'expand()'
	if (andUpdate)
	    update();
    }

    private void addChildren(Variable[] extra) {
	Variable[] child_list = new Variable[extra.length + children.length];
	int vx = 0;
	for (vx = 0; vx < children.length; vx++) {
	    child_list[vx] = children[vx];
	}
	int index = vx;
	for (vx = 0; vx < extra.length; vx++) {
	    child_list[index+vx] = extra[vx];
	}

	children = child_list;
    }
    
    public void addChildren(Variable[] extra, boolean andUpdate) {
	if (Log.Variable.debug) {
	    int nc = extra == null? -1: extra.length;
	    System.out.printf("Variable[%s].setChildren(%d, %s)\n", // NOI18N
		name, nc, andUpdate);
	}
	if (extra == null)
	    return;

	addChildren(extra);

	waitingForDebugger = false;	// set in 'expand()'
	if (andUpdate)
	    update();
    }

    public int getNumChild() {
	return getChildren().length;
    }

    public Variable[] getChildren() {
	if (Log.Variable.debug) {
	    int nc = children == null? -1: children.length;
	    System.out.printf("Variable[%s].getChildren() -> %d\n", // NOI18N
		name, nc);
	}
	if (children != null) {
	    return children;
	} else {
	    return new Variable[0];		// see IZ 99042
	}
    }
    
    /**
     * Shows the next 100 children of the variable
     * Support for GdbVariable.getNextChildren()
     */
    public void getMoreChildren() {}

    @Override
    public String toString() {
        return getVariableName() + '=' + getAsText();
    }

    public abstract String getDebugInfo();
    public abstract NativeDebugger getDebugger();
    public abstract void removeAllDescendantFromOpenList(boolean isLocal);

    public abstract Action[] getActions(boolean isWatch);
    public abstract boolean getDelta();
    public abstract void setVariableValue(String newValue);

    // Can we use the array browser on this variable?
    public abstract boolean isArrayBrowsable();

    public abstract void noteExpanded(boolean isWatch);
    public abstract void noteCollapsed(boolean isWatch);

    public abstract void postFormat(String format);
    public abstract String getFormat();
    
    // create a watch from a local variable
    public abstract void createWatch();
}
