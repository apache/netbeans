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
package org.netbeans.test.permanentUI;

import java.awt.Component;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.ProjectRootNode;
import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.util.PNGEncoder;
import org.netbeans.test.permanentUI.utils.ProjectContext;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Marian.Mirilovic@oracle.com
 */
public abstract class PermUITestCase extends JellyTestCase {

    protected static final char TREE_SEPARATOR = '|';
    protected static final boolean screen = false;
    protected boolean initialized = false;

    private static int index = 0;
    
    public PermUITestCase(String name) {
        super(name);
    }

    /**
     * Setup called before every test case.
     */
    @Override
    public void setUp() {
        // push Escape key to ensure there is no thing blocking shortcut execution
        MainWindowOperator.getDefault().pushKey(KeyEvent.VK_ESCAPE);
        
        System.setProperty("jemmy.screen.capture", "true");

        try {
//            clearWorkDir();
            System.setErr(new PrintStream(new File(getWorkDir(), getName() + ".error")));
            if (!initialized) {
                initialize();
                initialized = true;
            }
        } catch (IOException ex) {
            captureScreen();
            ex.printStackTrace(System.err);
        }
        System.out.println("########  " + " CONTEXT -> " + getContext().toString() + " - " + getName() + "  #######");
    }

    public abstract void initialize() throws IOException;
    public abstract ProjectContext getContext();

    public void ref(Object o) {
        getRef().println(o);
    }

    public void ref(File f) {
        getRef().println("==>" + f.getName());
        try {
            BufferedReader br = new BufferedReader(new FileReader(f));
            String s = br.readLine();
            while (s != null) {
                getRef().println(s);
                s = br.readLine();
            }
            br.close();
        } catch (FileNotFoundException ex) {
            fail(ex);
        } catch (IOException ex) {
            fail(ex);
        }
    }

    public void ref(FileObject fo) {
        if (fo.isValid()) {
            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(fo.getInputStream()));
                getRef().println("==>" + fo.getName());
                String s = br.readLine();
                while (s != null) {
                    getRef().println(s);
                    s = br.readLine();
                }
            } catch (IOException ioe) {
                fail(ioe);
            }
        }
    }

    /**
     * Opens file in editor.
     *
     * @param project
     * @param treeSubPackagePathToFile
     * @param fileName
     */
    protected void openFile(String project, String treeSubPackagePathToFile, String fileName) throws IOException {
        openDataProjects(project);
        waitScanFinished();
        ProjectRootNode projectRootNode = new ProjectsTabOperator().getProjectRootNode(project);
        Node node = new Node(projectRootNode, treeSubPackagePathToFile + TREE_SEPARATOR + fileName);
        //node.performPopupAction("Open");
        new OpenAction().performAPI(node);  //should be more stable then performing open action from popup
        new EditorOperator(fileName).pushHomeKey(); // request focus ... try to fix failures on Windows - if the node isn't selected it's not a part of menu items (context sensitive)
    }

    /**
     * @Override protected void tearDown() throws Exception { getRef().flush();
     * getRef().close(); assertFile("Golden file differs ", getReferencFile(),
     * getGoldenFile(), getWorkDir(), new LineDiff());
     * //compareReferenceFiles(); File diffFile =
     * getDiffFile(getReferencFile().getAbsolutePath(), getWorkDir());
     * System.out.println("+++++++++ Diff file [" + diffFile.getAbsolutePath() +
     * "] exists=" + diffFile.exists()); if (diffFile.exists()) {
     * System.out.println("============= DIFF >>>>
     * ======================================="); FileReader fr = new
     * FileReader(diffFile); int oneByte; while ((oneByte = fr.read()) != -1) {
     * System.out.print((char) oneByte); } System.out.flush();
     * System.out.println("============= <<<< DIFF
     * ======================================="); }
     *
     * System.out.println("Test " + getName() + " finished !"); }
     */
    protected File getGoldenFile(String category, String fileName) {
        return new File(getDataDir() + File.separator + "permanentUI" + File.separator + category + File.separator + fileName + ".txt");
    }

    /**
     * Copy of NbTestCate#getDiffName
     *
     * @param pass
     * @param diff
     * @return diff file
     */
    protected File getDiffFile(String pass, File diff) {
        StringBuilder d = new StringBuilder();
        int i1, i2;

        d.append(diff.getAbsolutePath());
        i1 = pass.lastIndexOf('\\');
        i2 = pass.lastIndexOf('/');
        i1 = i1 > i2 ? i1 : i2;
        i1 = -1 == i1 ? 0 : i1 + 1;

        i2 = pass.lastIndexOf('.');
        i2 = -1 == i2 ? pass.length() : i2;

        if (0 < d.length()) {
            d.append("/");
        }

        d.append(pass.substring(i1, i2));
        d.append(".diff");
        return new File(d.toString());
    }

    protected ComponentChooser getCompChooser(final String className) {
        return new ComponentChooser() {
            @Override
            public boolean checkComponent(Component comp) {
                return comp.getClass().getName().equals(className);
            }

            @Override
            public String getDescription() {
                return className;
            }
        };
    }

    /**
     * Take a screen shot.
     */
    protected void captureScreen() {
        if (screen) {
            try {
                index++;
                String captureFile = getWorkDir().getAbsolutePath() + File.separator + "screen" + index + ".png";
                PNGEncoder.captureScreen(captureFile, PNGEncoder.COLOR_MODE);
            } catch (Exception ex) {
                ex.printStackTrace(getLog());
            }
        }
    }
    
    protected class LogFiles {

        String pathToIdeLogFile;
        String pathToGoldenLogFile;
        String pathToDiffLogFile;
        PrintStream ideFileStream;
        PrintStream goldenFileStream;
        PrintStream diffFileStream;

        protected LogFiles() {
            pathToIdeLogFile = getWorkDirPath() + File.separator + getName() + "_ide.txt";
            pathToGoldenLogFile = getWorkDirPath() + File.separator + getName() + "_golden.txt";
            pathToDiffLogFile = getWorkDirPath() + File.separator + getName() + ".diff";
            ideFileStream = null;
            goldenFileStream = null;
        }

        protected PrintStream getIdeFileStream() throws FileNotFoundException {
            return ideFileStream = new PrintStream(pathToIdeLogFile);
        }

        protected PrintStream getGoldenFileStream() throws FileNotFoundException {
            return goldenFileStream = new PrintStream(pathToGoldenLogFile);
        }

        protected PrintStream getDiffFileStream() throws FileNotFoundException {
            return diffFileStream = new PrintStream(pathToDiffLogFile);
        }
    }

}
