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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
