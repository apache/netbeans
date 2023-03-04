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

