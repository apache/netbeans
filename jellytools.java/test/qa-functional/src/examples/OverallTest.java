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
package examples;

import junit.framework.Test;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.NewJavaFileNameLocationStepOperator;
import org.netbeans.jellytools.NewFileWizardOperator;
import org.netbeans.jellytools.OutputTabOperator;
import org.netbeans.jellytools.actions.Action;
import org.netbeans.jellytools.actions.CompileJavaAction;
import org.netbeans.jellytools.nodes.JavaNode;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.SourcePackagesNode;

/** Test of java class life cycle - create, edit, compile, run and delete
 * java main class.
 * 
 * @author Jiri Skrivanek
 */
public class OverallTest extends JellyTestCase {

    static final String[] tests = {
        "testCreate",
        "testEdit",
        "testCompile",
        "testRun",
        "testDelete"
    };

    /** Constructor required by JUnit. */
    public OverallTest(String testName) {
        super(testName);
    }

    /** Creates suite from particular test cases. */
    public static Test suite() {
        return createModuleTest(OverallTest.class, tests);
    }

    @Override
    public void setUp() throws Exception {
        System.out.println("########  " + getName() + "  #######");
        openDataProjects("SampleProject");
    }

    /** Create java main class
     * - in project SampleProject on sample1 node open New File Wizard
     * - choose category "Java" and "Java Main Class" file type
     * - set class name
     * - finish the wizard
     */
    public void testCreate() {
        Node sample1Node = new Node(new SourcePackagesNode("SampleProject"), "sample1");
        NewFileWizardOperator.invoke(sample1Node, "Java", "Java Main Class");
        NewJavaFileNameLocationStepOperator nameStepOper = new NewJavaFileNameLocationStepOperator();
        nameStepOper.setObjectName("SampleClass");
        nameStepOper.finish();
    }

    /** Edit java class
     * - find editor
     * - set caret to the end of line 27
     * - insert simple code 
     */
    public void testEdit() {
        EditorOperator editor = new EditorOperator("SampleClass");
        editor.replace("// TODO code application logic here", "System.out.println(\"Hello, world!\");");
    }

    /** Compile java class
     * - find SampleClass.java node
     * - call "Compile File" context menu on it
     * - wait proper message is printed out to the status bar
     */
    public void testCompile() {
        // start to track Main Window status bar
        MainWindowOperator.StatusTextTracer stt = MainWindowOperator.getDefault().getStatusTextTracer();
        stt.start();
        Node sample1Node = new Node(new SourcePackagesNode("SampleProject"), "sample1");
        Node sampleClassNode = new Node(sample1Node, "SampleClass.java");
        // call "Compile File" popup menu item
        new CompileJavaAction().perform(sampleClassNode);
        // wait message "Building SampleProject (compile-single)..."
        stt.waitText("compile-single", true);
        // wait message "Finished building SampleProject (compile-single)"
        stt.waitText("compile-single", true);
        stt.stop();
    }

    /** Run java class
     * - find SampleClass.java node
     * - call "Run|Run File" main menu item
     * - wait proper message is printed out to the status bar
     * - wait text is written to output window
     */
    public void testRun() {
        // start to track Main Window status bar
        MainWindowOperator.StatusTextTracer stt = MainWindowOperator.getDefault().getStatusTextTracer();
        stt.start();
        Node sample1Node = new Node(new SourcePackagesNode("SampleProject"), "sample1");
        Node sampleClassNode = new Node(sample1Node, "SampleClass.java");
        // call "Run|Run File|Run "SampleClass.java"" main menu item
        new Action("Run|Run File", null).perform(sampleClassNode);
        // wait message "Building SampleProject (run-single)..."
        stt.waitText("run-single", true);
        // wait message "Finished building SampleProject (run-single)"
        stt.waitText("run-single", true);
        stt.stop();
        // check "Hello, world!" was printed out to the output window
        OutputTabOperator outputOper = new OutputTabOperator("run-single"); //NOI18N
        outputOper.waitText("Hello, world!");
    }

    /** Delete java file
     * - find SampleClass.java node
     * - call Delete popup menu on node
     * - commit confirmation dialog
     */
    public void testDelete() {
        Node sample1Node = new Node(new SourcePackagesNode("SampleProject"), "sample1");
        JavaNode sampleClassNode = new JavaNode(sample1Node, "SampleClass.java");
        sampleClassNode.delete();
        new NbDialogOperator("Delete").ok();
    }
}
