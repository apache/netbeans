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

package org.openide.actions;

import org.netbeans.junit.NbTestCase;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import javax.swing.Action;
import org.openide.nodes.Node.Property;
import org.openide.nodes.Node.PropertySet;
import org.openide.util.actions.SystemAction;

/** Issue 68299.
 *
 * @author Jiri Rechtacek
 */
public class PropertiesActionTest extends NbTestCase {
    public PropertiesActionTest (String testName) {
        super (testName);
    }

    protected void setUp () throws Exception {
    }

    protected void tearDown () throws Exception {
    }

    public void testEnableOnEmptyProperties () throws Exception {
        testEnable (new PropertySet [0]);
    }

    public void testEnableOnNullProperties () throws Exception {
        testEnable (null);
    }
    
    public void testEnableOnNotNullProperties () throws Exception {
        PropertySet [] s = new PropertySet [] { new PropertySet () {
                        public Property[] getProperties () {
                            Property p = new Property<String>(String.class) {
                                public boolean canRead () {
                                    return true;
                                }
                                public boolean canWrite () {
                                    return true;
                                }
                                public String getValue() {
                                    return null;
                                }
                                public void setValue(String val) {}
                            };
                            return new Property [] { p };
                        }
                    } };

        testEnable (s);
    }
    
    private void testEnable (final PropertySet [] pros) throws Exception {
        Node n = new AbstractNode (Children.LEAF) {
            public PropertySet [] getPropertySets () {
                return pros;
            }
        };
        
        
        assertEquals ("Node has the given properties.", pros, n.getPropertySets ());
        
        PropertiesAction pa = SystemAction.get(PropertiesAction.class);
        Action a = pa.createContextAwareInstance (n.getLookup ());
        
        assertTrue ("PropertiesAction is enabled.", a.isEnabled ());
    }
    
}
