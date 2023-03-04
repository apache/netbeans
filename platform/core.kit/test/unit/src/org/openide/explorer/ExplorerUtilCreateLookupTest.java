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

package org.openide.explorer;



import org.openide.nodes.*;

/**
 * Check the behaviour of ExplorerManager's lookup by doing the same 
 * operations as in case of TopComponent's lookup. Done by providing a fake 
 * component that converts setActivatedNodes to ExplorerManager calls.
 *
 * @author Jaroslav Tulach
 */
public class ExplorerUtilCreateLookupTest extends org.openide.explorer.windows.TopComponentGetLookupTest {
    public ExplorerUtilCreateLookupTest(String testName) {
        super(testName);
    }
    
    @Override
    protected boolean runInEQ () {
        return true;
    }
    
    /** Setup component with lookup.
     */
    @Override
    protected void setUp () {
        class ExTC extends org.openide.windows.TopComponent 
        implements java.beans.PropertyChangeListener {
            ExplorerManager em = new ExplorerManager ();
            {
                addPropertyChangeListener (this);
                em.setRootContext (new AbstractNode (new Children.Array ()));
            }
            
            @Override
            public void propertyChange (java.beans.PropertyChangeEvent ev) {
                if ("activatedNodes".equals (ev.getPropertyName())) {
                    try {
                        Node[] arr = getActivatedNodes ();
                        Children.Array ch = (Children.Array)em.getRootContext ().getChildren ();
                        for (int i = 0; i < arr.length; i++) {
                            if (arr[i].getParentNode() != em.getRootContext()) {
                                assertTrue ("If this fails we are in troubles", ch.add (new Node[] { arr[i] }));
                            }
                        }
                        em.setSelectedNodes (getActivatedNodes ());
                    } catch (java.beans.PropertyVetoException ex) {
                        throw new AssertionError(ex);
                    }
                }
            }
        }
        ExTC e = new ExTC ();
        
        top = e;
        get = e;
        lookup = ExplorerUtils.createLookup (e.em, e.getActionMap ());
    }
    
}
