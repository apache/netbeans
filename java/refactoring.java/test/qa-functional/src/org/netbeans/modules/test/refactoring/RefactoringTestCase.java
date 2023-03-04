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
package org.netbeans.modules.test.refactoring;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JTree;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.parser.ParserDelegator;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.ProjectRootNode;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.Waitable;
import org.netbeans.jemmy.Waiter;
import org.netbeans.jemmy.operators.JTreeOperator;
import org.netbeans.junit.diff.LineDiff;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Jiri.Prox@oracle.com, Marian.Mirilovic@oracle.com
 */
public abstract class RefactoringTestCase extends JellyTestCase {

    public static final char TREE_SEPARATOR = '|';
    protected static ProjectRootNode testProjectRootNode = null;
    private static ProjectsTabOperator pto = null;
    /**
     * The distance from the root of preview tree. Nodes located closer to the
     * root then this values will be sorted before dumping to ref file
     */
    public static int sortLevel = 2;
    protected static final String REFACTORING_TEST = "RefactoringTest";

    public RefactoringTestCase(String name) {
        super(name);
    }

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
     * Dumps the tree structure into the ref file. The childs are sorted if they
     * are closer to root than {@link #sortLevel}
     *
     * @param model Model of the dumped tree
     * @param parent Current root whose childs are recursively dumped
     * @param level Distance current root - tree root
     */
    protected void browseChildren(TreeModel model, Object parent, int level) {
        String logNode = getPreviewItemLabel(parent);
        for (int i = 0; i < level; i++) {
            getRef().print("    ");
        }
        ref(logNode);

        int childs = model.getChildCount(parent);
        ArrayList<Object> al = new ArrayList<Object>(childs);  //storing childs for sorting        

        for (int i = 0; i < childs; i++) {
            Object child = model.getChild(parent, i);
            al.add(child);
        }
        if ((level + 1) <= sortLevel) {
            sortChilds(al);
        }

        while (!al.isEmpty()) {
            Object child = al.remove(0);
            browseChildren(model, child, level + 1);
        }

    }

    protected void browseRoot(JTree tree) {
        TreeModel model = tree.getModel();
        Object root = model.getRoot();
        browseChildren(model, root, 0);
    }

    /**
     * Opens file in editor.
     *
     * @param treeSubPackagePathToFile
     * @param fileName
     */
    protected void openFile(String treeSubPackagePathToFile, String fileName) {
        openProject(REFACTORING_TEST);
        StringTokenizer st = new StringTokenizer(treeSubPackagePathToFile, TREE_SEPARATOR + "");
        if (st.countTokens() > 1) {
            String token = st.nextToken();

            String fullpath = token;
            while (st.hasMoreTokens()) {
                token = st.nextToken();
                waitForChildNode(fullpath, token);
                fullpath += TREE_SEPARATOR + token;
            }
        }
        // last node
        waitForChildNode(treeSubPackagePathToFile, fileName);
        // end of fix of issue #51191

        Node node = new Node(testProjectRootNode, treeSubPackagePathToFile + TREE_SEPARATOR + fileName);
        //node.performPopupAction("Open");
        new OpenAction().performAPI(node);  //should be more stable then performing open action from popup
    }

    /**
     * Gets the file name form the selected path in the preview tree. Supposed
     * is that the name of file in the second element in the path
     *
     * @param tree Preview tree
     * @return File name related to selected node
     */
    public String getFileForSelectedNode(JTreeOperator tree) {
        TreePath selectionPath = tree.getSelectionPath();
        Object pathComponent = selectionPath.getPathComponent(2);
        return getPreviewItemLabel(pathComponent);
    }

    /**
     * Gets string label of tree item. Suppose that the object has metho
     * {@code getLabel} which is called by reflection.
     *
     * @param parent The tree item
     * @return Test label obtained by method {@code getLabel}
     */
    protected String getPreviewItemLabel(Object parent) {
        try {
            Method method = parent.getClass().getDeclaredMethod("getLabel");
            method.setAccessible(true);
            Object invoke = method.invoke(parent);
            Label2Text parser = new Label2Text();
            String invoke_str = (String) invoke;
            String ret = invoke_str.replace("&nbsp;", " ");
            parser.parse(new StringReader(ret));
            ret = parser.result.toString();
            return ret;
        } catch (IOException ex) {
            Logger.getLogger(RefactoringTestCase.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(RefactoringTestCase.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(RefactoringTestCase.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvocationTargetException ex) {
            Logger.getLogger(RefactoringTestCase.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchMethodException ex) {
            Logger.getLogger(RefactoringTestCase.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SecurityException ex) {
            Logger.getLogger(RefactoringTestCase.class.getName()).log(Level.SEVERE, null, ex);
        }
        fail("Error in reflection");
        return null;
    }

    private void sortChilds(List<Object> al) {
        final HashMap<Object, String> hashMap = new HashMap<Object, String>();
        for (Object object : al) {
            hashMap.put(object, getPreviewItemLabel(object));
        }

        Collections.<Object>sort(al, new Comparator() {
            @Override
            public int compare(Object o1, Object o2) {
                return hashMap.get(o1).compareTo(hashMap.get(o2));
            }
        });
    }

    private void waitForChildNode(String parentPath, String childName) {
        openProject(REFACTORING_TEST);
        Node parent = new Node(testProjectRootNode, parentPath);
        final String finalFileName = childName;
        try {
            // wait for max. 3 seconds for the file node to appear
            JemmyProperties.setCurrentTimeout("Waiter.WaitingTime", 3000);
            new Waiter(new Waitable() {
                @Override
                public Object actionProduced(Object parent) {
                    return ((Node) parent).isChildPresent(finalFileName) ? Boolean.TRUE : null;
                }

                @Override
                public String getDescription() {
                    return ("Waiting for the tree to load.");
                }
            }).waitAction(parent);
        } catch (InterruptedException e) {
            throw new JemmyException("Interrupted.", e);
        }
    }

    protected void openSourceFile(String dir, String srcName) {
        openFile(org.netbeans.jellytools.Bundle.getString("org.netbeans.modules.java.j2seproject.Bundle", "NAME_src.dir") + TREE_SEPARATOR + dir, srcName);
    }

    @Override
    protected void setUp() throws Exception {
        //jemmyOutput = new PrintStream(new File(getWorkDir(), getName() + ".jemmy"));
        System.setErr(new PrintStream(new File(getWorkDir(), getName() + ".error")));
        System.out.println("Test " + getName() + " started ... ");
        openProject(REFACTORING_TEST);
    }

    protected void openProject(String projectName) {
        if (pto == null) {
            pto = ProjectsTabOperator.invoke();
        }

        if (testProjectRootNode == null) {
            try {
                openDataProjects("projects/" + projectName);
                testProjectRootNode = pto.getProjectRootNode(projectName);
                testProjectRootNode.select();
            } catch (IOException ex) {
                throw new JemmyException("Open project [" + projectName + "] fails !!!", ex);
            }
        } else {
            log("Project is opened!");
        }
    }

    @Override
    protected void tearDown() throws Exception {
        getRef().flush();
        getRef().close();
        assertFile("Golden file differs ", getReferencFile(), getGoldenFile(), getWorkDir(), new LineDiff());
        //compareReferenceFiles();
        File diffFile = getDiffFile(getReferencFile().getAbsolutePath(), getWorkDir());
        System.out.println("+++++++++ Diff file ["+diffFile.getAbsolutePath()+"] exists=" + diffFile.exists());
        if (diffFile.exists()) {
            System.out.println("============= DIFF >>>> =======================================");
            FileReader fr = new FileReader(diffFile);
            int oneByte;
            while ((oneByte = fr.read()) != -1) {
                System.out.print((char) oneByte);
            }
            System.out.flush();
            System.out.println("============= <<<< DIFF =======================================");
        }

        System.out.println("Test " + getName() + " finished !");
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

    private File getReferencFile() throws IOException {
        File refFile = new File(getWorkDir(), getName() + ".ref");
        return refFile;
    }

    private static class Label2Text extends HTMLEditorKit.ParserCallback {

        StringBuilder result;

        public Label2Text() {
            result = new StringBuilder();
        }

        public void parse(StringReader in) throws IOException {
            new ParserDelegator().parse(in, this, Boolean.TRUE);
        }

        @Override
        public void handleText(char[] text, int pos) {
            result.append(text);
        }
    }
}
