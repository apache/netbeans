/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.jumpto.type;

import java.io.IOException;
import junit.framework.TestCase;
import org.netbeans.junit.MockServices;
import org.netbeans.spi.jumpto.type.TypeDescriptor;
import org.netbeans.spi.jumpto.type.TypeProvider;

/**
 *
 * @author Pavel Flaska
 */
public class GoToTypeActionTest extends TestCase {
    private static GoToPanel panel;
    
    public GoToTypeActionTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        MockServices.setServices(TestTypeProvider.class);
    }

    public void test1() throws IOException {
        GoToTypeAction action = new GoToTypeAction();
        Iterable<? extends TypeDescriptor> desc = action.getSelectedTypes(false);
        panel = action.panel;
        panel.nameField.setText("Pepik");
        action.waitSearchFinished();
        assertEquals("Provider queried once", 1, TestTypeProvider.count);
    }

    public void testPendingResults() throws IOException {
        TestTypeProvider.count = 10;
        GoToTypeAction action = new GoToTypeAction();
        Iterable<? extends TypeDescriptor> desc = action.getSelectedTypes(false);
        panel = action.panel;
        panel.nameField.setText("Pepik");
        action.waitSearchFinished();
        assertEquals("Provider queried once", 12, TestTypeProvider.count);
    }
    
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public static class TestTypeProvider implements TypeProvider {
        private static int count;

        public String name() {
            return "Test";
        }

        public String getDisplayName() {
            return "Test";
        }

        public void computeTypeNames(Context context, Result result) {
            final String text = panel.messageLabel.getText();
            assertTrue(text, text.startsWith("Searching"));
            count++;
            if (count == 10) result.pendingResult();
        }

        public void cancel() {
        }

        public void cleanup() {
        }
        
    }

}
