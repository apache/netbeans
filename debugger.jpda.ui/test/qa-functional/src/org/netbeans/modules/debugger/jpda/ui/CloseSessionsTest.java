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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

/**
 *
 * @author cyhelsky, jtulach, mentlicher
 */

package org.netbeans.modules.debugger.jpda.ui;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.List;
import junit.framework.Test;
import junit.textui.TestRunner;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.modules.debugger.actions.DebugJavaFileAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.SourcePackagesNode;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbTestCase;


public class CloseSessionsTest extends DebuggerTestCase {

    public CloseSessionsTest(String name) {
        super(name);
    }

    public static void main(String[] args) {
        TestRunner.run(suite());
    }


    public static Test suite() {
        return NbModuleSuite.create(
            NbModuleSuite.createConfiguration(CloseSessionsTest.class).addTest(
            "testAllSessionsClosed"
            ).enableModules(".*").clusters(".*"));
    }

    /** setUp method  */
    @Override
    public void setUp() throws IOException {
        super.setUp();
        System.out.println("########  " + getName() + "  #######");
    }

    @Override
    public void tearDown() {
        JemmyProperties.getCurrentOutput().printTrace("\nteardown\n");        
    }



    public void testAllSessionsClosed() {        
        //open source
        Node beanNode = new Node(new SourcePackagesNode(Utilities.testProjectName), "examples.advanced|MemoryView.java"); //NOI18N
        new OpenAction().perform(beanNode); // NOI18N
        EditorOperator eo = new EditorOperator("MemoryView.java");
        try {
            eo.clickMouse(50,50,1);
        } catch (Throwable t) {
            System.err.println(t.getMessage());
        }
        new EventTool().waitNoEvent(1000);
        //place breakpoint
        Utilities.toggleBreakpoint(eo, 104);
        //start debugging
        new DebugJavaFileAction().perform(beanNode);
        //wait for breakpoint
        Utilities.waitStatusText("Thread main stopped at MemoryView.java:104");
        List<? extends JPDADebugger> list = DebuggerManager
                .getDebuggerManager()
                .getCurrentSession()
                .lookup(null, JPDADebugger.class);
        JPDADebugger debugger = list.get(0);
        WeakReference<? extends JPDADebugger> debuggerRef = new WeakReference(debugger);

        //finish debugging
        Utilities.endAllSessions();
        //close sources
        eo.close();
        //nulling all temporary variables which could hold some references to debugger
        list = null;
        debugger = null;
        System.gc();
        try {
            NbTestCase.assertGC("All the debugging sessions were not correctly closed", debuggerRef);
        } catch (OutOfMemoryError u) {
            System.out.println(u.getMessage());
        }
    }
}

