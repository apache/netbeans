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
