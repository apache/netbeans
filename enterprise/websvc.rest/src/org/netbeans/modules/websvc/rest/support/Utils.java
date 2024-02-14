/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.websvc.rest.support;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.SwingUtilities;
import org.apache.tools.ant.module.api.support.ActionUtils;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.progress.ProgressUtils;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.web.api.webmodule.WebProjectConstants;
import org.netbeans.modules.websvc.api.support.LogUtils;
import org.netbeans.modules.websvc.rest.nodes.TestRestServicesAction;
import org.netbeans.modules.websvc.rest.spi.MiscUtilities;
import org.netbeans.modules.websvc.rest.spi.RestSupport;
import org.netbeans.spi.project.support.ant.GeneratedFilesHelper;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.HtmlBrowser;
import org.openide.cookies.LineCookie;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.text.Line;
import org.openide.text.Line.ShowOpenType;
import org.openide.text.Line.ShowVisibilityType;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Peter Liu
 */
public class Utils {
    
    public static String stripPackageName(String name) {
        int genericIndex = name.indexOf("<");//NOI18N
        int index = 0;
        if (genericIndex > 0) {
            String className = name.substring(0, genericIndex);
            index = className.lastIndexOf("."); //NOI18N
        } else {
            index = name.lastIndexOf("."); //NOI18N
        }
        if (index > 0) {
            return name.substring(index+1);
        }
        return name;
    }
    
    public static Collection<String> sortKeys(Collection<String> keys) {
        Collection<String> sortedKeys = new TreeSet<String>(
                new Comparator<String> () {
            public int compare(String str1, String str2) {
                return str1.compareTo(str2);
            }
        });
        
        sortedKeys.addAll(keys);
        return sortedKeys;
    }
    
    public static void showMethod(FileObject source, String methodName) {
        try {
            DataObject dataObj = DataObject.find(source);          
            JavaSource javaSource = JavaSource.forFileObject(source);
            
            // Force a save to make sure to make sure the line position in
            // the editor is in sync with the java source.
            SaveCookie sc = dataObj.getCookie(SaveCookie.class);
     
            if (sc != null) {
                sc.save();
            }
            
            LineCookie lc = dataObj.getCookie(LineCookie.class);
            
            if (lc != null) {
                final long[] position = JavaSourceHelper.getPosition(javaSource, methodName);
                final Line line = lc.getLineSet().getOriginal((int) position[0]);
                
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        line.show(ShowOpenType.OPEN, ShowVisibilityType.NONE, (int) position[1]);
                    }
                });
            }
        } catch (Exception de) {
            Exceptions.printStackTrace(de);
        }    
    }

    public static Method getValueOfMethod(Class type) {
        try {
            Method method = type.getDeclaredMethod("valueOf", String.class);
            if (method == null || ! Modifier.isStatic(method.getModifiers())) {
                return null;
            }
            return method;
        } catch (Exception e) {
            return null;
        }
    }
    
    public static Constructor getConstructorWithStringParam(Class type) {
        try {
            return type.getConstructor(String.class);
        } catch (Exception e) {
            return null;
        }
    }
    
    /** Finds all projects in given lookup. If the command is not null it will check 
     * whther given command is enabled on all projects. If and only if all projects
     * have the command supported it will return array including the project. If there
     * is one project with the command disabled it will return empty array.
     */
    public static Project[] getProjectsFromLookup(Lookup lookup) {    
        Set<Project> result = new HashSet<Project>();
        for (Project p : lookup.lookupAll(Project.class)) {
            result.add(p);
        }
        // Now try to guess the project from dataobjects
        for (DataObject dObj : lookup.lookupAll(DataObject.class)) {
            FileObject fObj = dObj.getPrimaryFile();
            Project p = FileOwnerQuery.getOwner(fObj);
            if ( p != null ) {
                result.add( p );
            }
        }
        Project[] projectsArray = result.toArray(new Project[0]);
        return projectsArray;
    }

    public static FileObject findBuildXml(Project project) {
        return project.getProjectDirectory().getFileObject(
                GeneratedFilesHelper.BUILD_XML_PATH);
    }
    
    public static void testRestWebService(final Project project) {
        if ( project == null ){
            return;
        }
        
        final TestRestTargetPanel panel = new TestRestTargetPanel(project);
        DialogDescriptor descriptor = new DialogDescriptor(panel,
                NbBundle.getMessage(Utils.class, 
                        "TTL_SelectTarget"));
        panel.setDescriptor( descriptor);
        if ( !DialogDisplayer.getDefault().notify(descriptor).equals(
                NotifyDescriptor.OK_OPTION)) 
        {
            return;
        }
        
        if (panel.isRemote()) {
            generateRemoteTester(project, panel.getProject());
        }
        else {
            generateLocalTester(project);
        }
    }

    private static void generateRemoteTester( final Project restProject, Project remoteProject ) {
        final RestSupport rs = remoteProject.getLookup().lookup(RestSupport.class);
        final RestSupport localSupport = restProject.getLookup().lookup(RestSupport.class);
        SourceGroup[] sourceGroups = ProjectUtils.getSources(remoteProject).
            getSourceGroups(WebProjectConstants.TYPE_DOC_ROOT);
        SourceGroup sourceGroup = sourceGroups[0];
        final FileObject rootFolder = sourceGroup.getRootFolder();
        try {
            if ( SwingUtilities.isEventDispatchThread() ){
                final FileObject[] testFO = new FileObject[1];
                AtomicBoolean cancel = new AtomicBoolean(false);
                ProgressUtils.runOffEventDispatchThread(new Runnable() {

                    @Override
                    public void run() {
                        try {
                            testFO[0] = rs.generateTestClient(
                                    FileUtil.toFile(rootFolder), 
                                    localSupport.getBaseURL());
                        }
                        catch (IOException e) {
                            Logger.getLogger(Utils.class.getName())
                                    .log(Level.WARNING, null, e);
                        }
                    }
                },NbBundle.getMessage(Utils.class, "TTL_GenTestClient") , // NOI18N
                    cancel, false);
                if ( cancel.get() ||testFO[0]==null){
                    return;
                }
                getTestClientRequestProcessor().post( new Runnable() {
                    
                    @Override
                    public void run() {
                        try {
                            localSupport.deploy();
                            rs.deploy();
                            URL url = new URL(MiscUtilities.getContextRootURL(restProject) +
                                    testFO[0].getNameExt());
                            if (url != null) {
                                HtmlBrowser.URLDisplayer.getDefault().showURL(url);
                            }
                        }
                        catch(IOException e){
                            Logger.getLogger(Utils.class.getName()).log(
                                    Level.WARNING, null, e);
                        }
                    }
                });
            }
            else {
                FileObject testFO = rs.generateTestClient(
                        FileUtil.toFile(rootFolder), localSupport.getBaseURL());
                localSupport.deploy();
                if ( localSupport!= rs ){
                    rs.deploy();
                }
                URL url = new URL(MiscUtilities.getContextRootURL(restProject) + testFO.getNameExt());
                if (url != null) {
                    HtmlBrowser.URLDisplayer.getDefault().showURL(url);
                }
            }
        }
        catch(IOException e ){
            Logger.getLogger( TestRestServicesAction.class.getName()).log( 
                    Level.WARNING, null, e);
        }
    }

    private static void generateLocalTester(final  Project prj ) {
        final FileObject buildFo = findBuildXml(prj);
        if (buildFo != null) {
            try {
                if ( SwingUtilities.isEventDispatchThread() ){
                    final Properties props[] = new Properties[1];
                    AtomicBoolean cancel = new AtomicBoolean(false);
                    ProgressUtils.runOffEventDispatchThread(new Runnable() {

                        @Override
                        public void run() {
                            try {
                                props[0] = setupTestRestBeans(prj);
                            }
                            catch(IOException e){
                                Logger.getLogger(Utils.class.getName()).log(
                                        Level.WARNING, null, e);
                            }
                        }
                    },NbBundle.getMessage(Utils.class, "TTL_GenTestClient") , // NOI18N
                        cancel, false);
                    if ( cancel.get() ||props[0]==null){
                        return;
                    }
                    getTestClientRequestProcessor().post( new Runnable() {
                        
                        @Override
                        public void run() {
                            try {
                                ActionUtils.runTarget(buildFo,
                                    new String[] { RestSupport.COMMAND_TEST_RESTBEANS },
                                    props[0]);                            
                            }
                            catch(IOException e){
                                Logger.getLogger(Utils.class.getName()).log(
                                        Level.WARNING, null, e);
                            }
                        }
                    });
                }
                else {
                    Properties props = setupTestRestBeans(prj); 
                    ActionUtils.runTarget(buildFo,
                            new String[] { RestSupport.COMMAND_TEST_RESTBEANS },
                            props);
                }
            }
            catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        else {
            // if there is a rest support (e.g. in Maven projects)
            final RestSupport rs = prj.getLookup().lookup(RestSupport.class);
            if (rs != null) {
                try {
                    if ( SwingUtilities.isEventDispatchThread() ){
                        final FileObject[] testFO = new FileObject[1];
                        AtomicBoolean cancel = new AtomicBoolean(false);
                        ProgressUtils.runOffEventDispatchThread(new Runnable() {

                            @Override
                            public void run() {
                                try {
                                    testFO[0] = rs.generateTestClient(
                                            rs.getLocalTargetTestRest(),
                                            rs.getBaseURL());
                                }
                                catch (IOException e) {
                                    Logger.getLogger(Utils.class.getName())
                                            .log(Level.WARNING, null, e);
                                }
                            }
                        },NbBundle.getMessage(Utils.class, "TTL_GenTestClient") , // NOI18N
                            cancel, false);
                        if ( cancel.get() || testFO[0]==null){
                            return;
                        }
                        RequestProcessor.getDefault().post( new Runnable() {
                            
                            @Override
                            public void run() {
                                try {
                                    rs.deploy();
                                    if (testFO != null) {
                                        URL url = testFO[0].toURL();
                                        if (url != null) {
                                            HtmlBrowser.URLDisplayer
                                                    .getDefault().showURL(url);
                                        }
                                    }
                                }
                                catch(IOException e){
                                    Logger.getLogger(Utils.class.getName()).log(
                                            Level.WARNING, null, e);
                                }
                            }
                        });
                    }
                    else {
                        FileObject testFO = rs.generateTestClient(
                                rs.getLocalTargetTestRest(), rs.getBaseURL());
                        rs.deploy();
                        if (testFO != null) {
                            URL url = testFO.toURL();
                            if (url != null) {
                                HtmlBrowser.URLDisplayer.getDefault().showURL(
                                        url);
                            }
                        }
                    }
                }
                catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }

        // logging usage of action
        Object[] params = new Object[2];
        params[0] = LogUtils.WS_STACK_JAXRS;
        params[1] = "TEST REST"; // NOI18N
        LogUtils.logWsAction(params);
    }
    
    private static Properties setupTestRestBeans(Project project) throws IOException {
        Properties p = new Properties();
        p.setProperty(RestSupport.PROP_BASE_URL_TOKEN, RestSupport.BASE_URL_TOKEN);

        RestSupport rs = project.getLookup().lookup(RestSupport.class);
        if (rs != null) {
            try {
                String applicationPath = rs.getApplicationPath();
                if (applicationPath != null) {
                    if (!applicationPath.startsWith("/")) {
                        applicationPath = "/"+applicationPath;
                    }
                    p.setProperty(RestSupport.PROP_APPLICATION_PATH, applicationPath);
                }
                File testdir = rs.getLocalTargetTestRest();
                FileObject testFO = MiscUtilities.generateTestClient(testdir);
                p.setProperty(RestSupport.PROP_RESTBEANS_TEST_URL, 
                        testFO.toURL().toString());
                p.setProperty(RestSupport.PROP_RESTBEANS_TEST_FILE, 
                        FileUtil.toFile(testFO).getAbsolutePath());
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return p;
    }
    
    private static RequestProcessor getTestClientRequestProcessor(){
        assert SwingUtilities.isEventDispatchThread();
        if ( TEST_CLIENT_RQ == null ){
            TEST_CLIENT_RQ = new RequestProcessor("REST-Test-Client-RQ");
        }
        return TEST_CLIENT_RQ;
    }
    
    /**
     * This request processor is used ONLY in EQ so it doesn't have to be synchronized
     */
    private static RequestProcessor TEST_CLIENT_RQ;
    
}

