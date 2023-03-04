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
