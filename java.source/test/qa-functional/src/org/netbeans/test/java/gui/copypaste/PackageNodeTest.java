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

package org.netbeans.test.java.gui.copypaste;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import junit.framework.Test;
import junit.textui.TestRunner;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.actions.CopyAction;
import org.netbeans.jellytools.actions.PasteAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.TestOut;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.test.java.JavaTestCase;
import org.netbeans.test.java.Utilities;
import org.netbeans.test.java.gui.GuiUtilities;


/**
 * Tests copy and paste operations on a package node.
 * @author Roman Strobl
 */
public class PackageNodeTest extends JavaTestCase {
    
    // name of sample project
    private static final String TEST_PROJECT_NAME = "default";
    
    // path to sample files
    private static final String TEST_PACKAGE_PATH =
            "org.netbeans.test.java.gui.copypaste";
    
    // name of sample package
    private static final String TEST_PACKAGE_NAME = TEST_PACKAGE_PATH+".test";
    
    // name of sample package 2
    private static final String TEST_PACKAGE_NAME_2 =
            TEST_PACKAGE_PATH+".test2";
    
    // name of sample class
    private static final String TEST_CLASS_NAME = "TestClass";
    
    /**
     * error log
     */
    protected static PrintStream err;
    
    /**
     * standard log
     */
    protected static PrintStream log;
    
    // workdir, default /tmp, changed to NBJUnit workdir during test
    private String workDir = "/tmp";
    
    // actual directory with project
    private static String projectDir;
    
    
    /**
     * Needs to be defined because of JUnit
     * @param name Name of test
     */
    public PackageNodeTest(String name) {
        super(name);
    }
    
    /**
     * Main method for standalone execution.
     * @param args the command line arguments
     */
    public static void main(java.lang.String[] args) {
        TestRunner.run(suite());
    }
    
    /**
     * Sets up logging facilities.
     */
    public void setUp() throws Exception {
        super.setUp();
        System.out.println("########  "+getName()+"  #######");
        err = getLog();
        log = getRef();
        JemmyProperties.getProperties().setOutput(new TestOut(null,
                new PrintWriter(err, true), new PrintWriter(err, false), null));
        try {
            File wd = getWorkDir();
            workDir = wd.toString();
        } catch (IOException e) { }
    }
    
    /**
     * Tests copy and paste operations on a package.
     */
    public void testCopyPaste() {
        Node pn = new ProjectsTabOperator().getProjectRootNode(
                TEST_PROJECT_NAME);
        pn.select();
        
        // perform copy
        Node n = new Node(pn, org.netbeans.jellytools.Bundle.getString(
                "org.netbeans.modules.java.j2seproject.Bundle",
                "NAME_src.dir")+"|"+TEST_PACKAGE_NAME);
        n.select();
        Utilities.takeANap(3000);
        new CopyAction().perform();
        
        // perform paste
        Node n2 = new Node(pn, org.netbeans.jellytools.Bundle.getString(
                "org.netbeans.modules.java.j2seproject.Bundle",
                "NAME_test.src.dir"));
        
        n2.select();
        new PasteAction().perform();
        
        Utilities.takeANap(1000);
        
        // check if created node exits
        GuiUtilities.waitForChildNode(TEST_PROJECT_NAME,
                org.netbeans.jellytools.Bundle.getString(
                "org.netbeans.modules.java.j2seproject.Bundle",
                "NAME_test.src.dir"), TEST_PACKAGE_NAME);
        Node n3 = new Node(pn, org.netbeans.jellytools.Bundle.getString(
                "org.netbeans.modules.java.j2seproject.Bundle",
                "NAME_test.src.dir")+"|"+TEST_PACKAGE_NAME);
        n3.select();
    }
    
    public static Test suite() {
        return NbModuleSuite.create(
                NbModuleSuite.createConfiguration(PackageNodeTest.class).enableModules(".*").clusters(".*"));
    }

}
