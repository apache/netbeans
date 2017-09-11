/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.openide.awt;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.Action;
import org.netbeans.junit.NbTestCase;
import org.openide.nodes.Node;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.Lookups;
import org.openide.windows.TopComponent;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class ContextManagerIntegrationTest extends NbTestCase 
implements Lookup.Provider {
    private TopComponent currentTC;
    
    public ContextManagerIntegrationTest(String name) {
        super(name);
    }
    
    @Override
    protected boolean runInEQ() {
        return true;
    }
    
    public void testSurviveFocusChange() throws Exception {
        InstanceContent ic = new InstanceContent();
        final Lookup lkp = new AbstractLookup(ic);
        final TopComponent tc = new TopComponent() {
            {
                associateLookup(lkp);
            }
        };
        currentTC = tc;

        Lookup revolving = Lookups.proxy(this);
        
        Action clone = ((ContextAwareAction) Actions.forID("cat33", "survive1")).createContextAwareInstance(revolving);
        L listener = new L();
        clone.addPropertyChangeListener(listener);
        
        assertFalse("Disabled", clone.isEnabled());
        Object val = Integer.valueOf(1);
        ic.add(val);
        assertTrue("Enabled now", clone.isEnabled());
        assertEquals("One change", 1, listener.cnt);
        
        currentTC = new TopComponent();
        currentTC.setActivatedNodes(null);
        revolving.lookup(Object.class);
        
        assertTrue("Still Enabled", clone.isEnabled());
        final Node[] arr = new Node[] { Node.EMPTY.cloneNode() };
        currentTC.setActivatedNodes(arr);
        revolving.lookup(Object.class);
        
        assertFalse("Disabled now", clone.isEnabled());
        
        Survival.value = 0;
        clone.actionPerformed(new ActionEvent(this, 0, ""));
        assertEquals("No change", 0, Survival.value);
        
        currentTC = tc;
        revolving.lookup(Object.class);
        assertTrue("Enabled again", clone.isEnabled());
        
        clone.actionPerformed(new ActionEvent(this, 0, ""));
        assertEquals("Incremented", 1, Survival.value);
        
        currentTC = new TopComponent();
        currentTC.setActivatedNodes(null);
        revolving.lookup(Object.class);
        
        clone.actionPerformed(new ActionEvent(this, 0, ""));
        assertEquals("Incremented again", 2, Survival.value);
    }

    @Override
    public Lookup getLookup() {
        return currentTC.getLookup();
    }
    
    private static class L implements PropertyChangeListener {
        int cnt;

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            cnt++;
        }
    }
    
    @ActionID(category="cat33", id="survive1")
    @ActionRegistration(displayName="Survive", surviveFocusChange=true)
    public static final class Survival implements ActionListener {
        static int value;
        
        private Integer context;

        public Survival(Integer context) {
            this.context = context;
        }

        @Override
        public void actionPerformed(ActionEvent ae) {
            value += context;
        }
    }
}
