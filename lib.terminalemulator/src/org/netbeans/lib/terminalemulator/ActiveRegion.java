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
 *
 */
/*
 * "ActiveRegion.java"
 * ActiveRegion.java 1.8 01/07/16
 */
package org.netbeans.lib.terminalemulator;

import java.util.LinkedList;
import java.util.ListIterator;

public class ActiveRegion {

    public Coord begin = new Coord();
    public Coord end = new Coord();

    // accessible to RegionManager
    ActiveRegion parent;
    boolean nested;
    private LinkedList<ActiveRegion> children;
    private boolean has_end;
    private int parentAttrs;      // attrs at the time this region began

    ActiveRegion(ActiveRegion parent, Coord begin, boolean nested) {
        this.parent = parent;
        this.begin.copyFrom(begin);
        this.nested = nested;
    }

    void setParentAttrs(int attrs) {
        this.parentAttrs = attrs;
    }

    int getParentAttrs() {
        return parentAttrs;
    }

    public ActiveRegion parent() {
        return this.parent;
    }

    public Extent getExtent() {
        if (has_end) {
            return new Extent(begin, end);
        } else {
            return new Extent(begin, begin);
        }
    }

    void setEnd(Coord end) {
        this.end.copyFrom(end);
        has_end = true;
    }

    void addChild(ActiveRegion child) {
        if (children == null) {
            children = new LinkedList<>();
        }
        children.add(child);
    }

    void removeChild(ActiveRegion child) {
        if (children == null) {
            return;
        }
        children.remove(child);
    }

    ActiveRegion contains(Coord coord) {

        // System.out.println("ActiveRegion [ " + begin + "-" + end + "] vs " +	// NOI18N
        // coord);
        boolean coord_past_end = has_end && coord.compareTo(end) > 0;
        if (this.parent != null && (coord.compareTo(begin) < 0 ||
                coord_past_end)) {
            return null;	// outside of us entirely
        }

        if (children != null) {
            ListIterator<ActiveRegion> iter = children.listIterator();
            while (iter.hasNext()) {
                ActiveRegion child = iter.next();
                if (coord.compareTo(child.begin) < 0) {
                    break;	// short circuit
                }		// 'target' is 'child' or one if it's children
                ActiveRegion target = child.contains(coord);
                if (target != null) {
                    return target;
                }
            }
        }
        return this;
    }


    // absolute coordinate mgmt
    void relocate(int delta) {
        this.begin.row += delta;
        this.end.row += delta;
        if (children != null) {
            for (ActiveRegion child : children)
                child.relocate(delta);
        }
    }

    void cull(int origin) {

        // See RegionManager.cull() for culling strategy.
        // This function isn't recursive. It's called only once on root.

        if (children == null) {
            return;
        }

        int nculled = 0;

        ListIterator<ActiveRegion> iter = children.listIterator();
        while (iter.hasNext()) {
            ActiveRegion child = iter.next();
            if (child.begin.row < origin) {
                iter.remove();
                nculled++;
            } else {
                break;	// short circuit out
            }
        }

    // System.out.println("cull'ed " + nculled + " regions");	// NOI18N
    }

    /**
     * Mark this region as one that may be converted to a selection.
     * <p>
     * This is just a convenience state-keeping flag
     */
    public void setSelectable(boolean selectable) {
        this.selectable = selectable;
    }

    /**
     * Return the value set using setSelectable().
     */
    public boolean isSelectable() {
        return selectable;
    }
    private boolean selectable;

    /**
     * Mark this region as one that will provide feedback when the mouse moves
     * over it.
     * <p>
     * This is just a convenience state-keeping flag
     */
    public void setFeedbackEnabled(boolean feedback) {
        this.feedback_enabled = feedback;
    }

    /**
     * Return the value set using setFeedback().
     */
    public boolean isFeedbackEnabled() {
        return feedback_enabled;
    }
    private boolean feedback_enabled;

    /**
     * Mark this region as one that will provide feedback in it's containing
     * region.
     */
    public void setFeedbackViaParent(boolean feedback_via_parent) {
        this.feedback_via_parent = feedback_via_parent;
    }

    public boolean isFeedbackViaParent() {
        return feedback_via_parent;
    }
    private boolean feedback_via_parent;

    /*
     * Mark this region as a "link".
     */
    public void setLink(boolean link) {
        this.link = link;
    }

    public boolean isLink() {
        return link;
    }
    private boolean link;

    /**
     * Associate additional data with this ActiveRegion.
     */
    public void setUserObject(Object object) {
        this.user_object = object;
    }

    /**
     * Retrieve the additional data associated with this ActiveRegion through 
     * setUserObject.
     */
    public Object getUserObject() {
        return user_object;
    }
    private Object user_object;

    // siblings
    //
    // I've chosen to delegate finding of siblings to the parent instead of 
    // using explicit sibling links. Here are the reasons:
    // - explicit links take up memory. I believe in the long run that is 
    //   more costly than dumb searches.
    // - AR's get created when stuff gets rendered. We want to render stuff
    //   as fast as possible. So instead of doing work at creation time
    //   which needs to be supra-human speed, we do the work at query time
    //   which is usualy in reponse to human actions and need not be too fast.
    // 
    // Ultimately a Vector is probably a better substitute for LinkedList
    // In that case each child could remember it's index in it's parents
    // vector (less storage than two pointers, and vector has less overhead
    // than linkedlist to compensate). Note that this will work only
    // because ActiveRegions are not editable.
    /**
     * Return the first child of this
     */
    public ActiveRegion firstChild() {
        if (children == null) {
            return null;
        }
        return children.getFirst();
    }

    /**
     * Return the last child of this
     */
    public ActiveRegion lastChild() {
        if (children == null) {
            return null;
        }
        return children.getLast();
    }

    /**
     * Get the previous sibling of this region
     */
    public ActiveRegion getPreviousSibling() {
        if (parent != null) {
            return parent.previous_sibling_of(this);
        } else {
            return null;
        }
    }

    /**
     * Get the next sibling of this region
     */
    public ActiveRegion getNextSibling() {
        if (parent != null) {
            return parent.next_sibling_of(this);
        } else {
            return null;
        }
    }

    private ActiveRegion previous_sibling_of(ActiveRegion child) {
        ActiveRegion previousChild = null;
        for (ActiveRegion candidate : children) {
            if (candidate == child) {
                // bug: iterator is already shifted, so following command
                // returns again child
                //return (ActiveRegion) (iter.hasPrevious()? iter.previous(): null);
                return previousChild;
            }
            previousChild = candidate;
        }
        return null;
    }

    private ActiveRegion next_sibling_of(ActiveRegion child) {
        ListIterator<ActiveRegion> iter = children.listIterator();
        while (iter.hasNext()) {
            ActiveRegion candidate = iter.next();
            if (candidate == child) {
                return iter.hasNext() ? iter.next() : null;
            }
        }
        return null;
    }
}
