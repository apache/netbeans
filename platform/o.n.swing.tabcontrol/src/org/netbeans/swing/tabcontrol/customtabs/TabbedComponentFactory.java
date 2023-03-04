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

package org.netbeans.swing.tabcontrol.customtabs;

import org.netbeans.swing.tabcontrol.WinsysInfoForTabbedContainer;

/**
 * Service interface used by the Window System for creating NetBeans specific
 * Tabbed Containers. Use this if you want to provide an alternative implementation
 * e.g. based on JTabbedPane:
        <pre>
            class MyTabbedPane extends JTabbedPane implements Tabbed.Accessor {
                private final Tabbed tabbedImpl = new Tabbed() {
                    //implement abstract methods while delegating most of them to JTabbedPane
                };

                public Tabbed getTabbed() {
                    return tabbedImpl;
                }
            }
        </pre>

        <p>Then inject your new implementation to the window system by registering
        your own TabbedComponentFactory:</p>
        <pre>
            &#64;ServiceProvider(service=TabbedComponentFactory.class,position=1000)
            public class MyTabbedPaneFactory implements TabbedComponentFactory {
                public Tabbed createTabbedComponent( TabbedType type, WinsysInfoForTabbedContainer info ) {
                    return new MyTabbedPane().getTabbed();
                }
            }
        </pre>
 *
 * @since 1.33
 * @author S. Aubrecht
 */
public interface TabbedComponentFactory {
    /**
     * Create Tabbed implementation for given type.
     *
     * @param type Type of the container to be created.
     * @param info Information from the window system that may affect the look
     * and feel of the tab control.
     * @return New Tabbed instance.
     */
    public Tabbed createTabbedComponent( TabbedType type, WinsysInfoForTabbedContainer info );
}
