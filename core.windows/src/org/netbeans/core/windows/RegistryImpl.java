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

package org.netbeans.core.windows;

import java.util.Collection;
import java.util.Iterator;
import org.openide.nodes.Node;
import org.openide.util.WeakSet;
import org.openide.windows.TopComponent;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/** Implementstion of registry of top components. This implementation
 * receives information about top component changes from the window
 * manager implementation, to which is listening to.
 *
 * @author Peter Zavadsky
 */
@org.openide.util.lookup.ServiceProvider(service=org.openide.windows.TopComponent.Registry.class)
public final class RegistryImpl extends Object implements TopComponent.Registry {
    
    // fields
    /** Activated top component */
    private WeakReference<TopComponent> activatedTopComponent = new WeakReference<TopComponent>(null);
    /** Previouly activated top component */
    private WeakReference<TopComponent> previousActivated;
    /** Set of opened TopComponents */
    private final Set<TopComponent> openSet = new WeakSet<TopComponent>(30);
    /** Currently selected nodes. */
    private Node[] currentNodes;
    /** Last non-null value of current nodes. (If null -> it means they are
     * not initialized and weren't fired yet. */
    private Node[] activatedNodes;
    /** PropertyChange support */
    private final PropertyChangeSupport support = new PropertyChangeSupport(this);
    
    /** Debugging flag. */
    private static final boolean DEBUG = Debug.isLoggable(RegistryImpl.class);

    /** Creates new RegistryImpl */
    public RegistryImpl() {
    }
    
    /** Get all opened componets in the system.
     *
     * @return immutable set of {@link TopComponent}s
     */
    public synchronized Set<TopComponent> getOpened() {
        return new SyncSet();
    }
    
    /** Get the currently selected element.
     * @return the selected top component, or <CODE>null</CODE> if there is none
     */
    public TopComponent getActivated() {
        return activatedTopComponent.get();
    }
    
    /** Getter for the currently selected nodes.
     * @return array of nodes or null if no component activated. */
    public Node[] getCurrentNodes() {
        return currentNodes;
    }
    
    /** Getter for the lastly activated nodes. Comparing
     * to previous method it always remembers the selected nodes
     * of the last component that had ones.
     *
     * @return array of nodes (not null)
     */
    public Node[] getActivatedNodes() {
        return activatedNodes == null ? new Node[0] : activatedNodes;
    }
    
    /** Add a property change listener.
     * @param l the listener to add
     */
    public void addPropertyChangeListener(PropertyChangeListener l) {
        support.addPropertyChangeListener(l);
    }
    
    /** Remove a property change listener.
     * @param l the listener to remove
     */
    public void removePropertyChangeListener(PropertyChangeListener l) {
        support.removePropertyChangeListener(l);
    }

    
    //////////////////////////////////////////////////////
    /// notifications of changes from window manager >>>>>
    /** Called when a TopComponent is activated.
     *
     * @param ev TopComponentChangedEvent
     */
    void topComponentActivated(TopComponent tc) {
        if(activatedTopComponent.get() == tc
        && activatedNodes != null) { // When null it means were not inited yet.
            return;
        }
        
        final TopComponent old = activatedTopComponent.get();
        if (old != null && old.getActivatedNodes() != null) {
            previousActivated = new WeakReference<TopComponent>(old);
        }
        activatedTopComponent = new WeakReference<TopComponent>(tc);
        
/** PENDING:  Firing the change asynchronously improves perceived responsiveness
 considerably (toolbars are updated after the component repaints, so it appears
 to immediately become selected), but means that 
 for one EQ cycle the activated TopComponent will be out of sync with the 
 global node selection.  Needs testing. -Tim
 
 C.f. issue 42256 - most of the delay may be called by contention in 
     ProxyLookup, but this fix will have some responsiveness benefits 
     even if that is fixed
*/ 
        final TopComponent tmp = this.activatedTopComponent.get();
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                doFirePropertyChange(PROP_ACTIVATED, old, tmp);
            }
        });

        selectedNodesChanged(tmp, tmp == null ? null : tmp.getActivatedNodes());
    }
    
    
    
    /** Called when a TopComponent is opened. */
    public synchronized void topComponentOpened(TopComponent tc) {
        assert null != tc;
        if (openSet.contains(tc)) {
            return;
        }
        Set<TopComponent> old = new HashSet<TopComponent>(openSet);
        openSet.add(tc);
        doFirePropertyChange(PROP_TC_OPENED, null, tc);
        doFirePropertyChange(PROP_OPENED, old, new HashSet<TopComponent>(openSet));
    }
    
    /** Called when a TopComponent is closed. */
    public synchronized void topComponentClosed(TopComponent tc) {
        if (!openSet.contains(tc)) {
            return;
        }

        Set<TopComponent> old = new HashSet<TopComponent>(openSet);
        openSet.remove(tc);
        doFirePropertyChange(PROP_TC_CLOSED, null, tc);
        doFirePropertyChange(PROP_OPENED, old, new HashSet<TopComponent>(openSet));

        if (activatedNodes != null) {
            Node[] closedNodes = tc.getActivatedNodes();
            if (closedNodes != null && Arrays.equals(closedNodes, activatedNodes)) {
                // The component whose nodes were activated has been closed; cancel the selection.
                activatedNodes = null;
                doFirePropertyChange(PROP_ACTIVATED_NODES, closedNodes, null);
            }
        }
    }

    /**
     * Called during 'Reset Windows' to re-add document windows that the Reset
     * action does not close.
     * @param tc
     */
    public synchronized void addTopComponent( TopComponent tc ) {
        assert null != tc;
        openSet.add( tc );
    }
    
    /** Called when selected nodes changed. */
    public void selectedNodesChanged(TopComponent tc, Node[] newNodes) {
        Node[] oldNodes = currentNodes;
        
        //Fixed bug #8933 27 Mar 2001 by Marek Slama
        //Selected nodes event was processed for other than activated component.
        //Check if activatedTopComponent is the same as event source top component
        //If not ignore event
        if(tc != activatedTopComponent.get()
        && activatedNodes != null) { // When null it means were not inited yet.
            // #82319: update activated nodes from previously active TopComponent 
            // under special conditions
            if (!isProperPrevious(tc, newNodes)) {
                return;
            }
        }
        //End of bugfix #8933
        
        if(Arrays.equals(oldNodes, newNodes)
        && activatedNodes != null) { // When null it means were not inited yet.
            return;
        }

        currentNodes = newNodes == null ? null : newNodes.clone();
        // fire immediatelly only if window manager in proper state
        tryFireChanges(oldNodes, currentNodes);
    }
    
    /** Part of #82319 bugfix.
     * Returns true if given top component is the one previously selected
     * and conditions are met to update activated nodes from it.
     */
    private boolean isProperPrevious (TopComponent tc, Node[] newNodes) {
        if (previousActivated == null || newNodes == null) {
            return false;
        }
        
        TopComponent previousTC = previousActivated.get();
        if (previousTC == null || !previousTC.equals(tc)) {
            return false;
        }

        TopComponent tmp = activatedTopComponent.get();
        return tmp != null && tmp.getActivatedNodes() == null;
    }
    
    /// notifications of changes from window manager <<<<<
    //////////////////////////////////////////////////////

    /** Cancels the menu if it is not assigned to specified window.
     * @param window window that the menu should be checked against
     *    (if this window contains the menu, then the menu will not be closed)
     */
    /** Closes popup menu.
     */
    public static void cancelMenu(Window window) {
        MenuSelectionManager msm = MenuSelectionManager.defaultManager();
        MenuElement[] path = msm.getSelectedPath();
        
        for (int i = 0; i < path.length; i++) {
            //      if (newPath[i] != path[i]) return;
            java.awt.Window w = SwingUtilities.windowForComponent(
                path[i].getComponent()
            );
            
            // we must check for null because windowForComponent above can return null
            if ((w != null) && (w == window || w.getOwner() == window)) {
                // ok, this menu can stay
                return;
            }
            
        }
        if( path.length > 0 )
            msm.clearSelectedPath();
    }
    
    
    /** If window manager in proper state, fire selected and
     * activated node changes */
    private void tryFireChanges(Node[] oldNodes, Node[] newNodes) {
        doFirePropertyChange(PROP_CURRENT_NODES, oldNodes, newNodes);
        
        if(newNodes == null && activatedNodes == null) {
            // Ensure activated nodes are going to be fired first time in session for this case.
            newNodes = new Node[0];
        }
        
        if (newNodes != null) {
            oldNodes = activatedNodes;
            activatedNodes = newNodes;
            support.firePropertyChange(PROP_ACTIVATED_NODES, oldNodes, activatedNodes);
        }
    }
    
    
    private void doFirePropertyChange(final String propName,
    final Object oldValue, final Object newValue) {
        if(DEBUG) {
            debugLog(""); // NOI18N
            debugLog("Scheduling event firing: propName=" + propName); // NOI18N
            debugLog("\toldValue=" + (oldValue instanceof Object[] ? Arrays.asList((Object[])oldValue) : oldValue)); // NOI18N
            debugLog("\tnewValue=" + (newValue instanceof Object[] ? Arrays.asList((Object[])newValue) : newValue)); // NOI18N
        }
        // PENDING When #37529 finished, then uncomment the next row and move the 
        // checks of AWT thread away.
        //  WindowManagerImpl.assertEventDispatchThread();
        if(SwingUtilities.isEventDispatchThread()) {
            support.firePropertyChange(propName, oldValue, newValue);
        } else {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    support.firePropertyChange(propName, oldValue, newValue);
                }
            });
        }
    }
    
    void clear() {
        activatedTopComponent.clear();
        openSet.clear();
        currentNodes = null;
        activatedNodes = null;
    }
    
    private static void debugLog(String message) {
        Debug.log(RegistryImpl.class, message);
    }

    private final class SyncSet implements Set<TopComponent> {
        public int size() {
            synchronized (RegistryImpl.this) {
                return openSet.size();
            }
        }

        public boolean isEmpty() {
            synchronized (RegistryImpl.this) {
                return openSet.isEmpty();
            }
        }

        public boolean contains(Object o) {
            synchronized (RegistryImpl.this) {
                return openSet.contains(o);
            }
        }

        public Iterator<TopComponent> iterator() {
            synchronized (RegistryImpl.this) {
                return new HashSet<TopComponent>(openSet).iterator();
            }
        }

        public Object[] toArray() {
            synchronized (RegistryImpl.this) {
                return openSet.toArray();
            }
        }

        public <T> T[] toArray(T[] a) {
            synchronized (RegistryImpl.this) {
                return openSet.toArray(a);
            }
        }

        public boolean containsAll(Collection<?> c) {
            synchronized (RegistryImpl.this) {
                return openSet.containsAll(c);
            }
        }

        public boolean add(TopComponent e) {
            throw new UnsupportedOperationException();
        }

        public boolean remove(Object o) {
            throw new UnsupportedOperationException();
        }

        public boolean addAll(Collection<? extends TopComponent> c) {
            throw new UnsupportedOperationException();
        }

        public boolean retainAll(Collection<?> c) {
            throw new UnsupportedOperationException();
        }

        public boolean removeAll(Collection<?> c) {
            throw new UnsupportedOperationException();
        }

        public void clear() {
            throw new UnsupportedOperationException();
        }
    }
}
