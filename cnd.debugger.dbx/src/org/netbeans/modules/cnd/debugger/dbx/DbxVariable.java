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


package org.netbeans.modules.cnd.debugger.dbx;

import javax.swing.SwingUtilities;
import javax.swing.Action;
import org.openide.util.actions.SystemAction;

import java.util.Vector;
import java.util.Stack;

import org.netbeans.modules.cnd.debugger.common2.utils.IpeUtils;

import org.netbeans.modules.cnd.debugger.common2.debugger.actions.MaxObjectAction;

import org.netbeans.modules.cnd.debugger.common2.debugger.options.DebuggerOption;

import org.netbeans.modules.cnd.debugger.common2.debugger.ModelChangeDelegator;

import org.netbeans.modules.cnd.debugger.common2.debugger.WatchModel;
import org.netbeans.modules.cnd.debugger.common2.debugger.VariableModel;
import org.netbeans.modules.cnd.debugger.common2.debugger.NativeDebugger;
import org.netbeans.modules.cnd.debugger.common2.debugger.Variable;

class DbxVariable extends Variable {

    private final DbxDebuggerImpl debugger;

    // "permanent" attributes

    // Exact copies of :derefid and :deref we get from VDL.
    // Retain them for debugging purposes.
    private String derefId;
    private String derefExp;

    // These may be null
    private String deref_name;		// corresponds to derefId
    private String deref_expr;		// corresponds to derefExp

    private String assign_str = null;
    private String atype = "<unset atype>"; // NOI18N
    private boolean delta = false;
    private boolean isJava = false;
    private boolean isWatch = false;

    // "changing" attributes
    private String rhs;
    private String rhs_vdl;
    private VDLParser parser;
    private boolean firstAggregate;

    protected DbxVariable(DbxDebuggerImpl debugger, ModelChangeDelegator updater,
		       Variable parent, String name,
		       String deref_name, String type, String atype, 
		       String value, boolean isWatch) {
	super(updater, parent, name, type, value);
	this.debugger = debugger;
	this.deref_name = deref_name;
	this.atype = atype;
	if (isWatch == true)
	    this.isWatch = isWatch;
     }

    // interface Variable
    public void noteExpanded(boolean is_watch) {
	if (isExpanded())
	    return;
	setExpanded(true);
	expand(this.isWatch);
	updateOpenNodes(true, !this.isWatch);
    }

    // interface Variable
    public void noteCollapsed(boolean is_watch) {
	setExpanded(false);
	updateOpenNodes(false, !this.isWatch);
	// Don't do this. It doesn't work. See IZ 97706.
	// var.removeAllDescendantFromOpenList(isLocal());
    }

    private void expand(final boolean fromWatch) {

	// We may get called redundantly so make sure we send only one request
	// to the engine.
	if (waitingForDebugger) {
	    if (org.netbeans.modules.cnd.debugger.common2.debugger.Log.Variable.debug)
		System.out.printf("DbxVariable.expand(): re-entered !!!!!\n"); // NOI18N
	    return;
	}

	if (!isJava && isPtr()) {
	    // 'this' is a pointer. Chase it!
	    if (deref_expr == null)
		return;

	    /* LATER
	    */
	    if (children != null) {
		setExpanded(true);
		update();
		return;
	    }

	    setExpanded(true);
	    waitingForDebugger = true;	// reset in setChildren()

	    final DbxVariable t = this;
	    SwingUtilities.invokeLater(new Runnable() {
		public void run() {
		    try {
			if (fromWatch)
			    debugger.chaseWatchPointer(t, getDerefExp());
			else
			    debugger.chaseLocalPointer(t, getDerefExp());
		    } catch (Exception e) {
			e.printStackTrace();
		    }
		}
	    } );

	} else {
	    // 'this' is an aggregate. We should already have children.
	    assert children != null;
	    setExpanded(true);
	    update();
	}
    }



    /**
     * Called from ...
     * setWatchChasedPointer
     * setLocalChasedPointer
     * setExpandedNodes
     * updateWatches with null on an error
     */

    void setChildren(String rhs_vdl, DbxVariable v, boolean isWatch) {

	// CR 6972600, v is the node that will show error msg
	if (v != null) {
	    Variable vars[] = new Variable[1];
	    vars[0] = v;
	    setChildren(vars, true);
	    return;
	} 

	if (rhs_vdl == null) {
	    if (org.netbeans.modules.cnd.debugger.common2.debugger.Log.Variable.ctx)
		System.out.printf("setChildren[%s](null)\n", this.getVariableName()); // NOI18N
	    setLeaf(true);
	    setChildren(null, true);
	    return;
	}

	if (org.netbeans.modules.cnd.debugger.common2.debugger.Log.Variable.ctx)
	    System.out.printf("setChildren[%s](...)\n", this.getVariableName()); // NOI18N

	assert isPtr() : "DbxVariable.setChildren() called on non-ptr";

	valueStack = new CtxStack();
	Ctx firstCtx = new Ctx(this);
	valueStack.push(firstCtx);
	valueStack.top().setMayHaveChildren();

	firstAggregate = true;
	parser = new VDLParser(new DerefVDLActions(this, updater, isWatch));
	parser.parse(rhs_vdl);

	// If 'this' is an aggregate then we'll encounter a startAggregate and 
	// an endAggregate and endAggregate will actually set the children.
	// If this is a pointer to a scalar which we're indirecting then we
	// won't see an endAggregate and have to set the children
	// ourselves.
	// If the pointer is bad then valueStack.size() should be 0.

	if (valueStack.size() > 0) {

	    Ctx lastCtx = valueStack.pop();
	    assert lastCtx.currentVariable() == this;

	    // simlar sequence occurs in endAggregate:
	    DbxVariable vars[] = lastCtx.getChildren();
	    if (vars == null || vars.length == 0)
		setChildren(null, true);
	    else
		setChildren(vars, true);
	}

	// SHOULD see if can eliminate since setChildren() above passes a true.
	// OLD update();
    }

    // for assign new value from view nodes
    public void setVariableValue(String assign_value) {
	// CR 7094157
	// LATER should fix this in VariableModel
	if (!value.equals(assign_value)) {
            if ( (assign_value.indexOf(" ") >= 0) && (assign_value.indexOf(" ") <= assign_value.indexOf("\"")) ) { // NOI18N
                assign_value = assign_value.substring(assign_value.indexOf("\"")); // NOI18N
            }
            debugger.execute(this.assign_str + assign_value);
        }
    }

    private String getDerefExp() {
	return deref_expr;
    }

    private String getDerefName() {
	return deref_name;
    }

    private void setDerefName(String deref) {
        deref_name = deref;
    }
    public String getNodeKey() {
	if (isPtr() && deref_expr != null)
	    return deref_expr;
	else
	    return deref_name;
    }

    public boolean matchesNodeKey(String key) {
	return IpeUtils.sameString(key, getNodeKey());
    }

    public String getAType () {
	return atype;
    }

    // implement Variable
    public boolean getDelta() {
        return delta;
    }

    public void setDelta(boolean d) {
	this.delta = d;
    }

    public void setJava(boolean e) {
	isJava = e;
    }
    
    public boolean isJava() {
	return isJava;
    }

    public void setAType(String atype) {
	this.atype = atype;
    }

    public String getRHS () {
	return rhs;
    }

    public String getRHS_VDL () {
	return rhs_vdl;
    }

    public void setRHS(String rhs, String rhs_vdl, boolean isWatch) {
	if (org.netbeans.modules.cnd.debugger.common2.debugger.Log.Variable.ctx)
	    System.out.printf("setRHS[%s]()\n", this.getVariableName()); // NOI18N

	this.rhs = rhs;
	this.rhs_vdl = rhs_vdl;
	this.setAsText(rhs);

	valueStack = new CtxStack();
	Ctx firstCtx = new Ctx(this);
	valueStack.push(firstCtx);

	firstAggregate = true;
	parser = new VDLParser(new DerefVDLActions(this, updater, isWatch));
	parser.parse(rhs_vdl);
    }

    // Machinery for creating trees of DbxVariabels based on directives
    // from the VDLParser ...

    /**
     * class Ctx holds a variable and the children destined to be added to it.
     * It gets stacked because in the middle of processing a child we might 
     * recurse.
     * 
     * Children are kept separately until "the end" when they get assigned
     * to the proper parent. On "end" is in endAggregate(), the other "end"
     * is in setChildren().
     *
     * 'mayHaveChildren' captures explicitly what was previously done
     * implicitly by assigning a Vector (which may never have gotten filled)
     * and testing for it's presence.
     */

    private static class Ctx {
	public Ctx(DbxVariable currentVariable) {
	    this._currentVariable = currentVariable;
	}

	public DbxVariable currentVariable() {
	    return _currentVariable;
	}

	public boolean mayHaveChildren() {
	    return _mayHaveChildren || _children != null;
	}

	public void setMayHaveChildren() {
	    _mayHaveChildren = true;
	}

	public void add(DbxVariable child) {
	    if (_children == null)
		_children = new Vector<DbxVariable>();
	    _children.add(child);
	}

	public DbxVariable[] getChildren() {
	    if (_children == null)
		return null;
	    DbxVariable vars[] = new DbxVariable[_children.size()];
	    for (int vx = 0; vx < _children.size(); vx++)
		vars[vx] = _children.elementAt(vx);
	    return vars;
	}


	private final DbxVariable	_currentVariable;
	private Vector<DbxVariable>	_children;
	private boolean _mayHaveChildren;
    };

    private static class CtxStack extends Stack<Ctx> {
	public Ctx top() {
	    return super.peek();
	}
        
        @Override
	public Ctx pop() {
	    if (org.netbeans.modules.cnd.debugger.common2.debugger.Log.Variable.ctx)
		System.out.printf("CtxStack.pop()\n"); // NOI18N
	    return super.pop();
	}

        @Override
	public Ctx push(Ctx ctx) {
	    if (org.netbeans.modules.cnd.debugger.common2.debugger.Log.Variable.ctx) {
		System.out.printf("CtxStack.push(%s)\n", // NOI18N
		    ctx.currentVariable().getVariableName());
	    }
	    return super.push(ctx);
	}
    }

    private CtxStack valueStack;

    private static class DerefVDLActions implements VDLActions {
	private final DbxVariable var;
	private final ModelChangeDelegator updater;
	private final boolean isWatch;

	DerefVDLActions(DbxVariable var, ModelChangeDelegator updater, boolean isWatch) {
	    this.var = var;
	    this.updater = updater;
	    this.isWatch = isWatch;
	}

	// interface VDLActions
	public void setLeaf(boolean l) {
	    if (org.netbeans.modules.cnd.debugger.common2.debugger.Log.Variable.ctx) {
		System.out.printf("setLeaf[%s](%s)\n", // NOI18N
		    var.getVariableName(), l);
	    }
	    var.setLeaf(l);
	}

	// interface VDLActions
	public void setType(String type, String atype) {
	    var.setType(type);
	    var.setAType(atype);
	}

	// interface VDLActions
	public void setDelta(boolean e) {
	    var.delta = e;
	}

	// interface VDLActions
	public void setJava(boolean e) {
	    var.setJava(e);
	}
	
	// interface VDLActions
	public void newSmplval(String name,
			       String derefId,	// VDL :derefid
			       String type,
			       String atype,
			       boolean stat,
			       String value,
			       String set_str,
			       String derefAction,// VDL: :action (:deref
			       String hint,
			       boolean delta ) {

	    if (org.netbeans.modules.cnd.debugger.common2.debugger.Log.Variable.ctx) {
		System.out.printf("newSmplval[%s](%s, %s, %s)\n", // NOI18N
		    var.getVariableName(), name, derefId, derefAction);
	    }
	    
	    DbxVariable newVariable = null;
	    int newVariableCase = 0;

	    // CR 4925255, 4960157, 6371371
	    if (var.valueStack.top().mayHaveChildren()) {

		newVariable = new DbxVariable(var.debugger, updater,
		      var.valueStack.top().currentVariable(),
		      name, derefId, type, atype, value, isWatch);
		newVariableCase = 2;
		var.valueStack.top().add(newVariable);
		// we just added a child so it can't be a leaf anymore
		var.valueStack.top().currentVariable().setLeaf(false);

	    } else {
		// real simple type var, not a struct member
		newVariable = var.valueStack.top().currentVariable();
		newVariableCase = 3;
	    }

	    if (org.netbeans.modules.cnd.debugger.common2.debugger.Log.Variable.ctx) {
		System.out.printf("\tnewVariable case %d %s\n", // NOI18N
		    newVariableCase, newVariable.getVariableName());
	    }
	    newVariable.deref_name = derefId;
	    newVariable.setType(type);
	    newVariable.setAType(atype);
	    newVariable.setStatic(stat);
	    if (hint != null && hint.equals("literal")) // NOI18N
		newVariable.setLiteral(true);
	    newVariable.setLeaf(true);
	    newVariable.setDelta(delta);
	    newVariable.assign_str = set_str;
	    
	    if (derefAction != null) {
		// convert something of the form "display X" to "X":
	        String deref_exp = derefAction.substring(8);

		if (!(value.equals("(nil)") || value.equals("((nil))"))) // NOI18N
		    newVariable.setLeaf(false);
		newVariable.setPtr(true);

		// CR 6892637
		if (var.valueStack.size() > 0 || 
		    !var.getVariableName().equals(name)) { 

		    // ptr member of a struct or ptr deref
	            Ctx newCtx = new Ctx(newVariable);
	            var.valueStack.push(newCtx);
		    derefExpr(deref_exp, derefId, deref_exp, derefId);
		    var.valueStack.pop();

		} else {
		    derefExpr(deref_exp, derefId, deref_exp, derefId);
		}
		
		String disp_name = newVariable.getVariableName();
		// replace full qualified name to username for ptr.
		if (disp_name != null) { 
		    // The following code converts something of the form:
		    // 	`overload`overload.cc`abc()`c
		    // to
		    //  cc`abc()`c
		    if (! (newVariable instanceof DbxWatch)) {
			int dot = disp_name.lastIndexOf('.');
			if (dot != -1)
			    disp_name = disp_name.substring(dot+1);
			dot = disp_name.lastIndexOf(':');
			if (dot != -1)
			    disp_name = disp_name.substring(dot+1);
		    }
	            newVariable.setVariableName(disp_name);
		}
	    } 

	    // fire proper change for var nodes
	    newVariable.setAsText(value);
	}
	

	// interface VDLActions
	public void startAggregate(String name, String derefId,
				   String type, String atype, boolean stat,
				   boolean delta, boolean isopen) {

	    if (org.netbeans.modules.cnd.debugger.common2.debugger.Log.Variable.ctx) {
		System.out.printf("startAggregate[%s](%s, %s)\n", // NOI18N
		    var.getVariableName(), name, derefId);
	    }

	    //
	    // Pick a node which will act as the parent of the children
	    // in this aggregate
	    //

	    DbxVariable parent = null;
	    int parentCase = 0;

	    if (var.firstAggregate) {
		// top-level aggregate
		parentCase = 1;
		parent = var;
		// IZ 179035
                var.setDerefName(derefId);
	    }

	    if (var.valueStack.size() > 1) {
		// This plays a role when 
		// a) we're refreshing something and
		// b) we are processing a nested aggregate for which a 
		//    variable was created in the container.
		parentCase = 2;
	        parent = findChild(var.valueStack.top().currentVariable(),
					name);
	    }


	    if (parent == null) {
		// Nested aggregate
		// Create a Variable representing the field.
		parentCase = 3;
	        parent = new DbxVariable(var.debugger, updater,
				var.valueStack.top().currentVariable(),
				name, derefId, type, atype, null, isWatch);
	    } else {
		parent.setType(type);
		parent.setAType(atype);
	    }

	    if (org.netbeans.modules.cnd.debugger.common2.debugger.Log.Variable.ctx) {
		System.out.printf("\tparent case %d %s\n", // NOI18N
		    parentCase, parent.getVariableName());
	    }

	    parent.setStatic(stat);
	    parent.setLeaf(false);
	    parent.setDelta(delta);

	    // 'isopen' is tricky.
	    // It is only valid with nested aggregates, not chased pointers.
	    // With the case of chased pointers setExpanded() is called 
	    // elsewhere and here 'isopen' is always false.
	    // In other cases (I think) the default expanded state is always
	    // false so we only expand it if it's true.
	    if (isopen)
		parent.setExpanded(isopen);

	    if (var.valueStack.size() > 0) {
		// install as a field in the current aggregate
		if (parentCase == 3)
		    var.valueStack.top().add(parent);
	    }
	    // OLD if (parent != var) {
	    if (!var.firstAggregate) {
		Ctx newCtx = new Ctx(parent);
		var.valueStack.push(newCtx);
	    }
	    var.valueStack.top().setMayHaveChildren();
	    var.firstAggregate = false;
	}

	// interface VDLActions
	public void endAggregate() {

	    if (org.netbeans.modules.cnd.debugger.common2.debugger.Log.Variable.ctx) {
		System.out.printf("endAggregate[%s]()\n", // NOI18N
		    var.getVariableName());
		System.out.printf("\ttop variable %s\n", // NOI18N
		    var.valueStack.top().currentVariable().getVariableName());
	    }

	    // The following derefExpr() calls play a role in
	    // cleaning things up if a pointer field in an aggregate
	    // goes to 0 or <ERROR>.

	    DbxVariable parent = var.valueStack.top().currentVariable();
	    derefExpr(parent.deref_expr, parent.deref_name,
		      parent.derefExp, parent.derefId);
	    Ctx top = var.valueStack.pop();

	    DbxVariable children[] = top.getChildren();
	    if (children == null || children.length == 0) {
		parent.setChildren(null, true);

	    } else {
		parent.setChildren(children, true);

		if (!parent.isPtr() || parent.isJava() ) {
		    // Create a value representing a summary of the fields.

		    StringBuffer fvalue = new StringBuffer("("); // NOI18N
		    int cx = 0;
		    for (cx = 0; cx < children.length-1; cx++) {
			fvalue.append(children[cx].getAsText());
			fvalue.append(","); // NOI18N
		    }
		    fvalue.append(children[cx].getAsText());
		    fvalue.append(")"); // NOI18N

		    parent.setAsText(fvalue.toString());
		}
	    }
	}

	/**
	 * Set the dereferencing expr for current Watch or Variable.
	 */
	private void derefExpr(String expr, String name,
			      String derefExp, String derefId) {
	    DbxVariable currentVariable = var.valueStack.top().currentVariable();
	    if (org.netbeans.modules.cnd.debugger.common2.debugger.Log.Variable.ctx) {
		System.out.printf("derefExpr [%s]{%s, %s, %s, %s} \n", // NOI18N
		    var.getVariableName(), expr, name, derefExp, derefId);
		System.out.printf("\ttop %s\n", currentVariable.getVariableName()); // NOI18N
	    }
	    if (currentVariable != null) {
		// we used to set both deref_expr and deref_name to expr
		// But keeping them separate and choosing which to use in
		// getNodeKey() is a bit clearer.
		currentVariable.deref_expr = expr;
		currentVariable.deref_name = name;

		currentVariable.derefExp = derefExp;
		currentVariable.derefId = derefId;
	    }
	}

	private DbxVariable findChild(DbxVariable parent, String child_name) {
	    if (org.netbeans.modules.cnd.debugger.common2.debugger.Log.Variable.ctx) {
		System.out.printf("\tfindChild(%s, %s)\n", // NOI18N
		    parent.getVariableName(), child_name);
	    }
	    if (child_name == null)
		return null;

	    Variable[] children = parent.getChildren();
	    if (children == null) 
		return null;

	    for (int vx = 0; vx < children.length; vx++) {
		Variable child = children[vx];
		if (child != null && child_name.equals(child.getVariableName()))
		    return (DbxVariable) child;
	    }
	    return null;
	}

    }

    public NativeDebugger getDebugger () {
	return this.debugger;
    }

    public String getAssignStr() {
        return this.assign_str;
    }

    public void updateOpenNodes(boolean expanded, boolean isLocal) {
	debugger.updateOpenNodes(this, getNodeKey(), expanded, isLocal);
    }

    public void removeAllDescendantFromOpenList(boolean isLocal) {
	for (Variable v : getChildren()) {
	    DbxVariable child = (DbxVariable) v;

	    // This shouldn't really happen. Somewhere if child expansion
	    // fails we're ending up assigning a null child. SHOULD track
	    // down where that place is.
	    if (child == null)
		continue;

	    if (child.isExpanded()) {
		child.setExpanded(false);
		child.update();
		debugger.updateOpenNodes(this, child.getNodeKey(), false, isLocal);
	    }
	    child.removeAllDescendantFromOpenList(isLocal);
	}
    }

    // interface Variable
    public String getDebugInfo() {
	int nc = children == null? 0: children.length;
	return "<code>" + // NOI18N
	       "<b>isPtr</b> " + isPtr() + "<br>" + // NOI18N
	       "<b>isLeaf</b> " + isLeaf() + "<br>" + // NOI18N
	       "<b>isExpanded</b> " + isExpanded() + "<br>" + // NOI18N
	       "<b>nChildren</b> " + nc + "<br>" + // NOI18N
	       "<b>deref_name</b> " + deref_name + "<br>" + // NOI18N
	       "<b>deref_expr</b> " + deref_expr + "<br>" + // NOI18N
	       "<b>derefId</b> " + derefId + "<br>" + // NOI18N
	       "<b>derefExp</b> " + derefExp + // NOI18N
	       "</code>"; // NOI18N
    }

    // interface Variable
    public Action[] getActions(boolean isWatch) {
	if (isWatch) {
	    return new Action[] {
                WatchModel.NEW_WATCH_ACTION,
		null,
		new WatchModel.DeleteAllAction(),
		null,
		VariableModel.Action_INHERITED_MEMBERS,
		VariableModel.Action_DYNAMIC_TYPE,
		VariableModel.Action_STATIC_MEMBERS,
		VariableModel.Action_PRETTY_PRINT,
		VariableModel.getOutputFormatAction(this),
		null
	    };

	} else {
	    // local
            if (org.netbeans.modules.cnd.debugger.dbx.Log.ArrayBrowser.enabled) {
                return new Action[] {
                    VariableModel.Action_INHERITED_MEMBERS,
                    VariableModel.Action_DYNAMIC_TYPE,
		    VariableModel.Action_STATIC_MEMBERS,
                    VariableModel.Action_PRETTY_PRINT,
                    VariableModel.getOutputFormatAction(this),
                    new VariableModel.BrowseArrayAction(this),
                    SystemAction.get(MaxObjectAction.class),
                    null,
                };
	    } else {
                return new Action[] {
                    VariableModel.Action_INHERITED_MEMBERS,
                    VariableModel.Action_DYNAMIC_TYPE,
		    VariableModel.Action_STATIC_MEMBERS,
                    VariableModel.Action_PRETTY_PRINT,
                    VariableModel.getOutputFormatAction(this),
                    SystemAction.get(MaxObjectAction.class),
                    null,
                };
	    }
	}
    }

    // interface Variable
    public boolean isArrayBrowsable() {
	// tmp hack, should find a better way to do it
	if (atype != null && atype.contains("[]")) // NOI18N
	    return true;
	else
	    return false;

    }

    // interface Variable
    public void postFormat(String format) {
	debugger.setOption("DBX_output_base", format); // NOI18N
	debugger.postOutputBase(format);
    }

    // interface Variable
    public String getFormat() {
	return DebuggerOption.OUTPUT_BASE.getCurrValue(debugger.optionLayers());
    }
    
    @Override
    public void createWatch() {
        throw new UnsupportedOperationException("Not implemented yet."); //NOI18N
    }
}
