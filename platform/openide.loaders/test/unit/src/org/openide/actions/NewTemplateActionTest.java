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

package org.openide.actions;


import javax.swing.Action;
import javax.swing.JPopupMenu;
import junit.textui.TestRunner;

import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;

import org.openide.loaders.TemplateWizard;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.Utilities;
import org.openide.util.lookup.*;
/** Test creating NewTemplateAction by context.
 *  See issue 28785.
 */
public class NewTemplateActionTest extends NbTestCase {
    public NewTemplateActionTest(String name) {
        super(name);
    }
    
    public void testContextAware () {
        NewTemplateAction global = NewTemplateAction.get(NewTemplateAction.class);
        
        CookieNode node = new CookieNode ();
        JPopupMenu popup = Utilities.actionsToPopup (new Action[] {
            global
        }, node.getLookup ());
        
        assertTrue ("NewTemplateAction's cookie must be called.", node.counter > 0);
        
        global.getPopupPresenter ();
        
        assertTrue ("When calling wizard on global action, the CookieNode's cookie is not " +
            "as it is not selected", node.counter > 0
        );
    }

    public void testContextAwareWithChanges () {
        doContextAwareWithChanges (false);
    }
    public void testContextAwareWithChangesWithDeepGC () {
        doContextAwareWithChanges (true);
    }
    
    private void doContextAwareWithChanges (boolean withGC) {
        class P implements Lookup.Provider {
            private Lookup lookup = Lookup.EMPTY;
            
            public Lookup getLookup () {
                return lookup;
            }
        }
        P provider = new P ();
        Lookup lookup = Lookups.proxy (provider);
        
        NewTemplateAction global = NewTemplateAction.get(NewTemplateAction.class);
        Action clone = global.createContextAwareInstance (lookup);
        CookieNode node = new CookieNode ();
        
        //assertTrue ("Global is enabled", global.isEnabled ());
        assertFalse ("Local is not enabled if no nodes provided", clone.isEnabled ());
        
        JPopupMenu popup = Utilities.actionsToPopup (new Action[] {
            global
        }, lookup);
        
        if (withGC) {
            try {
                assertGC ("Will fail", new java.lang.ref.WeakReference (this));
            } catch (Throwable t) {
            }
        }
        
        assertFalse ("No node selected, no query", node.counter > 0);
        
        provider.lookup = node.getLookup ();
        lookup.lookup (Object.class); // does refresh
        
        assertTrue ("After change of Lookup the CookieNode is queried for cookie", node.counter > 0);
        assertTrue ("Local is enabled if a node is provided", clone.isEnabled ());
    }
    
    private static class CookieNode extends AbstractNode implements NewTemplateAction.Cookie {
        public CookieNode () {
            super (Children.LEAF);
            getCookieSet ().add (this);
        }
        
        int counter = 0;
        public TemplateWizard getTemplateWizard () {
            counter ++;
            return new TemplateWizard ();
        }
    }
}
