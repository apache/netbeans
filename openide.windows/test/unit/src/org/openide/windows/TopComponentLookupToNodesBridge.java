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

package org.openide.windows;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collections;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import org.netbeans.junit.NbTestCase;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 * Checks behaviour of a bridge that listens on a provided lookup
 * and initializes activated nodes according to the nodes in the
 * lookup.
 *
 * @author Jaroslav Tulach, Jesse Glick
 */
public final class TopComponentLookupToNodesBridge extends NbTestCase {
    /** own action map */
    protected ActionMap map;
    /** top component we work on */
    protected TopComponent top;
    /** instance in the lookup */
    protected InstanceContent ic;
    /** its lookup */
    protected Lookup lookup;
    
    public TopComponentLookupToNodesBridge(String testName) {
        super(testName);
    }
    
    protected boolean runInEQ() {
        return true;
    }
    
    /** Setup component with lookup.
     */
    protected void setUp() {
        System.setProperty("org.openide.util.Lookup", "-");
        
        map = new ActionMap();
        ic = new InstanceContent();
        ic.add(map);
        
        lookup = new AbstractLookup(ic);
        top = new TopComponent(lookup);
    }
    
    
    public void testTheLookupIsReturned() {
        assertEquals("Lookup provided to TC in constructor is returned", lookup, top.getLookup());
    }
    
    public void testActionMapIsTakenFromTheLookupIfProvided() {
        Action a1 = new Action();
        map.put("key", a1);
        
        assertEquals("Action map is set", a1, top.getActionMap().get("key"));
        
        ActionMap another = new ActionMap();
        
        ic.set(Collections.singleton(another), null);
        assertEquals("And is not changed (right now) if modified in list", a1, top.getActionMap().get("key"));
    }
    
    public void testEmptyLookupGeneratesZeroLengthArray() {
        assertNotNull("Array is there", top.getActivatedNodes());
        assertEquals("No nodes", 0, top.getActivatedNodes().length);
    }
    
    public void testNodeIsThereIfInLookup() {
        class Listener implements PropertyChangeListener {
            public int cnt;
            public String name;
            
            public void propertyChange(PropertyChangeEvent ev) {
                cnt++;
                name = ev.getPropertyName();
            }
        }
        
        Listener l = new Listener();
        top.addPropertyChangeListener(l);
        
        ic.add(Node.EMPTY);
        
        assertNotNull("Array exists", top.getActivatedNodes());
        assertEquals("One node", 1, top.getActivatedNodes().length);
        assertEquals("The node", Node.EMPTY, top.getActivatedNodes()[0]);
        assertEquals("One PCE", 1, l.cnt);
        assertEquals("Name of property", "activatedNodes", l.name);
        
        
        ic.set(Collections.nCopies(2, Node.EMPTY), null);
        
        assertEquals("Two nodes", 2, top.getActivatedNodes().length);
        assertEquals("The same", Node.EMPTY, top.getActivatedNodes()[0]);
        assertEquals("The same", Node.EMPTY, top.getActivatedNodes()[1]);
        assertEquals("second PCE change", 2, l.cnt);
        assertEquals("Name of property", "activatedNodes", l.name);
        
    }
    
    private static final class Action extends AbstractAction {
        public void actionPerformed(ActionEvent e) {
        }
    }
}
