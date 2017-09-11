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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.tasklist.todo;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.tasklist.impl.CurrentEditorScanningScope;
import org.netbeans.modules.tasklist.projectint.MainProjectScanningScope;
import org.netbeans.modules.tasklist.projectint.OpenedProjectsScanningScope;
import org.netbeans.spi.tasklist.Task;
import org.netbeans.spi.tasklist.TaskScanningScope;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.windows.TopComponent;

/**
 * Tests ToDo scanner.
 *
 * @author Petr Zajac
 */
public class ToDoTest extends NbTestCase /*extends TestBase*/ {
    
    static {
        // #65461: do not try to load ModuleInfo instances from ant module
        System.setProperty("org.netbeans.core.startup.ModuleSystem.CULPRIT", "true");
//        LayerTestBase.Lkp.setLookup(new Object[0]);
        FileObject reg = FileUtil.getConfigFile("Services/AntBasedProjectTypes/org-netbeans-modules-apisupport-project.instance");
        assertNotNull("apisupport definition is registered", reg);
        Object abpt = reg.getAttribute("instanceCreate");
        FileObject reg2 = FileUtil.getConfigFile("Services/AntBasedProjectTypes/org-netbeans-modules-apisupport-project-suite.instance");
        assertNotNull("j2seproject definition is registered", reg2);
        Object abpt2 = reg2.getAttribute("instanceCreate");
//        LayerTestBase.Lkp.setLookup(new Object[]{new WindowManagerMock(),new TopComponentRegistryMock(), abpt, abpt2});
//        DialogDisplayerImpl.returnFromNotify(DialogDescriptor.NO_OPTION);
    }
    
    public ToDoTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
        MockServices.setServices(TopComponentRegistryMock.class,WindowManagerMock.class);
        clearWorkDir();
//        noDataDir = true;
        
        super.setUp();
//        InstalledFileLocatorImpl.registerDestDir(destDirF);
    }
    
    private static final String javaFile = "public class Main1 {\n" +
            "/** TODO \n" +
            " * TODO \n" +
            "*/\n" +
            "}";

    private static final String noToDoFile = "public class NoTodo {\n" +
            "}";

    /*
    static FileObject createSrcFile(NbModuleProject prj,String path,String content ) throws IOException {
        FileObject fo = prj.getSourceDirectory().createData(path);
        Writer writer = new OutputStreamWriter(fo.getOutputStream());
        writer.write(content);
        writer.close();
        return fo;
    }
     */
    
    private List<Task> scanOpenProjectsTasks() {
        return scanTasks(OpenedProjectsScanningScope.create());
    }
    
    private List<Task> scanTasks(TaskScanningScope scope) {
         List<Task> tasks = new ArrayList<Task>();
        TodoTaskScanner tts = TodoTaskScanner.create();
        for (FileObject jfo : scope) {
            List<? extends Task> filTasks = tts.scan(jfo);
            if (filTasks != null) {
                tasks.addAll(filTasks);
            }
        }
        return tasks;
    }
    private List<Task> scanMainProjectTasks() {
       return scanTasks(MainProjectScanningScope.create());
    }
    private List<Task> scanCurrentEditorTasks() { 
        CurrentEditorScanningScope scanScope = CurrentEditorScanningScope.create();
        scanScope.run();
        return scanTasks(scanScope);
    }

    /*
    @RandomlyFails // NB-Core-Build #852
    public void testProject1() throws IOException {
        NbModuleProject prj1 = generateStandaloneModule(getWorkDir(), "prj1");
        NbModuleProject prj2 = generateStandaloneModule(getWorkDir(), "prj2");
        FileObject fo = createSrcFile(prj1,"Main.java",javaFile);
        FileObject fo2 = createSrcFile(prj1,"Main2.java",javaFile);
        FileObject fo3 = createSrcFile(prj2,"Main.java",javaFile);
        OpenProjects.getDefault().open(new Project[]{prj1,prj2}, false);

        List<Task> tasks = scanOpenProjectsTasks();        
        assertEquals("Number of tasks",6,tasks.size());
        
        OpenProjects.getDefault().close(new Project[]{prj1});
        tasks = scanOpenProjectsTasks();   
        logTasks(tasks); 
        assertEquals("Number of tasks",2,tasks.size());
    }

    @RandomlyFails // NB-Core-Build #1049
    public void testMainProject() throws Exception {
        NbModuleProject prj1 = generateStandaloneModule(getWorkDir(), "prj1");
        NbModuleProject prj2 = generateStandaloneModule(getWorkDir(), "prj2");
        FileObject fo = createSrcFile(prj1,"Main.java",javaFile);
        FileObject fo2 = createSrcFile(prj1,"Main2.java",javaFile);
        FileObject fo3 = createSrcFile(prj2,"Main.java",javaFile);
        OpenProjects.getDefault().open(new Project[]{prj1,prj2}, false);
        OpenProjects.getDefault().setMainProject(prj1);
        List<Task> tasks = scanMainProjectTasks();        
        assertEquals("Number of tasks",4,tasks.size());
        OpenProjects.getDefault().close(new Project[]{prj1});
        tasks = scanMainProjectTasks();        
        assertEquals("Number of tasks",0,tasks.size());        
        
        // test subproject
        
        SuiteProject suite = generateSuite("suiteproject");
        NbModuleProject prj3 = generateSuiteComponent(suite, getWorkDir(), "prjInSuite1");
        NbModuleProject prj4 = generateSuiteComponent(suite, getWorkDir(), "prjInSuite2");
        
        createSrcFile(prj3,"Main.java",javaFile);
        createSrcFile(prj4,"Main.java",javaFile);
        ProjectXMLManagerTest.addDependency(prj3, prj4);
        OpenProjects.getDefault().open(new Project[]{suite,prj3,prj4}, false);
        OpenProjects.getDefault().setMainProject(prj4);
        
        tasks = scanMainProjectTasks();
        assertEquals("Number of tasks",4,tasks.size());
    }
    
    public void testCurrentEditorScanningScope() throws IOException, InterruptedException {
        MockServices.setServices(TopComponentRegistryMock.class,WindowManagerMock.class);
        assertTrue("TopComponentRegistryMock ",Lookup.getDefault().lookup(TopComponent.Registry.class) instanceof TopComponentRegistryMock);
        assertTrue("WindowManagerMock ",Lookup.getDefault().lookup(WindowManager.class) instanceof WindowManagerMock);
        NbModuleProject prj1 = generateStandaloneModule(getWorkDir(), "prj1");
        FileObject fo = createSrcFile(prj1,"Main.java",javaFile);
        FileObject fo2 = createSrcFile(prj1,"NoTo.java",noToDoFile);
        
        List<Task> tasks = scanCurrentEditorTasks();
        assertEquals("No document opened",0,tasks.size());
        
        TopComponent tc = createTopComponent(fo);
        TopComponentRegistryMock registry =  (TopComponentRegistryMock) org.openide.windows.TopComponent.getRegistry();
        registry.setActivated(tc);
        
        registry.setOpened(Collections.singleton(tc));
        WindowManagerMock wm = (WindowManagerMock) Lookup.getDefault().lookup(WindowManager.class);
        wm.setOpenedEditorTopComponent(tc);
        tasks = scanCurrentEditorTasks();
        assertEquals("Document with todo",2,tasks.size());
        
        tc = createTopComponent(fo2);
        registry.setActivated(tc);
        
        registry.setOpened(Collections.singleton(tc));
        wm.setOpenedEditorTopComponent(tc);
        tasks = scanCurrentEditorTasks();
        
        assertEquals("Document with no todo",0,tasks.size());
    }
     */
    
//    public void testTodoAndTaskManager() throws IOException, InterruptedException {
//        NbModuleProject prj1 = generateStandaloneModule(getWorkDir(), "prj1");
//        NbModuleProject prj2 = generateStandaloneModule(getWorkDir(), "prj2");
//        FileObject fo = createSrcFile(prj1,"Main.java",javaFile);
//        FileObject fo2 = createSrcFile(prj1,"Main2.java",javaFile);
//        FileObject fo3 = createSrcFile(prj2,"Main.java",javaFile);
//        OpenProjects.getDefault().open(new Project[]{prj1,prj2}, false);
//
//        TaskManagerImpl manager = TaskManagerImpl.getInstance();
//        manager.observe(OpenedProjectsScanningScope.create(), null);
//        TaskManagerImplTest.waitFinished(manager);
//        TaskList tasks =  manager.getTasks();
//        Thread.sleep(2000);
//        assertEquals("Number of tasks",2,tasks.size());
//        
//    }
    private void logTasks(List<Task> tasks) {
        for (Task t : tasks) {
            System.out.println( t );
        }
    }       
    private TopComponent createTopComponent(FileObject fo) throws DataObjectNotFoundException {
        DataObject dobj = DataObject.find(fo);
        Lookup lookup = Lookups.fixed(dobj);
        return  new TopComponent(lookup) {
          public boolean isShowing() {
              return true;
          }  
        };
    }
}

