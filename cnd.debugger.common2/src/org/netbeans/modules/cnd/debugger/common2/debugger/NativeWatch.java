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


/*
 * NativeWatch.java"
 */

package org.netbeans.modules.cnd.debugger.common2.debugger;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.netbeans.api.debugger.Watch;

import org.netbeans.spi.viewmodel.ModelEvent;
import org.netbeans.spi.viewmodel.ModelListener;


/**
 * Our own version of debuggercore Watch. In fact debuggercore Watch
 * is final and cannot be extended.
 * 
 * Same strategy as our own version of Breakpoint, so read comments there.
 *
 * Like NativeBreakpoint, which is cross-connected to a Handler, we're 
 * cross-connected with a WatchVariable.
 */

public class NativeWatch {
    private String expression;
    private String qexpression;	// qualified expression
    private String scope;
    private String replacedwith = null;
    private boolean restricted;
    private ModelListener updater;
    private final Watch watch;	// back-pointer

    public NativeWatch(Watch watch) {
	this.watch = watch;
    }

    public Watch watch() {
	return watch;
    }


    public String getExpression() {
	if (watch != null)
	    return watch.getExpression();
	else
	    return expression;
    } 

    public void setExpression(String expression) {
	if (watch != null)
	watch.setExpression(expression);
	else
	this.expression = expression;
    } 


    public String getScope() {
	assert watch == null;
	return scope;
    } 

    public void setScope(String scope) {
	assert watch == null;
	this.scope = scope;
    } 

    public void replacedWith(String r) {
	replacedwith = r;
    }

    public String getReplaced() {
        return replacedwith;
    }
    
    public boolean isReplaced() {
	return replacedwith != null;
    }
    
    public boolean isRestricted() {
	return restricted;
    }

    public void setRestricted(boolean restricted) {
	assert watch == null;
	this.restricted = restricted;
    }
    
    public boolean isEnabled() {
        return watch.isEnabled();
    }
    public String getQualifiedExpression() {
	assert watch == null;
	return qexpression;
    }

    public void setQualifiedExpression(String qexpression) {
	assert watch == null;
	// Only allow setting it once
	// First setter wins
	if (this.qexpression != null)
	    return;
	this.qexpression = qexpression;
    }

    public void setUpdater(ModelListener updater) {
	this.updater = updater;
    }

    public void update() {
	if (updater != null) {
	    if (watch == null)
		updater.modelChanged(new ModelEvent.NodeChanged(this, this));
	    else
		updater.modelChanged(new ModelEvent.NodeChanged(this, watch));
	}
    }


    /**
     * Free resources associated with this watch
     */
    public void cleanup() {
	setUpdater(null);
    }

    /**
     * Call right before encoding into persistent storage.
     */
    void prepareForSaving() {
    }

    /**
     * Call right after decoding from persistent storage.
     */
    void restored() {
    }

    private int routingToken = 0;

    public int getRoutingToken() {
	if (routingToken == 0)
	    routingToken = RoutingToken.WATCHES.getUniqueRoutingTokenInt();
	return routingToken;
    }


    // See comment for NativeBreakpoint.deletingChildren
    private boolean deletingChildren = false;

    /**
     * Actually delete this NativeWatch, delete it's associated debuggercore
     * Watch and remove it from watchBag().
     */
    public void delete() {
	assert getSubWatches().length == 0 :
	       "NativeWatch.delete(): still have children"; // NOI18N
	if (watch != null) {
	    watch.remove();
	    // Will come back as DebuggerManager.watchRemoved()
	    // which will call WatchBag.remove()
	} else {
	    NativeDebuggerManager.get().watchBag().remove(this);
	}
    }

    /**
     * Request that this Watch be deleted.
     *
     * See comment for NativeBreakpoint.postDelete().
     */

    public void postDelete(boolean spreading) {
	if (deletingChildren)
	    return;
	deletingChildren = true;

	// Use an array because
	// 	for (WatchVariable w : subWatches)
	// will run into ConcurrentModificationException's

	WatchVariable[] children = getSubWatches();
	if (children.length == 0) {
	    // 0 native sessions
	    delete();
	} else {
	    for (WatchVariable w : children)
		w.getDebugger().postDeleteWatch(w, spreading);
	}
    }

    private final List<WatchVariable> subWatches = new CopyOnWriteArrayList<WatchVariable>();

    public final WatchVariable[] getSubWatches() {
	WatchVariable array[] = new WatchVariable[subWatches.size()];
	subWatches.toArray(array);
	return array;
    }

    public int nChildren() {
	return subWatches.size();
    }

    public WatchVariable findByDebugger(NativeDebugger debugger) {
	assert debugger != null;

	for (WatchVariable w : subWatches) {
	    assert w.getDebugger() != null;
	    if (w.getDebugger() == debugger)
		return w;
	}

	if (Log.Watch.map) {
	    System.out.printf("NativeWatch.findByDebugger(): " + // NOI18N
			      "no WatchVariable for %s --- NsubWatches = %d\n", // NOI18N
		getExpression(), subWatches.size());
	}
	return null;
    }

    /**
     * Remove 'subWatch' from 'this'.
     *
     * @param subWatch
     * @param debugger
     * Passed in to double-check if the removed watch truly belongs to
     * the given debugger. may be null.
     */

    public final void removeSubWatch(WatchVariable subWatch,
				     NativeDebugger debugger) {
	assert subWatch != null :
	       "removeSubWatch(): null subWatch"; // NOI18N
	assert subWatch.getDebugger() == debugger :
	       "removeSubWatch(): " + // NOI18N
	       "sub-watch not associated with debugger or removed twice"; // NOI18N
        // This subWatch may have already been removed by another thread
        // i.e. one remove initiated by user and another by session finish
        subWatches.remove(subWatch);
    }

    public void setSubWatchFor(WatchVariable subWatch, NativeDebugger debugger) {
	assert subWatch != null :
	       "setSubWatchFor(): null subWatch"; // NOI18N
	assert debugger != null :
	       "setSubWatchFor(): null debugger"; // NOI18N
	assert subWatch.getDebugger() == debugger :
	       "setSubWatchFor(): debuggers don't match"; // NOI18N

	deletingChildren = false;

	subWatch.setNativeWatch(this);
        assert !subWatches.contains(subWatch);
	subWatches.add(subWatch);
	update();
    }
}
