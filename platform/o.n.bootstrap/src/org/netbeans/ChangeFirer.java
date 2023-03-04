/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans;

import java.util.logging.Level;
import org.openide.util.BaseUtilities;
import java.util.*;

/** Thread which fires changes in the modules.
 * Used to separate property change events and
 * lookup changes from the dynamic scope of the
 * changes themselves. Also to batch up possible
 * changes and avoid firing duplicates.
 * Accepts changes at any time
 * and fires them from within the mutex (as a reader).
 * @author Jesse Glick
 */
final class ChangeFirer {

    private final ModuleManager mgr;
    // Pending things to perform:
    private final Set<Change> changes = new LinkedHashSet<Change>(100);
    private final Set<Module> modulesCreated = new HashSet<Module>(100);
    private final Set<Module> modulesDeleted = new HashSet<Module>(10);
    
    /** Make a new change firer.
     * @param mgr the associated module manager
     */
    public ChangeFirer(ModuleManager mgr) {
        this.mgr = mgr;
    }
    
    /** Add a change to the list of pending things to be fired.
     * @param c the change which will be fired
     */
    public void change(Change c) {
        changes.add(c);
    }
    
    /** Add a module creation event to the list of pending things to be fired.
     * @param m the module whose creation event will be fired
     */
    public void created(Module m) {
        modulesCreated.add(m);
    }
    
    /** Add a module deletion event to the list of pending things to be fired.
     * Note that this will cancel any pending creation event for the same module!
     * @param m the module whose creation event will be fired
     */
    public void deleted(Module m) {
        // Possible that a module was added and then removed before any change
        // was fired; in this case skip it.
        if (! modulesCreated.remove(m)) {
            modulesDeleted.add(m);
        }
    }
    
    /** Fire all pending changes.
     * While this is happening, the manager is locked in a read-only mode.
     * Should only be called from within a write mutex!
     */
    public void fire() {
        mgr.readOnly(true);
        try {
            for (Change c: changes) {
                if (c.source instanceof Module) {
                    ((Module) c.source).firePropertyChange0(c.prop, c.old, c.nue);
                } else if (c.source == mgr) {
                    mgr.firePropertyChange(c.prop, c.old, c.nue);
                } else {
                    throw new IllegalStateException("Strange source: " + c.source); // NOI18N
                }
            }
            changes.clear();
            if (! modulesCreated.isEmpty() || ! modulesDeleted.isEmpty()) {
                mgr.fireModulesCreatedDeleted(modulesCreated, modulesDeleted);
            }
            modulesCreated.clear();
            modulesDeleted.clear();
        } catch (RuntimeException e) {
            // Recover gracefully.
            Util.err.log(Level.SEVERE, null, e);
        } finally {
            mgr.readOnly(false);
        }
    }
    
    /** Possible change event to be fired.
     * Used instead of PropertyChangeEvent as it can be stored in a set.
     */
    public static final class Change {
        public final String prop;
        public final Object source, old, nue;
        public Change(Object source, String prop, Object old, Object nue) {
            this.source = source;
            this.prop = prop;
            this.old = old;
            this.nue = nue;
        }
        // Semantic equality, to avoid duplicate changes:
        public boolean equals(Object o) {
            if (! (o instanceof Change)) return false;
            Change c = (Change) o;
            return BaseUtilities.compareObjects(prop, c.prop) &&
                   BaseUtilities.compareObjects(source, c.source) &&
                   BaseUtilities.compareObjects(old, c.old) &&
                   BaseUtilities.compareObjects(nue, c.nue);
        }
        public int hashCode() {
            return source.hashCode() ^ (prop == null ? 0 : prop.hashCode());
        }
        public String toString() {
            return "Change[" + source + ":" + prop + ";" + old + "->" + nue + "]"; // NOI18N
        }
    }
    
}
