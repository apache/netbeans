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
package org.netbeans.modules.maven.j2ee.web;

import java.io.IOException;
import javax.lang.model.element.TypeElement;
import javax.swing.SwingUtilities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.openide.util.RequestProcessor;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.j2ee.dd.api.web.WebAppMetadata;
import org.netbeans.modules.j2ee.dd.api.web.model.ServletInfo;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.spi.actions.ActionConvertor;
import org.netbeans.modules.maven.spi.actions.ReplaceTokenProvider;
import org.netbeans.modules.web.api.webmodule.RequestParametersQuery;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author mkleint
 */
@ProjectServiceProvider(service = {ReplaceTokenProvider.class, ActionConvertor.class}, projectType = {"org-netbeans-modules-maven/" + NbMavenProject.TYPE_WAR})
public class WebReplaceTokenProvider implements ReplaceTokenProvider, ActionConvertor {

    private static final String WEB_PATH =          "webpagePath";      //NOI18N
    private static final String IS_SERVLET_FILE = "org.netbeans.modules.web.IsServletFile";   //NOI18N
    public static final String ATTR_EXECUTION_URI = "execution.uri";    //NOI18N
    public static final String FILE_DD        =     "web.xml";          //NOI18N
    
    private static final Set<WebModule> SERVLET_SEARCH_MODULES = new HashSet<>();
    private static RequestProcessor SERVLETS_REQUEST_PROCESSOR = new RequestProcessor(WebReplaceTokenProvider.class);
    private static RequestProcessor RP = new RequestProcessor(WebReplaceTokenProvider.class);

    private Project project;
    private AtomicBoolean   isScanStarted;
    private AtomicBoolean   isScanFinished;
    
    public WebReplaceTokenProvider(Project prj) {
        project = prj;
        isScanStarted = new AtomicBoolean( false );
        isScanFinished = new AtomicBoolean(false);
    }

    /**
     * Just gets the array of FOs from lookup.
     */
    protected static FileObject[] extractFileObjectsfromLookup(Lookup lookup) {
        List<FileObject> files = new ArrayList<>();
        Iterator<? extends DataObject> it = lookup.lookupAll(DataObject.class).iterator();
        while (it.hasNext()) {
            DataObject d = it.next();
            FileObject f = d.getPrimaryFile();
            files.add(f);
        }
        return files.toArray(new FileObject[0]);
    }

    @Override
    public Map<String, String> createReplacements(String action, Lookup lookup) {
        FileObject[] fos = extractFileObjectsfromLookup(lookup);
        String relPath = null;
        FileObject fo = null;
        HashMap<String, String> replaceMap = new HashMap<>();
        if (fos.length > 0 && action.endsWith(".deploy")) { //NOI18N
            fo = fos[0];
            Sources srcs = ProjectUtils.getSources(project);
            //for jsps
            String requestParams = RequestParametersQuery.getFileAndParameters(fo);
            if (requestParams != null && !"/null".equals(requestParams)) { //IMHO a bug in the RPQI in WebExSupport.java
                relPath =  requestParams;
            }
            if (relPath == null) {
            //for html
                String url = FileUtil.getRelativePath(WebModule.getWebModule(fo).getDocumentBase(), fo); 
                if (url != null) {
                    url = url.replace(" ", "%20"); //NOI18N
                    relPath =  "/" + url; //NOI18N
                }
            }
            if (relPath == null) {
                //TODO we shall check the resources as well, not sure that is covered here..
                // if not, this code is a duplication of the above snippet only..
                SourceGroup[] grp = srcs.getSourceGroups("doc_root"); //NOI18N J2EE
                for (int i = 0; i < grp.length; i++) {
                    relPath = FileUtil.getRelativePath(grp[i].getRootFolder(), fo);
                    if (relPath != null) {
                        break;
                    }
                }
            }

            if (relPath == null) {
                // run servlet
                if ("text/x-java".equals(fo.getMIMEType())) { //NOI18N
                    WebModule webModule = WebModule.getWebModule(fo);
                    String[] urlPatterns = getServletMappings(webModule, fo);
                    if (urlPatterns != null && urlPatterns.length > 0) {
                        ServletUriPanel uriPanel = new ServletUriPanel(urlPatterns, null, true);
                        DialogDescriptor desc = new DialogDescriptor(uriPanel,
                                NbBundle.getMessage(WebReplaceTokenProvider.class, "TTL_setServletExecutionUri"));
                        Object res = DialogDisplayer.getDefault().notify(desc);
                        if (res.equals(NotifyDescriptor.YES_OPTION)) {
                            relPath = uriPanel.getServletUri(); //NOI18N
                            try {
                                fo.setAttribute(ATTR_EXECUTION_URI, uriPanel.getServletUri());
                            } catch (IOException ex) {
                            }
                        } else if (res.equals(NotifyDescriptor.CANCEL_OPTION)) {
                            replaceMap.put(WEB_PATH, null);
                            return replaceMap;
                        }
                    }
                }

            }
            if (relPath == null) {
                relPath = "";
            }
            replaceMap.put(WEB_PATH, relPath);//NOI18N
        }
        return replaceMap;
    }

    public static String[] getServletMappings(WebModule webModule, FileObject javaClass) {
        if (webModule == null) {
            return null;
        }

        final ClassPath classPath = ClassPath.getClassPath (javaClass, ClassPath.SOURCE);
        if (classPath == null) {
            return null;
        }

        final String className = classPath.getResourceName(javaClass,'.',false);
        try {
            final List<ServletInfo> servlets = WebAppMetadataHelper.getServlets(webModule.getMetadataModel());
            final List<String> mappingList = new ArrayList<>();
            for (ServletInfo si : servlets) {
                if (className.equals(si.getServletClass())) {
                    mappingList.addAll(si.getUrlPatterns());
                }
            }
            return mappingList.toArray(new String[0]);
        } catch (java.io.IOException ex) {
            return null;
        }
    }

    @Override
    public String convert(String action, Lookup lookup) {
        if (ActionProvider.COMMAND_RUN_SINGLE.equals(action) ||
            ActionProvider.COMMAND_DEBUG_SINGLE.equals(action) || 
            ActionProvider.COMMAND_PROFILE_SINGLE.equals(action)) {

            FileObject[] fos = extractFileObjectsfromLookup(lookup);
            if (fos.length > 0) {
                FileObject fo = fos[0];
                String mimeType = fo.getMIMEType();
                if ("text/x-java".equals(mimeType)) { //NOI18N
                    return convertJavaAction(action, fo);
                }
                if ("text/x-jsp".equals(mimeType) || "text/html".equals(mimeType) || "text/xhtml".equals(mimeType)) { // NOI18N
                    return action + ".deploy"; //NOI18N
                }
            }
        }
        return null;
    }

    private String convertJavaAction(String action, FileObject fo) {
        //TODO sorty of clashes with .main (if both servlet and main are present.
        // also prohitibs any other conversion method.
        if ( fo.getAttribute(ATTR_EXECUTION_URI) == null && servletFilesScanning(fo)) {
            return null;
        }
        Sources srcs = ProjectUtils.getSources(project);
        SourceGroup[] sourceGroups = srcs.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        for (SourceGroup group : sourceGroups) {
            if (!"2TestSourceRoot".equals(group.getName())) { //NOI18N hack
                String relPath = FileUtil.getRelativePath(group.getRootFolder(), fo);
                if (relPath != null) {
                    if (fo.getAttribute(ATTR_EXECUTION_URI) != null ||
                            Boolean.TRUE.equals(fo.getAttribute(IS_SERVLET_FILE))) {//NOI18N
                        return action + ".deploy"; //NOI18N
                    }
                    if (isServletFile(fo,false))  {
                        try {
                            fo.setAttribute(IS_SERVLET_FILE, Boolean.TRUE);
                        } catch (java.io.IOException ex) {
                            //we tried
                        }
                        return action + ".deploy"; //NOI18N
                    }
                }
            }
        }
        return null;
    }

    private static boolean isServletFile(final FileObject javaClass, boolean initialScan) {
        if (javaClass == null) {
            return false;
        }

        ClassPath classPath = ClassPath.getClassPath (javaClass, ClassPath.SOURCE);
        if (classPath == null) {
            return false;
        }
        String className = classPath.getResourceName(javaClass,'.',false);
        if (className == null) {
            return false;
        }

        final WebModule webModule = WebModule.getWebModule(javaClass);
        if (webModule == null) {
            //not sure how it can happen, but #176535 proves it can
            return false;
        }
        try {
            MetadataModel<WebAppMetadata> metadataModel = webModule.getMetadataModel();
            boolean result = false;
            if ( initialScan || metadataModel.isReady()) {
                List<ServletInfo> servlets = WebAppMetadataHelper.getServlets(metadataModel);
                final List<String> servletClasses = new ArrayList<>( servlets.size() );

                for (ServletInfo si : servlets) {
                    if (className.equals(si.getServletClass())) {
                        result =  true;
                    }
                    else {
                        servletClasses.add(si.getServletClass());
                    }
                }
                synchronized (SERVLET_SEARCH_MODULES) {
                    if (!SERVLET_SEARCH_MODULES.contains(webModule)) {
                        SERVLET_SEARCH_MODULES.add(webModule);
                        if ( !initialScan || SwingUtilities.isEventDispatchThread()){
                            Runnable runnable = new Runnable() {
                                @Override
                                public void run() {
                                    setServletClasses(servletClasses, javaClass, webModule);
                                }
                            };
                            SERVLETS_REQUEST_PROCESSOR.post(runnable);
                        }
                        else {
                            setServletClasses(servletClasses, javaClass, webModule);
                        }
                    }
                }
            }
            return result;
        } catch (java.io.IOException ex) {
            ex.printStackTrace();
        }
        return false;
    }
    
    /**
     * Method check if initial servlet scanning has been started.
     * It's done via setting special mark for project ( actually 
     * for  ProjectWebModule ).
     * 
     * Fix for IZ#172931 - [68cat] AWT thread blocked for 29229 ms.
     */
    private boolean servletFilesScanning( final FileObject fileObject ) {
        if ( isScanFinished.get()){
            return false;
        }
        if ( isScanStarted.get()) {
            return true;
        }
        else {
            Runnable runnable = new Runnable() {

                @Override
                public void run() {
                    isServletFile(fileObject, true);
                    isScanFinished.set(true);
                }
            };
            if (!isScanStarted.get()) {
                /*
                 * Double check . It's not good but not fatal. In the worst case
                 * we will start several initial scanning.
                 */
                isScanStarted.set(true);
                RP.post(runnable);
            }
            return true;
        }
    }
    
    /*
     * Created as  fix for IZ#172931 - [68cat] AWT thread blocked for 29229 ms.
     */
    private static void setServletClasses( final List<String> servletClasses, 
            final FileObject orig , WebModule module ) 
    {
        JavaSource javaSource = JavaSource.forFileObject(orig);
        if (javaSource == null) {
            return;
        }
        final Project project = FileOwnerQuery.getOwner(orig);
        if ( project == null ){
            return;
        }
        try {
            javaSource.runUserActionTask(new Task<CompilationController>() {

                @Override
                public void run( CompilationController controller ) throws Exception {
                    controller.toPhase(Phase.ELEMENTS_RESOLVED);
                    for (String servletClass : servletClasses) {
                        if (servletClass == null) {
                            continue;
                        }
                        TypeElement typeElem = controller.getElements().getTypeElement(servletClass);
                        if (typeElem == null) {
                            continue;
                        }
                        ElementHandle<TypeElement> handle = ElementHandle.
                                create(typeElem);
                        FileObject fileObject = SourceUtils.getFile(handle, 
                                controller.getClasspathInfo());
                        if (fileObject != null && 
                                !Boolean.TRUE.equals(fileObject.getAttribute(
                                        IS_SERVLET_FILE))) 
                        {
                            Sources sources = ProjectUtils.getSources(project);
                            if ( sources != null ){
                                SourceGroup[] sourceGroups = sources.getSourceGroups(
                                        JavaProjectConstants.SOURCES_TYPE_JAVA );
                                for (SourceGroup group : sourceGroups) {
                                    FileObject root = group.getRootFolder();
                                    if ( FileUtil.isParentOf(root, fileObject)){
                                        fileObject.setAttribute(IS_SERVLET_FILE, 
                                                Boolean.TRUE);
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }, true);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            synchronized( SERVLET_SEARCH_MODULES ) {
                SERVLET_SEARCH_MODULES.remove( module );
            }
        }
    }
}
