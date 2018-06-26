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

package org.netbeans.modules.web.struts;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.j2ee.dd.api.common.CreateCapability;
import org.netbeans.modules.j2ee.dd.api.common.InitParam;
import org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException;
import org.netbeans.modules.j2ee.dd.api.web.DDProvider;
import org.netbeans.modules.j2ee.dd.api.web.JspConfig;
import org.netbeans.modules.j2ee.dd.api.web.Servlet;
import org.netbeans.modules.j2ee.dd.api.web.Taglib;
import org.netbeans.modules.j2ee.dd.api.web.WebApp;
import org.netbeans.modules.web.api.webmodule.ExtenderController;
import org.netbeans.modules.web.spi.webmodule.WebModuleExtender;
import org.netbeans.modules.web.struts.config.model.MessageResources;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.FileLock;

import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.modules.j2ee.common.dd.DDHelper;
import org.netbeans.modules.j2ee.dd.api.web.ServletMapping25;
import org.netbeans.modules.j2ee.dd.api.web.WelcomeFileList;

import org.netbeans.modules.web.spi.webmodule.WebFrameworkProvider;
import org.netbeans.modules.web.api.webmodule.WebModule;

import org.netbeans.modules.web.struts.ui.StrutsConfigurationPanel;

import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;


/**
 *
 * @author petr
 */
public class StrutsFrameworkProvider extends WebFrameworkProvider {
    
    
    private static String RESOURCE_FOLDER = "org/netbeans/modules/web/struts/resources/"; //NOI18N
    
    private StrutsConfigurationPanel panel;
    private static String defaultAppResource ="com.myapp.struts.ApplicationResource";  //NOI18N
    
    public StrutsFrameworkProvider(){
        super (
                NbBundle.getMessage(StrutsFrameworkProvider.class, "Sruts_Name"),               //NOI18N
                NbBundle.getMessage(StrutsFrameworkProvider.class, "Sruts_Description"));       //NOI18N
    }
    
    // not named extend() so as to avoid implementing WebFrameworkProvider.extend()
    // better to move this to JSFConfigurationPanel
    public Set extendImpl(WebModule wm) {
        return StrutsUtilities.enableStruts(wm, panel);
    }
    
    private static String readResource(InputStream is, String encoding) throws IOException {
        // read the config from resource first
        StringBuffer sb = new StringBuffer();
        String lineSep = System.getProperty("line.separator");//NOI18N
        BufferedReader br = new BufferedReader(new InputStreamReader(is, encoding));
        String line = br.readLine();
        while (line != null) {
            sb.append(line);
            sb.append(lineSep);
            line = br.readLine();
        }
        br.close();
        return sb.toString();
    }

    public java.io.File[] getConfigurationFiles(org.netbeans.modules.web.api.webmodule.WebModule wm) {
        FileObject webinf = wm.getWebInf();
        List files = new ArrayList();
        // The JavaEE 5 introduce web modules without deployment descriptor. 
        // In such wm can not be struts used. 
        FileObject dd = wm.getDeploymentDescriptor();
        if (dd != null){
            FileObject[] configs = StrutsConfigUtilities.getConfigFilesFO(dd);
            if (configs != null) {
                for (int i = 0; i < configs.length; i ++){
                    files.add(FileUtil.toFile(configs[i]));
                }
            }
            FileObject fo = webinf.getFileObject("tiles-defs.xml");  //NOI18N
            if (fo != null) files.add(FileUtil.toFile(fo));
            fo = webinf.getFileObject("validation.xml");            //NOI18N
            if (fo != null) files.add(FileUtil.toFile(fo));
            fo = webinf.getFileObject("validator-rules.xml");       //NOI18N
            if (fo != null) files.add(FileUtil.toFile(fo));
        }
        
        File [] rFiles = new File [files.size()];
        files.toArray(rFiles);
        return rFiles;
    }

    public boolean isInWebModule(org.netbeans.modules.web.api.webmodule.WebModule wm) {
        return StrutsUtilities.isInWebModule(wm);
    }
    
    public WebModuleExtender createWebModuleExtender(WebModule wm, ExtenderController controller) {
        boolean defaultValue = (wm == null || !isInWebModule(wm));
        panel = new StrutsConfigurationPanel(this, controller, !defaultValue);
        if (defaultValue){
            // get configuration panel with default value
            panel.setAppResource(defaultAppResource);
        }
        else {
            // get configuration panel with values from the wm
            Servlet servlet = StrutsConfigUtilities.getActionServlet(wm.getDeploymentDescriptor());
            panel.setServletName(servlet.getServletName());
            panel.setURLPattern(StrutsConfigUtilities.getActionServletMapping(wm.getDeploymentDescriptor()));
            MessageResources resource = StrutsConfigUtilities.getDefatulMessageResource(wm.getDeploymentDescriptor());
            if (resource != null){
                String name = resource.getAttributeValue("parameter");
                if (name != null) {
                    name = name.replaceAll("/", ".");
                    panel.setAppResource(name);
                }
            }
        }
        
        return panel;
    }
    
    protected static class CreateStrutsConfig implements FileSystem.AtomicAction{
        
        private final WebModule wm;
        private final StrutsConfigurationPanel panel;

        public CreateStrutsConfig (WebModule wm, StrutsConfigurationPanel panel) {
            this.wm = wm;
            this.panel = panel;
        }
        
        private void createFile(FileObject target, String content, String encoding) throws IOException{            
            FileLock lock = target.lock();
            try {
                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(target.getOutputStream(lock), encoding));
                bw.write(content);
                bw.close();

            }
            finally {
                lock.releaseLock();
            }
        }
        
        public void run() throws IOException {
            FileObject target;
            String content;
            // copy struts-config.xml
            if (canCreateNewFile(wm.getWebInf(), "struts-config.xml")) { //NOI18N
                content = readResource (this.getClass().getClassLoader().getResourceAsStream(RESOURCE_FOLDER + "struts-config.xml"), "UTF-8"); //NOI18N
                content = content.replaceFirst("____ACTION_MAPPING___",  //NOI18N
                        StrutsConfigUtilities.getActionAsResource(panel.getURLPattern(), "/Welcome"));
                content = content.replaceFirst("_____MESSAGE_RESOURCE____",  //NOI18N
                        panel.getAppResource().replace('.', '/'));
                target = FileUtil.createData(wm.getWebInf(), "struts-config.xml");//NOI18N
                createFile(target, content, "UTF-8"); //NOI18N
            }
            //copy tiles-defs.xml
            if (canCreateNewFile(wm.getWebInf(), "tiles-defs.xml")) { //NOI18N
                content = readResource (this.getClass().getClassLoader().getResourceAsStream(RESOURCE_FOLDER + "tiles-defs.xml"), "UTF-8"); //NOI18N
                target = FileUtil.createData(wm.getWebInf(), "tiles-defs.xml");//NOI18N
                createFile(target, content, "UTF-8"); //NOI18N
            }
            //copy validation.xml
            if (canCreateNewFile(wm.getWebInf(), "validation.xml")) { //NOI18N
                content = readResource (this.getClass().getClassLoader().getResourceAsStream(RESOURCE_FOLDER + "validation.xml"), "UTF-8"); //NOI18N
                target = FileUtil.createData(wm.getWebInf(), "validation.xml");//NOI18N
                createFile(target, content, "UTF-8"); //NOI18N
            }
            //copy validator-rules.xml
            if (canCreateNewFile(wm.getWebInf(), "validator-rules.xml")) { //NOI18N
                content = readResource (this.getClass().getClassLoader().getResourceAsStream(RESOURCE_FOLDER + "validator-rules.xml"), "UTF-8"); //NOI18N
                target = FileUtil.createData(wm.getWebInf(), "validator-rules.xml");//NOI18N
                createFile(target, content, "UTF-8"); //NOI18N
            }
            
            //MessageResource.properties
            Project project = FileOwnerQuery.getOwner(wm.getDocumentBase());            
            String sresource = panel.getAppResource();
            if (sresource != null && sresource.trim().length()>0) {
                int index = sresource.lastIndexOf('.');
                String path = "";
                String name = sresource;
                if (index > -1){
                    path = sresource.substring(0, sresource.lastIndexOf("."));   //NOI18N
                    name = sresource.substring(sresource.lastIndexOf(".")+1);    //NOI18N
                }
                name = name + ".properties";   //NOI18N
                SourceGroup[] resourceGroups = ProjectUtils.getSources(project).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_RESOURCES);
                if (resourceGroups.length == 0) {
                    resourceGroups = ProjectUtils.getSources(project).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
                }
                FileObject targetFolder = resourceGroups[0].getRootFolder();
                String folders[] = path.split("\\.");
                for (int i = 0; i < folders.length; i++){
                    if (targetFolder.getFileObject(folders[i])== null)
                        targetFolder = targetFolder.createFolder(folders[i]);
                    else
                        targetFolder = targetFolder.getFileObject(folders[i]);
                }
                if (canCreateNewFile(targetFolder, name)) { //NOI18N
                    content = readResource (this.getClass().getClassLoader().getResourceAsStream(RESOURCE_FOLDER + "MessageResources.properties"), "UTF-8"); //NOI18N
                    target = FileUtil.createData(targetFolder, name);//NOI18N
                    createFile(target, content, "UTF-8"); //NOI18N
                }
            }
            
            if (panel.addTLDs()){
                //copy struts-bean.tld
                if (canCreateNewFile(wm.getWebInf(), "struts-bean.tld")) { //NOI18N
                    content = readResource (this.getClass().getClassLoader().getResourceAsStream(RESOURCE_FOLDER + "struts-bean.tld"), "UTF-8"); //NOI18N
                    target = FileUtil.createData(wm.getWebInf(), "struts-bean.tld");//NOI18N
                    createFile(target, content, "UTF-8"); //NOI18N
                }
                //copy struts-html.tld
                if (canCreateNewFile(wm.getWebInf(), "struts-html.tld")) { //NOI18N
                    content = readResource (this.getClass().getClassLoader().getResourceAsStream(RESOURCE_FOLDER + "struts-html.tld"), "UTF-8"); //NOI18N
                    target = FileUtil.createData(wm.getWebInf(), "struts-html.tld");//NOI18N
                    createFile(target, content, "UTF-8"); //NOI18N
                }
                //copy struts-logic.tld
                if (canCreateNewFile(wm.getWebInf(), "struts-logic.tld")) { //NOI18N
                    content = readResource (this.getClass().getClassLoader().getResourceAsStream(RESOURCE_FOLDER + "struts-logic.tld"), "UTF-8"); //NOI18N
                    target = FileUtil.createData(wm.getWebInf(), "struts-logic.tld");//NOI18N
                    createFile(target, content, "UTF-8"); //NOI18N
                }
                //copy struts-nested.tld
                if (canCreateNewFile(wm.getWebInf(), "struts-nested.tld")) { //NOI18N
                    content = readResource (this.getClass().getClassLoader().getResourceAsStream(RESOURCE_FOLDER + "struts-nested.tld"), "UTF-8"); //NOI18N
                    target = FileUtil.createData(wm.getWebInf(), "struts-nested.tld");//NOI18N
                    createFile(target, content, "UTF-8"); //NOI18N
                }
                //copy struts-tiles.tld
                if (canCreateNewFile(wm.getWebInf(), "struts-tiles.tld")) { //NOI18N
                    content = readResource (this.getClass().getClassLoader().getResourceAsStream(RESOURCE_FOLDER + "struts-tiles.tld"), "UTF-8"); //NOI18N
                    target = FileUtil.createData(wm.getWebInf(), "struts-tiles.tld");//NOI18N
                    createFile(target, content, "UTF-8"); //NOI18N
                }
            }
            
            // Enter servlet into the deployment descriptor
            FileObject dd = wm.getDeploymentDescriptor();
            if(dd == null) {
                dd = DDHelper.createWebXml(wm.getJ2eeProfile(), wm.getWebInf());
            }
            WebApp ddRoot = DDProvider.getDefault().getDDRoot(dd);
            if (ddRoot != null && ddRoot.getStatus() == WebApp.STATE_VALID) {
                try{
                    Servlet servlet = (Servlet)ddRoot.createBean("Servlet"); //NOI18N
                    servlet.setServletName("action"); //NOI18N
                    servlet.setServletClass("org.apache.struts.action.ActionServlet"); //NOI18N    

                    ddRoot.addServlet(servlet);

                    InitParam param = (InitParam)servlet.createBean("InitParam"); //NOI18N
                    param.setParamName("config");//NOI18N
                    param.setParamValue("/WEB-INF/struts-config.xml");//NOI18N
                    servlet.addInitParam(param);
                    param = (InitParam)servlet.createBean("InitParam"); //NOI18N
                    param.setParamName("debug");//NOI18N
                    param.setParamValue("2");//NOI18N
                    servlet.addInitParam(param);
                    param = (InitParam)servlet.createBean("InitParam"); //NOI18N
                    param.setParamName("detail");//NOI18N
                    param.setParamValue("2");//NOI18N
                    servlet.addInitParam(param);
                    servlet.setLoadOnStartup(new BigInteger("2"));//NOI18N

                    ServletMapping25 mapping = (ServletMapping25)ddRoot.createBean("ServletMapping"); //NOI18N
                    mapping.setServletName(panel.getServletName());//NOI18N
                    mapping.addUrlPattern(panel.getURLPattern());//NOI18N

                    ddRoot.addServletMapping(mapping);
                    
                    if (panel.addTLDs()){
                        try{
                            JspConfig jspConfig = ddRoot.getSingleJspConfig();
                            if (jspConfig==null){
                                jspConfig = (JspConfig)ddRoot.createBean("JspConfig");
                                ddRoot.setJspConfig(jspConfig);
                            }
                            jspConfig.addTaglib(createTaglib(jspConfig, "/WEB-INF/struts-bean.tld", "/WEB-INF/struts-bean.tld"));  //NOI18N
                            jspConfig.addTaglib(createTaglib(jspConfig, "/WEB-INF/struts-html.tld", "/WEB-INF/struts-html.tld"));  //NOI18N
                            jspConfig.addTaglib(createTaglib(jspConfig, "/WEB-INF/struts-logic.tld", "/WEB-INF/struts-logic.tld"));    //NOI18N
                            jspConfig.addTaglib(createTaglib(jspConfig, "/WEB-INF/struts-nested.tld", "/WEB-INF/struts-nested.tld"));  //NOI18N
                            jspConfig.addTaglib(createTaglib(jspConfig, "/WEB-INF/struts-tiles.tld", "/WEB-INF/struts-tiles.tld"));    //NOI18N
                        }
                        catch (VersionNotSupportedException e){
                            Logger.getLogger("global").log(Level.WARNING, null, e);
                        }
                    }
                    WelcomeFileList welcomeFiles = ddRoot.getSingleWelcomeFileList();
                    if (welcomeFiles == null) {
                        welcomeFiles = (WelcomeFileList) ddRoot.createBean("WelcomeFileList");
                        ddRoot.setWelcomeFileList(welcomeFiles);
                    }
                    if (welcomeFiles.sizeWelcomeFile() == 0) {
                        welcomeFiles.addWelcomeFile("index.jsp"); //NOI18N
                    }
                    ddRoot.write(dd);
                }
                catch (ClassNotFoundException cnfe){
                    Exceptions.printStackTrace(cnfe);
                }
            } else {
                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        NotifyDescriptor warningDialog = new NotifyDescriptor.Message(
                            NbBundle.getMessage(StrutsFrameworkProvider.class, "WARN_UnknownDeploymentDescriptorText"), //NOI18N
                            NotifyDescriptor.WARNING_MESSAGE);
                        DialogDisplayer.getDefault().notify(warningDialog);
                    }
                });

            }
            
            //copy Welcome.jsp
            if (canCreateNewFile(wm.getDocumentBase(), "welcomeStruts.jsp")) { //NOI18N
                content = readResource (this.getClass().getClassLoader().getResourceAsStream(RESOURCE_FOLDER + "welcome.jsp"), "UTF-8"); //NOI18N
                content = content.replaceAll("__ENCODING__", FileEncodingQuery.getDefaultEncoding().name());
                target = FileUtil.createData(wm.getDocumentBase(), "welcomeStruts.jsp");//NOI18N
                createFile(target, content, "UTF-8"); //NOI18N
                File indexJsp = new File(FileUtil.toFile(wm.getDocumentBase()), "index.jsp");  //NOI18N
                if (indexJsp.exists()) {
                    // changing index.jsp
                    FileObject documentBase = wm.getDocumentBase();
                    FileObject indexjsp = documentBase.getFileObject("index.jsp"); //NOI18N
                    if (indexjsp != null){
                        changeIndexJSP(indexjsp);
                    }
                } else {
                    //create welcome file with forward
                    content = "<%@page contentType=\"text/html\"%>\n" + "<%@page pageEncoding=\"" + FileEncodingQuery.getDefaultEncoding().name() + "\"%>\n\n" + //NOI18N
                              "<jsp:forward page=\"" + StrutsConfigUtilities.getWelcomeFile(panel.getURLPattern(), "Welcome") + "\"/>"; //NOI18N
                    target = FileUtil.createData(wm.getDocumentBase(), "index.jsp");//NOI18N
                    createFile(target, content, "UTF-8"); //NOI18N
                }
            }
        }
        
        private boolean canCreateNewFile(FileObject parent, String name){
            File fileToBe = new File(FileUtil.toFile(parent), name);
            boolean create = true;
            if (fileToBe.exists()){
                DialogDescriptor dialog = new DialogDescriptor(
                        NbBundle.getMessage(StrutsFrameworkProvider.class, "MSG_OverwriteFile", fileToBe.getAbsolutePath()),
                        NbBundle.getMessage(StrutsFrameworkProvider.class, "TTL_OverwriteFile"),
                        true, DialogDescriptor.YES_NO_OPTION, DialogDescriptor.NO_OPTION, null);
                java.awt.Dialog d = org.openide.DialogDisplayer.getDefault().createDialog(dialog);
                d.setVisible(true);
                create = (dialog.getValue() == org.openide.DialogDescriptor.NO_OPTION);
            }
            return create;
        }
        
        private Taglib createTaglib(CreateCapability createObject, String location, String uri) throws ClassNotFoundException {
            Taglib taglib = (Taglib)createObject.createBean("Taglib"); //NOI18N
            taglib.setTaglibLocation(location);
            taglib.setTaglibUri(uri);
            return taglib;
        }
        
        /** Changes the index.jsp file. Only when there is <h1>JSP Page</h1> string.
         */
        private void changeIndexJSP(FileObject indexjsp) throws IOException {
            String content = readResource(indexjsp.getInputStream(), "UTF-8"); //NOI18N
            // what find
            String find = "<h1>JSP Page</h1>"; // NOI18N
            String endLine = System.getProperty("line.separator"); //NOI18N
            if ( content.indexOf(find) > 0){
                StringBuffer replace = new StringBuffer();
                replace.append(find);
                replace.append(endLine);
                replace.append("    <br/>");                        //NOI18N
                replace.append(endLine);
                replace.append("    <a href=\".");                  //NOI18N
                replace.append(StrutsConfigUtilities.getActionAsResource(panel.getURLPattern(), "/Welcome")); //NOI18N
                replace.append("\">");                              //NOI18N
                replace.append(NbBundle.getMessage(StrutsFrameworkProvider.class,"LBL_STRUTS_WELCOME_PAGE"));
                replace.append("</a>");                             //NOI18N
                content = content.replaceFirst(find, replace.toString());
                createFile(indexjsp, content, "UTF-8"); //NOI18N
            }
        }
    }
}
