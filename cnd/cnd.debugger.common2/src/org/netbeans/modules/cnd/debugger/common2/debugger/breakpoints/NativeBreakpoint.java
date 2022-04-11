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

package org.netbeans.modules.cnd.debugger.common2.debugger.breakpoints;

import org.netbeans.modules.cnd.debugger.common2.values.Action;
import org.netbeans.modules.cnd.debugger.common2.utils.props.PropertyOwnerSupport;
import org.netbeans.modules.cnd.debugger.common2.values.EditUndo;
import org.netbeans.modules.cnd.debugger.common2.utils.props.StringProperty;
import org.netbeans.modules.cnd.debugger.common2.utils.props.EnumProperty;
import org.netbeans.modules.cnd.debugger.common2.utils.props.Property;
import org.netbeans.modules.cnd.debugger.common2.utils.StackHistory;
import org.netbeans.modules.cnd.debugger.common2.utils.IpeUtils;
import org.netbeans.modules.cnd.debugger.common2.utils.props.BooleanProperty;
import org.netbeans.modules.cnd.debugger.common2.utils.props.IntegerProperty;
import org.netbeans.modules.cnd.debugger.common2.values.CountLimit;
import org.netbeans.modules.cnd.debugger.common2.values.EditUndoable;
import org.netbeans.modules.cnd.debugger.common2.utils.props.PropertyOwner;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import java.util.Set;
import java.util.Date;
import java.util.HashSet;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import javax.swing.SwingUtilities;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.project.Project;

import org.xml.sax.Attributes;

import org.openide.text.Line;

import org.netbeans.spi.debugger.ui.BreakpointType;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.ModelEvent;

import org.netbeans.modules.cnd.debugger.common2.DbgGuiModule;

import org.netbeans.modules.cnd.debugger.common2.debugger.RoutingToken;
import org.netbeans.modules.cnd.debugger.common2.debugger.NativeDebugger;
import org.netbeans.modules.cnd.debugger.common2.debugger.NativeDebuggerManager;
import org.netbeans.modules.cnd.debugger.common2.debugger.NativeSession;
import org.netbeans.modules.cnd.debugger.common2.debugger.DebuggerAnnotation;
import org.netbeans.modules.cnd.debugger.common2.debugger.EditorBridge;
import org.netbeans.modules.cnd.debugger.common2.debugger.Constants;
import org.netbeans.modules.cnd.debugger.common2.debugger.breakpoints.props.ActionProperty;
import org.netbeans.modules.cnd.debugger.common2.debugger.breakpoints.props.ContextProperty;
import org.netbeans.modules.cnd.debugger.common2.debugger.breakpoints.props.CountLimitProperty;

import org.netbeans.modules.cnd.debugger.common2.debugger.breakpoints.types.LineBreakpoint;
import org.netbeans.modules.cnd.debugger.common2.debugger.breakpoints.types.LineBreakpointType;

import org.netbeans.modules.cnd.debugger.common2.debugger.breakpoints.types.InstructionBreakpoint;
import org.netbeans.modules.cnd.debugger.common2.debugger.breakpoints.types.InstructionBreakpointType;
import org.netbeans.modules.cnd.utils.CndPathUtilities;

import org.netbeans.spi.debugger.ContextAwareSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;


/**
 * Native breakpoints.
 *
 * was: IpeBreakpointEvent
 */

public abstract class NativeBreakpoint
    extends org.netbeans.api.debugger.Breakpoint
    implements PropertyOwner, EditUndoable {

    static boolean enableDifferentiates = true;
    static private boolean skipSingleParent = true;
    static private boolean sessionOnly = true;
    static private boolean ghostBuster = ! manager().isPerTargetBpts();

    static private int nextSerialNo = 0;
    private int serialNo = nextSerialNo++;


    static boolean getSkipSingleParent() {
	return skipSingleParent;
    }

    static void toggleSkipSingleParent() {
	skipSingleParent = ! skipSingleParent;

	// Have the view pull the model.
	// 
	// not sure what the src for ModelEvent.TreeChanged should be
	// "just anything" seems to do just fine.
	// See http://www.netbeans.org/issues/show_bug.cgi?id=82955

	breakpointBag().breakpointUpdater().
	    modelChanged(new ModelEvent.TreeChanged(new Object()));
    }

    static boolean getSessionOnly() {
	return sessionOnly;
    }

    static void toggleSessionOnly() {
	sessionOnly = ! sessionOnly;

	// Have the view pull the model.
	breakpointBag().breakpointUpdater().
	    modelChanged(new ModelEvent.TreeChanged(new Object()));
    }

    static boolean getGhostBuster() {
	return ghostBuster;
    }

    static void toggleGhostBuster() {
	ghostBuster = ! ghostBuster;
    }

    private final NativeBreakpointType breakpointType;
    private ModelListener updater;

    // Keep track of calls to dispose() and do side-effects only once.
    // dispose() will get called on the default Delete action through
    // DebuggerManager.removeBreakpoint() and then when
    // _we_ call DebuggerManager.removeBreakpoint().

    private boolean disposed = false;


    private boolean isConditional = false;

    // Points back to original B when we makeEditableCopy()
    private NativeBreakpoint original;

    // remember to update timestamp when saving the bpt
    private boolean updateTimestamp = false;


    // Once we detect that a bpt is out of date wrt to srcfiles it's in
    // we mark it 'srcOutOfSync'. Once it's in this state we _don't_ 
    // update it's timestamp on saving otherwise starting and stopping the IDE
    // twice would mysteriously make the bpt not be OOD anymore.
    // 'srcOutOfSync' when the bpt is "repaired", i.e. error is set to null.

    private boolean srcOutOfSync = false;

    private Date timestamp = new Date(0);

    // The following properties are destined to become per-session
    private ArrayList<DebuggerAnnotation> annotations =
	new ArrayList<DebuggerAnnotation>();
    private int visitedAnnotationId = 0;	// to support GoToSource

    private final int flags;

    static final int RESTORED = (1<<0);

    public static final int TOPLEVEL = (1<<1);
    public static final int SUBBREAKPOINT = (1<<2);
    public static final int MIDBREAKPOINT = (1<<3);

    private boolean isRestored() {
	return (flags & RESTORED) == RESTORED;
    }

    public final boolean isToplevel() {
	return (flags & TOPLEVEL) == TOPLEVEL;
    }

    public final boolean isMidlevel() {
	return (flags & MIDBREAKPOINT) == MIDBREAKPOINT;
    }

    public final boolean isSubBreakpoint() {
	return (flags & SUBBREAKPOINT) == SUBBREAKPOINT;
    }

    protected NativeBreakpoint(NativeBreakpointType breakpointType, int flags) {
	this.breakpointType = breakpointType;
	this.flags = flags;

	// Only one of the flags is allowd to be turned on to the exclusion
	// of others
	assert isSubBreakpoint() ^ isMidlevel() ^ isToplevel();

	// create one for children too so getChildren() naturally
	// returns a 0-sized array
	children = new CopyOnWriteArrayList<NativeBreakpoint>();

	if (!isRestored())
	    updateTimestamp();	// record creation date

	// adjust properties that are not differentiating
	// TMP enabled.setDifferentiating(false);
	temp.setDifferentiating(false);
	adjusted.setDifferentiating(false);
	// TMP count.setDifferentiating(false);
	id.setDifferentiating(false);
    }


    private static NativeDebuggerManager manager() {
	return NativeDebuggerManager.get();
    } 

    protected static NativeDebugger currentDebugger() {
	return manager().currentNativeDebugger();
    }

    final boolean isCurrent() {
	return debugger != null &&
	       debugger == currentDebugger();
    }

    private static BreakpointBag breakpointBag() {
	return manager().breakpointBag();
    }

    private NativeDebugger debugger;

    private void setDebugger(NativeDebugger debugger) {
	// See comments in cleanup()
	assert !isToplevel() : "Cannot setDebugger() on toplevel bpt";
	this.debugger = debugger;
    }

    public NativeDebugger getDebugger() {
	if (isToplevel()) {
	    return null;
	} else {
	    return debugger;
	}
    }

    /**
     * Given a bpt at any level return the midlevel bpt associated 
     * with the current session.
     */
    private NativeBreakpoint findCurrent() {
	if (isToplevel()) {
	    for (NativeBreakpoint c : children) {
		if (c.getDebugger() == currentDebugger())
		    return c;
	    }
	    return null;

	} else if (isMidlevel()) {
	    if (getDebugger() == currentDebugger())
		return this;
	    else
		return null;
	} else {
	    return getParent().findCurrent();
	}
    }

    // interface org.netbeans.api.debugger.Breakpoint
    @Override
    public void enable() {
	if (NativeDebuggerManager.isPerTargetBpts()) {
	    NativeBreakpoint current = findCurrent();
	    if (current != null)
		current.setPropEnabled(true);
	} else {
	    setPropEnabled(true);
	}
    }

    // interface org.netbeans.api.debugger.Breakpoint
    @Override
    public void disable() {
	if (NativeDebuggerManager.isPerTargetBpts()) {
	    NativeBreakpoint current = findCurrent();
	    if (current != null)
		current.setPropEnabled(false);
	} else {
	    setPropEnabled(false);
	}
    }

    /* See below
    // interface org.netbeans.api.debugger.Breakpoint
    public boolean isEnabled() {
	assert false;
    }
    */

    void setDisposed(boolean disposed) {
	this.disposed = disposed;
    }

    // interface org.netbeans.api.debugger.Breakpoint
    @Override
    public void dispose() {
	// 6706848
	if (!SwingUtilities.isEventDispatchThread()) {
	    SwingUtilities.invokeLater(new Runnable() {
                @Override
		public void run() {
		    dispose();
		}
	    } );
	    return;
	}


	if (disposed)
	    return;
	disposed = true;

	postDelete(false, Gen.primary(this));
    }


    public final NativeBreakpointType getBreakpointType() {
	return breakpointType;
    } 


    final boolean isOfType(BreakpointType type) {
	// Compare names because we might have distinct instances of
	// the "same" BreakpointType (and that because cannot get
	// BreakpointType singletonizing to work yet.
	// SHOULD have BreakpointType.equals()
	String t1 = this.breakpointType.getTypeDisplayName();
	String t2 = type.getTypeDisplayName();
	return IpeUtils.sameString(t1, t2);
    }

    private boolean sameTypeAs(NativeBreakpoint that) {
	return isOfType(that.getBreakpointType());
    }



    /**
     * Return true if I am an only child.
     */
    boolean isOnlyChild() {
	assert !isToplevel() : "NB.isOnlyChild(): Cannot apply to toplevel bpt";

	return parent.nChildren() == 1;
    }

    public final boolean isEditable() {
	// Whether a bpt is editable is ultimately decided in
	// BreakpointFilter.CustomizeAction.perform()
	return original != null;
    } 

    public NativeBreakpoint original() {
	return original;
    }

    private Handler handler;

    public final void setHandler(Handler handler) {
	assert isSubBreakpoint() : "Setting a Handler for a Toplevel bpt";
	this.handler = handler;
	if (handler == null)
	    setId(0);
	else {
	    setId(handler.getId());
	    update();
	}

	// SHOULD we automatically remove the subBreakpoint?
    }

    public final Handler getHandler() {
	assert isSubBreakpoint() :
	       "Can only get Handlers for sub-bpts"; // NOI18N
	assert ! isEditable() :
	       "NB.getHandler(): disallowed for editable bpts"; // NOI18N
	return handler;
    } 


    private void echoUpdater(String who) {
	// For debugging 6568407
	/* DEBUG
	System.out.printf("%s(): update\n\t%s -->\n\t%s\n", who, this, updater);
	*/
    }

    private final CopyOnWriteArrayList<NativeBreakpoint> children;

    private NativeBreakpoint parent;

    private void setParent(NativeBreakpoint parent) {
	assert parent != null : "Can't set bpt parent to null";
	assert ! this.isToplevel();
	if (this.isMidlevel()) {
	    assert parent.isToplevel();
	} else {
	    assert parent.isMidlevel();
	}

	this.parent = parent;
	this.updater = parent.updater;
	this.echoUpdater("setParent"); // NOI18N
    }

    public NativeBreakpoint getParent() {
	return parent;
    }

    private StackHistory stackHistory =
	new org.netbeans.modules.cnd.debugger.common2.utils.StackHistory();

    /**
     * Return index of child corresponding to debugger.
     */

    /* OLD
    private final int findByDebugger(NativeDebugger debugger) {
	assert isToplevel();

	assert ! isEditable() :
	       "NB.findByDebugger(): disallowed for editable bpts";
	if (original != null)
	    return original.findByDebugger(debugger);

	if (debugger == null)
	    return -1;

	for (int bx = 0; bx < children.size(); bx++) {
	    NativeBreakpoint candidate = children.get(bx);
	    if (candidate.debugger == debugger)
		return bx;
	}
	return -1;
    }
    */

    public NativeBreakpoint getMidlevelFor(NativeDebugger debugger) {
	assert isToplevel();

	assert ! isEditable() :
	       "NB.setMidlevelFor(): disallowed for editable bpts"; // NOI18N
	if (original != null)
	    return original.getMidlevelFor(debugger);

	if (debugger == null)
	    return null;

	for (NativeBreakpoint candidate : children) {
	    if (candidate.debugger == debugger)
		return candidate;
	}
	return null;
    }

    private int findByBreakpoint(NativeBreakpoint subBpt) {
	// Vector.indexOf uses equals() which is overkill for us
        int bx = 0;
	for (NativeBreakpoint candidate : children) {
	    if (candidate == subBpt)
		return bx;
            bx++;
	}
	return -1;
    }


    /**
     * Remove 'child' from 'this'.
     *
     * @param debugger
     * Passed in to double-check if the removed breakpoint truly belongs to
     * the given debugger. May be null.
     */

    private void removeChild(NativeBreakpoint child,
				  NativeDebugger debugger) {
	assert isToplevel() || isMidlevel();
	assert child != null : "removeChild(): null child";
	if (isToplevel()) {
	    assert child.isMidlevel();
	    assert child.getDebugger() == debugger :
		   "removeChild(): " + // NOI18N
		   "child not associated with debugger or removed twice"; // NOI18N
	} else {
	    assert child.isSubBreakpoint();
	}

	int bx = findByBreakpoint(child);
	NativeBreakpoint removed = children.remove(bx);
	assert removed != null :
	       "removeChild(): " + // NOI18N
	       "No bpt under this debugger " + debugger + "\n" + // NOI18N
	       "last removal: " + stackHistory.toString() // NOI18N
	       ;
	assert removed == child :
	       "removeChild(): " + // NOI18N
	       "bpt under debugger different from one being removed.\n" + // NOI18N
	       "last removal: " + stackHistory.toString() // NOI18N
	       ;

	if (isToplevel()) {
	    child.setDebugger(null);

	    // Don't recalculate enabledness ...
	    // If we do that then a single child will not be differentiated 
	    // from it's parent and we won't be able to persists context
	    // specific disablings.
	    // setEnabled(recalculateIsEnabled());
	}

	child.removeAnnotations();

	updateAndParent();
    } 

    /**
     * Used for recovering from errors in per-debug-target bpts.
     */
    public void removeOnlyChild() {
	assert this.isToplevel() : 
	       "NB.removeOnlyChild(): not a top-level bpt"; // NOI18N
	assert this.nChildren() == 1 : 
	       "NB.removeOnlyChild(): has " + // NOI18N
	       nChildren() +
	       " children instead of 1"; // NOI18N
	NativeBreakpoint removed = children.remove(0);
	removed.setDebugger(null);
	removed.removeAnnotations();
    }

    public final void unbind() {
	assert isMidlevel();

	// We don't null the context ... we become a ghost
	for (NativeBreakpoint c : children) {
	    c.setDebugger(null);
	    c.setHandler(null);
	    c.update();
	}
	this.setDebugger(null);
	this.update();
    }

    public void bindTo(NativeDebugger debugger) {
	assert isSubBreakpoint() || isMidlevel();

	assert getDebugger() == null || getDebugger() == debugger :
	       "NativeBreakpoint.bindTo(): already have a debugger"; // NOI18N
	final String executable = debugger.session().getTarget();
	final String hostname = debugger.session().getSessionHost();
	setContext(new Context(executable, hostname));
	setDebugger(debugger);

	// OLD getParent().setEnabled(getParent().recalculateIsEnabled());
	updateAndParent();
    }



    public final void addSubBreakpoint(NativeBreakpoint subBreakpoint) {
	assert this.isMidlevel();
	assert !isEditable() :
	       "addSubBreakpoint(): cannot add to editable bpt"; // NOI18N
	assert subBreakpoint != null :
	       "addSubBreakpoint(): null subBreakpoint"; // NOI18N

	assert subBreakpoint.isSubBreakpoint();

	deletingChildren = false;

	// inherit parents context
	subBreakpoint.setContext(this.getContext());
	subBreakpoint.setParent(this);
	subBreakpoint.setDebugger(debugger);

	children.add(subBreakpoint);

	setEnabled(recalculateIsEnabled());

	subBreakpoint.updateAndParent();
    } 

    public final void setMidBreakpointFor(NativeBreakpoint midBreakpoint,
					  NativeDebugger debugger) {
	assert isToplevel();
	assert !isEditable() :
	       "setMidBreakpointFor(): cannot add to editable bpt"; // NOI18N
	assert midBreakpoint != null :
	       "setMidBreakpointFor(): null midBreakpoint"; // NOI18N
	assert debugger != null :
	       "setMidBreakpointFor(): null debugger key"; // NOI18N

	assert midBreakpoint.isMidlevel();
	assert midBreakpoint.debugger == null :
	       "setMidBreakpointFor(): " + // NOI18N
	       "mid-bpt already associated with " + midBreakpoint.debugger; // NOI18N

	deletingChildren = false;

	final String executable = debugger.session().getTarget();
	final String hostname = debugger.session().getSessionHost();
	midBreakpoint.setContext(new Context(executable, hostname));
	midBreakpoint.setParent(this);
	midBreakpoint.setDebugger(debugger);

	// SHOULD assert that no other midBreakpoints exist for this debugger
	children.add(midBreakpoint);

	setEnabled(recalculateIsEnabled());

	midBreakpoint.updateAndParent();
    } 

    public final int nChildren() {
	return children.size();
    }

    /**
     * Return number of children which are associated with a debugger.
     */
    public final int nBoundChildren() {
	int nBound = 0;
	for (NativeBreakpoint b : children) {
	    if (b.isBound())
		nBound++;
	}
	return nBound;
    }


    public final NativeBreakpoint[] getChildren() {
	return children.toArray(new NativeBreakpoint[0]);
    }

    /**
     OLD:
     * Find a _unique_ sub-bpt in this which is associated with the
     * given context.
     *
     * If more than one match is found, null is returned.
     * Perhaps SHOULD return an array so client can decide what to do with
     * multiple matches?
     */

    public List<NativeBreakpoint> findByContext(Context context) {
	assert isToplevel();

	ArrayList<NativeBreakpoint> matches = new ArrayList<NativeBreakpoint>();
	for (NativeBreakpoint candidate : getChildren()) {
	    if (candidate.matches(context)) {
		if (contains(matches, candidate))
		    continue;
		matches.add(candidate);
	    }
	}
	return matches;
    }

    private boolean matches(Context context) {
	assert this.isMidlevel();

	Context thisContext = (Context) this.context.getAsObject();
	return thisContext.matches(context);
    }

    /**
     * @return return true if 'list' contains a breakpoint that
     * matches 'b' according to sibling matching rules.
     */

    private boolean contains(List<NativeBreakpoint> list, NativeBreakpoint b) {
	for (NativeBreakpoint c : list) {
	    if (b.matchesSibling(c))
		return true;
	}
	return false;
    }


    /**
     * Return true if this mid-bpt is unique.
     * Such bpts are good candidates for deletion on session-exit.
     *
     * Definition of unique:
     * It's different enough from the top-level breakpoint that it cannot be
     * re-created from it.
     *
     * OLD Definition of unique:
     * Suitably differentiated from all it's other siblings.
     * Only-childs are implicitly unique.
     */

    public boolean isUnique() {
	if (Log.Bpt.ghostbuster)
	    System.out.printf("\nConsidering if %s is unique\n", this); // NOI18N
	assert isMidlevel();
	NativeBreakpoint top = getParent();

	if (isAdjusted()) {
	    if (Log.Bpt.ghostbuster)
		System.out.printf("\tIt required intervention. Keep\n"); // NOI18N
	    return true;

	} else if (ghostBuster && this.matchesTemplate(top)) {
	    if (Log.Bpt.ghostbuster)
		System.out.printf("\tIt matches the template. Bust it!\n"); // NOI18N
	    return false;

	} else {

	    // See if it has matching siblings
	    for (NativeBreakpoint sibling : top.children) {
		if (sibling == this)
		    continue;
		if (this.matchesSibling(sibling)) {
		    if (Log.Bpt.ghostbuster)
			System.out.printf("\tIt different from the template but matches a sibling. Bust it!\n"); // NOI18N
		    return false;
		}
	    }
	    if (Log.Bpt.ghostbuster)
		System.out.printf("\tIt's different from the template and siblings. Keep\n"); // NOI18N
	    return true;
	}
    }

    boolean isUniqueLite() {
	if (Log.Bpt.ghostbuster)
	    System.out.printf("\nConsidering (lite) if %s is unique\n", this); // NOI18N
	assert isMidlevel();
	NativeBreakpoint top = getParent();

	if (isAdjusted()) {
	    if (Log.Bpt.ghostbuster)
		System.out.printf("\tIt required intervention. Keep\n"); // NOI18N
	    return true;
	} else if (this.matchesTemplate(top)) {
	    if (Log.Bpt.ghostbuster)
		System.out.printf("\tIt matches the template. Bust it!\n"); // NOI18N
	    return false;
	} else {
	    if (Log.Bpt.ghostbuster)
		System.out.printf("\tIt's different from the template. Keep\n"); // NOI18N
	    return true;
	}
    }


    /**
     * Return true if two sub-bpts are similar enough that one of them 
     * can be tossed out.
     */
    private boolean matchesSibling(NativeBreakpoint that) {


	// Cannot get BreakpointType singletonizing to work yet, so use
	// type name instead for now.
	assert sameTypeAs(that) :
	       "mismatched types: \n" + // NOI18N
		"\tthis: " + this.getBreakpointType() + "\n" + // NOI18N
		"\tthat: " + that.getBreakpointType(); // NOI18N

	if (this.isMidlevel()) {
	    assert that.isMidlevel();
	    assert this.getParent() == that.getParent();

	    // recursively match overloaded children

	    Iterator<NativeBreakpoint> thisIter = this.children.iterator();
	    Iterator<NativeBreakpoint> thatIter = that.children.iterator();
	    while (true) {
		if (thisIter.hasNext() && thatIter.hasNext()) {
		    NativeBreakpoint thisNB = thisIter.next();
		    NativeBreakpoint thatNB = thatIter.next();
		    if (!thisNB.matchesSibling(thatNB))
			return false;
		} else if (thisIter.hasNext() || thatIter.hasNext()) {
		    // mismatched nChildren
		    // 6791860
		    // Can't reproduce so putting out a warning to help
		    // identify situations where this happens.
		    DbgGuiModule.logger.log(Level.WARNING,
			String.format("matchesSibling(): mismatched children\n" + // NOI18N
				      "\tthis: %s\n" + // NOI18N
				      "\tthat: %s\n", this, that)); // NOI18N

		    return false;
		} else {
		    // we've reached the end
		    break;
		}
	    }
	    return true;
	} else {
	    assert that.isSubBreakpoint();
	    assert this.getParent().getParent() == that.getParent().getParent();
	}


	return this.equals(that, new PropertyOwner.Comparator() {
            @Override
	    public boolean equals(Property thisP, Property thatP) {
		if (enableDifferentiates && thisP == enabled) {
		} else if (ghostBuster && thisP == context) {
		    return true;
		} else {
		    if (!thisP.isDifferentiating())
			return true;
		}

		if (! thisP.matches(thatP)) {
		    if (Log.Bpt.hierarchy) {
			System.out.println("mismatched properties against sibling: "); // NOI18N
			System.out.println("\tthis " + thisP.name() + ": " + thisP); // NOI18N
			System.out.println("\tthat " + thatP.name() + ": " + thatP); // NOI18N
		    }
		    return false;
		} else {
		    return true;
		}
	    }
	} );
    }


    /**
     * @return returns true if 'p' is a qualified property.
     */
    private boolean isQualified(Property p) {
	if (p.name().startsWith("q")) // NOI18N
	    return true;
	else
	    return false;
    }

    /**
     * @return returns true if 'p' is a defining property.
     */

    private boolean isDefining(Property p) {
	if (p == context)
	    return false;
	if (p == whileIn)
	    return false;
	if (p == qwhileIn)
	    return false;
	if (p == condition)
	    return false;
	if (p == qcondition)
	    return false;
	if (p == lwp)
	    return false;
	if (p == thread)
	    return false;
	if (p == count)
	    return false;
	if (p == temp)
	    return false;
	if (p == countLimit)
	    return false;
	if (p == action)
	    return false;
	if (p == script)
	    return false;
	if (p == java)
	    return false;
	if (p == enabled)
	    return false;

	if (p == id)
	    return false;

	return true;
    }


    /**
     * @return true if properties which are defining properties we changed.
     * Note that if additional non-defining properties were changed we will
     * still return true.
     */

    public boolean isChangeInDefiningProperty() {

	assert isEditable() :
	       "isChangeInDefiningProperty() applied to non edited bpt"; // NOI18N
	for (Property p : pos) {
	    if (p.isDirty() && isDefining(p))
		return true;
	}
	return false;
    }

    /**
     * Return true if 'this' matches toplevel 'that' enough for it to
     * be instantaited in a new session.
     */

    private boolean matchesTemplate(NativeBreakpoint that) {
	assert that.isToplevel();

	if (isMidlevel())
	    assert getParent() == that;
	else 
	    assert getParent().getParent() == that;

	// For overloaded bpts recurse ...
	if (isMidlevel()) {
	    for (NativeBreakpoint c : children) {
		if (!c.matchesTemplate(that))
		    return false;
	    }
	    return true;
	}


	// Cannot get BreakpointType singletonizing to work yet, so use
	// type name instead for now.
	assert sameTypeAs(that) :
	       "mismatched types: \n" + // NOI18N
		"\tthis: " + this.getBreakpointType() + "\n" + // NOI18N
		"\tthat: " + that.getBreakpointType(); // NOI18N

	return this.equals(that, new PropertyOwner.Comparator() {
            @Override
	    public boolean equals(Property thisP, Property thatP) {
		if (enableDifferentiates && thisP == enabled) {
		} else if (thisP == context) {
		    // context is not differentiating when comparing against
		    // template
		    return true;
		} else if (isQualified(thisP)) {
		    // do not differentiate based on qualified properties
		    return true;
		} else if (isDefining(thisP)) {
		    // do not differentiate based on defining properties
		    return true;
		} else {
		    if (!thisP.isDifferentiating())
			return true;
		}

		if (! thisP.matches(thatP)) {
		    if (Log.Bpt.hierarchy) {
			System.out.println("mismatched properties against template: "); // NOI18N
			System.out.println("\tthis " + thisP.name() + ": " + thisP); // NOI18N
			System.out.println("\tthat " + thatP.name() + ": " + thatP); // NOI18N
		    }
		    return false;
		} else {
		    return true;
		}
	    }
	} );
    }

    /**
     * Return true if the bpt has a corresponding engine handler.
     *
     * In general a bpt will always have a corresponding local Handler except
     * during transient times so we're obviously not bound if we don't have 
     * a local Handler.
     */

    // Would've made it private except that BB uses it.
    final public boolean hasHandler() {
	assert ! isEditable() :
	       "NB.hasHandler(): disallowed for editable bpts"; // NOI18N
	if (original != null)
	    return original.hasHandler();

	assert isSubBreakpoint() : "Can only ask hasHandler() of subbpts";

	if (getHandler() == null) {
	    return false;

	} else if (getHandler().getId() == 0) {

	    /* OLD
	    // This is a bit of an ambiguous case and we really should
	    // get rid of it. Consider that our callers usually want
	    // to do this:
	    // 
	    // if (hasHandler)
	    //      cope with handler
	    // else
	    //      assume no handler
	    //
	    // Hence, the assert.
	    // 
	    // But it's still confused ... do we mean class Handler here
	    // or 'handler' in the engine? (We have a getHandler() but id of 0
	    // signifies that there's no handler in dbx/break in gdb)
	    // Postponing this decision until the time when we decide
	    // whether sub-bpts and Handlers should be merged.
	    // 
	    // This confusion makes for irregular code. Consider how we
	    // check for isBroken() in setPropEnabled() vs
	    // NativeDebugger.postDeleteHandler().
	    */

	    /* TMP
	    if (!isBroken()) {
		assert false :
		       "hasHandler() Handler with 0 id";
	    }
	    */
	    // OLD return true;
	    return false;

	} else {
	    return true;
	}
    } 

    /**
     * Eliminate any mid-level bpts which are not associated with any
     * context in {contexts}.
     * If 'contexts' is null eliminate them all. This is to support 
     * DebuggerOption.SAVE_BREAKPOINTS=false.
     * This method is the workhorse of BB.cleanupBpts().
     */
    void discardUnused(Set<Context> contexts) {
	assert this.isToplevel();

	if (nChildren() == 0) {
	    // We get such (childless toplevel) bpts in non-pertarget mode
	    // due to ghost-busting. Eliminate any such in pertarget mode.
	    this.primDelete(false, Gen.primary(null));
	    return;
	}

	for (NativeBreakpoint c : getChildren()) {
	    if (Log.Bpt.pertarget) {
		System.out.printf("\tIs '%s'/'%s' in set?\n", // NOI18N
		    c, c.getContext());
	    }

	    if ( contexts == null || ! contexts.contains(c.getContext())) {

		// cull 'c'

		if (c.isBound()) {
		    // This one we'll get when BB.cleanupBpts is called
		    // on session exit.
		    if (Log.Bpt.pertarget)
			System.out.printf("\tno -- but it's bound\n"); // NOI18N

		} else {
		    if (Log.Bpt.pertarget)
			System.out.printf("\tno -- deleting\n"); // NOI18N

		    boolean keepParent = false;

		    // A null 'origin' defeats the special casing of "deletion
		    // of only-child ghosts w/o deleting their parents." in
		    // primDelete.
		    NativeBreakpoint origin = null;

		    c.primDelete(keepParent, Gen.primary(origin));
		}
	    }
	}
    }

    public boolean isBound() {
	return getDebugger() != null;
    }

    public String getError() {
	if (srcOutOfSync)
	    return "File newer than breakpoint (modified outside of IDE?)"; // NOI18N

	if (isToplevel()) {
	    return null;
	} else if (isMidlevel()) {
	    return null;
	} else {
	    if (getHandler() != null)
		return getHandler().getError();
	    else
		return null;
	}
    } 

    public boolean isBroken() {
	if (isToplevel()) {
	    // return true if the child which is in the current session
	    // is broken
	    for (NativeBreakpoint child : getChildren()) {
		if (child.getDebugger() == null)
		    continue;
		if (child.isCurrent() && child.isBroken())
		    return true;
	    }
	    return false;

	} else if (isMidlevel()) {
	    // return true if any child is broken
	    for (NativeBreakpoint child : getChildren()) {
		if (child.isBroken())
		    return true;
	    }
	    return false;

	} else {
	    return getError() != null;
	}

	/* LATER
	if (nChildren() == 0) {
	    return getError() != null;
	} else {
	    // return true if the child which is in the current session
	    // is broken
	    // This doesn't quite work correctly for mid-level bpts
	    // but that shouldn't show up when masquareding.
	    for (NativeBreakpoint child : getChildren()) {
		if (child.isCurrent() && child.isBroken())
		    return true;
	    }
	    return false;
	}
	*/
    }

    private boolean isFired() {
	if (isToplevel()) {
	    // return true if a child that has fired is in the current session
	    for (NativeBreakpoint child : getChildren()) {
		if (child.getDebugger() == null)
		    continue;
		if (child.isFired() && child.isCurrent())
		    return true;
	    }
	    return false;

	} else if (isMidlevel()) {
	    // return true if any child has fired
	    for (NativeBreakpoint child : getChildren()) {
		if (child.isFired())
		    return true;
	    }
	    return false;

	} else {
	    // subBreakpoint
	    if (getHandler() != null) {
		// For now don't temper child firedness with the current session
		// This means that it will show up as fired even for non-current
		// sessions. This was confusing when I was showing it to ChihIn.
		return getHandler().isFired();

	    } else {
		return false;
	    }
	}
    }




    public void setUpdater(ModelListener updater) {
	// See comments in cleanup()
	this.updater = updater;
	this.echoUpdater("setUpdater"); // NOI18N
    }

    public Date timestamp() {
	return timestamp;
    }

    void restoreTimestamp(Date newDate) {
	timestamp = (Date) newDate.clone();
    }

    /**
     * Remember to update timestamp when saving the bpt.
     */
    private void updateTimestamp() {
	if (!srcOutOfSync)
	    updateTimestamp = true;
    }


    /**
     * Call right before encoding into persistent storage.
     */
    void prepareForSaving() {
	if (updateTimestamp) {
	    Date now = new Date();
	    timestamp = now;
	}
    }

    /**
     * Call right after decoding from persistent storage.
     * Usually called from BreakpointBag or restoreChild().
     */
    void restoredChild() {
	checkOutofDate(timestamp());
	updateAnnotations();
    }

    /**
     * Add a restored bpt to this.
     */

    void restoringChild(NativeBreakpoint child) {
        assert this.isRestored();
        assert child.isRestored();
        assert this.isToplevel() || this.isMidlevel();

	if (this.isToplevel()) {
	    assert child.isMidlevel();
	    child.setDebugger(null);
	} else {
	    assert child.isSubBreakpoint();
	    child.setDebugger(null);
	}

        child.setParent(this);
        children.add(child);
        // OLD restoredChild();
    }

    public void changeOne(NativeBreakpoint edited, Gen gen) {

	if (! edited.isChangeInDefiningProperty()) {

	    // The following ensures that qualified versions of properties
	    // which haven't been edited are present in 'edited'
	    // The 'copyFrom' is there so that the level origin of 
	    // edited isn't perturbed as we base spreading decisions on it.

	    NativeBreakpoint template = this.makeEditableCopy();
	    template.original = edited;
	    template.pos.assign(edited.pos);
	    edited.copyFrom(template);
	}

	if (this.isToplevel()) {
	    this.copyFrom(edited);
	    update();
	    NativeDebuggerManager.get().bringDownDialog();

	} else if (this.isMidlevel()) {
	    this.copyFrom(edited);
	    updateAndParent();
	    NativeDebuggerManager.get().bringDownDialog();

	} else {
	    if (this.isBound()) {
		// This will come back via
		// org.netbeans.modules.cnd.debugger.common2.debugger.
		//	DbxDebuggerImpl.replaceHandler
		// Or 
		//	NativeDebuggerImpl.noteBreakpointError
		Handler.postChange(this.getDebugger(), this, edited, gen);
	    } else {
		this.copyFrom(edited);
		updateAndParent();
		NativeDebuggerManager.get().bringDownDialog();
	    }
	}
    }

    private static enum Spread {
	SELF,		// only change self (only when editing subbpts)
	OVERLOAD,	// change siblings of overloaded bpt
	ALL		// change siblings in other sessions (aka cousins).
    };

    /**
     * A change described by 'edited' (any of toplevel, midlevel or subbpt)
     * has been validated and applied to 'validated' (a child of this).
     * Now spread it to other family members.
     */
    public void spreadChange(NativeBreakpoint validated,
		             NativeBreakpoint edited,
		             Gen gen) {

	if (Log.Bpt.pathway) {
	    System.out.printf("spread(%s)\n" + // NOI18N
			      "\tthis    %s\n" + // NOI18N
			      "\tvalid   %s\n" + // NOI18N
			      "\tedited  %s\n", // NOI18N
		gen, this, validated, edited);
	}

	assert isMidlevel() :
	       "spread() can only be applied to midlevel bpts"; // NOI18N
	assert validated.isSubBreakpoint() :
	       "spread(): validated is not a subbpt"; // NOI18N
	assert validated.parent == this :
	       "spread():  this is not the parent of validated"; // NOI18N


	//
	// Figure how we want to spread the change
	//

	Spread spread;

	if (edited.original.isSubBreakpoint()) {
	    if (edited.isChangeInDefiningProperty()) {
		spread = Spread.ALL;
	    } else {
		spread = Spread.SELF;
	    }

	} else if (edited.original.isMidlevel()) {
	    if (edited.isChangeInDefiningProperty()) {
		spread = Spread.ALL;
	    } else {
		spread = Spread.OVERLOAD;
	    }

	} else {
	    // We originally edited a toplevel bpt
	    spread = Spread.ALL;
	}


	//
	// Do the spreading
	//
	switch (spread) {
	    case SELF:
		// See EditBreakpointPanel.switchTo()
		/* TMP
		if (!isOnlyChild()) {
		    assert ! edited.isChangeInDefiningProperty() :
			   "forbidden defining property change";
		}
		*/

		if (nChildren() == 1) {
		    // mid-level to reflect unique child
		    changeOne(edited, gen);
		    if (isOnlyChild()) {
			parent.changeOne(edited, gen);
		    }
		} else {
		    // child is now differenatiated
		}
		break;

	    case OVERLOAD:
		if (gen.isTertiary())
		    return;

		// commit self
		changeOne(edited, gen);
		if (isOnlyChild())
		    parent.changeOne(edited, gen);

		// spread to siblings of validated
		// no-op if not overloaded
		changeAllButTo(validated, edited, gen);

		break;

	    case ALL:
		if (edited.isChangeInDefiningProperty())
		    clearOldOverloaded(gen);

		if (gen.isTertiary())
		    return;

		// commit self
		changeOne(edited, gen);

		if (! edited.isChangeInDefiningProperty()) {
		    // spread to siblings of validated
		    // no-op if not overloaded
		    changeAllButTo(validated, edited, gen);
		}

		if (gen.isSecondary())
		    return;

		// commit toplevel parent
		parent.changeOne(edited, gen);

		// spread to my siblings
		parent.changeAllButTo(this, edited, gen);
		break;
	}
    }

    /**
     * Called when properties have been modified by the dialog as well as the
     * property sheet or the table cell editor.
     */

    final void postChange(NativeBreakpoint edited, Gen gen) {
	assert edited.isEditable() :
	       "NB.postChange(): passed in bpt isn't editable"; // NOI18N
	assert edited.isDirty() :
	       "NativeBreakpoint.postChange(): passed in bpt isn't dirty!"; // NOI18N

	if (Log.Bpt.pathway) {
	    System.out.printf("postChange(%s):\n" + // NOI18N
			      "\tthis   %s\n" + // NOI18N
			      "\tedited %s\n", // NOI18N
		gen, this, edited);
	}

	if (isToplevel()) {
	    if (nChildren() == 0) {
		changeOne(edited, gen);
		return;
	    } 

	    // pick a representative mid-level bpt.
	    NativeBreakpoint rep = getMidlevelFor(currentDebugger());
	    if (rep == null)
		rep = children.get(0);
	    rep.postChange(edited, gen);

	} else if (isMidlevel()) {
	    // We're either ...
	    //							    	gen usr
	    // A representative picked when editing a top-level bpt	1   F
	    // Called after validating the representative bpt		2   F
	    // Being edited directly					1   T
	    // Called from changeAllButTo (called from us)		2   F


	    // Pick a representative sub-bpt
	    // This works for both simple and overloaded bpts
	    NativeBreakpoint b = children.get(0);
	    b.changeOne(edited, gen);

	    if (isBound()) {
		// Will come around and call spread()
	    } else { 
		// Call spread now
		spreadChange(b, edited, gen);
	    }

	} else {
	    assert edited.original.isSubBreakpoint() :
		   "postChange called recursively on subbpt"; // NOI18N

	    this.changeOne(edited, gen);

	    if (isBound()) {
		// Will come around and call our parents spread()
	    } else { 
		// Call spread now
		parent.spreadChange(this, edited, gen);
	    }
	}
    }


    /**
     * Apply changes in 'edited' to all children of 'this' except 'exclude'.
     * Helper for spreadChange().
     */

    private void changeAllButTo(NativeBreakpoint exclude,
			       NativeBreakpoint edited,
			       Gen gen) {
	if (Log.Bpt.pathway) {
	    System.out.printf("changeAllButTo(%s)\n" + // NOI18N
			      "\tthis    %s\n" + // NOI18N
			      "\texclude %s\n" + // NOI18N
			      "\tedited  %s\n", // NOI18N
		gen, this, exclude, edited);
	}

	if (isToplevel()) {
	    for (NativeBreakpoint child : children) {
		if (child == exclude)
		    continue;
		child.postChange(edited, gen.second());
	    }

	} else if (isMidlevel())  {
	    for (NativeBreakpoint child : children) {
		if (child == exclude)
		    continue;
		child.changeOne(edited, gen.third());
	    }

	} else {
	    assert false : "Cannot apply changeAllButTo to subbpt";
	}
    }


    /**
     * Eliminate all but the first child.
     */

    private void clearOldOverloaded(Gen gen) {
	if (Log.Bpt.pathway) {
	    System.out.printf("clearOldOverloaded():\n\tthis %s\n", this); // NOI18N
	}
	assert isMidlevel();
	NativeBreakpoint sbs[] = getChildren();	// copy
	for (int bx = 1; bx < sbs.length; bx++) {
	    NativeBreakpoint b = sbs[bx];
	    b.postDelete(false, gen.third());
	}
    }

    /**
     * Called when we want to realize the breakpoint as a dbx handler/gdb break
     */
    public final void postCreate() {
	Handler.postNewHandler(currentDebugger(), this, getRoutingToken());
    }


    // interface EditUndoable
    @Override
    public void undo(String property) {
	 // re-pull the whole line instead of just the cell.
	 update();
    }

    /**
     * Get the gui to pull ... 
     */
    public void update() {
	if (isEditable())
	    return;
	updateAnnotations();
	if (updater == null)
	    return;

	// triggers a pull of ...
	
	// ... icon+Summary+children
	updater.modelChanged(new ModelEvent.NodeChanged(this, this));

	// ... individual columns via BreakpointFilter.getValueAt
	for (Property p : pos) {
	    if (p == enabled)
		continue;
            // do not fire updates of disabled columns (IZ 186750)
            if (p == java || p == lwp || p == temp) {
                continue;
            }
	    if (p.key() != null /* LATER && p.isDirty() */) {
		ModelEvent e =
		    new ModelEvent.TableValueChanged(this, this, p.key());
		updater.modelChanged(e);
	    }
	}
    }

    /**
     * Get the gui to pull ... us as well as our parent.
     */
    void updateAndParent() {
	if (isEditable())
	    return;
	update();
	if (getParent() != null) {
	    getParent().updateAndParent();
	} else {
	    if (updater == null)
		return;
	
	    // triggers a pull of the whole tree so we get to pulling up
	    // of only children.
	    updater.modelChanged(new ModelEvent.TreeChanged(this));
	}
    }

    // 'deletingChildren' helps control race conditions arising from
    // combination of recursion and spreading involving postDelete().
    //
    // scanario 1:
    // postDelete() on topLevel bpt
    //		it iterates through children
    //		each child sends to engine and gets back an ack which it
    //		spreads via postDelete(), but deletingChildren stops it.
    //
    // scenario 2:
    // postDelete() on sub-bpt via spontaneous message from debugger engine.
    //		removes itself
    //		spreads via postDelete() which takes us to scenario 1 minus
    //		the already deleted sub-bpt.
    //		
    // scenario 3:
    // postDelete() on sub-bpt via user action in GUI
    //		sends to engine and gets back an ack.
    //		goto scenario 2.
    //
    // It's important that we _reset_ 'deletingChildren'. Ideally when
    // the last child goes away, but for now we reset it when we add sub-bpts
    // in setSubBreakpointFor().

    private boolean deletingChildren = false;


    /**
     * Request that this bpt be deleted.
     * This is the main deletion workhorse.
     *
     * @param gen
     *
     * @param keepParent
     * Normally when the last child is removed the parent is also removed.
     * If keepParent is true this policy is defeated.
     * <br>
     * Seems to be only true for an esoteric case in postChange+overloaded.
     */

    public void postDelete(boolean keepParent, Gen gen) {

	// We come here for both primary and secondary Gen
	// If we come from dispose() primary and spread are true
	// If we come from direct recursion to us secondary is true
	// If we come from indirect recursion secondary is true

	if (Log.Bpt.pathway) {
	    System.out.printf("postDelete(%s, %s):\n\tthis %s\n", // NOI18N
		keepParent, gen, this);
	}
	if (isToplevel()) {
	    assert keepParent == false :
		   "NB.postDelete(): " + // NOI18N
		   "keepParent doesn't make sense for toplevel"; // NOI18N
	    if (nChildren() == 0) {
		// virgin bpt
		assert deletingChildren == false;
		primDelete(keepParent, gen);

	    } else {
		// See big comment above
		if (deletingChildren) {
		    if (Log.Bpt.pathway) {
			System.out.printf("postDelete(top): deletingChildren = true\n"); // NOI18N
		    }
		    return;
		}
		deletingChildren = true;

		for (NativeBreakpoint c : getChildren())
		    c.postDelete(false, gen);
	    }

	} else if (isMidlevel()) {
	    if (nChildren() == 0) {
		// midlevel bpts are supposed to have children but ocasionally,
		// because of a previous errors, they might not.
		assert deletingChildren == false;
		primDelete(keepParent, gen);

	    } else {
		// See big comment above
		if (deletingChildren) {
		    if (Log.Bpt.pathway) {
			System.out.printf("postDelete(mid): deletingChildren = true\n"); // NOI18N
		    }
		    return;
		}
		deletingChildren = true;

		for (NativeBreakpoint c : getChildren())
		    c.postDelete(false, gen);
	    }

	} else {
	    if (isBound()) {
		getDebugger().bm().postDeleteHandler(this, gen);
		// we come back to NativeDebugger.deleteHandler()
	    } else {
		primDelete(keepParent, gen);
	    }
	}
    }

    public void primDelete(boolean keepParent, Gen gen) {

	cleanup();

	if (isSubBreakpoint()) {

	    // Consider editing the list of overloaded breakpoints as a
	    // kind of intervention.

	    if (parent.nChildren() > 1)
		parent.setAdjusted(true);


	    // remove self from parent
	    parent.removeChild(this, getDebugger());

	    if (!keepParent) {
		if (parent.nChildren() == 0)
		    parent.primDelete(keepParent, gen);
	    }

	} else if (isMidlevel()) {
            if (isEnabled()) {//if it is disabled do not remove annotation
                removeAnnotations();
            }

	    // remember state from before removeChild()
	    boolean isOnlyChild = isOnlyChild();
	    boolean isBound = isBound();

	    // remove self from parent
	    parent.removeChild(this, getDebugger());


	    // Allow deletion of only-child ghosts w/o deleting their parents.
	    // The gen tests ensures that if the deletion was initiated from
	    // the parent we don't short-circuit.

	    if (isOnlyChild &&
		!isBound &&
		(gen.origin() == this || (gen.origin() != null &&
					  gen.origin().getParent() == this)) ) {
		return;
	    }

	    if (! isOnlyChild && NativeDebuggerManager.isPerTargetBpts()) {
		// Don't spread bpt deletion
		return;
	    }

	    if (!keepParent) {
		if (parent.nChildren() == 0) {
		    parent.primDelete(keepParent, gen);
		} else {
		    if (gen.isPrimary() && isBound)
			parent.postDelete(false, gen.second());
		}
	    }

	} else if (isToplevel()) {
            if (isEnabled()) {//if it is disabled do not remove annotation
                removeAnnotations();
            }

	    // no parent to remove self from
	    breakpointBag().remove(this);
	}
    }

    /**
     * Free resources associated with this Bpt
     */
    public void cleanup() {
	// OLD 6502483, 6521943 removeAnnotations();

	// OLD 6700641
	// In the global bpts regime the updater is set to always be
	// the singleton DebuggerManagers updater, so there's no point
	// clearing it especially since (as 6700641 demonstrated) we have no
	// reliable way of restoring it.

	// setUpdater(null)

	// If we ever go to non-global bpts this solution won't do especially
	// if debugger will continue to have it's own updater. In that case,
	// have setDebugger() set the bpt updater to the debuggers updater
	// or to the debugger managers if the debugger is being set to null.
    }


    public DebuggerAnnotation[] annotations() {
	return annotations.toArray(new DebuggerAnnotation[annotations.size()]);
    }

    /**
     * Register a new bpt glyph with this Handler
     */

    public void addAnnotation(final String filename, final int line, final long addr) {
        NativeDebuggerManager.getRequestProcessor().submit(new Runnable() {
            @Override
            public void run() {
                final Line l = getLine(filename, line);
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        addAnnotation(l, addr);
                    }
                });
            }
        });
    }
    
    protected Line getLine(String filename, int line) {
        if (line != 0) {
            return EditorBridge.getLine(filename, line, currentDebugger());
        }
        return null;
    }

    /**
     * Register a new annotation with this bpt.
     */
    public void addAnnotation(Line l, long addr) {
	assert ! isMidlevel() : "cannot add annotations to midlevel bpts";

	DebuggerAnnotation.Listener listener =
	    new DebuggerAnnotation.Listener() {
                @Override
		public void annotationMoved() {
		    update();
		    updateTimestamp();
		}
	    };
	DebuggerAnnotation a =
	    new DebuggerAnnotation(listener,
				   getAnnotationType(),
				   l,
				   addr,
				   true,
                                   this);

	/* OLD
	6678347
	if (addr != 0) {
	    annotations.add(a);
	    a.addInstBpt(this);
	}

	if (l != null)
	    annotations.add(a);
	*/

	if (addr != 0 || l != null) {
	    annotations.add(a);
	    if (addr != 0)
		a.addInstBpt(this);
	}

	// DebuggerAnnotation constructor will show annotations by default.
	// Adjust ....

	if (currentDebugger() == null) {
	    if (isToplevel()) {
		if (!NativeDebuggerManager.isPerTargetBpts()) {
		    showAnnotation(a, true);
                }
	    } else {
		showAnnotation(a, false);
	    }
	} else {
	    if (isToplevel()) {
		if (!NativeDebuggerManager.isPerTargetBpts()) {
		    showAnnotation(a, false);
                }
	    } else {
		showAnnotation(a, currentDebugger() == debugger);
	    }
	}

	// debug case where we were accumulating annotations
	if (this instanceof LineBreakpoint) {
	    // 6816843
	    // We may have no annotations if the files being referred to
	    // don't exist or can't be found. Hence, <= 1 as opposed to == 1.
	    assert annotations.size() <= 1 :
		   annotations.size() + " > 1 annotations for LineBreakpoint"; // NOI18N
	}
    }

    /**
     * De-register all annotations from this bpt.
     */
    public void removeAnnotations() {
	if (isMidlevel()) {
	    for (NativeBreakpoint c : children) {
		c.removeAnnotations();
	    }
	} else {
	    for (DebuggerAnnotation a : annotations) {
		a.setLine(null, true);
		a.removeInstBpt(this);
	    } 
	    annotations = new ArrayList<DebuggerAnnotation>();
	}
    } 

    public void seedToplevelAnnotations() {
	assert this.isToplevel();
    }


    /**
     * Control whether annotations of this bpt are visible or not.
     *
     * was: combination of removeAnnotations + reinstateAnnotations
     */

    private void showAnnotationsHelp(boolean show) {
	assert ! isMidlevel() : "cannot show annotations of midlevel bpts";
	for (DebuggerAnnotation a : annotations)
	    showAnnotation(a, show);
    }


    /**
     * Control whether annotations of this bpt are visible or not.
     * Used directly for multiplexing them between sessions in the
     * non-global case.
     */

    public void showAnnotations(boolean show) {
	showAnnotationsHelp(show);
    }

    private void showAnnotation(DebuggerAnnotation a, boolean show) {
	if (show)
	    a.attach(true);
	else
	    a.ourDetach();
    }


    /**
     * Control whether annotations of this bpt are visible for the
     * session represented by 'debugger'.
     */

    public void showAnnotationsFor(boolean show, NativeDebugger debugger) {
	assert isToplevel();

	if (! NativeDebuggerManager.isPerTargetBpts()) {
	    if (show) {
		// That we're turning on annotations for _some_ session implies
		// that we have at least one session so ...
		// ... turn off annotations hanging off of top-level bpt
		//
		// (nChildren() might still be 0 because the engine hasn't
		// populated the sub-bpts)

		showAnnotationsHelp(false);

	    } else if (nBoundChildren() == 0) {
		// No sessions ... 
		// ... turn on annotations hanging off of top-level bpt
		// (In this case nChildren() should be reliable)
		showAnnotationsHelp(true);
	    }
	}

	// adjust annotations hanging off of sub-bpt.
	for (NativeBreakpoint mid : getChildren()) {
	    for (NativeBreakpoint sub : mid.getChildren()) {
		if (sub.debugger == debugger)
		    sub.showAnnotationsHelp(show);
		if (!sub.hasHandler())
		    sub.showAnnotationsHelp(false);
	    }
	}
    }

    /**
     * Support for bpt GoToSource action
     */

    public void visitNextAnnotation() {
	DebuggerAnnotation annotation = getNextAnnotation();
	if (annotation != null) {
	    Line line = annotation.getLine();
            if (line != null) {
                line.show(Line.ShowOpenType.OPEN, Line.ShowVisibilityType.FOCUS);
            }
	}
    }

    public boolean isVisitable() {
	if (annotations == null)
	    return false;
	else if (annotations.isEmpty())
	    return false;
	else
	    return true;
    }

    private DebuggerAnnotation getNextAnnotation() {
	if (!isVisitable())
	    return null;

	DebuggerAnnotation r = annotations.get(visitedAnnotationId);

	visitedAnnotationId++;
	if (visitedAnnotationId == annotations.size())
	    visitedAnnotationId = 0; // wrap around

        return r;
    }




    /**
     * Return true if this NativeBreakpoint has an annotation on the given line.
     */
    public boolean matchesLine(String src, int line) {
	for (DebuggerAnnotation a : annotations) {
	    if (a.matchesLine(src, line))
		return true;
	}
	return false;
    }

    /**
     * Return true if this NativeBreakpoint belongs to the given debugger and
     * has an annotation on the given line.
     * Used for breakpoint toggling.
     */
    public boolean matchesLineIn(String src, int line,
				 NativeDebugger debugger) {

	return getDebugger() == debugger && matchesLine(src, line);
    }





    protected PropertyOwnerSupport pos = new PropertyOwnerSupport() {
        @Override
	protected void propagateDirty() {
	}
    };

    private Action actionType = Action.STOP;

    protected final ContextProperty context =
	new ContextProperty(pos, "context", Constants.PROP_BREAKPOINT_CONTEXT, true, null); // NOI18N

    protected final StringProperty whileIn =
	new StringProperty(pos, "whileIn", Constants.PROP_BREAKPOINT_WHILEIN, false, null); // NOI18N

    protected final StringProperty qwhileIn =
	new StringProperty(pos, "qwhileIn", null, false, null); // NOI18N

    protected final StringProperty condition =
	new StringProperty(pos, "condition", Constants.PROP_BREAKPOINT_CONDITION, false, null); // NOI18N

    protected final StringProperty qcondition =
	new StringProperty(pos, "qcondition", null, false, null); // NOI18N

    protected final StringProperty lwp =
	new StringProperty(pos, "lwp", Constants.PROP_BREAKPOINT_LWP, false, null); // NOI18N

    protected final StringProperty thread =
	new StringProperty(pos, "thread", Constants.PROP_BREAKPOINT_THREAD, false, null); // NOI18N

    protected final IntegerProperty id =
	new IntegerProperty(pos, "id", Constants.PROP_BREAKPOINT_ID, true, 0) { // NOI18N
        @Override
	    public Object getAsObject() {
		if (get() == 0)
		    return "";
		else
		    return super.getAsObject();
	    }
        @Override
	    protected void setFromStringImpl(String s) {
		if (IpeUtils.isEmpty(s))
		    super.setFromStringImpl("0"); // NOI18N
		else
		    super.setFromStringImpl(s);
	    }
        @Override
	    protected void setFromObjectImpl(Object o) {
		if (o instanceof String)
		    setFromStringImpl((String) o);
		else
		    super.setFromObjectImpl(o);
	    }
	};

    protected final IntegerProperty count =
	new IntegerProperty(pos, "count", Constants.PROP_BREAKPOINT_COUNT, true, 0); // NOI18N

    protected final BooleanProperty temp =
	new BooleanProperty(pos, "temp", Constants.PROP_BREAKPOINT_TEMP, false, false); // NOI18N

    protected final BooleanProperty adjusted =
	new BooleanProperty(pos, "adjusted", null, false, false); // NOI18N

    protected final CountLimitProperty countLimit =
	new CountLimitProperty(pos, "countLimit", Constants.PROP_BREAKPOINT_COUNTLIMIT, false, null); // NOI18N

    // protected Action action = Action.STOP;
    protected final ActionProperty action =
	new ActionProperty(pos, "action", null, false, Action.STOP); // NOI18N

    protected final StringProperty script =
	new StringProperty(pos, "script", null, false, null); // NOI18N

    protected final BooleanProperty java =
	new BooleanProperty(pos, "java", Constants.PROP_BREAKPOINT_JAVA, false, false); // NOI18N

    private final BooleanProperty enabled =
	new BooleanProperty(pos, "enabled", Constants.PROP_BREAKPOINT_ENABLE, false, true); // NOI18N


    public PropertyOwnerSupport getPos() {
	return pos;
    }

    /*
     * Delegation of interface PropertyOwner to our 'pos'.
     */

    // interface PropertyOwner
    @Override
    public final void register(Property p) {
	pos.register(p);
    }

    // interface PropertyOwner
    @Override
    public Property propertyByName(String name) {
	return pos.propertyByName(name);
    }

    // interface PropertyOwner
    @Override
    public Property propertyByKey(String key) {
	return pos.propertyByKey(key);
    }

    // interface PropertyOwner
    @Override
    public int size() {
	return pos.size();
    }

    // interface PropertyOwner/Iterable
    @Override
    public Iterator<Property> iterator() {
	return pos.iterator();
    }

    // interface PropertyOwner
    @Override
    public boolean isDirty() {
	return pos.isDirty();
    }

    // interface PropertyOwner
    @Override
    public void clearDirty() {
	pos.clearDirty();
    }

    // interface PropertyOwner
    @Override
    public boolean equals(PropertyOwner that, Comparator comparator) {
	return pos.equals(((NativeBreakpoint)that).pos, comparator);
    }

    public String getAnnotationType() {
        StringBuilder type = new StringBuilder();
        if (!isEnabled()) {
	    type.append(DebuggerAnnotation.TYPE_BPTX_DISABLED);
        }
        if (isConditional) {
	    type.append(DebuggerAnnotation.TYPE_BPTX_COMPLEX);
        }
        type.append(DebuggerAnnotation.TYPE_BPT);
        if (isBroken() && isEnabled()) {
	    type.append(DebuggerAnnotation.TYPE_BPTX_BROKEN);
        }
        return type.toString();
    }

    public void updateAnnotations() {

	isConditional = false;

	if (getAction() != Action.STOP && getAction() != Action.STOPINSTR) {
	    // Not really "conditional", but "complex" - execution doesn't
	    // necessarily stop
	    isConditional = true;
	} else if (getCondition() != null) {
	    isConditional = true;
	}

	String type = getAnnotationType();
	for (DebuggerAnnotation a : annotations) {
	    a.setAnnotationType(type);
	    a.setShortDescription(getError());
	    long addr = a.getAddr();
	    if (addr != 0) {
		a.enableInstBpt(this);
            }
	}
    }

    private boolean expanded = false;

    final boolean isExpanded() {
	return expanded;
    }

    final void setExpanded(boolean expanded) {
	this.expanded = expanded;
    }

    // interface org.netbeans.api.debugger.Breakpoint
    @Override
    public final boolean isEnabled() {
	boolean isEnabled = enabled.get();
	if (Log.Bpt.enabling) {
	    if (isToplevel()) {
		System.out.println("?T  isEnabled() ->" + isEnabled); // NOI18N
	    } else if (isMidlevel()) {
		System.out.println("? M isEnabled() ->" + isEnabled); // NOI18N
	    } else {
		System.out.println("?  SisEnabled() ->" + isEnabled); // NOI18N
	    }
	}
	return isEnabled;
    }



    /**
     * Returns whether this bpts enabled property is set.
     * It bypasses the regular "summarizing" behaviour of isEnabled().
     */

    // All properties that get summarized in the toplevel SHOULD have a
    // is/getProp variation.

    public final boolean isPropEnabled() {
	if (Log.Bpt.enabling)
	    System.out.println("?  isPropEnabled() ->" + enabled.get()); // NOI18N
	return enabled.get();
    }

    final void setEnabled(boolean enabled) {
	this.enabled.set(enabled);
	if (isToplevel()) {
	    if (Log.Bpt.enabling)
		System.out.println("<T  setEnabled" + enabled + ")"); // NOI18N
	    update();

	} else if (isMidlevel()) {
	    if (Log.Bpt.enabling)
		System.out.println("< M setEnabled(" + enabled + ")"); // NOI18N
	    update();

	    NativeBreakpoint parent = getParent();
	    if (parent != null)
		parent.setEnabled(parent.recalculateIsEnabled());

	} else {
	    if (Log.Bpt.enabling)
		System.out.println("<  SsetEnabled(" + enabled + ")"); // NOI18N
	    update();

	    NativeBreakpoint parent = getParent();
	    if (parent != null)
		parent.setEnabled(parent.recalculateIsEnabled());
	}
    }

    private boolean recalculateIsEnabled() {
	if (isSubBreakpoint()) {
	    return isEnabled();
	} else {
	    if (nChildren() == 0) {
		return isEnabled();
	    } else {
		boolean isEnabled = false;
		for (NativeBreakpoint c : getChildren())
		    isEnabled |= c.recalculateIsEnabled();
		return isEnabled;
	    }
	}
    }

    public void setPropEnabled(boolean b) {
	if (isToplevel()) {
	    if (Log.Bpt.enabling)
		System.out.println(">T  setPropEnabled(" + b + ")"); // NOI18N
	    if (nChildren() == 0) {
		// change the state right now
		setEnabled(b);
                if (b && debugger == null && NativeDebuggerManager.get().currentDebugger() != null) {
                    //while fixing bz#271311 - start debug performance: do not post disabled breakpoint
                    //need to hide disabled annotation for the top level breakpoint
                    showAnnotations(false);
                    //post breakpoint (the same action as if we would create it
                    //new subbreakpoint will be created and its breakpoint annotation will be shown in the editor
                    //when debug session will be finished  subbreakpoint will be deleted
                    //and this (top level) breakpoint annotation will be shown in the editor
                    NativeDebuggerManager.get().currentDebugger().bm().postRestoreBreakpoint(this);
                }
	    } else {
		for (NativeBreakpoint c : getChildren())
		    c.setPropEnabled(b);
	    }

	} else if (isMidlevel()) {
	    if (Log.Bpt.enabling)
		System.out.println("> M setPropEnabled(" + b + ")"); // NOI18N
	    if (nChildren() == 0) {
		// change the state right now
		setEnabled(b);
	    } else {
		for (NativeBreakpoint c : getChildren())
		    c.setPropEnabled(b);
	    }

	} else {
	    if (Log.Bpt.enabling)
		System.out.println(">  SsetPropEnabled(" + b + ")"); // NOI18N
	    if (hasHandler()) {
		getHandler().postEnable(b, this.getRoutingToken());
	    } else {
                // change the state right now
                setEnabled(b);
	    }
	}
    }


    public final int getId() {
	return id.get();
    }

    public final String getWhileIn() {
        return whileIn.get();
    }

    public final String getQwhileIn() {
        return qwhileIn.get();
    }

    public final void setQwhileIn(String whileIn) {
        this.qwhileIn.setFromString(whileIn);
    }

    public final void setWhileIn(String whileIn) {
	this.whileIn.setFromString(whileIn);
    }

    public final void setPropWhileIn(String whileIn) {
	if (!IpeUtils.sameString(whileIn, getWhileIn())) {
	    new EditUndo(this, Constants.PROP_BREAKPOINT_WHILEIN);
	    NativeBreakpoint editable = this.makeEditableCopy();

	    // See trim() in BreakpointPanel.
	    if (IpeUtils.isEmpty(whileIn))
		whileIn = null;

	    editable.setWhileIn(whileIn);
	    editable.setQwhileIn(null);
	    this.postChange(editable, Gen.primary(null));
	}
    }

    public final String getCondition() {
        return condition.get();
    }

    public final void setCondition(String newCond) {
        condition.setFromString(newCond);
	update();
    }

    public final String getQcondition() {
        return qcondition.get();
    }

    public final void setQcondition(String newCond) {
        qcondition.setFromString(newCond);
	update();
    }

    public final void setPropCondition(String condition) {
	new EditUndo(this, Constants.PROP_BREAKPOINT_CONDITION);
	NativeBreakpoint editable = this.makeEditableCopy();

	// See trim() in BreakpointPanel.
	if (IpeUtils.isEmpty(condition))
	    condition = null;

	editable.setCondition(condition);
	editable.setQcondition(null);
	this.postChange(editable, Gen.primary(null));
    }

    public final String getLwp() {
        return lwp.get();
    }

    public final void setLwp(String newLwp) {
        lwp.setFromString(newLwp);
    }

    public final void setPropLwp(String lwp) {
	new EditUndo(this, Constants.PROP_BREAKPOINT_LWP);
	NativeBreakpoint editable = this.makeEditableCopy();
	editable.setLwp(lwp);
	this.postChange(editable, Gen.primary(null));
    }

    public final String getThread() {
        return thread.get();
    }

    public final void setThread(String newThread) {
	thread.setFromString(newThread);
    }

    public final void setPropThread(String thread) {
	new EditUndo(this, Constants.PROP_BREAKPOINT_THREAD);
	NativeBreakpoint editable = this.makeEditableCopy();
	editable.setThread(thread);
	this.postChange(editable, Gen.primary(null));
    }

    /*
     * Context
     */
    public final Context getContext() {
	return context.get();
    } 

    public final void setContext(Context newContext) {
	context.set(newContext);

	if (isMidlevel()) {
	    for (NativeBreakpoint c : getChildren())
		c.context.set(newContext);
	}
    }

    private long getPid() {
	NativeDebugger debugger = getDebugger();
	if (debugger == null)
	    return -1;
	if (Log.Bpt.embellish)
	    System.out.printf("\thave debugger\n"); // NOI18N
	NativeSession session = debugger.session();
	if (session == null)
	    return -1;
	long pid = session.getPid();
	if (Log.Bpt.embellish) {
	    System.out.printf("\thave session\n"); // NOI18N
	    System.out.printf("\tpid -> %d\n", pid); // NOI18N
	}
	return pid;
    }

    public String embellishedContext(String contextPropertyValue) {
	if (contextPropertyValue == null)
	    return null;
	if (Log.Bpt.embellish)
	    System.out.printf("embellishedContext(%s)\n", contextPropertyValue); // NOI18N
	String basename = CndPathUtilities.getBaseName(contextPropertyValue);
	long pid = getPid();
	if (pid != -1)
	    return "[" + pid + "] " + basename; // NOI18N
	else
	    return basename;
    }


    /**
     * Only update the bpts bound to 'debugger'.
     */

    public void updateFor(NativeDebugger debugger) {
	assert isToplevel();
	for (NativeBreakpoint c : getChildren()) {
	    if (c.debugger == debugger)
		c.update();
	}
    }

    public final void setId(int newId) {
	id.set(newId);
    }

    /*
     * Count and CountLimit properties
     */

    public final void setCount(int newCount) {
	count.set(newCount);

	// propagate to parent otherwise it will be differentiated and 
	// linger as an awkward ghost especially in a single project debugging
	// scernario
	// When a handler is first being created we get called w/o a parent,
	// hence the parent check.

	if (getParent() != null) {
	    if (isOnlyChild()) {
		getParent().setCount(newCount);
	    }
	}
    }

    public final int getCount() {
        return count.get();
    }

    public final long getCountLimit() {
	if (countLimit.get() != null)
	    return countLimit.get().count();
	else
	    return 0;	// SHOULD throw an exception?
    }

    public final boolean hasCountLimit() {
	return countLimit.get() != null;
    }

    public final void setCountLimit(long newCountLimit, boolean hasLimit) {
	if (hasLimit)
	    countLimit.set(new CountLimit(newCountLimit));
	else {
	    countLimit.set(null);
	    setCount(0);
	}
    }

    public final void setPropCountLimit(Object o) {
        CountLimit cl = (CountLimit) o;
        cl = cl.possiblySetToCurrentCount(getCount());

	new EditUndo(this, Constants.PROP_BREAKPOINT_COUNTLIMIT);
	NativeBreakpoint editable = this.makeEditableCopy();
	editable.setCountLimit(cl.count(), cl.isEnabled());
	this.postChange(editable, Gen.primary(null));
    }



    /*
     * Adjusted property
     * Required user intervention (i.e. overload menu).
     */

    public final boolean isAdjusted() {
        return adjusted.get();
    }

    public final void setAdjusted(boolean adjusted) {
	this.adjusted.set(adjusted);
    }

    /*
     * Temporary property
     */

    public final boolean getTemp() {
        return temp.get();
    }

    public final void setTemp(boolean newTemp) {
	temp.set(newTemp);
    }

    public final void setPropTemp(boolean temp) {
	new EditUndo(this, Constants.PROP_BREAKPOINT_TEMP);
	NativeBreakpoint editable = this.makeEditableCopy();
	editable.setTemp(temp);
	this.postChange(editable, Gen.primary(null));
    }


    public final void setPropScript(String script) {
	// OLD new EditUndo(this, Constants.PROP_BREAKPOINT_SCRIPT);
	NativeBreakpoint editable = this.makeEditableCopy();
	editable.setScript(script);
	this.postChange(editable, Gen.primary(null));
    }

    public final void setScript(String newScript) {
        if (action.get() == Action.WHEN || action.get() == Action.WHENINSTR)
	    script.setFromString(newScript);
	else 
	    script.setFromString(null);
    }

    public final String getScript() {
	return script.get();
    }

    public final void setJava(boolean java) {
	this.java.set(java);
    }

    public final boolean getJava() {
	return this.java.get();
    } 

    public final void setPropJava(boolean java) {
	new EditUndo(this, Constants.PROP_BREAKPOINT_JAVA);
	NativeBreakpoint editable = this.makeEditableCopy();
	editable.setJava(java);
	this.postChange(editable, Gen.primary(null));
    }

    public final Action getAction() {
        return action.get();
    }

    public final void setAction(Action newAction) {
	action.set(newAction);
	update();
    }


    public final void setPropAction(Object o) {
        Action a = (Action) o;
	// OLD new EditUndo(this, Constants.PROP_BREAKPOINT_ACTION);
	NativeBreakpoint editable = this.makeEditableCopy();
	editable.setAction(a);
	this.postChange(editable, Gen.primary(null));
    }


    private String originalEventspec;

    public final void setOriginalEventspec(String originalEventspec) {
	this.originalEventspec = originalEventspec;
    }

    public final String getOriginalEventspec() {
	return originalEventspec;
    }

    protected abstract void processOriginalEventspec(String eventspec);


    /**
     * copyFromHelp().
     *
     * Common code used for
     *  copyFrom
     *	makeEditableCopy
     *	makeToplevelCopy
     *	makeSubBreakpointCopy
     *	makeMidBreakpointCopy
     */

    private void copyFromHelp(NativeBreakpoint that) {
	// breakpointType is already set 

	// this.original = done in caller
	// this.updater =  done in caller

	// OLD this.restoreTimestamp(that.timestamp());
	this.timestamp = that.timestamp;
	this.updateTimestamp = that.updateTimestamp;
	this.srcOutOfSync = that.srcOutOfSync;

	// Experiment for 6568490 but doesn't seem relevent:
	// LATER this.overloaded = that.overloaded;

	// For LineBreakpoint need to do it this way to retain information
	// stored in annotations.
	if (that instanceof LineBreakpoint) {
	    LineBreakpoint olb = (LineBreakpoint) that;
	    LineBreakpoint nlb = (LineBreakpoint) this;
	    nlb.setLineNumberInitial(olb.getLineNumber());
	}

	// copy properties over
	// SHOULD n't this be done by PropertyOwner?

	for (Property thatProp : that.pos) {
	    String thatName = thatProp.name();
	    Object thatObject = thatProp.getAsObject();
	    for (Property thisProp : this.getPos()) {
	        String thisName = thisProp.name();
		if (thisName != null && thisName.equals(thatName)) {
		    thisProp.setFromObjectInitial(thatObject);
		}
	    }
	}

	adjusted.setFromObjectInitial(false);

    }

    /**
     * Replace the contents of this breakpoint with the given one
     * Used specifically to apply changes so is not as generic as you'd expect.
     */

    public void copyFrom(NativeBreakpoint that) {
	assert this.sameTypeAs(that) :
	       "NB.copyFrom(): can only copy from compatible type"; // NOI18N
	// this.flags = that.flags;

	assert that.isEditable() :
	       "NB.copyFrom(): can only copy from toplevel bpts"; // NOI18N

	// leave this.parent unmolested
	// leave this.debugger unmolested
	// this.debugger = that.debugger;
	// leave this.original unmolested -- only editable 'that' has originals
	// this.original = that.original;
	// leave this.updater unmolested -- that always has a null updater
	// this.updater = that.updater;

	if (this.isToplevel() || that.isToplevel() ) {
	    // don't clobber our context with that of 'that'.
	    final Object originalContext = this.context.getAsObject();
		this.copyFromHelp(that);
	    this.context.setFromObject(originalContext);

	} else {
	    this.copyFromHelp(that);
	}

    }

    /**
     * Make a clone of this breakpoint for editing by user in a panel.
     */

    public NativeBreakpoint makeEditableCopy() {
	NativeBreakpoint editable = breakpointType.newInstance(this.flags);

	editable.debugger = this.debugger;
	editable.original = this;
	editable.parent = this.parent;
	// we never want the updater set
	editable.updater = null;
	editable.echoUpdater("makeEditableCopy"); // NOI18N

	editable.copyFromHelp(this);

	return editable;
    }


    /**
     * Make a toplevel clone of this sub-breakpoint.
     */

    public NativeBreakpoint makeToplevelCopy(boolean useOeventspec) {
	assert this.isSubBreakpoint();

	final int flags = NativeBreakpoint.TOPLEVEL;
	NativeBreakpoint toplevel = breakpointType.newInstance(flags);

	toplevel.original = null;
	// updater will be set when we add this to a BreakpointBag
	toplevel.updater = null;
	toplevel.echoUpdater("makeToplevelCopy"); // NOI18N

	toplevel.copyFromHelp(this);

	if (useOeventspec) {
	    // SHOULD be done by a HandlerExpert?
	    toplevel.processOriginalEventspec(this.getOriginalEventspec());
	}

	return toplevel;
    }


    /**
     * Make a sub-bpt clone of this toplevel-breakpoint.
     */

    public NativeBreakpoint makeSubBreakpointCopy() {
	assert this.isToplevel();

	final int flags = NativeBreakpoint.SUBBREAKPOINT;
	NativeBreakpoint subbpt = breakpointType.newInstance(flags);

	subbpt.original = null;
	// updater will be set when we add this to a BreakpointBag
	subbpt.updater = null;
	subbpt.echoUpdater("makeSubBreakpointCopy"); // NOI18N

	subbpt.copyFromHelp(this);

	return subbpt;
    }

    /**
     * Make a mid-bpt clone of this toplevel or midlevel breakpoint.
     */

    public NativeBreakpoint makeMidlevelCopy() {
	assert isToplevel() || isMidlevel() :
	       "Can makeMidBreakpointCopy() only of Toplevel or Midlevel bpt"; // NOI18N

	NativeBreakpoint midbpt;
	final int flags = NativeBreakpoint.MIDBREAKPOINT;
	midbpt = breakpointType.newInstance(flags);

	midbpt.original = null;
	// updater will be set when we add this to a BreakpointBag
	midbpt.updater = null;
	midbpt.echoUpdater("makeMidBreakpointCopy"); // NOI18N

	midbpt.copyFromHelp(this);
	return midbpt;
    }

    // interface Object
    @Override
    public String toString() {
	String level;
	if (isToplevel())
	    level = ".--"; // NOI18N
	else if (isMidlevel())
	    level = " .-"; // NOI18N
	else 
	    level = "  ."; // NOI18N
	return "(" + // NOI18N
	       level + 
	       (isEditable()? "*": " ") + // NOI18N
	       getDisplayNameHelp() +
	       " [" + nChildren() + "] " + // NOI18N
	       (isBound()? "  bound": "unbound") + // NOI18N
	       ")"; // NOI18N
    }


    // for flyover "tip"
    protected abstract String getSummary();


    // for name column
    protected abstract String getDisplayNameHelp();


    private static String quote(String s) {
	try {
	    s = org.openide.xml.XMLUtil.toAttributeValue(s);
	} catch (java.io.CharConversionException x) {
	}
	return s;
    }

    private static enum Rendition {
	PLAIN,
	CURRENT,
	GHOST
    };

    public String getDisplayName() {
	// SHOULD revisit the whole InProgress business
	if (isSubBreakpoint()) {
	    if (getHandler() != null && getHandler().isInProgress())
		return Catalog.get("AddingBreakpoint"); // NOI18N
	}

	String summary = getDisplayNameHelp();


	Rendition rendition;
	if (isSubBreakpoint()) {
	    if (isCurrent() && ! parent.isOnlyChild())
		rendition = Rendition.CURRENT;
	    else if (! isBound()) 
		rendition = Rendition.GHOST;
	    else
		rendition = Rendition.PLAIN;
	} else if (isMidlevel()) {
	    if (isCurrent() && ! isOnlyChild())
		rendition = Rendition.CURRENT;
	    else if (! isBound()) 
		rendition = Rendition.GHOST;
	    else
		rendition = Rendition.PLAIN;
	} else {
	    rendition = Rendition.PLAIN;
	}


	String newSummary;

	switch (rendition) {
	    case CURRENT:
		if (sessionOnly) {
		    newSummary = summary;
		} else {
		    newSummary = "<html><b>"; // NOI18N
		    newSummary += quote(summary);
		    newSummary += "</b></html>"; // NOI18N
		}
		break;
	    case GHOST:
		newSummary = "<html>"; // NOI18N
		newSummary += "<font color=\"#C0C0C0\"/>";	// gray // NOI18N
		newSummary += quote(summary);
		newSummary += "</html>"; // NOI18N
		break;
	    case PLAIN:
		newSummary = summary;
		break;
	    default:
		newSummary = summary;
		break;
	}

	return newSummary;
    }


    private static final String debugger_icon_dir =
	"org/netbeans/modules/debugger/resources/breakpointsView";       // NOI18N

    private static final String icon_dir =
	"org/netbeans/modules/cnd/debugger/common2/icons";       // NOI18N
    
    public String getIconBase() {
	// SHOULD merge this with annotation types?

	// SHOULD use StringBuilder

	// "bigger" experimental icons
	// StringBuffer name = new StringBuffer("viewBpt");

	// texteditor glyph gutter icons
        if (debugger != null && !debugger.areBreakpointsActivated()) {
            if (isEnabled()) {
                return debugger_icon_dir + "/DeactivatedBreakpoint";       // NOI18N
            } else {
                return debugger_icon_dir + "/DeactivatedDisabledBreakpoint";       // NOI18N
            }
        }
	StringBuilder name = new StringBuilder();
        if (!isEnabled()) {
	    name.append(DebuggerAnnotation.TYPE_BPTX_DISABLED);
        }
        if (isConditional) {
	    name.append("Conditional"); //NOI18N
        }
        name.append(DebuggerAnnotation.TYPE_BPT);
        if (isBroken() && isEnabled()) {
	    name.append(DebuggerAnnotation.TYPE_BPTX_BROKEN);
        } else if (isFired()) {
            name.append("Hit"); //NOI18N
        }
	return debugger_icon_dir + "/" + name.toString(); // NOI18N
    }

    private int routingToken = 0;

    /**
     * Get the routing token associated with this breakpoint.
     * This is used to handle asynchronous breakpoint commands
     * such that we can correlate a request with an answer.
     */

    public int getRoutingToken() {
	if (routingToken == 0)
	    routingToken = RoutingToken.BREAKPOINTS.getUniqueRoutingTokenInt();
        return routingToken;
    }


    public void setAttrs(Attributes genericAttributes) {

	// slow way ... SHOULD loop over genericAttributes, not all of
	// which are always present

	for (Property p : pos) {
	    String pname = p.name();
	    String value = genericAttributes.getValue(pname);
	    // System.out.println("PROP " + pname + "=" + value);
	    if (value == null)
		continue;
	    if (p instanceof EnumProperty) {
		EnumProperty ep = (EnumProperty) p;
		ep.setFromName(value);
	    } else {
		p.setFromString(value);
	    }
	}
    }

    private void checkOutofDate(Date time) {
	for (DebuggerAnnotation a : annotations) {
	    if (a.fileIsNewerThan(time)) {
		srcOutOfSync = true;
		break;
	    }
	}
    }

    private static NativeBreakpoint newBreakpointOfType(Class<? extends NativeBreakpointType> cls) {
        // NativeBreakpointType nbt = breakpointType(cls);
        NativeBreakpointType instance = (NativeBreakpointType)ContextAwareSupport.createInstance(
                cls.getCanonicalName(),
                org.netbeans.api.debugger.DebuggerManager.getDebuggerManager());
        if (instance != null) {
            return instance.newInstance(NativeBreakpoint.TOPLEVEL);
        } else {
            // previous approach
            List breakpointTypes =
                    org.netbeans.api.debugger.DebuggerManager.getDebuggerManager().
                    lookup(null, BreakpointType.class);

            if (breakpointTypes == null) {
                return null;
            }

            NativeBreakpointType nbt = null;
            for (int btx = 0; btx < breakpointTypes.size(); btx++) {
                BreakpointType bt = (BreakpointType) breakpointTypes.get(btx);
                if (cls.isInstance(bt)) {
                    nbt = (NativeBreakpointType) bt;
                    break;
                }
            }
            if (nbt == null) {
                return null;
            }
            NativeBreakpoint bpt = nbt.newInstance(NativeBreakpoint.TOPLEVEL);
            return bpt;
        }
    }

    /**
     * Quick and easy way to create an instruction bpt.
     * Used for toggling.
     */
    public static NativeBreakpoint newInstructionBreakpoint(String address) {
	NativeBreakpoint bpt =
	    newBreakpointOfType(InstructionBreakpointType.class);
	if (bpt == null)
	    return null;
	InstructionBreakpoint ib = (InstructionBreakpoint) bpt;
	ib.setAddress(address);
	return ib;
    }

    /**
     * Quick and easy way to create a line bpt.
     * Used for toggling.
     */
    public static NativeBreakpoint newLineBreakpoint(String fileName,
						     int lineNo,
                                                     FileSystem fs) {
	NativeBreakpoint bpt =
	    newBreakpointOfType(LineBreakpointType.class);
	if (bpt == null)
	    return null;
	LineBreakpoint lb = (LineBreakpoint) bpt;
	lb.setFileAndLine(fileName, lineNo, fs);
	return lb;
    }
    
    /**
     * Returns a list of changed properties (with the new values)
     * @param oldBpt
     * @param newBpt
     */
    public static Set<Property> diff(NativeBreakpoint oldBpt, NativeBreakpoint newBpt) {
        assert oldBpt.getClass() == newBpt.getClass() : "Only compares similar breakpoints"; //NOI18N
        
        Set<Property> res = new HashSet<Property>();
        for (Property pOld : oldBpt.pos) {
            Property pNew = newBpt.pos.propertyByName(pOld.name());
            if (!pOld.matches(pNew)) {
                res.add(pNew);
            }
        }
        return res;
    }

    @Override
    public GroupProperties getGroupProperties() {
        return new NativeGroupProperties();
    }
    
    protected class NativeGroupProperties extends GroupProperties {
        @Override
        public String getType() {
            return getBreakpointType().getTypeDisplayName();
        }
        
        @Override
        public String getLanguage() {
            return "C/C++"; //NOI18N
        }

        @Override
        public FileObject[] getFiles() {
            return null;
        }

        @Override
        public Project[] getProjects() {
            return null;
        }

        @Override
        public DebuggerEngine[] getEngines() {
            return null;
        }

        @Override
        public boolean isHidden() {
            return false;
        }
    }
}
