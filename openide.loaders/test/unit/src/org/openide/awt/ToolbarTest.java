/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.openide.awt;

import java.awt.EventQueue;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.ToolBarUI;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class ToolbarTest extends NbTestCase  {
    
    public ToolbarTest(String n) {
        super(n);
    }

    public void testInitOutsideOfEDT() throws Exception {
        class MyToolbar extends Toolbar implements Runnable {

            @Override
            protected void setUI(ComponentUI newUI) {
                assertTrue("Can only be called in EDT", EventQueue.isDispatchThread());
                super.setUI(newUI);
            }

            @Override
            public void setUI(ToolBarUI ui) {
                assertTrue("Can only be called in EDT", EventQueue.isDispatchThread());
                super.setUI(ui);
            }

            private void assertUI() throws Exception {
                EventQueue.invokeAndWait(this);
            }

            @Override
            public void run() {
                assertNotNull("UI delegate is specified", getUI());
            }
        }
        
        assertFalse("We are not in EDT", EventQueue.isDispatchThread());
        MyToolbar mt = new MyToolbar();
        assertNotNull("Instance created", mt);
        
        mt.assertUI();
    }
}
