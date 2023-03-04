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

package org.netbeans.modules.options.classic;

import org.openide.actions.PropertiesAction;
import org.openide.actions.ToolsAction;
import org.openide.loaders.DataFolder;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.Mutex;
import org.openide.util.actions.SystemAction;

/** This object represents environment settings in the Corona system.
* This class is final only for performance purposes.
* Can be unfinaled if desired.
*
* @author Petr Hamernik, Dafe Simonek
*/
final class EnvironmentNode {
    /** generated Serialized Version UID */
    static final long serialVersionUID = 4782447107972624693L;
    /** name of section to filter */
    private String filter;
    /** map between type of node and the parent node for this type */
    private static java.util.HashMap<String, Node> types = new java.util.HashMap<String, Node> (11);
    /** A lock for the find method. */
    private static final Object lock = new Object();
    /** Type to add an entry to the Session settings. */
    public static final String TYPE_SESSION = "session"; // NOI18N
    
    /** Finds the node for given name.
     */
    public static Node find (final String name) {
        // XXX this is probably obsolete? consider deleting
        Node retValue = 
            Children.MUTEX.readAccess(new Mutex.Action<Node>() {
                public Node run() {
                    synchronized (lock) {
                        Node n = types.get (name);
                        if (n == null) {
                            DataFolder folder = null;
                            assert TYPE_SESSION.equals(name) : name;
                            folder = NbPlaces.findSessionFolder("UI/Services"); // NOI18N

                            n = new PersistentLookupNode(name, folder);
                            types.put (name, n);
                        }
                        return n;
                    }
                }
            });
        if (retValue != null) {
            return retValue;
        }
        throw new IllegalStateException();
    }
        

    public HelpCtx getHelpCtx () {
        return new HelpCtx (EnvironmentNode.class);
    }

    /** Getter for set of actions that should be present in the
    * popup menu of this node. This set is used in construction of
    * menu returned from getContextMenu and specially when a menu for
    * more nodes is constructed.
    *
    * @return array of system actions that should be in popup menu
    */
    public SystemAction[] createActions () {
        return new SystemAction[] {
                   SystemAction.get(ToolsAction.class),
                   SystemAction.get(PropertiesAction.class)
               };
    }

    /** Adds serialization support to LookupNode */
    private static final class PersistentLookupNode extends LookupNode 
    implements java.beans.PropertyChangeListener {
        
        private String filter;
        
        public PersistentLookupNode (String filter, DataFolder folder) {
            super(folder);
            this.filter = filter;
        }
        
        public Node.Handle getHandle () {
            return new EnvironmentHandle (filter);
        }

        /** Listens on changes on root nodes. */
        public void propertyChange(java.beans.PropertyChangeEvent evt) {
            if(DataFolder.PROP_CHILDREN.equals(evt.getPropertyName())) {
                NbPlaces.getDefault().fireChange();
            }
        }
    } // end of PersistentLookupNode

    static final class EnvironmentHandle implements Node.Handle {
        static final long serialVersionUID =-850350968366553370L;
        
        /** field */
        private String filter;
        
        /** constructor */
        public EnvironmentHandle (String filter) {
            this.filter = filter;
        }
        public Node getNode () {
            String f = filter;
            if (f == null) {
                // use the original node
                f = TYPE_SESSION;
            }
            
            return find (f);
        }
    }
}
