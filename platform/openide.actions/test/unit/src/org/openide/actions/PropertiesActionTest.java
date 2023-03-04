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
