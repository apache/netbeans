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

package org.netbeans.modules.websvc.core.client.wizard;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.queries.UnitTestForSourceQuery;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.websvc.api.jaxws.client.JAXWSClientSupport;
import org.netbeans.modules.websvc.api.jaxws.project.config.Client;
import org.netbeans.modules.websvc.api.jaxws.project.config.JaxWsModel;
import org.netbeans.modules.websvc.core.ClientWizardProperties;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Task;
import org.netbeans.modules.websvc.api.support.ClientCreator;
import org.netbeans.modules.websvc.core.WSStackUtils;

/**
 *
 * @author Radko, Milan Kuchtiak
 */
public class JaxWsClientCreator implements ClientCreator {
    private Project project;
    private WizardDescriptor wiz;
    
    private static final boolean DEBUG = false;
    private static final int JSE_PROJECT_TYPE = 0;
    private static final int WEB_PROJECT_TYPE = 1;
    private static final int EJB_PROJECT_TYPE = 2;
    private static final int CAR_PROJECT_TYPE = 3;
    
    /**
     * Creates a new instance of WebServiceClientCreator
     */
    public JaxWsClientCreator(Project project, WizardDescriptor wiz) {
        this.project = project;
        this.wiz = wiz;
    }
        
    public void createClient() throws IOException {
        
        
        WSStackUtils stackUtils = new WSStackUtils(project);
        final boolean isJsr109Supported = stackUtils.isJsr109Supported();
        //final boolean isJWSDPSupported = isJWSDPSupported();
        
        // Use Progress API to display generator messages.
        final ProgressHandle handle = ProgressHandleFactory.createHandle(NbBundle.getMessage(JaxWsClientCreator.class, "MSG_WizCreateClient")); //NOI18N
        
        task = new Task(new Runnable() {
            public void run() {
                try {
                    handle.start();
                    generate15Client((isJsr109Supported /*|| isJWSDPSupported*/), handle);
                } catch (IOException exc) {
                    //finish progress bar
                    handle.finish();
                    
                    ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, exc);
                }
            }
        });
        RequestProcessor.getDefault().post(task);
    }
    
    private void generate15Client(boolean isJsr109Platform, ProgressHandle handle) throws IOException {
        
        // !PW Get client support from project (from first page of wizard)
        JAXWSClientSupport jaxWsClientSupport=null;
        if(project != null) {
            jaxWsClientSupport = JAXWSClientSupport.getJaxWsClientSupport(project.getProjectDirectory());
        }
        if(jaxWsClientSupport == null) {
            // notify no client support
//			String mes = MessageFormat.format (
//				NbBundle.getMessage (WebServiceClientWizardIterator.class, "ERR_WebServiceClientSupportNotFound"),
//				new Object [] {"Servlet Listener"}); //NOI18N
            String mes = NbBundle.getMessage(WebServiceClientWizardIterator.class, "ERR_NoWebServiceClientSupport"); // NOI18N
            NotifyDescriptor desc = new NotifyDescriptor.Message(mes, NotifyDescriptor.Message.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(desc);
            handle.finish();
            return;
        }
        
        String wsdlUrl = (String)wiz.getProperty(ClientWizardProperties.WSDL_DOWNLOAD_URL);
        String filePath = (String)wiz.getProperty(ClientWizardProperties.WSDL_FILE_PATH);
        Boolean useDispatch = (Boolean) wiz.getProperty(ClientWizardProperties.USEDISPATCH);
        //if (wsdlUrl==null) wsdlUrl = "file:"+(filePath.startsWith("/")?filePath:"/"+filePath); //NOI18N
        if(wsdlUrl == null){
            wsdlUrl = FileUtil.toFileObject(FileUtil.normalizeFile(new File(filePath))).toURL().toExternalForm();
        }
        String packageName = (String)wiz.getProperty(ClientWizardProperties.WSDL_PACKAGE_NAME);
        if (packageName!=null && packageName.length()==0) packageName=null;
        String clientName = jaxWsClientSupport.addServiceClient(getWsdlName(wsdlUrl),wsdlUrl,packageName, isJsr109Platform); 
        if (useDispatch) {
            List clients = jaxWsClientSupport.getServiceClients();
            for (Object c : clients) {
                if (((Client)c).getName().equals(clientName)) {
                    ((Client)c).setUseDispatch(useDispatch);
                }
            }
            JaxWsModel jaxWsModel = (JaxWsModel) project.getLookup().lookup(JaxWsModel.class);
            jaxWsModel.write();
        }
        handle.finish();
    }
    
    private String getWsdlName(String wsdlUrl) {
        int ind = wsdlUrl.lastIndexOf("/"); //NOI18N
        String wsdlName = ind>=0?wsdlUrl.substring(ind+1):wsdlUrl;
        if (wsdlName.toUpperCase().endsWith("?WSDL")) wsdlName = wsdlName.substring(0,wsdlName.length()-5); //NOI18N
        ind = wsdlName.lastIndexOf("."); //NOI18N
        if (ind>0) wsdlName = wsdlName.substring(0,ind);
        // replace special characters with '_'
        return convertAllSpecialChars(wsdlName);
    }
    
    private String convertAllSpecialChars(String resultStr){
        StringBuffer sb = new StringBuffer(resultStr);
        for(int i = 0; i < sb.length(); i++){
            char c = sb.charAt(i);
            if( Character.isLetterOrDigit(c) ||
                    (c == '/') ||
                    (c == '.') ||
                    (c == '_') ||
                    (c == ' ') ||
                    (c == '-')){
                continue;
            }else{
                sb.setCharAt(i, '_');
            }
        }
        return sb.toString();
    }
    
    /**
     * Returns Java source groups for all source packages in given project.<br>
     * Doesn't include test packages.
     *
     * @param project Project to search
     * @return Array of SourceGroup. It is empty if any probelm occurs.
     */
    static SourceGroup[] getJavaSourceGroups(Project project) {
        SourceGroup[] sourceGroups = ProjectUtils.getSources(project).getSourceGroups(
                JavaProjectConstants.SOURCES_TYPE_JAVA);
        Set<SourceGroup> testGroups = getTestSourceGroups(project, sourceGroups);
        List<SourceGroup> result = new ArrayList<SourceGroup>();
        for (int i = 0; i < sourceGroups.length; i++) {
            if (!testGroups.contains(sourceGroups[i])) {
                result.add(sourceGroups[i]);
            }
        }
        return result.<SourceGroup>toArray(new SourceGroup[result.size()]);
    }
    
    private static Set<SourceGroup> getTestSourceGroups(Project project, SourceGroup[] sourceGroups) {
        Map<FileObject, SourceGroup> foldersToSourceGroupsMap = createFoldersToSourceGroupsMap(sourceGroups);
        Set<SourceGroup> testGroups = new HashSet<SourceGroup>();
        for (int i = 0; i < sourceGroups.length; i++) {
            testGroups.addAll(getTestTargets(sourceGroups[i], foldersToSourceGroupsMap));
        }
        return testGroups;
    }
    
    private static List<SourceGroup> getTestTargets(SourceGroup sourceGroup, Map<FileObject, SourceGroup> foldersToSourceGroupsMap) {
        final URL[] rootURLs = UnitTestForSourceQuery.findUnitTests(sourceGroup.getRootFolder());
        if (rootURLs.length == 0) {
            return new ArrayList<SourceGroup>();
        }
        List<SourceGroup> result = new ArrayList<SourceGroup>();
        List<FileObject> sourceRoots = getFileObjects(rootURLs);
        for (int i = 0; i < sourceRoots.size(); i++) {
            FileObject sourceRoot = sourceRoots.get(i);
            SourceGroup srcGroup = foldersToSourceGroupsMap.get(sourceRoot);
            if (srcGroup != null) {
                result.add(srcGroup);
            }
        }
        return result;
    }
    
    private static Map<FileObject, SourceGroup> createFoldersToSourceGroupsMap(final SourceGroup[] sourceGroups) {
        Map<FileObject, SourceGroup> result;
        if (sourceGroups.length == 0) {
            result = Collections.<FileObject, SourceGroup>emptyMap();
        } else {
            result = new HashMap<FileObject, SourceGroup>(2 * sourceGroups.length, .5f);
            for (int i = 0; i < sourceGroups.length; i++) {
                SourceGroup sourceGroup = sourceGroups[i];
                result.put(sourceGroup.getRootFolder(), sourceGroup);
            }
        }
        return result;
    }
    
    private static List<FileObject> getFileObjects(URL[] urls) {
        List<FileObject> result = new ArrayList<FileObject>();
        for (int i = 0; i < urls.length; i++) {
            FileObject sourceRoot = URLMapper.findFileObject(urls[i]);
            if (sourceRoot != null) {
                result.add(sourceRoot);
            } else {
                int severity = ErrorManager.INFORMATIONAL;
                if (ErrorManager.getDefault().isNotifiable(severity)) {
                    ErrorManager.getDefault().notify(severity, new IllegalStateException(
                            "No FileObject found for the following URL: " + urls[i])); //NOI18N
                }
            }
        }
        return result;
    }

    /**
      *
      * <b>DON'T USE</b>, for tests only
      */
    Task task;
}
