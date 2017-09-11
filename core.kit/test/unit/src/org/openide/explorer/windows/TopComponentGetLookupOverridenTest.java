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

package org.openide.explorer.windows;

import java.util.Arrays;
import java.util.logging.Logger;
import javax.swing.ActionMap;
import javax.swing.text.DefaultEditorKit;


import org.openide.explorer.*;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.windows.TopComponent;



/** Tests behaviour of GlobalContextProviderImpl
 * and its cooperation with activated and current nodes when TopComponent is
 * using its own lookup as in examples of ExplorerUtils...
 *
 * @author Jaroslav Tulach
 */
public class TopComponentGetLookupOverridenTest extends TopComponentGetLookupTest {
    private Logger LOG = Logger.getLogger(TopComponentGetLookupOverridenTest.class + ".TEST-" + getName());

    public TopComponentGetLookupOverridenTest (java.lang.String testName) {
        super(testName);
    }
    
    /** Setup component with lookup.
     */
    protected void setUp () {
        ListingYourComponent tc = new ListingYourComponent (LOG);
        top = tc;
        get = tc.delegate;
        lookup = tc.delegate.getLookup ();
    }


    private static class ListingYourComponent extends TopComponent
    implements java.beans.PropertyChangeListener {
        YourComponent delegate;
        private Logger LOG;

        public ListingYourComponent (Logger l) {
            delegate = new YourComponent();
            LOG = l;
            
            addPropertyChangeListener (this);
            delegate.getExplorerManager ().setRootContext (new AbstractNode (new Children.Array ()));
            java.lang.ref.SoftReference ref = new java.lang.ref.SoftReference (new Object ());
            assertGC ("Trying to simulate issue 40842, to GC TopComponent$SynchronizeNodes", ref);
            
            delegate.getExplorerManager().addPropertyChangeListener(this);
        }
        
        private ThreadLocal callbacks = new ThreadLocal ();
        public void propertyChange (java.beans.PropertyChangeEvent ev) {
            ExplorerManager manager = delegate.getExplorerManager ();

            LOG.info("propertyChange: " + ev.getPropertyName());

            if ("activatedNodes".equals (ev.getPropertyName())) {
                if (Boolean.TRUE.equals (callbacks.get ())) {
                    LOG.info("  it was callback");
                    return;
                }
                try {
                    callbacks.set (Boolean.TRUE);
                    Node[] arr = getActivatedNodes ();

                    LOG.info("settings ndoes to zero");
                    // first of all clear the previous values otherwise
                    // we will not test SynchronizeNodes (associateLookup (..., true))
                    setActivatedNodes (ownNode());


                    Children.Array ch = (Children.Array)manager.getRootContext ().getChildren ();
                    for (int i = 0; i < arr.length; i++) {
                        if (arr[i].getParentNode() != manager.getRootContext()) {
                            assertTrue ("If this fails we are in troubles", ch.add (new Node[] { arr[i] }));
                        }
                    }
                    LOG.info("em setSelectedNodes: " + Arrays.asList(arr));
                    manager.setSelectedNodes (arr);
                    LOG.info("em setSelectedNodes done: " + Arrays.asList(arr));
                } catch (java.beans.PropertyVetoException ex) {
                    ex.printStackTrace();
                    fail (ex.getMessage());
                } finally {
                    callbacks.set (null);
                }
            }

        }

        public String toString() {
            return "ListingYourComponent";
        }


        private static Node[] ownNode() {
            AbstractNode a = new AbstractNode(Children.LEAF);
            a.setName("ownNode");
            return new Node[] { a };
        }
    } // end of ListingYourComponent
    
    // The following class is copied from example in ExplorerUtils:
    //
    public static class YourComponent extends TopComponent
    implements ExplorerManager.Provider, Lookup.Provider {
        private ExplorerManager manager;
        public YourComponent() {
            this.manager = new ExplorerManager ();
            ActionMap map = getActionMap ();
            map.put(DefaultEditorKit.copyAction, ExplorerUtils.actionCopy(manager));
            map.put(DefaultEditorKit.cutAction, ExplorerUtils.actionCut(manager));
            map.put(DefaultEditorKit.pasteAction, ExplorerUtils.actionPaste(manager));
            map.put("delete", ExplorerUtils.actionDelete(manager, true)); // or false
            
            associateLookup (ExplorerUtils.createLookup (manager, map));
        }
        public ExplorerManager getExplorerManager() {
            return manager;
        }
        // It is good idea to switch all listeners on and off when the
        // component is shown or hidden. In the case of TopComponent use:
        protected void componentActivated() {
            ExplorerUtils.activateActions(manager, true);
        }
        protected void componentDeactivated() {
            ExplorerUtils.activateActions(manager, false);
        }
        public String toString() {
            return "YourComponent";
        }
    } // end of YourComponent
}  
    
