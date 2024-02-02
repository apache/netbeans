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

package org.netbeans.modules.web.jsf.wizards;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.MethodTree;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.EnumSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.swing.text.BadLocationException;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.progress.aggregate.ProgressContributor;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.editor.indent.api.Reformat;
import org.netbeans.modules.j2ee.core.api.support.java.method.MethodModel;
import org.netbeans.modules.j2ee.core.api.support.java.method.MethodModelSupport;
import org.netbeans.modules.j2ee.core.api.support.java.GenerationUtils;
import org.netbeans.modules.j2ee.core.api.support.java.SourceUtils;
import org.netbeans.modules.j2ee.dd.api.common.InitParam;
import org.netbeans.modules.j2ee.dd.api.web.DDProvider;
import org.netbeans.modules.j2ee.dd.api.web.Servlet;
import org.netbeans.modules.j2ee.dd.api.web.WebApp;
import org.netbeans.modules.j2ee.persistence.api.PersistenceScope;
import org.netbeans.modules.j2ee.persistence.dd.PersistenceMetadata;
import org.netbeans.modules.j2ee.persistence.dd.PersistenceUtils;
import org.netbeans.modules.j2ee.persistence.dd.common.Persistence;
import org.netbeans.modules.j2ee.persistence.dd.common.PersistenceUnit;
import org.netbeans.modules.j2ee.persistence.wizard.Util;
import org.netbeans.modules.j2ee.persistence.wizard.fromdb.ProgressPanel;
import org.netbeans.modules.j2ee.persistence.wizard.jpacontroller.JpaControllerIterator;
import org.netbeans.modules.j2ee.persistence.wizard.jpacontroller.JpaControllerUtil;
import org.netbeans.modules.j2ee.persistence.wizard.jpacontroller.JpaControllerUtil.AnnotationInfo;
import org.netbeans.modules.j2ee.persistence.wizard.jpacontroller.JpaControllerUtil.EmbeddedPkSupport;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.api.webmodule.WebProjectConstants;
import org.netbeans.modules.web.jsf.JSFFrameworkProvider;
import org.netbeans.modules.web.jsf.api.ConfigurationUtils;
import org.netbeans.modules.web.jsf.api.facesmodel.Converter;
import org.netbeans.modules.web.jsf.api.facesmodel.FacesConfig;
import org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigModel;
import org.netbeans.modules.web.jsf.api.facesmodel.ManagedBean;
import org.netbeans.modules.web.jsf.api.facesmodel.NavigationCase;
import org.netbeans.modules.web.jsf.api.facesmodel.NavigationRule;
import org.netbeans.modules.web.jsf.palette.items.JsfForm;
import org.netbeans.modules.web.jsf.palette.items.JsfTable;
import org.netbeans.modules.j2ee.persistence.wizard.jpacontroller.JpaControllerUtil.TypeInfo;
import org.netbeans.modules.j2ee.persistence.wizard.jpacontroller.JpaControllerUtil.MethodInfo;
import org.netbeans.modules.web.jsf.api.facesmodel.Application;
import org.netbeans.modules.web.jsf.api.facesmodel.JsfVersionUtils;
import org.netbeans.modules.web.jsfapi.api.JsfVersion;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author Pavel Buzek
 * @author mbohm
 * @author Alexey Butenko
 */
public class JSFClientGenerator {
    
    private static final String WELCOME_JSF_JSP_PAGE = "welcomeJSF.jsp";  //NOI18N
    private static final String WELCOME_JSF_FL_PAGE = "index.xhtml";  //NOI18N
    public static final String TEMPLATE_JSF_FL_PAGE = "template.xhtml";  //NOI18N
    public static final String JSFCRUD_STYLESHEET = "jsfcrud.css"; //NOI18N
    public static final String JSFCRUD_JAVASCRIPT = "jsfcrud.js"; //NOI18N
    private static final String JSPF_FOLDER = "WEB-INF/jspf"; //NOI18N
    private static final String JSFCRUD_AJAX_JSPF = "AjaxScripts.jspf"; //NOI18N
    private static final String JSFCRUD_AJAX_BUSY_IMAGE = "busy.gif"; //NOI18N
    static final String RESOURCE_FOLDER = "org/netbeans/modules/web/jsf/resources/"; //NOI18N
    static final String FL_RESOURCE_FOLDER = "org/netbeans/modules/web/jsf/facelets/resources/templates/"; //NOI18N
    private static final String TEMPLATE_FOLDER = RESOURCE_FOLDER+"templates/";  //NOI18N
    private static final String COMMAND_LINK_TEMPLATE = "commandLink.template"; //NOI18N
    private static final String COMMAND_LINK_TEMPLATE2 = "commandLink-jsf2.template"; //NOI18N
    private static final String FACADE_SUFFIX = "Facade"; //NOI18N
    private static final String CONVERTER_SUFFIX = "Converter"; //NOI18N
    private static final String COTROLLER_SUFFIX = "Controller"; //NOI18N
    private static final String JSP_MIME_TYPE = "text/x-jsp"; //NOI18N
    private static final String LIFECYCLE_ID_CLASS = "javax.faces.LIFECYCLE_ID";  //NOI18N
    private static final String PARTIAL_CLASS = "com.sun.faces.lifecycle.PARTIAL";  //NOI18N
    private static final String INITPARAM_BEAN_NAME = "InitParam";                        //NOI18N
    static final int PROGRESS_STEP_COUNT = 8;

    private static final String NEW_JSP_TEMPLATE = "newJsp.template";  //NOI18N
    private static final String LIST_JSP_TEMPLATE = "listJsp.template";  //NOI18N
    private static final String COMMANDS_TEMPLATE = "tableCommands.template";  //NOI18N
    private static final String EDIT_JSP_TEMPLATE = "editJsp.template"; //NOI18N
    private static final String DETAIL_JSP_TEMPLATE = "detailJsp.template"; //NOI18N

    private static final String NEW_JSP = "New.jsp";  //NOI18N
    private static final String LIST_JSP = "List.jsp";  //NOI18N
    private static final String EDIT_JSP = "Edit.jsp"; //NOI18N
    private static final String DETAIL_JSP = "Detail.jsp"; //NOI18N

    private static final String ENCODING_VAR="__ENCODING__"; //NOI18N
    private static final String LINK_TO_SS_VAR="__LINK_TO_SS__"; //NOI18N
    private static final String ENTITY_NAME_VAR = "__ENTITY_NAME__"; //NOI18N
    private static final String MANAGED_BEAN_NAME_VAR = "__MANAGED_BEAN_NAME__"; //NOI18N
    private static final String FIELD_NAME_VAR = "__FIELD_NAME__"; //NOI18N
    private static final String FORM_BODY_VAR = "__FORM_BODY__"; //NOI18N
    private static final String TABLE_BODY_VAR = "__TABLE_BODY__"; //NOI18N
    private static final String JSF_UTIL_CLASS_VAR = "__JSF_UTIL_CLASS__"; //NOI18N
    private static final String LINK_TO_INDEX_VAR = "__LINK_TO_INDEX__"; //NOI18N
    private static final String INDENT = "            "; // TODO: jsut reformat generated code

    
    public static void generateJSFPages(ProgressContributor progressContributor, ProgressPanel progressPanel, final Project project, final String entityClass, String jsfFolderBase, String jsfFolderName, final String controllerPackage, final String controllerClass, FileObject pkg, FileObject controllerFileObject, final EmbeddedPkSupport embeddedPkSupport, final List<String> entities, final boolean ajaxify, String jpaControllerPackage, FileObject jpaControllerFileObject, FileObject converterFileObject, final boolean genSessionBean, int progressIndex) throws IOException {
        final boolean isInjection = Util.isContainerManaged(project); //Util.isSupportedJavaEEVersion(project);
        final boolean jakartaJsfPackages = JsfVersionUtils.forProject(project).isAtLeast(JsfVersion.JSF_3_0);
        
        String simpleControllerName = controllerFileObject.getName();
        
        String progressMsg = NbBundle.getMessage(JSFClientGenerator.class, "MSG_Progress_Jsf_Controller_Pre", simpleControllerName + ".java");//NOI18N
        progressContributor.progress(progressMsg, progressIndex++);
        progressPanel.setText(progressMsg); 
        
        final String simpleEntityName = JpaControllerUtil.simpleClassName(entityClass);
        String jsfFolder = jsfFolderBase.length() > 0 ? jsfFolderBase + File.separator + jsfFolderName : jsfFolderName;
        
        String simpleConverterName = simpleEntityName + CONVERTER_SUFFIX;
        
        String jpaControllerClass = ((jpaControllerPackage == null || jpaControllerPackage.length() == 0) ? "" : jpaControllerPackage + ".") + jpaControllerFileObject.getName();
        
        String utilPackage = ((controllerPackage == null || controllerPackage.length() == 0) ? "" : controllerPackage + ".") + PersistenceClientIterator.UTIL_FOLDER_NAME;
        
        Sources srcs = (Sources) project.getLookup().lookup(Sources.class);
        int lastIndexOfDotInControllerClass = controllerClass.lastIndexOf('.');
        String pkgName = lastIndexOfDotInControllerClass == -1 ? "" : controllerClass.substring(0, lastIndexOfDotInControllerClass);
        
        String persistenceUnit = null;
        PersistenceScope persistenceScopes[] = PersistenceUtils.getPersistenceScopes(project);
        if (persistenceScopes.length > 0) {
            FileObject persXml = persistenceScopes[0].getPersistenceXml();
            if (persXml != null) {
                Persistence persistence = PersistenceMetadata.getDefault().getRoot(persXml);
                PersistenceUnit units[] = persistence.getPersistenceUnit();
                if (units.length > 0) {
                    persistenceUnit = units[0].getName();
                }
            }
        }
        SourceGroup sgWeb[] = srcs.getSourceGroups(WebProjectConstants.TYPE_DOC_ROOT);
        FileObject pagesRootFolder = sgWeb[0].getRootFolder();
        int jsfFolderNameAttemptIndex = 1;
        while (pagesRootFolder.getFileObject(jsfFolder) != null && jsfFolderNameAttemptIndex < 1000) {
            jsfFolder += "_" + jsfFolderNameAttemptIndex++;
        }
        final FileObject jsfRoot = FileUtil.createFolder(pagesRootFolder, jsfFolder);
        
        String converterName = ((pkgName == null || pkgName.length() == 0) ? "" : pkgName + ".") + simpleConverterName;
        final String fieldName = JpaControllerUtil.fieldFromClassName(simpleEntityName);

        final List<ElementHandle<ExecutableElement>> idGetter = new ArrayList<ElementHandle<ExecutableElement>>();
        final FileObject[] arrEntityClassFO = new FileObject[1];
        final List<ElementHandle<ExecutableElement>> toOneRelMethods = new ArrayList<ElementHandle<ExecutableElement>>();
        final List<ElementHandle<ExecutableElement>> toManyRelMethods = new ArrayList<ElementHandle<ExecutableElement>>();
        final boolean[] fieldAccess = new boolean[] { false };
        final String[] idProperty = new String[1];

        //detect access type
        final ClasspathInfo classpathInfo = ClasspathInfo.create(pkg);
        JavaSource javaSource = JavaSource.create(classpathInfo);
        javaSource.runUserActionTask(new Task<CompilationController>() {
            @Override
            public void run(CompilationController controller) throws IOException {
                controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                TypeElement jc = controller.getElements().getTypeElement(entityClass);
                arrEntityClassFO[0] = org.netbeans.api.java.source.SourceUtils.getFile(ElementHandle.create(jc), controller.getClasspathInfo());
                fieldAccess[0] = JpaControllerUtil.isFieldAccess(jc);
                for (ExecutableElement method : JpaControllerUtil.getEntityMethods(jc)) {
                    String methodName = method.getSimpleName().toString();
                    if (methodName.startsWith("get")) {
                        Element f = fieldAccess[0] ? JpaControllerUtil.guessField(method) : method;
                        if (f != null) {
                            if (JpaControllerUtil.isAnnotatedWith(f, "jakarta.persistence.Id")
                                    || JpaControllerUtil.isAnnotatedWith(f, "jakarta.persistence.EmbeddedId")
                                    || JpaControllerUtil.isAnnotatedWith(f, "javax.persistence.Id")
                                    || JpaControllerUtil.isAnnotatedWith(f, "javax.persistence.EmbeddedId")
                            ) {
                                idGetter.add(ElementHandle.create(method));
                                idProperty[0] = JpaControllerUtil.getPropNameFromMethod(methodName);
                            } else if (JpaControllerUtil.isAnnotatedWith(f, "jakarta.persistence.OneToOne")
                                    || JpaControllerUtil.isAnnotatedWith(f, "jakarta.persistence.ManyToOne")
                                    || JpaControllerUtil.isAnnotatedWith(f, "javax.persistence.OneToOne")
                                    || JpaControllerUtil.isAnnotatedWith(f, "javax.persistence.ManyToOne")
                            ) {
                                toOneRelMethods.add(ElementHandle.create(method));
                            } else if (JpaControllerUtil.isAnnotatedWith(f, "jakarta.persistence.OneToMany")
                                    || JpaControllerUtil.isAnnotatedWith(f, "jakarta.persistence.ManyToMany")
                                    || JpaControllerUtil.isAnnotatedWith(f, "javax.persistence.OneToMany")
                                    || JpaControllerUtil.isAnnotatedWith(f, "javax.persistence.ManyToMany")
                            ) {
                                toManyRelMethods.add(ElementHandle.create(method));
                            }
                        }
                    }
                }
            }
        }, true);
        
        if (idGetter.size() < 1) {
            String msg = entityClass + ": " + NbBundle.getMessage(JSFClientGenerator.class, "ERR_GenJsfPages_CouldNotFindIdProperty"); //NOI18N
            if (fieldAccess[0]) {
                msg += " " + NbBundle.getMessage(JSFClientGenerator.class, "ERR_GenJsfPages_EnsureSimpleIdNaming"); //NOI18N
            }
            throw new IOException(msg);
        }
            
        final BaseDocument doc = new BaseDocument(false, JSP_MIME_TYPE);
        WebModule wm = WebModule.getWebModule(jsfRoot);
        
        FileObject dd = wm.getDeploymentDescriptor();
        WebApp ddRoot = DDProvider.getDefault().getDDRoot(dd);
        if (ajaxify && ddRoot != null) {
            boolean foundAjaxInitParam = false;
            Servlet servlet = ConfigurationUtils.getFacesServlet(wm);
            InitParam[] initParams = servlet.getInitParam();
            for (InitParam initParam : initParams) {
                if (LIFECYCLE_ID_CLASS.equals(initParam.getParamName()) &&
                        PARTIAL_CLASS.equals(initParam.getParamValue())) {
                    foundAjaxInitParam = true;
                    break;
                }
            }
            if (!foundAjaxInitParam) {
                InitParam contextParam = null;
                try {
                    contextParam = (InitParam)servlet.createBean(INITPARAM_BEAN_NAME);
                } catch (ClassNotFoundException cnfe) {
                    Logger.getLogger(JSFClientGenerator.class.getName()).log(Level.WARNING, "CNFE attempting to create "+ LIFECYCLE_ID_CLASS+" init parameter in web.xml", cnfe);
                }
                contextParam.setParamName(LIFECYCLE_ID_CLASS);
                contextParam.setParamValue(PARTIAL_CLASS);
                servlet.addInitParam(contextParam);
            }
            ddRoot.write(dd);
        }
        
        String projectEncoding = JpaControllerUtil.getProjectEncodingAsString(project, controllerFileObject);
        
        if (wm.getDocumentBase().getFileObject(WELCOME_JSF_JSP_PAGE) == null) {
            if(wm.getDocumentBase().getFileObject(WELCOME_JSF_FL_PAGE)==null)//also there is no default facelet
            {
                String content = JSFFrameworkProvider.readResource(JSFClientGenerator.class.getClassLoader().getResourceAsStream(RESOURCE_FOLDER + WELCOME_JSF_JSP_PAGE), "UTF-8"); //NOI18N
                content = content.replaceAll(ENCODING_VAR, Matcher.quoteReplacement(projectEncoding));
                FileObject target = FileUtil.createData(wm.getDocumentBase(), WELCOME_JSF_JSP_PAGE);
                JSFFrameworkProvider.createFile(target, content, projectEncoding);
            }
        }
        
        if (pagesRootFolder.getFileObject(JSFCRUD_STYLESHEET) == null) {
            String content = JSFFrameworkProvider.readResource(JSFClientGenerator.class.getClassLoader().getResourceAsStream(RESOURCE_FOLDER + JSFCRUD_STYLESHEET), "UTF-8"); //NOI18N
            FileObject target = FileUtil.createData(pagesRootFolder, JSFCRUD_STYLESHEET);
            JSFFrameworkProvider.createFile(target, content, projectEncoding);
        }

        final String facesServletMapping = ConfigurationUtils.getFacesServletMapping(wm);
        final String busyIconPath = wm.getContextPath() + "/"+ConfigurationUtils.translateURI(facesServletMapping, JSFCRUD_AJAX_BUSY_IMAGE);
        
        if (pagesRootFolder.getFileObject(JSFCRUD_JAVASCRIPT) == null) {
            String content = JSFFrameworkProvider.readResource(JSFClientGenerator.class.getClassLoader().getResourceAsStream(RESOURCE_FOLDER + JSFCRUD_JAVASCRIPT), "UTF-8"); //NOI18N
            FileObject target = FileUtil.createData(pagesRootFolder, JSFCRUD_JAVASCRIPT);//NOI18N
            content = content.replaceAll("__WEB_BUSY_ICON_PATH__", Matcher.quoteReplacement(busyIconPath));//NOI18N
            JSFFrameworkProvider.createFile(target, content, projectEncoding);  //NOI18N
        }

        if (ajaxify) {
            String ajaxJspfPath = JSPF_FOLDER + "/" + JSFCRUD_AJAX_JSPF;
            if (pagesRootFolder.getFileObject(ajaxJspfPath) == null) {
                String content = JSFFrameworkProvider.readResource(JSFClientGenerator.class.getClassLoader().getResourceAsStream(RESOURCE_FOLDER + JSFCRUD_AJAX_JSPF), "UTF-8"); //NOI18N
                FileObject target = FileUtil.createData(pagesRootFolder, ajaxJspfPath);//NOI18N
                JSFFrameworkProvider.createFile(target, content, projectEncoding);  //NOI18N
            }
            
            if (pagesRootFolder.getFileObject(JSFCRUD_AJAX_BUSY_IMAGE) == null) {
                FileObject target = FileUtil.createData(pagesRootFolder, JSFCRUD_AJAX_BUSY_IMAGE);//NOI18N
                FileLock lock = target.lock();
                try {
                    InputStream is = JSFClientGenerator.class.getClassLoader().getResourceAsStream(RESOURCE_FOLDER + JSFCRUD_AJAX_BUSY_IMAGE);
                    BufferedInputStream bis = new BufferedInputStream(is);
                    OutputStream os = target.getOutputStream(lock);
                    BufferedOutputStream bos = new BufferedOutputStream(os);
                    int c;
                    while ((c = bis.read()) != -1) {
                        bos.write(c);
                    }
                    bis.close();
                    bos.close();
                } finally {
                    lock.releaseLock();
                }
            }
        }
        
        progressMsg = NbBundle.getMessage(JSFClientGenerator.class, "MSG_Progress_Jsf_Now_Generating", simpleControllerName + ".java"); //NOI18N
        progressContributor.progress(progressMsg, progressIndex++);
        progressPanel.setText(progressMsg);
        
        controllerFileObject = generateControllerClass(fieldName, pkg, idGetter.get(0),
                persistenceUnit, controllerPackage, controllerClass, simpleConverterName,
                entityClass, simpleEntityName, toOneRelMethods, toManyRelMethods, isInjection,
                fieldAccess[0], controllerFileObject, embeddedPkSupport, jpaControllerPackage,
                jpaControllerClass, genSessionBean, utilPackage, jakartaJsfPackages);
        
        progressMsg = NbBundle.getMessage(JSFClientGenerator.class, "MSG_Progress_Jsf_Now_Generating", simpleConverterName + ".java"); //NOI18N
        progressContributor.progress(progressMsg, progressIndex++);
        progressPanel.setText(progressMsg);
        
        final String managedBean =  getManagedBeanName(simpleEntityName);
        converterFileObject = generateConverter(converterFileObject, controllerFileObject, pkg, controllerClass, simpleControllerName, entityClass, 
                simpleEntityName, idGetter.get(0), managedBean, jpaControllerClass, genSessionBean, isInjection, jakartaJsfPackages);
        
        final String styleAndScriptTags = "<link rel=\"stylesheet\" type=\"text/css\" href=\"" + wm.getContextPath() + "/" + ConfigurationUtils.translateURI(facesServletMapping, JSFCRUD_STYLESHEET) + "\" />" +
            (ajaxify ? "<%@ include file=\"/" + JSPF_FOLDER + "/" + JSFCRUD_AJAX_JSPF + "\" %><script type=\"text/javascript\" src=\"" + wm.getContextPath() + "/" + ConfigurationUtils.translateURI(facesServletMapping, JSFCRUD_JAVASCRIPT) + "\"></script>" : "");
            
        boolean welcomePageExists = addLinkToListJspIntoIndexJsp(wm, simpleEntityName, styleAndScriptTags, projectEncoding, "");
        final String linkToIndex = welcomePageExists ? "<br />\n<h:commandLink value=\"Index\" action=\"welcome\" immediate=\"true\" />\n" : "";  //NOI18N

        progressMsg = NbBundle.getMessage(JSFClientGenerator.class, "MSG_Progress_Jsf_Now_Generating", jsfFolderName + File.separator + LIST_JSP);
        progressContributor.progress(progressMsg, progressIndex++);
        progressPanel.setText(progressMsg);
        generateListJsp(project, jsfRoot, classpathInfo, entityClass, simpleEntityName, managedBean, linkToIndex, fieldName, idProperty[0], doc, embeddedPkSupport, styleAndScriptTags, entities, controllerPackage, genSessionBean);
        
        progressMsg = NbBundle.getMessage(JSFClientGenerator.class, "MSG_Progress_Jsf_Now_Generating", jsfFolderName + File.separator + NEW_JSP);
        progressContributor.progress(progressMsg, progressIndex++);
        progressPanel.setText(progressMsg);
        javaSource.runUserActionTask(new Task<CompilationController>() {
            public void run(CompilationController controller) throws IOException {
                controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                generateNewJsp(project, controller, entityClass, simpleEntityName, managedBean, fieldName, toOneRelMethods, fieldAccess[0], linkToIndex, doc, jsfRoot, embeddedPkSupport, controllerClass, styleAndScriptTags, controllerPackage);
            }
        }, true);
        
        progressMsg = NbBundle.getMessage(JSFClientGenerator.class, "MSG_Progress_Jsf_Now_Generating", jsfFolderName + File.separator + EDIT_JSP);
        progressContributor.progress(progressMsg, progressIndex++);
        progressPanel.setText(progressMsg);
        javaSource.runUserActionTask(new Task<CompilationController>() {
            public void run(CompilationController controller) throws IOException {
                controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                generateEditJsp(project, controller, entityClass, simpleEntityName, managedBean, fieldName, linkToIndex, doc, jsfRoot, embeddedPkSupport, controllerClass, styleAndScriptTags, controllerPackage);
            }
        }, true);
        
        progressMsg = NbBundle.getMessage(JSFClientGenerator.class, "MSG_Progress_Jsf_Now_Generating", jsfFolderName + File.separator+ DETAIL_JSP);
        progressContributor.progress(progressMsg, progressIndex++);
        progressPanel.setText(progressMsg);
        javaSource.runUserActionTask(new Task<CompilationController>() {
            public void run(CompilationController controller) throws IOException {
                controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                generateDetailJsp(project, controller, entityClass, simpleEntityName, managedBean, fieldName, idProperty[0], isInjection, linkToIndex, doc, jsfRoot, embeddedPkSupport, controllerClass, styleAndScriptTags, entities, controllerPackage, genSessionBean);
            }
        }, true);
        
        progressMsg = NbBundle.getMessage(JSFClientGenerator.class, "MSG_Progress_Updating_Faces_Config", simpleEntityName); //NOI18N
        progressContributor.progress(progressMsg, progressIndex++);
        progressPanel.setText(progressMsg);
        String facesConfigSimpleControllerName = simpleEntityName + COTROLLER_SUFFIX;
        String facesConfigControllerClass = pkgName.length() == 0 ? facesConfigSimpleControllerName : pkgName + "." + facesConfigSimpleControllerName;
        String facesConfigJsfFolderName = simpleEntityName.substring(0, 1).toLowerCase() + simpleEntityName.substring(1);
        String facesConfigJsfFolder = jsfFolderBase.length() > 0 ? jsfFolderBase + "/" + facesConfigJsfFolderName : facesConfigJsfFolderName;
        addStuffToFacesConfigXml(classpathInfo, wm, managedBean, facesConfigControllerClass, jpaControllerClass, entityClass, converterName, fieldName, facesConfigJsfFolder, idGetter.get(0), pkgName, utilPackage);
    }

    public static boolean addLinkToListJspIntoIndexJsp(WebModule wm, String simpleEntityName, String styleAndScriptTags, 
            String projectEncoding, String pageLink) throws FileNotFoundException, IOException {
        FileObject documentBase = wm.getDocumentBase();
        FileObject indexjsp = documentBase.getFileObject(WELCOME_JSF_JSP_PAGE); //NOI18N
        FileObject indexfl = documentBase.getFileObject(WELCOME_JSF_FL_PAGE);

        if (indexjsp != null) {
            String content = JSFFrameworkProvider.readResource(indexjsp.getInputStream(), projectEncoding); //NO18N
            String endLine = System.getProperty("line.separator"); //NOI18N
            
            //insert style and script tags if not already present
            if (content.indexOf(styleAndScriptTags) == -1) {
                String justTitleEnd = "</title>"; //NOI18N
                String replaceHeadWith = justTitleEnd + endLine + styleAndScriptTags;    //NOI18N
                content = content.replace(justTitleEnd, replaceHeadWith); //NOI18N
            }
            
            //make sure <f:view> is outside of <html>
            String html = "<html>";
            String htmlEnd = "</html>";
            int htmlIndex = content.indexOf(html);
            int htmlEndIndex = content.indexOf(htmlEnd);
            if (htmlIndex != -1 && htmlEndIndex != -1) {
                String fview = "<f:view>";
                String fviewEnd = "</f:view>";
                int fviewIndex = content.indexOf(fview);
                if (fviewIndex != -1 && fviewIndex > htmlIndex) {
                    content = content.replace(fview, ""); //NOI18N
                    content = content.replace(fviewEnd, ""); //NOI18N
                    String fviewPlusHtml = fview + endLine + html;
                    String htmlEndPlusFviewEnd = htmlEnd + endLine + fviewEnd;
                    content = content.replace(html, fviewPlusHtml); //NOI18N
                    content = content.replace(htmlEnd, htmlEndPlusFviewEnd); //NOI18N
                }
            }
            
            String find = "<h1><h:outputText value=\"JavaServer Faces\"/></h1>"; //NOI18N
            if ( content.indexOf(find) > -1){
                StringBuffer replace = new StringBuffer();
                replace.append(find);
                replace.append(endLine);
                StringBuffer replaceCrux = new StringBuffer();
                replaceCrux.append("    <br/>");                        //NOI18N
                replaceCrux.append(endLine);
                String managedBeanName = getManagedBeanName(simpleEntityName);
                String commandLink = JSFFrameworkProvider.readResource(JSFClientGenerator.class.getClassLoader().getResourceAsStream(TEMPLATE_FOLDER + COMMAND_LINK_TEMPLATE), "UTF-8"); //NOI18N
                commandLink = commandLink.replaceAll(MANAGED_BEAN_NAME_VAR, Matcher.quoteReplacement(managedBeanName));
                commandLink = commandLink.replaceAll(ENTITY_NAME_VAR, Matcher.quoteReplacement(simpleEntityName));
                if (content.indexOf(replaceCrux.toString()) > -1) {
                    //return, indicating welcomeJsp exists
                    return true;
                }
                replace.append(commandLink);
                content = content.replace(find, replace.toString()); //NOI18N
                JSFFrameworkProvider.createFile(indexjsp, content, projectEncoding); //NOI18N
                //return, indicating welcomeJsp exists
                return true;
            }
        }
        else if(indexfl != null) {
            String content = JSFFrameworkProvider.readResource(indexfl.getInputStream(), projectEncoding); //NO18N
            String endLine = System.getProperty("line.separator"); //NOI18N
            //insert style and script tags if not already present
            if (content.indexOf(styleAndScriptTags) == -1) {
                String justTitleEnd = "</title>"; //NOI18N
                String replaceHeadWith = justTitleEnd + endLine + styleAndScriptTags;    //NOI18N
                content = content.replace(justTitleEnd, replaceHeadWith); //NOI18N
            }
            
            //make sure <f:view> is outside of <html>
            String html = "<html>";
            String htmlEnd = "</html>";
            int htmlIndex = content.indexOf(html);
            int htmlEndIndex = content.indexOf(htmlEnd);
            if (htmlIndex != -1 && htmlEndIndex != -1) {
                String fview = "<f:view>";
                String fviewEnd = "</f:view>";
                int fviewIndex = content.indexOf(fview);
                if (fviewIndex != -1 && fviewIndex > htmlIndex) {
                    content = content.replace(fview, ""); //NOI18N
                    content = content.replace(fviewEnd, ""); //NOI18N
                    String fviewPlusHtml = fview + endLine + html;
                    String htmlEndPlusFviewEnd = htmlEnd + endLine + fviewEnd;
                    content = content.replace(html, fviewPlusHtml); //NOI18N
                    content = content.replace(htmlEnd, htmlEndPlusFviewEnd); //NOI18N
                }
            }

            String find = "</h:body>"; //NOI18N
            boolean isFound = false;
            if (content.indexOf(find)>-1) {
                isFound = true;
            } else {
                find = "</body>";   //NOI18N
                if (content.indexOf(find)>-1) {
                    isFound = true;
                }
            }

            if ( isFound ){
                StringBuffer replace = new StringBuffer();
                String managedBeanName = getManagedBeanName(simpleEntityName);
                String commandLink = "";
                if (pageLink == null || "".equals(pageLink)) {
                    commandLink = JSFFrameworkProvider.readResource(JSFClientGenerator.class.getClassLoader().getResourceAsStream(TEMPLATE_FOLDER + COMMAND_LINK_TEMPLATE), "UTF-8"); //NOI18N
                    commandLink = commandLink.replaceAll(MANAGED_BEAN_NAME_VAR, Matcher.quoteReplacement(managedBeanName));
                    commandLink = commandLink.replaceAll(ENTITY_NAME_VAR, Matcher.quoteReplacement(simpleEntityName));
                } else {
                    commandLink = JSFFrameworkProvider.readResource(JSFClientGenerator.class.getClassLoader().getResourceAsStream(TEMPLATE_FOLDER + COMMAND_LINK_TEMPLATE2), "UTF-8"); //NOI18N
                    commandLink = commandLink.replaceAll("\\_\\_PAGE\\_LINK\\_\\_", Matcher.quoteReplacement(pageLink));
                    commandLink = commandLink.replaceAll(ENTITY_NAME_VAR, Matcher.quoteReplacement(simpleEntityName));
                }
                if (content.indexOf(commandLink) > -1) {
                    //return, indicating welcomeJsp exists
                    return true;
                }
                replace.append(commandLink);
                replace.append(find);
                replace.append(endLine);
                content = content.replace(find, replace.toString()); //NOI18N

                // reformat document
                content = reformat(content);

                JSFFrameworkProvider.createFile(indexfl, content, projectEncoding); //NOI18N
                //return, indicating welcomeJsp exists
                return true;
            }
        }
        return false;
    }

    private static void generateListJsp(Project project, final FileObject jsfRoot, ClasspathInfo classpathInfo, final String entityClass, String simpleEntityName, 
            final String managedBean, String linkToIndex, final String fieldName, String idProperty, BaseDocument doc, final EmbeddedPkSupport embeddedPkSupport, String styleAndScriptTags, List<String> entities, String controllerPackage, boolean useSessionBean) throws FileStateInvalidException, IOException {
        final String tableVarName = JsfForm.getFreeTableVarName("item", entities); //NOI18N
        FileSystem fs = jsfRoot.getFileSystem();
        final Charset encoding = JpaControllerUtil.getProjectEncoding(project, jsfRoot);

        String content = JSFFrameworkProvider.readResource(JSFClientGenerator.class.getClassLoader().getResourceAsStream(TEMPLATE_FOLDER + LIST_JSP_TEMPLATE), "UTF-8"); //NOI18N
        content = content.replaceAll(ENCODING_VAR, Matcher.quoteReplacement(encoding.name()));
        content = content.replaceAll(ENTITY_NAME_VAR, Matcher.quoteReplacement(simpleEntityName));
        content = content.replaceAll(LINK_TO_SS_VAR, Matcher.quoteReplacement(styleAndScriptTags));
        content = content.replaceAll(MANAGED_BEAN_NAME_VAR, Matcher.quoteReplacement(managedBean));
        content = content.replaceAll(FIELD_NAME_VAR, Matcher.quoteReplacement(fieldName+"Items"));  //NOI18N
        content = content.replaceAll("__TABLE_VAR_NAME__", Matcher.quoteReplacement(tableVarName));
        
        final StringBuffer tableBody = new StringBuffer();


        String utilPackage = controllerPackage == null || controllerPackage.length() == 0 ? PersistenceClientIterator.UTIL_FOLDER_NAME : controllerPackage + "." + PersistenceClientIterator.UTIL_FOLDER_NAME;
        String jsfUtilClass = utilPackage + "." + PersistenceClientIterator.UTIL_CLASS_NAMES[1];
        
        String commands = JSFFrameworkProvider.readResource(JSFClientGenerator.class.getClassLoader().getResourceAsStream(TEMPLATE_FOLDER + COMMANDS_TEMPLATE), "UTF-8"); //NOI18N
        commands = commands.replaceAll(MANAGED_BEAN_NAME_VAR, Matcher.quoteReplacement(managedBean));
        commands = commands.replaceAll(ENTITY_NAME_VAR, Matcher.quoteReplacement(simpleEntityName));
        commands = commands.replaceAll(JSF_UTIL_CLASS_VAR, Matcher.quoteReplacement(jsfUtilClass));
        commands = commands.replaceAll("__REMOVE_METHOD__", useSessionBean ? "remove" : "destroy");

        final String allCommands = commands;

        JavaSource javaSource = JavaSource.create(classpathInfo);
        javaSource.runUserActionTask(new Task<CompilationController>() {
            public void run(CompilationController controller) throws IOException {
                controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                TypeElement typeElement = controller.getElements().getTypeElement(entityClass);
                JsfTable.createTable(controller, typeElement, managedBean + "." + fieldName, tableBody, allCommands, embeddedPkSupport, tableVarName);
            }
        }, true);

        content = content.replaceAll(TABLE_BODY_VAR, Matcher.quoteReplacement(tableBody.toString()));
        content = content.replaceAll(LINK_TO_INDEX_VAR, Matcher.quoteReplacement(linkToIndex));
        
        try {
            content = reformat(content, doc);
        } catch (BadLocationException e) {
            Logger.getLogger(JSFClientGenerator.class.getName()).log(Level.INFO, null, e);
        }

        final String listText = content;

        createFile(jsfRoot, listText, LIST_JSP, encoding);
    }
    
    private static void generateNewJsp(Project project, CompilationController controller, String entityClass, String simpleEntityName, String managedBean, String fieldName, 
            List<ElementHandle<ExecutableElement>> toOneRelMethods, boolean fieldAccess, String linkToIndex, BaseDocument doc, final FileObject jsfRoot, EmbeddedPkSupport embeddedPkSupport, String controllerClass, String styleAndScriptTags, String controllerPackage) throws FileStateInvalidException, IOException {
        final Charset encoding = JpaControllerUtil.getProjectEncoding(project, jsfRoot);
        String content = JSFFrameworkProvider.readResource(JSFClientGenerator.class.getClassLoader().getResourceAsStream(TEMPLATE_FOLDER + NEW_JSP_TEMPLATE), "UTF-8"); //NOI18N
        content = content.replaceAll(ENCODING_VAR, Matcher.quoteReplacement(encoding.name()));
        content = content.replaceAll(ENTITY_NAME_VAR, Matcher.quoteReplacement(simpleEntityName));
        content = content.replaceAll(LINK_TO_SS_VAR, Matcher.quoteReplacement(styleAndScriptTags));
        content = content.replaceAll(MANAGED_BEAN_NAME_VAR, Matcher.quoteReplacement(managedBean));

        StringBuffer formBody = new StringBuffer();
        String utilPackage = controllerPackage == null || controllerPackage.length() == 0 ? PersistenceClientIterator.UTIL_FOLDER_NAME : controllerPackage + "." + PersistenceClientIterator.UTIL_FOLDER_NAME;
        String jsfUtilClass = utilPackage + "." + PersistenceClientIterator.UTIL_CLASS_NAMES[1];

        TypeElement typeElement = controller.getElements().getTypeElement(entityClass);
        JsfForm.createForm(controller, typeElement, JsfForm.FORM_TYPE_NEW, managedBean + "." + fieldName, formBody, entityClass, embeddedPkSupport, controllerClass, jsfUtilClass);
        content = content.replaceAll(FORM_BODY_VAR, Matcher.quoteReplacement(formBody.toString()));
        content = content.replaceAll(FIELD_NAME_VAR, Matcher.quoteReplacement(fieldName));
        content = content.replaceAll(LINK_TO_INDEX_VAR, Matcher.quoteReplacement(linkToIndex));

        try {
            content = reformat(content, doc);
        } catch (BadLocationException e) {
            Logger.getLogger(JSFClientGenerator.class.getName()).log(Level.INFO, null, e);
        }
        final String newText = content;

        createFile(jsfRoot, newText, NEW_JSP, encoding);
    }
    
    private static void generateEditJsp(Project project, CompilationController controller, String entityClass, String simpleEntityName, String managedBean, String fieldName, 
            String linkToIndex, BaseDocument doc, final FileObject jsfRoot, EmbeddedPkSupport embeddedPkSupport, String controllerClass, String styleAndScriptTags, String controllerPackage) throws FileStateInvalidException, IOException {
        final Charset encoding = JpaControllerUtil.getProjectEncoding(project, jsfRoot);

        String content  = JSFFrameworkProvider.readResource(JSFClientGenerator.class.getClassLoader().getResourceAsStream(TEMPLATE_FOLDER + EDIT_JSP_TEMPLATE), "UTF-8"); //NOI18N
        content = content.replaceAll(ENCODING_VAR, Matcher.quoteReplacement(encoding.name()));
        content = content.replaceAll(ENTITY_NAME_VAR, Matcher.quoteReplacement(simpleEntityName));
        content = content.replaceAll(LINK_TO_SS_VAR, Matcher.quoteReplacement(styleAndScriptTags));
        content = content.replaceAll(MANAGED_BEAN_NAME_VAR, Matcher.quoteReplacement(managedBean));

        StringBuffer formBody = new StringBuffer();
        
        String utilPackage = controllerPackage == null || controllerPackage.length() == 0 ? PersistenceClientIterator.UTIL_FOLDER_NAME : controllerPackage + "." + PersistenceClientIterator.UTIL_FOLDER_NAME;
        String jsfUtilClass = utilPackage + "." + PersistenceClientIterator.UTIL_CLASS_NAMES[1];
        
        TypeElement typeElement = controller.getElements().getTypeElement(entityClass);        
        JsfForm.createForm(controller, typeElement, JsfForm.FORM_TYPE_EDIT, managedBean + "." + fieldName, formBody, entityClass, embeddedPkSupport, controllerClass, jsfUtilClass);
        content = content.replaceAll(FORM_BODY_VAR, Matcher.quoteReplacement(formBody.toString()));
        content = content.replaceAll(FIELD_NAME_VAR, Matcher.quoteReplacement(fieldName));
        content = content.replaceAll(JSF_UTIL_CLASS_VAR, Matcher.quoteReplacement(jsfUtilClass));
        content = content.replaceAll(LINK_TO_INDEX_VAR, Matcher.quoteReplacement(linkToIndex));

        try {
            content = reformat(content, doc);
        } catch (BadLocationException e) {
            Logger.getLogger(JSFClientGenerator.class.getName()).log(Level.INFO, null, e);
        }

        final String editText = content;

        createFile(jsfRoot, editText, EDIT_JSP, encoding);
    }

    private static void generateDetailJsp(Project project, CompilationController controller, String entityClass, String simpleEntityName, String managedBean, 
            String fieldName, String idProperty, boolean isInjection, String linkToIndex, BaseDocument doc, final FileObject jsfRoot, EmbeddedPkSupport embeddedPkSupport, String controllerClass, String styleAndScriptTags, List<String> entities, String controllerPackage, boolean useSessionBean) throws FileStateInvalidException, IOException {
        final Charset encoding = JpaControllerUtil.getProjectEncoding(project, jsfRoot);

        String content  = JSFFrameworkProvider.readResource(JSFClientGenerator.class.getClassLoader().getResourceAsStream(TEMPLATE_FOLDER + DETAIL_JSP_TEMPLATE), "UTF-8"); //NOI18N
        content = content.replaceAll(ENCODING_VAR, Matcher.quoteReplacement(encoding.name()));
        content = content.replaceAll(ENTITY_NAME_VAR, Matcher.quoteReplacement(simpleEntityName));
        content = content.replaceAll(LINK_TO_SS_VAR, Matcher.quoteReplacement(styleAndScriptTags));
        content = content.replaceAll(MANAGED_BEAN_NAME_VAR, Matcher.quoteReplacement(managedBean));

        String utilPackage = controllerPackage == null || controllerPackage.length() == 0 ? PersistenceClientIterator.UTIL_FOLDER_NAME : controllerPackage + "." + PersistenceClientIterator.UTIL_FOLDER_NAME;
        String jsfUtilClass = utilPackage + "." + PersistenceClientIterator.UTIL_CLASS_NAMES[1];
        
        TypeElement typeElement = controller.getElements().getTypeElement(entityClass);

        StringBuffer formBody = new StringBuffer();
        JsfForm.createForm(controller, typeElement, JsfForm.FORM_TYPE_DETAIL, managedBean + "." + fieldName, formBody, entityClass, embeddedPkSupport, controllerClass, jsfUtilClass);

        content = content.replaceAll(FORM_BODY_VAR, Matcher.quoteReplacement(formBody.toString()));

        StringBuffer tableBody = new StringBuffer();
        JsfForm.createTablesForRelated(controller, typeElement, JsfForm.FORM_TYPE_DETAIL, managedBean + "." + fieldName, idProperty, isInjection, tableBody, embeddedPkSupport, controllerClass, entities, jsfUtilClass);
        content = content.replaceAll(TABLE_BODY_VAR, Matcher.quoteReplacement(tableBody.toString()));
        content = content.replaceAll(FIELD_NAME_VAR,Matcher.quoteReplacement(fieldName));
        content = content.replaceAll("__REMOVE_VALUE__", useSessionBean ? "remove" : "destroy");
        content = content.replaceAll(JSF_UTIL_CLASS_VAR, Matcher.quoteReplacement(jsfUtilClass));
        content = content.replaceAll(LINK_TO_INDEX_VAR, Matcher.quoteReplacement(linkToIndex));
        try {
            content = reformat(content,doc);
        } catch (BadLocationException e) {
            Logger.getLogger(JSFClientGenerator.class.getName()).log(Level.INFO, null, e);
        }

        final String detailText = content;

        createFile(jsfRoot, detailText, DETAIL_JSP, encoding);
    }

    private static void createFile(final FileObject jsfRoot, final String content, final String name, final Charset encoding) throws IOException{
        FileSystem fs = jsfRoot.getFileSystem();

        fs.runAtomicAction(new FileSystem.AtomicAction() {
            public void run() throws IOException {
                FileObject detailForm = FileUtil.createData(jsfRoot, name);//NOI18N
                FileLock lock = detailForm.lock();
                try {
                    BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(detailForm.getOutputStream(lock), encoding));
                    bw.write(content);
                    bw.close();
                }
                finally {
                    lock.releaseLock();
                }
            }
        });
    }

    private static String reformat(String content) {
        try {
            return reformat(content, new BaseDocument(false, JSP_MIME_TYPE));
        } catch (BadLocationException e) {
            Logger.getLogger(JSFClientGenerator.class.getName()).log(Level.INFO, null, e);
            return content;
        }
    }

    private static String reformat(String content, final BaseDocument doc) throws BadLocationException {
            doc.remove(0, doc.getLength());
            doc.insertString(0, content, null);
            final Reformat reformat = Reformat.get(doc);
            reformat.lock();
            try {
                doc.runAtomic(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            reformat.reformat(0, doc.getLength());
                        } catch (BadLocationException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                });
            } finally {
                reformat.unlock();
            }
            return doc.getText(0, doc.getLength());
    }

    private static void addStuffToFacesConfigXml(ClasspathInfo classpathInfo, WebModule wm, String managedBean, String controllerClass, String jpaControllerClass, String entityClass,
            String converterName, String fieldName, String jsfFolder, final ElementHandle<ExecutableElement> idGetterHandle, String pkgName, String utilPackage) {
        FileObject[] configFiles = ConfigurationUtils.getFacesConfigFiles(wm);
        if (configFiles.length > 0) {
            // using first found faces-config.xml, is it OK?
            FileObject fo = configFiles[0];
            JSFConfigModel model = null;
            try {
                model = ConfigurationUtils.getConfigModel(fo, true);
                model.startTransaction();
                FacesConfig config = model.getRootComponent();
                
                boolean resolverFound = false;
                final String elResolverTagName = "el-resolver"; //NOI18N
                String resolverClass = utilPackage + ".JsfCrudELResolver"; //NOI18N
                List<Application> applications = config.getApplications();
                applicationsLoop:
                for (Application existingApplication : applications) {
                    org.w3c.dom.Element existingApplicationPeer = existingApplication.getPeer();
                    org.w3c.dom.NodeList elResolverNodes = existingApplicationPeer.getElementsByTagName(elResolverTagName);
                    for (int i = 0; i < elResolverNodes.getLength(); i++) {
                        org.w3c.dom.Node elResolverNode = elResolverNodes.item(i);
                        org.w3c.dom.NodeList elResolverNodeChildren = elResolverNode.getChildNodes();
                        for (int j = 0; j < elResolverNodeChildren.getLength(); j++) {
                            org.w3c.dom.Node elResolverNodeChild = elResolverNodeChildren.item(j);
                            if (resolverClass.equals(elResolverNodeChild.getNodeValue())) {
                                resolverFound = true;
                                break applicationsLoop;
                            }
                        }
                    }
                }

                if (!resolverFound) {
                    org.w3c.dom.Element configPeer = config.getPeer();
                    org.w3c.dom.Document doc = configPeer.getOwnerDocument();
                    org.w3c.dom.Element elRes = doc.createElement(elResolverTagName);
                    org.w3c.dom.Text text = doc.createTextNode(resolverClass);
                    elRes.appendChild(text);
                    Application appl = model.getFactory().createApplication();
                    org.w3c.dom.Element applPeer = appl.getPeer();
                    applPeer.appendChild(elRes);
                    config.addApplication(appl);
                }

                if (wm.getDocumentBase().getFileObject(WELCOME_JSF_FL_PAGE) != null) {
                    addNavigationRuleToFacesConfig(model, config, "welcome", "/"+WELCOME_JSF_FL_PAGE);   //NOI18N
                } else if(wm.getDocumentBase().getFileObject(WELCOME_JSF_JSP_PAGE) != null) {
                    addNavigationRuleToFacesConfig(model, config, "welcome", "/"+WELCOME_JSF_JSP_PAGE);   //NOI18N
                }
                
                addManagedBeanToFacesConfig(model, config, managedBean, controllerClass);
                addManagedBeanToFacesConfig(model, config, managedBean + "Jpa", jpaControllerClass);   //NOI18N
                
                Converter cv = null;
                List<Converter> converters = config.getConverters();
                for (Converter existingConverter : converters) {
                    if (entityClass.equals(existingConverter.getConverterForClass())) {
                        cv = existingConverter;
                        break;
                    }
                }
                boolean cvIsNew = false;
                if (cv == null) {
                    cv = model.getFactory().createConverter();
                    cvIsNew = true;
                }
                cv.setConverterForClass(entityClass);
                cv.setConverterClass(converterName);
                if (cvIsNew) {
                    config.addConverter(cv);
                }
                
                String[] fromOutcomes = {
                    fieldName + "_create",    //NOI18N
                    fieldName + "_list",    //NOI18N
                    fieldName + "_edit",   //NOI18N
                    fieldName + "_detail"   //NOI18N
                };
                String separator = "/";    //NOI18N
                String[] toViewIds = {
                    separator + jsfFolder + separator + NEW_JSP,
                    separator + jsfFolder + separator + LIST_JSP,
                    separator + jsfFolder + separator + EDIT_JSP,
                    separator + jsfFolder + separator + DETAIL_JSP,
                };
                
                for (int i = 0; i < fromOutcomes.length; i++) {
                    addNavigationRuleToFacesConfig(model, config, fromOutcomes[i], toViewIds[i]);
                }
            }
            finally {
                //TODO: RETOUCHE correct write to JSF model?
                model.endTransaction();
                DataObject facesDO;
                try {
                    facesDO = DataObject.find(fo);
                    if (facesDO !=null) {
                        SaveCookie save = facesDO.getCookie(SaveCookie.class);
                        if (save != null) {
                            save.save();
                        }
                    }
                } catch (DataObjectNotFoundException ex) {
                    Exceptions.printStackTrace(ex);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                } catch (IllegalStateException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
    }
    
    private static void addManagedBeanToFacesConfig(JSFConfigModel model, FacesConfig config, String managedBean, String managedBeanClass) {
        ManagedBean mb = null;
        List<ManagedBean> managedBeans = config.getManagedBeans();
        for (ManagedBean existingManagedBean : managedBeans) {
            if (managedBean.equals(existingManagedBean.getManagedBeanName())) {
                mb = existingManagedBean;
                break;
            }
        }
        boolean mbIsNew = false;
        if (mb == null) {
            mb = model.getFactory().createManagedBean();
            mbIsNew = true;
        }
        mb.setManagedBeanName(managedBean);
        mb.setManagedBeanClass(managedBeanClass);
        mb.setManagedBeanScope(ManagedBean.Scope.SESSION);
        if (mbIsNew) {
            config.addManagedBean(mb);
        }
    }
    
    private static void addNavigationRuleToFacesConfig(JSFConfigModel model, FacesConfig config, String fromOutcome, String toViewId) {
        NavigationRule nr = null;
        NavigationCase nc = null;
        List<NavigationRule> navigationRules = config.getNavigationRules();
        for (NavigationRule existingNavigationRule : navigationRules) {
            List<NavigationCase> navigationCases = existingNavigationRule.getNavigationCases();
            for (NavigationCase existingNavigationCase : navigationCases) {
                if ( fromOutcome.equals(existingNavigationCase.getFromOutcome()) ) {
                    nr = existingNavigationRule;
                    nc = existingNavigationCase;
                    break;
                }
            }
        }
        boolean nrIsNew = false;
        if (nr == null || nc == null) {
            nr = model.getFactory().createNavigationRule();
            nc = model.getFactory().createNavigationCase();
            nrIsNew = true;
        }

        nc.setFromOutcome(fromOutcome);
        nc.setToViewId(toViewId);
        if (nrIsNew) {
            nr.addNavigationCase(nc);
            nr.setFromViewId("*"); // NOI18N
            config.addNavigationRule(nr);
        }
    }
    
    private static FileObject generateConverter(
            final FileObject converterFileObject,
            final FileObject controllerFileObject,
            final FileObject pkg,
            final String controllerClass,
            final String simpleControllerName,
            final String entityClass,
            final String simpleEntityName,
            final ElementHandle<ExecutableElement> idGetter,
            final String managedBeanName,
            final String jpaControllerClass,
            final boolean useSessionBean,
            final boolean isInjection,
            final boolean jakartaJsfPackages) throws IOException {

        final boolean[] embeddable = new boolean[] { false };
        final String[] idClassSimpleName = new String[1];
        final String[] idPropertyType = new String[1];
        final ArrayList<MethodModel> paramSetters = new ArrayList<MethodModel>();
        final boolean[] isPrimitiveIdPropertyType = new boolean[]{false};

        final String converterFQN;
        final String uiComponentFQN;
        final String facesContextFQN;

        if (jakartaJsfPackages) {
            converterFQN = "jakarta.faces.convert.Converter";
            uiComponentFQN = "jakarta.faces.component.UIComponent";
            facesContextFQN = "jakarta.faces.context.FacesContext";
        } else {
            converterFQN = "javax.faces.convert.Converter";
            uiComponentFQN = "javax.faces.component.UIComponent";
            facesContextFQN = "javax.faces.context.FacesContext";
        }

        final String[] idGetterName = new String[1];
        JavaSource controllerJavaSource = JavaSource.forFileObject(controllerFileObject);
        controllerJavaSource.runUserActionTask(new Task<CompilationController>() {
            public void run(CompilationController compilationController) throws IOException {
                compilationController.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                ExecutableElement idGetterElement = idGetter.resolve(compilationController);
                idGetterName[0] = idGetterElement.getSimpleName().toString();
                TypeMirror idType = idGetterElement.getReturnType();
                if (TypeKind.DECLARED == idType.getKind()) {
                    DeclaredType declaredType = (DeclaredType) idType;
                    TypeElement idClass = (TypeElement) declaredType.asElement();
                    embeddable[0] = idClass != null && JpaControllerUtil.isEmbeddableClass(idClass);
                    idClassSimpleName[0] = idClass.getSimpleName().toString();
                    idPropertyType[0] = idClass.getQualifiedName().toString();
                    if (embeddable[0]) {
                        for (ExecutableElement method : ElementFilter.methodsIn(idClass.getEnclosedElements())) {
                            if (method.getSimpleName().toString().startsWith("set")) {
                                paramSetters.add(MethodModelSupport.createMethodModel(compilationController, method));
                            }
                        }
                    }
                } else if (TypeKind.BOOLEAN == idType.getKind()) {
                    idClassSimpleName[0] = idPropertyType[0] = "boolean";
                    isPrimitiveIdPropertyType[0] = true;
                } else if (TypeKind.BYTE == idType.getKind()) {
                    idClassSimpleName[0] = idPropertyType[0] = "byte";
                    isPrimitiveIdPropertyType[0] = true;
                } else if (TypeKind.CHAR == idType.getKind()) {
                    idClassSimpleName[0] = idPropertyType[0] = "char";
                    isPrimitiveIdPropertyType[0] = true;
                } else if (TypeKind.DOUBLE == idType.getKind()) {
                    idClassSimpleName[0] = idPropertyType[0] = "double";
                    isPrimitiveIdPropertyType[0] = true;
                } else if (TypeKind.FLOAT == idType.getKind()) {
                    idClassSimpleName[0] = idPropertyType[0] = "float";
                    isPrimitiveIdPropertyType[0] = true;
                } else if (TypeKind.INT == idType.getKind()) {
                    idClassSimpleName[0] = idPropertyType[0] = "int";
                    isPrimitiveIdPropertyType[0] = true;
                } else if (TypeKind.LONG == idType.getKind()) {
                    idClassSimpleName[0] = idPropertyType[0] = "long";
                    isPrimitiveIdPropertyType[0] = true;
                } else if (TypeKind.SHORT == idType.getKind()) {
                    idClassSimpleName[0] = idPropertyType[0] = "short";
                    isPrimitiveIdPropertyType[0] = true;
                }
            }
        }, true);
        
        String controllerReferenceName = controllerClass;
        StringBuffer getAsObjectBody = new StringBuffer();
        getAsObjectBody.append("if (string == null || string.length() == 0) {\n return null;\n }\n");

        String controllerVariable;
        if (isInjection) {
            controllerVariable = controllerClass + " controller = ("
                    + controllerClass
                    + ") facesContext.getApplication().getELResolver().getValue(\nfacesContext.getELContext(), null, \"" 
                    + managedBeanName + "\");\n";
        } else {
            controllerVariable = controllerClass + " controller = ("
                    + controllerClass
                    + ") facesContext.getApplication().getVariableResolver().resolveVariable(\nfacesContext, \"" 
                    + managedBeanName + "\");\n";
        }
        if (embeddable[0]) {
            getAsObjectBody.append(idPropertyType[0] + " id = getId(string);\n");
            getAsObjectBody.append(controllerVariable + "\n return controller.getJpaController().find" + (useSessionBean ? "" : simpleEntityName) + "(id);");
        } else {
            getAsObjectBody.append(createIdFieldDeclaration(idPropertyType[0], "string", jakartaJsfPackages) + "\n"
                    + controllerVariable
                    + "\n return controller.getJpaController().find" + (useSessionBean ? "" : simpleEntityName) + "(id);");
        }
        
        final MethodModel getAsObject = MethodModel.create("getAsObject",
                "java.lang.Object",
                getAsObjectBody.toString(),
                Arrays.asList(MethodModel.Variable.create(facesContextFQN, "facesContext"),
                    MethodModel.Variable.create(uiComponentFQN, "component"),
                    MethodModel.Variable.create("java.lang.String", "string")
                ),
                Collections.<String>emptyList(),
                Collections.singleton(Modifier.PUBLIC)
                );
        
        StringBuffer getIdBody = null;
        if (embeddable[0]) {
            getIdBody = new StringBuffer();
            int params = paramSetters.size();
            if(params > 0){
                getIdBody.append(idPropertyType[0] + " id = new " + idPropertyType[0] + "();\n");
            }
            else {
                getIdBody.append(idPropertyType[0] + " id;\n");//do not initialize, user need to update code and may be use not default constructor
            }

            getIdBody.append("String params[] = new String[" + params + "];\n" +
                    "int p = 0;\n" +
                    "int grabStart = 0;\n" +
                    "String delim = \"#\";\n" +
                    "String escape = \"~\";\n" +
                    "Pattern pattern = Pattern.compile(escape + \"*\" + delim);\n" +
                    "Matcher matcher = pattern.matcher(string);\n" +
                    "while (matcher.find()) {\n" +
                    "String found = matcher.group();\n" +
                    "if (found.length() % 2 == 1) {\n" +
                    "params[p] = string.substring(grabStart, matcher.start());\n" +
                    "p++;\n" +
                    "grabStart = matcher.end();\n" +
                    "}\n" +
                    "}\n" +
                    "if (p != params.length - 1) {\n" +
                    "throw new IllegalArgumentException(\"string \" + string + \" is not in expected format. expected " + params + " ids delimited by \" + delim);\n" +
                    "}\n" +
                    "params[p] = string.substring(grabStart);\n" +
                    "for (int i = 0; i < params.length; i++) {\n" +
                    "params[i] = params[i].replace(escape + delim, delim);\n" +
                    "params[i] = params[i].replace(escape + escape, escape);\n" +
                    "}\n\n"
                    );
            for (int i = 0; i < paramSetters.size(); i++) {
                MethodModel setter = paramSetters.get(i);
                String type = setter.getParameters().get(0).getType();
                getIdBody.append("id." + setter.getName() + "("
                        + createIdFieldInitialization(type, "params[" + i + "]", jakartaJsfPackages) + ");\n");
            }
            
            if( params==0) {
                     getIdBody.append(NbBundle.getMessage(JSFClientGenerator.class, "ERR_NO_SETTERS_CONVERTER", new String[]{INDENT, idPropertyType[0], "getId()"})+"\n");//NOI18N;
            }
            getIdBody.append("return id;\n");
        }
        
        final MethodModel getId = embeddable[0] ? MethodModel.create(
                "getId",
                idPropertyType[0],
                getIdBody.toString(),
                Arrays.asList(
                    MethodModel.Variable.create("java.lang.String", "string")
                ),
                Collections.<String>emptyList(),
                Collections.<Modifier>emptySet()    //no modifiers
                ) : null;

        String entityReferenceName = entityClass;
        StringBuffer getAsStringBody = new StringBuffer();
        getAsStringBody.append("if (object == null) {\n return null;\n }\n"
                + "if(object instanceof " + entityReferenceName + ") {\n"
                + entityReferenceName + " o = (" + entityReferenceName +") object;\n");
        if (embeddable[0]) {
            getAsStringBody.append(idPropertyType[0] + " id  = o." + idGetterName[0] + "();\n" +
                    "if (id == null) {\n" +
                    "return \"\";\n" +
                    "}\n" +
                    "String delim = \"#\";\n" +
                    "String escape = \"~\";\n\n"               
                    );
            for(int i = 0; i < paramSetters.size(); i++) {
                MethodModel setter = paramSetters.get(i);
                String propName = JpaControllerUtil.getPropNameFromMethod(setter.getName());
                String type = setter.getParameters().get(0).getType();
                boolean isString = "String".equals(type) || "java.lang.String".equals(type);
                boolean isPrimitive = "boolean".equals(type) || "char".equals(type) ||
                        "double".equals(type) || "float".equals(type) || "int".equals(type) || "long".equals(type);
                if (isString) {
                    getAsStringBody.append("String " + propName + " = id.g" + setter.getName().substring(1) + "();\n");
                }
                else if (isPrimitive) {
                    getAsStringBody.append("String " + propName + " = String.valueOf(id.g" + setter.getName().substring(1) + "());\n");
                }
                else {
                    getAsStringBody.append("Object " + propName + "Obj = id.g" + setter.getName().substring(1) + "();\n" +
                            "String " + propName + " = " + propName + "Obj == null ? \"\" : String.valueOf(" + propName + "Obj);\n");
                }
                getAsStringBody.append(propName + " = ");
                if (isString) {
                    getAsStringBody.append(propName + " == null ? \"\" : ");
                }
                getAsStringBody.append(propName + ".replace(escape, escape + escape);\n" +
                        propName + " = " + propName + ".replace(delim, escape + delim);\n");
            }
            getAsStringBody.append("return ");
            for(int i = 0; i < paramSetters.size(); i++) {
                MethodModel setter = paramSetters.get(i);
                String propName = JpaControllerUtil.getPropNameFromMethod(setter.getName());
                if (i > 0) {
                    getAsStringBody.append(" + delim + ");
                }
                getAsStringBody.append(propName);
            }
            getAsStringBody.append(";\n");
            getAsStringBody.append(NbBundle.getMessage(JSFClientGenerator.class, "ERR_NO_SETTERS_CONVERTER", new String[]{INDENT, idPropertyType[0], "getAsString()"})+"\n");//NOI18N;
        } else {
            String oDotGetId = "o." + idGetterName[0] + "()";
            if (isPrimitiveIdPropertyType[0]) {
                getAsStringBody.append("return String.valueOf(" + oDotGetId + ");\n");
            } else {
                getAsStringBody.append("return " + oDotGetId + " == null ? \"\" : " + oDotGetId + ".toString();\n");
            }
        }
        getAsStringBody.append("} else {\n"
                + "throw new IllegalArgumentException(\"object \" + object + \" is of type \" + object.getClass().getName() + \"; expected type: " + entityClass +"\");\n}");
        

        final MethodModel getAsString = MethodModel.create("getAsString",
                "java.lang.String",
                getAsStringBody.toString(),
                Arrays.asList(MethodModel.Variable.create(facesContextFQN, "facesContext"),
                    MethodModel.Variable.create(uiComponentFQN, "component"),
                    MethodModel.Variable.create("java.lang.Object", "object")
                ),
                Collections.<String>emptyList(),
                Collections.singleton(Modifier.PUBLIC)
                );

//        FileObject converterFileObject = GenerationUtils.createClass(pkg, simpleConverterName, null);
        JavaSource converterJavaSource = JavaSource.forFileObject(converterFileObject);
        converterJavaSource.runModificationTask(new Task<WorkingCopy>() {
            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(JavaSource.Phase.RESOLVED);
                GenerationUtils generationUtils = GenerationUtils.newInstance(workingCopy);
                TypeElement converterTypeElement = SourceUtils.getPublicTopLevelElement(workingCopy);
                ClassTree classTree = workingCopy.getTrees().getTree(converterTypeElement);
                ClassTree modifiedClassTree = generationUtils.addImplementsClause(classTree, converterFQN);
                MethodTree getAsObjectTree = MethodModelSupport.createMethodTree(workingCopy, getAsObject);
                MethodTree getIdTree = embeddable[0] ? MethodModelSupport.createMethodTree(workingCopy, getId) : null;
                MethodTree getAsStringTree = MethodModelSupport.createMethodTree(workingCopy, getAsString);
                modifiedClassTree = workingCopy.getTreeMaker().addClassMember(modifiedClassTree, getAsObjectTree);
                if (embeddable[0]) {
                    modifiedClassTree = workingCopy.getTreeMaker().addClassMember(modifiedClassTree, getIdTree);
                }
                modifiedClassTree = workingCopy.getTreeMaker().addClassMember(modifiedClassTree, getAsStringTree);
                if (embeddable[0]) {
                    String[] importFqs = {"java.util.regex.Pattern",
                                "java.util.regex.Matcher",
                                jpaControllerClass
                    };
                    CompilationUnitTree modifiedImportCut = null;
                    for (String importFq : importFqs) {
                        modifiedImportCut = JpaControllerUtil.TreeMakerUtils.createImport(workingCopy, modifiedImportCut, importFq);
                    }
                }
                workingCopy.rewrite(classTree, modifiedClassTree);
            }
        }).commit();

        return converterFileObject;
    }
    
    private static FileObject generateControllerClass(
            final String fieldName, 
            final FileObject pkg, 
            final ElementHandle<ExecutableElement> idGetter, 
            final String persistenceUnit, 
            final String controllerPackage,
            final String controllerClass,
            final String simpleConverterName,
            final String entityClass, 
            final String simpleEntityName,
            final List<ElementHandle<ExecutableElement>> toOneRelMethods,
            final List<ElementHandle<ExecutableElement>> toManyRelMethods,
            final boolean isInjection,
            final boolean isFieldAccess,
            final FileObject controllerFileObject, 
            final EmbeddedPkSupport embeddedPkSupport,
            final String jpaControllerPackage,
            final String jpaControllerClass,
            final boolean useSessionBean,
            final String utilPackage,
            final boolean jakartaJsfPackages
    ) throws IOException {
        
            final String[] idPropertyType = new String[1];
            final String[] idGetterName = new String[1];
            final boolean[] embeddable = new boolean[] { false };

            final String persistenceFQN;
            final String persistenceUnitFQN;
            final String annotationResourceFQN;
            final String selectItemArrayFQN;
            final String facesExceptionFQN;
            final String entityManagerFactoryFQN;
            final String userTransationFQN;
            final String converterFQN;
            final String uiComponentFQN;
            final String facesContextFQN;
            final String rollbackExceptionFQN;

            if (jakartaJsfPackages) {
                persistenceFQN = "jakarta.persistence.Persistence";
                persistenceUnitFQN = "jakarta.persistence.PersistenceUnit";
                annotationResourceFQN = "jakarta.annotation.Resource";
                selectItemArrayFQN = "jakarta.faces.model.SelectItem[]";
                facesExceptionFQN = "jakarta.faces.FacesException";
                entityManagerFactoryFQN = "jakarta.persistence.EntityManagerFactory";
                userTransationFQN = "jakarta.transaction.UserTransaction";
                converterFQN = "jakarta.faces.convert.Converter";
                uiComponentFQN = "jakarta.faces.component.UIComponent";
                facesContextFQN = "jakarta.faces.context.FacesContext";
                rollbackExceptionFQN = "jakarta.transaction.RollbackException";
            } else {
                persistenceFQN = "javax.persistence.Persistence";
                persistenceUnitFQN = "javax.persistence.PersistenceUnit";
                annotationResourceFQN = "javax.annotation.Resource";
                selectItemArrayFQN = "javax.faces.model.SelectItem[]";
                facesExceptionFQN = "javax.faces.FacesException";
                entityManagerFactoryFQN = "javax.persistence.EntityManagerFactory";
                userTransationFQN = "javax.transaction.UserTransaction";
                converterFQN = "javax.faces.convert.Converter";
                uiComponentFQN = "javax.faces.component.UIComponent";
                facesContextFQN = "javax.faces.context.FacesContext";
                rollbackExceptionFQN = "javax.transaction.RollbackException";
            }

            JavaSource controllerJavaSource = JavaSource.forFileObject(controllerFileObject);
            controllerJavaSource.runModificationTask(new Task<WorkingCopy>() {
                public void run(WorkingCopy workingCopy) throws IOException {
                    workingCopy.toPhase(JavaSource.Phase.RESOLVED);
                    
                    ExecutableElement idGetterElement = idGetter.resolve(workingCopy);
                    idGetterName[0] = idGetterElement.getSimpleName().toString();
                    TypeMirror idType = idGetterElement.getReturnType();
                    TypeElement idClass = null;
                    if (TypeKind.DECLARED == idType.getKind()) {
                        DeclaredType declaredType = (DeclaredType) idType;
                        idClass = (TypeElement) declaredType.asElement();
                        embeddable[0] = idClass != null && JpaControllerUtil.isEmbeddableClass(idClass);
                        idPropertyType[0] = idClass.getQualifiedName().toString();
                    } else if (TypeKind.BOOLEAN == idType.getKind()) {
                        idPropertyType[0] = "boolean";
                    } else if (TypeKind.BYTE == idType.getKind()) {
                        idPropertyType[0] = "byte";
                    } else if (TypeKind.CHAR == idType.getKind()) {
                        idPropertyType[0] = "char";
                    } else if (TypeKind.DOUBLE == idType.getKind()) {
                        idPropertyType[0] = "double";
                    } else if (TypeKind.FLOAT == idType.getKind()) {
                        idPropertyType[0] = "float";
                    } else if (TypeKind.INT == idType.getKind()) {
                        idPropertyType[0] = "int";
                    } else if (TypeKind.LONG == idType.getKind()) {
                        idPropertyType[0] = "long";
                    } else if (TypeKind.SHORT == idType.getKind()) {
                        idPropertyType[0] = "short";
                    } else {
                        //instead of throwing exceptions later, just use Object
                        idPropertyType[0] = "java.lang.Object";//NOI18N
                    }
                    
                    String simpleIdPropertyType = JpaControllerUtil.simpleClassName(idPropertyType[0]);
                    
                    TypeElement controllerTypeElement = SourceUtils.getPublicTopLevelElement(workingCopy);
                    ClassTree classTree = workingCopy.getTrees().getTree(controllerTypeElement);
                    ClassTree modifiedClassTree = classTree;
                    
                    int privateModifier = java.lang.reflect.Modifier.PRIVATE;
                    int publicModifier = java.lang.reflect.Modifier.PUBLIC;
                    
                    modifiedClassTree = JpaControllerUtil.TreeMakerUtils.addVariable(modifiedClassTree, workingCopy, fieldName, entityClass, privateModifier, null, null);
                   
                    modifiedClassTree = JpaControllerUtil.TreeMakerUtils.addVariable(modifiedClassTree, workingCopy, fieldName + "Items", new TypeInfo("java.util.List", new String[]{entityClass}), privateModifier, null, null);
                    
                    modifiedClassTree = JpaControllerUtil.TreeMakerUtils.addVariable(modifiedClassTree, workingCopy, "jpaController", jpaControllerClass, privateModifier, null, null);
                    
                    String converterClass = ((controllerPackage == null || controllerPackage.length() == 0) ? "" : controllerPackage + ".") + simpleConverterName;
                    modifiedClassTree = JpaControllerUtil.TreeMakerUtils.addVariable(modifiedClassTree, workingCopy, "converter", converterClass, privateModifier, null, null);
                    
                    modifiedClassTree = JpaControllerUtil.TreeMakerUtils.addVariable(modifiedClassTree, workingCopy, "pagingInfo", utilPackage + ".PagingInfo", privateModifier, null, null);

                    CompilationUnitTree modifiedImportCut = null;
                    AnnotationInfo[] annotations = null;
                    if (isInjection) {
                        annotations = new AnnotationInfo[1];
                        annotations[0] = new AnnotationInfo(annotationResourceFQN);
                        modifiedClassTree = JpaControllerUtil.TreeMakerUtils.addVariable(modifiedClassTree, workingCopy, "utx", userTransationFQN, privateModifier, null, annotations);

                        if (persistenceUnit == null) {
                            annotations[0] = new AnnotationInfo(persistenceUnitFQN);
                        } else {
                            annotations[0] = new AnnotationInfo(persistenceUnitFQN, new String[]{"unitName"}, new Object[]{persistenceUnit});
                        }
                        modifiedClassTree = JpaControllerUtil.TreeMakerUtils.addVariable(modifiedClassTree, workingCopy, "emf", entityManagerFactoryFQN, privateModifier, null, annotations);

                    }

                    String bodyText;
                    MethodInfo methodInfo;
                    
                    String managedBeanName = getManagedBeanName(simpleEntityName);
                    bodyText = "pagingInfo = new PagingInfo();\n" +
                            "converter = new " + simpleConverterName + "();";
                    methodInfo = new MethodInfo("<init>", publicModifier, "void", null, null, null, bodyText, null, null);
                    modifiedClassTree = JpaControllerUtil.TreeMakerUtils.modifyDefaultConstructor(classTree, modifiedClassTree, workingCopy, methodInfo);
                    
                    bodyText = "if pagingInfo.getItemCount() == -1) {\n" +
                            "pagingInfo.setItemCount(getJpaController()."+ (useSessionBean ? "count()" :  "get" + simpleEntityName + "Count()")+");\n" +
                            "}\n" +
                            "return pagingInfo;";
                    methodInfo = new MethodInfo("getPagingInfo", publicModifier, utilPackage + ".PagingInfo", null, null, null, bodyText, null, null);
                    modifiedClassTree = JpaControllerUtil.TreeMakerUtils.addMethod(modifiedClassTree, workingCopy, methodInfo);

                    String jpaControllerInit = "";
                    if (useSessionBean) {
                        jpaControllerInit = "FacesContext facesContext = FacesContext.getCurrentInstance();\n" + "jpaController = ("+simpleEntityName+ FACADE_SUFFIX+")facesContext.getApplication().getELResolver().getValue(facesContext.getELContext(), null, \"" + managedBeanName + "Jpa\");\n";
                    } else {

                        String parameters = isInjection ? "utx, emf": persistenceFQN + ".createEntityManagerFactory(\"" + persistenceUnit + "\")";
                        jpaControllerInit = "jpaController = new "+simpleEntityName+"JpaController("+parameters+");\n";
                    }
                    bodyText = "if (jpaController == null) {\n"+
                                jpaControllerInit +"}\n return jpaController;";
                    methodInfo = new MethodInfo("getJpaController", publicModifier, jpaControllerClass, null, null, null, bodyText, null, null);
                    modifiedClassTree = JpaControllerUtil.TreeMakerUtils.addMethod(modifiedClassTree, workingCopy, methodInfo);

                    List<ElementHandle<ExecutableElement>> allRelMethods = new ArrayList<ElementHandle<ExecutableElement>>(toOneRelMethods);
                    allRelMethods.addAll(toManyRelMethods);
                    
                    String jpaExceptionsPackage = jpaControllerPackage == null || jpaControllerPackage.length() == 0 ? JpaControllerIterator.EXCEPTION_FOLDER_NAME : jpaControllerPackage + "." + JpaControllerIterator.EXCEPTION_FOLDER_NAME;
                    
                    String illegalOrphanExceptionClass = jpaExceptionsPackage + ".IllegalOrphanException";

                    String[] importFqs = null;
                    boolean methodThrowsIllegalOrphanExceptionInCreate = false;
                    boolean methodThrowsIllegalOrphanExceptionInEdit = false;
                    boolean methodThrowsIllegalOrphanExceptionInDestroy = false;
                    if(!useSessionBean)
                    {
                        methodThrowsIllegalOrphanExceptionInCreate = JpaControllerUtil.exceptionsThrownIncludes(workingCopy, jpaControllerClass, "create", Collections.<String>singletonList("java.lang.Object"), illegalOrphanExceptionClass);
                        methodThrowsIllegalOrphanExceptionInEdit = JpaControllerUtil.exceptionsThrownIncludes(workingCopy, jpaControllerClass, "edit", Collections.<String>singletonList("java.lang.Object"), illegalOrphanExceptionClass);
                        methodThrowsIllegalOrphanExceptionInDestroy = JpaControllerUtil.exceptionsThrownIncludes(workingCopy, jpaControllerClass, useSessionBean ? "remove" : "destroy", Collections.<String>singletonList("java.lang.Object"), illegalOrphanExceptionClass);

                        importFqs = (methodThrowsIllegalOrphanExceptionInCreate || methodThrowsIllegalOrphanExceptionInEdit || methodThrowsIllegalOrphanExceptionInDestroy) ? new String[]{
                                    "java.lang.reflect.InvocationTargetException",
                                    "java.lang.reflect.Method",
                                    facesExceptionFQN,
                                    utilPackage + ".JsfUtil",
                                    jpaExceptionsPackage + ".NonexistentEntityException",
                                    illegalOrphanExceptionClass
                        } : new String[]{
                                    "java.lang.reflect.InvocationTargetException",
                                    "java.lang.reflect.Method",
                                    facesExceptionFQN,
                                    utilPackage + ".JsfUtil",
                                    jpaExceptionsPackage + ".NonexistentEntityException"
                        };
                    }
                    else
                    {
                        importFqs = new String[]{
                                    "java.lang.reflect.InvocationTargetException",
                                    "java.lang.reflect.Method",
                                    facesExceptionFQN,
                                    annotationResourceFQN,
                                    userTransationFQN,
                                    utilPackage + ".JsfUtil"
                        };

                    }
                    modifiedImportCut = null;
                    for (String importFq : importFqs) {
                        modifiedImportCut = JpaControllerUtil.TreeMakerUtils.createImport(workingCopy, modifiedImportCut, importFq);
                    }
                    
                    if (embeddable[0] && !controllerClass.startsWith(entityClass + "Controller")) {
                        modifiedImportCut = JpaControllerUtil.TreeMakerUtils.createImport(workingCopy, modifiedImportCut, idPropertyType[0]);
                    }

                    
                    bodyText = "return JsfUtil.getSelectItems(getJpaController().find" + (useSessionBean ? "All()" : simpleEntityName + "Entities()")+", false);";
                    methodInfo = new MethodInfo("get" + simpleEntityName + "ItemsAvailableSelectMany", publicModifier, selectItemArrayFQN, null, null, null, bodyText, null, null);
                    modifiedClassTree = JpaControllerUtil.TreeMakerUtils.addMethod(modifiedClassTree, workingCopy, methodInfo);
                    
                    bodyText = "return JsfUtil.getSelectItems(getJpaController().find" + (useSessionBean ? "All()" : simpleEntityName + "Entities()")+", true);";
                    methodInfo = new MethodInfo("get" + simpleEntityName + "ItemsAvailableSelectOne", publicModifier, selectItemArrayFQN, null, null, null, bodyText, null, null);
                    modifiedClassTree = JpaControllerUtil.TreeMakerUtils.addMethod(modifiedClassTree, workingCopy, methodInfo);
                                        
                    bodyText = "if (" + fieldName + " == null) {\n" +
                            fieldName + " = (" + simpleEntityName + ")JsfUtil.getObjectFromRequestParameter(\"jsfcrud.current" + simpleEntityName + "\", converter, null);\n" +
                            "}\n" + 
                            "if (" + fieldName + " == null) {\n" +
                            fieldName + " = new " + simpleEntityName + "();\n" +
                            "}\n" + 
                            "return " + fieldName + ";";
                    methodInfo = new MethodInfo("get" + simpleEntityName, publicModifier, entityClass, null, null, null, bodyText, null, null);
                    modifiedClassTree = JpaControllerUtil.TreeMakerUtils.addMethod(modifiedClassTree, workingCopy, methodInfo);

                    bodyText = "reset(true);\n" + 
                            "return \"" + fieldName + "_list\";";
                    methodInfo = new MethodInfo("listSetup", publicModifier, "java.lang.String", null, null, null, bodyText, null, null);
                    modifiedClassTree = JpaControllerUtil.TreeMakerUtils.addMethod(modifiedClassTree, workingCopy, methodInfo);
                    
                    bodyText = "reset(false);\n" +
                            fieldName + " = new " + simpleEntityName + "();\n" + 
                            (embeddable[0] ? fieldName + ".s" + idGetterName[0].substring(1) + "(new " + idClass.getSimpleName() + "());\n" : "") +
                            "return \"" + fieldName + "_create\";";
                    methodInfo = new MethodInfo("createSetup", publicModifier, "java.lang.String", null, null, null, bodyText, null, null);
                    modifiedClassTree = JpaControllerUtil.TreeMakerUtils.addMethod(modifiedClassTree, workingCopy, methodInfo);
                                        
                    String newEntityStringVar = "new" + simpleEntityName + "String";
                    String entityStringVar = fieldName + "String";
                    
                    
                    TypeElement entityType = workingCopy.getElements().getTypeElement(entityClass);
                    StringBuffer codeToPopulatePkFields = new StringBuffer();
                    if (embeddable[0]) {
                        Set<ExecutableElement> methods = embeddedPkSupport.getPkAccessorMethods(entityType);
                        boolean missedSetters = true;
                        for (ExecutableElement pkMethod : methods) {
                            if (embeddedPkSupport.isRedundantWithRelationshipField(entityType, pkMethod)) {
                                codeToPopulatePkFields.append(fieldName + "." +idGetterName[0] + "().s" + pkMethod.getSimpleName().toString().substring(1) + "(" +  //NOI18N
                                    fieldName + "." + embeddedPkSupport.getCodeToPopulatePkField(entityType, pkMethod) + ");\n");
                                if(!embeddedPkSupport.getPkSetterMethodExist(entityType, pkMethod)) {
                                    missedSetters = false;
                                }
                            }
                        }
                        if(missedSetters){
                            codeToPopulatePkFields.append(NbBundle.getMessage(JSFClientGenerator.class, "ERR_NO_SETTERS_CONTROLLER", new String[]{INDENT, idPropertyType[0]})+"\n");//NOI18N;
                        }
                    }



                    bodyText =
                            codeToPopulatePkFields.toString() +
                            (useSessionBean ? "try{utx.begin();} catch( Exception ex ){}\n" : "")+
                            "try {\n" +
                            (useSessionBean ? "Exception transactionException = null;\n" : "")+
                            "getJpaController().create(" + fieldName + ");\n" +
                            (useSessionBean ? "try{utx.commit();} catch(" + rollbackExceptionFQN + " ex){transactionException = ex;} catch( Exception ex ){}\n" : "")+
                            (useSessionBean ? "if(transactionException==null)" : "") +"JsfUtil.addSuccessMessage(\"" + simpleEntityName + " was successfully created.\");\n"  + //NOI18N
                            (useSessionBean ? "else JsfUtil.ensureAddErrorMessage(transactionException, \"A persistence error occurred.\");\n" : "")+
                            (methodThrowsIllegalOrphanExceptionInCreate ? "} catch (IllegalOrphanException oe) {\n" + 
                            "JsfUtil.addErrorMessages(oe.getMessages());\n" +
                            "return null;\n" : "") +
                            "} catch (Exception e) {\n" +
                            (useSessionBean ? "try{utx.rollback();} catch( Exception ex ){}\n" : "")+
                            "JsfUtil.ensureAddErrorMessage(e, \"A persistence error occurred.\");\n" +
                            "return null;\n" +
                            "}\n" +
                            "return listSetup();";
                            
                    methodInfo = new MethodInfo("create", publicModifier, "java.lang.String", null, null, null, bodyText, null, null);
                    modifiedClassTree = JpaControllerUtil.TreeMakerUtils.addMethod(modifiedClassTree, workingCopy, methodInfo);
                    
                    bodyText = "return scalarSetup(\"" + fieldName + "_detail\");";
                    methodInfo = new MethodInfo("detailSetup", publicModifier, "java.lang.String", null, null, null, bodyText, null, null);
                    modifiedClassTree = JpaControllerUtil.TreeMakerUtils.addMethod(modifiedClassTree, workingCopy, methodInfo);
                    
                    bodyText = "return scalarSetup(\"" + fieldName + "_edit\");";
                    methodInfo = new MethodInfo("editSetup", publicModifier, "java.lang.String", null, null, null, bodyText, null, null);
                    modifiedClassTree = JpaControllerUtil.TreeMakerUtils.addMethod(modifiedClassTree, workingCopy, methodInfo);  
                    
                    bodyText = "reset(false);\n" + 
                            fieldName + " = (" + simpleEntityName + ")JsfUtil.getObjectFromRequestParameter(\"jsfcrud.current" + simpleEntityName + "\", converter, null);\n" +
                            "if (" + fieldName + " == null) {\n" +
                            "String request" + simpleEntityName + "String = JsfUtil.getRequestParameter(\"jsfcrud.current" +  simpleEntityName + "\");\n" +
                            "JsfUtil.addErrorMessage(\"The " + fieldName + " with id \" + request" + simpleEntityName + "String + \" no longer exists.\");\n" +
                            "return relatedOrListOutcome();\n" +
                            "}\n" +
                            "return destination;";
                    methodInfo = new MethodInfo("scalarSetup", privateModifier, "java.lang.String", null, new String[]{"java.lang.String"}, new String[]{"destination"}, bodyText, null, null);
                    modifiedClassTree = JpaControllerUtil.TreeMakerUtils.addMethod(modifiedClassTree, workingCopy, methodInfo);  

                    entityStringVar = fieldName + "String";
                    String currentEntityStringVar = "current" + simpleEntityName + "String";
                
                    
                    bodyText = codeToPopulatePkFields.toString() + 
                            "String " + entityStringVar + " = converter.getAsString(FacesContext.getCurrentInstance(), null, " + fieldName + ");\n" +
                            "String " + currentEntityStringVar + " = JsfUtil.getRequestParameter(\"jsfcrud.current" + simpleEntityName + "\");\n" +
                            "if " + entityStringVar + " == null || " + entityStringVar + ".length() == 0 || !" + entityStringVar + ".equals(" + currentEntityStringVar + ")) {\n" +
                            "String outcome = editSetup();\n" +
                            "if (\"" + fieldName + "_edit\".equals(outcome)) {\n" +
                            "JsfUtil.addErrorMessage(\"Could not edit " + fieldName + ". Try again.\");\n" +
                            "}\n" +
                            "return outcome;\n" +
                            "}\n";

                    bodyText +=
                            (useSessionBean ? "try{utx.begin();} catch( Exception ex ){}\n" : "")+
                            "try {\n" +
                            (useSessionBean ? "Exception transactionException = null;\n" : "")+
                            "getJpaController().edit(" + fieldName + ");\n" +
                            (useSessionBean ? "try{utx.commit();} catch(" + rollbackExceptionFQN + " ex){transactionException = ex;} catch( Exception ex ){}\n" : "")+
                            (useSessionBean ? "if(transactionException==null)" : "") +"JsfUtil.addSuccessMessage(\"" + simpleEntityName + " was successfully updated.\");\n"  + //NOI18N
                            (useSessionBean ? "else JsfUtil.ensureAddErrorMessage(transactionException, \"A persistence error occurred.\");\n" : "")+
                            (methodThrowsIllegalOrphanExceptionInEdit ? "} catch (IllegalOrphanException oe) {\n" + 
                            "JsfUtil.addErrorMessages(oe.getMessages());\n" +
                            "return null;\n" : "") +
                            (useSessionBean ? 
                                ("") :
                                ("} catch (NonexistentEntityException ne) {\n" +
                                "JsfUtil.addErrorMessage(ne.getLocalizedMessage());\n" +
                                "return listSetup();\n")
                            ) +
                            "} catch (Exception e) {\n" +
                            (useSessionBean ? "try{utx.rollback();} catch( Exception ex ){}\n" : "")+
                            "JsfUtil.ensureAddErrorMessage(e, \"A persistence error occurred.\");\n" +
                            "return null;\n" +
                            "}\n" +
                            "return detailSetup();";
                    methodInfo = new MethodInfo("edit", publicModifier, "java.lang.String", null, null, null, bodyText, null, null);
                    modifiedClassTree = JpaControllerUtil.TreeMakerUtils.addMethod(modifiedClassTree, workingCopy, methodInfo);
                    

                    bodyText = "String idAsString = JsfUtil.getRequestParameter(\"jsfcrud.current" + simpleEntityName + "\");\n" +
                            (embeddable[0] ? simpleIdPropertyType + " id = converter.getId(idAsString);" : createIdFieldDeclaration(idPropertyType[0], "idAsString", jakartaJsfPackages)) +
                            "\n";
                    bodyText +=
                            (useSessionBean ? "try{utx.begin();} catch( Exception ex ){}\n" : "")+
                            "try {\n" +
                            (useSessionBean ? "Exception transactionException = null;\n" : "")+
                            "getJpaController()."+(useSessionBean ? "remove(getJpaController().find(id))" : "destroy(id)")+";\n" +
                            (useSessionBean ? "try{utx.commit();} catch(" + rollbackExceptionFQN + " ex){transactionException = ex;} catch( Exception ex ){}\n" : "")+
                            (useSessionBean ? "if(transactionException==null)" : "") +"JsfUtil.addSuccessMessage(\"" + simpleEntityName + " was successfully deleted.\");\n"  + //NOI18N
                            (useSessionBean ? "else JsfUtil.ensureAddErrorMessage(transactionException, \"A persistence error occurred.\");\n" : "")+
                            (methodThrowsIllegalOrphanExceptionInDestroy ? "} catch (IllegalOrphanException oe) {\n" + 
                            "JsfUtil.addErrorMessages(oe.getMessages());\n" +
                            "return null;\n" : "") +
                            (useSessionBean ?
                                ("") :
                                ("} catch (NonexistentEntityException ne) {\n" +
                                "JsfUtil.addErrorMessage(ne.getLocalizedMessage());\n" +
                                "return relatedOrListOutcome();\n")
                            ) +
                            "} catch (Exception e) {\n" +
                            (useSessionBean ? "try{utx.rollback();} catch( Exception ex ){}\n" : "")+
                            "JsfUtil.ensureAddErrorMessage(e, \"A persistence error occurred.\");\n" +
                            "return null;\n" +
                            "}\n" +
                            "return relatedOrListOutcome();";
                    methodInfo = new MethodInfo((useSessionBean ? "remove" : "destroy"), publicModifier, "java.lang.String", null, null, null, bodyText, null, null);
                    modifiedClassTree = JpaControllerUtil.TreeMakerUtils.addMethod(modifiedClassTree, workingCopy, methodInfo);  
                    
                    bodyText = "String relatedControllerOutcome = relatedControllerOutcome();\n" +
                            "if (relatedControllerOutcome != null {\n" +
                            "return relatedControllerOutcome;\n" +
                            "}\n" +
                            "return listSetup();";
                    methodInfo = new MethodInfo("relatedOrListOutcome", privateModifier, "java.lang.String", null, null, null, bodyText, null, null);
                    modifiedClassTree = JpaControllerUtil.TreeMakerUtils.addMethod(modifiedClassTree, workingCopy, methodInfo); 

                    TypeInfo listOfEntityType = new TypeInfo("java.util.List", new String[]{entityClass});

                    bodyText = "if (" + fieldName + "Items == null) {\n" +
                            "getPagingInfo();\n" +
                            fieldName + "Items = getJpaController().find" + (useSessionBean ? "Range(new int[]{pagingInfo.getFirstItem(), pagingInfo.getFirstItem() + pagingInfo.getBatchSize()})" : simpleEntityName + "Entities(pagingInfo.getBatchSize(), pagingInfo.getFirstItem())" )+";\n" +//TODO : add this method to session bean generation???
                            "}\n" +
                            "return " + fieldName + "Items;";
                    methodInfo = new MethodInfo("get" + simpleEntityName + "Items", publicModifier, listOfEntityType, null, null, null, bodyText, null, null);
                    modifiedClassTree = JpaControllerUtil.TreeMakerUtils.addMethod(modifiedClassTree, workingCopy, methodInfo); 

                    bodyText = "reset(false);\n" +
                            "getPagingInfo().nextPage();\n "+
                            "return \"" + fieldName + "_list\"";
                    methodInfo = new MethodInfo("next", publicModifier, "java.lang.String", null, null, null, bodyText, null, null);
                    modifiedClassTree = JpaControllerUtil.TreeMakerUtils.addMethod(modifiedClassTree, workingCopy, methodInfo);  

                    bodyText = "reset(false);\n" +
                        "getPagingInfo().previousPage();\n" +
                        "return \"" + fieldName + "_list\";\n";
                    methodInfo = new MethodInfo("prev", publicModifier, "java.lang.String", null, null, null, bodyText, null, null);
                    modifiedClassTree = JpaControllerUtil.TreeMakerUtils.addMethod(modifiedClassTree, workingCopy, methodInfo);  

                    bodyText = "String relatedControllerString = JsfUtil.getRequestParameter(\"jsfcrud.relatedController\");\n" +
                        "String relatedControllerTypeString = JsfUtil.getRequestParameter(\"jsfcrud.relatedControllerType\");\n" +
                        "if (relatedControllerString != null && relatedControllerTypeString != null) {\n" +
                        "FacesContext context = FacesContext.getCurrentInstance();\n" +
                        "Object relatedController = context.getApplication().getELResolver().getValue(context.getELContext(), null, relatedControllerString);\n" +
                        "try {\n" +
                        "Class<?> relatedControllerType = Class.forName(relatedControllerTypeString);\n" +
                        "Method detailSetupMethod = relatedControllerType.getMethod(\"detailSetup\");\n" +
                        "return (String)detailSetupMethod.invoke(relatedController);\n" +
                        "} catch (ClassNotFoundException e) {\n" +
                        "throw new FacesException(e);\n" +
                        "} catch (NoSuchMethodException e) {\n" +
                        "throw new FacesException(e);\n" +
                        "} catch (IllegalAccessException e) {\n" +
                        "throw new FacesException(e);\n" +
                        "} catch (InvocationTargetException e) {\n" +
                        "throw new FacesException(e);\n" +
                        "}\n" +
                        "}\n" +
                        "return null;";
                    methodInfo = new MethodInfo("relatedControllerOutcome", privateModifier, "java.lang.String", null, null, null, bodyText, null, null);
                    modifiedClassTree = JpaControllerUtil.TreeMakerUtils.addMethod(modifiedClassTree, workingCopy, methodInfo);  

                    bodyText = fieldName + " = null;\n" +
                            fieldName + "Items = null;\n" +
                            "pagingInfo.setItemCount(-1);\n" +
                            "if (resetFirstItem) {\n" +
                            "pagingInfo.setFirstItem(0);\n" +
                            "}\n";
                    methodInfo = new MethodInfo("reset", privateModifier, "void", null, new String[]{"boolean"}, new String[]{"resetFirstItem"}, bodyText, null, null);
                    modifiedClassTree = JpaControllerUtil.TreeMakerUtils.addMethod(modifiedClassTree, workingCopy, methodInfo);    

                    String newEntityStringInit;
                    if (embeddable[0]) {
                        newEntityStringInit = "new" + simpleEntityName + ".s" + idGetterName[0].substring(1) + "(new " + idClass.getSimpleName() + "());\n" + 
                                "String " + newEntityStringVar + " = converter.getAsString(FacesContext.getCurrentInstance(), null, new" + simpleEntityName + ");\n";
                    }
                    else {
                        newEntityStringInit = "String " + newEntityStringVar + " = converter.getAsString(FacesContext.getCurrentInstance(), null, new" + simpleEntityName + ");\n";
                    }
                    bodyText = simpleEntityName + " new" + simpleEntityName + " = new " + simpleEntityName + "();\n" +
                            newEntityStringInit +
                            "String " + entityStringVar + " = converter.getAsString(FacesContext.getCurrentInstance(), null, " + fieldName + ");\n" +
                            "if (!" + newEntityStringVar + ".equals(" + entityStringVar + ")) {\n" +
                            "createSetup();\n" +
                            "}\n";
                    methodInfo = new MethodInfo("validateCreate", publicModifier, "void", null, new String[]{facesContextFQN, uiComponentFQN, "java.lang.Object"}, new String[]{"facesContext", "component", "value"}, bodyText, null, null);
                    modifiedClassTree = JpaControllerUtil.TreeMakerUtils.addMethod(modifiedClassTree, workingCopy, methodInfo);    
                    
                    methodInfo = new MethodInfo("getConverter", publicModifier, converterFQN, null, null, null, "return converter;", null, null);
                    modifiedClassTree = JpaControllerUtil.TreeMakerUtils.addMethod(modifiedClassTree, workingCopy, methodInfo);    

                    workingCopy.rewrite(classTree, modifiedClassTree);
                }
            }).commit();
    
        return controllerFileObject;
    }

    private static HashSet<String> CONVERTED_TYPES = new HashSet<String>();
    static {
        CONVERTED_TYPES.add("Boolean");
        CONVERTED_TYPES.add("Byte");
        CONVERTED_TYPES.add("Double");
        CONVERTED_TYPES.add("Float");
        CONVERTED_TYPES.add("Integer");
        CONVERTED_TYPES.add("Long");
        CONVERTED_TYPES.add("Short");
        CONVERTED_TYPES.add("StringBuffer");
    }
    private static HashMap<String,String> PRIMITIVE_TYPES = new HashMap<String, String>();
    static {
        PRIMITIVE_TYPES.put("boolean", "Boolean");
        PRIMITIVE_TYPES.put("byte", "Byte");
        PRIMITIVE_TYPES.put("double", "Double");
        PRIMITIVE_TYPES.put("float", "Float");
        PRIMITIVE_TYPES.put("int", "Integer");
        PRIMITIVE_TYPES.put("long", "Long");
        PRIMITIVE_TYPES.put("short", "Short");
    }
    
    /** @param valueVar is name of a String variable */
    private static String createIdFieldDeclaration(String idPropertyType, String valueVar, boolean jakartaJsfPackages) {
    	String idField;
        if (idPropertyType.startsWith("java.lang.")) {
            String shortName = idPropertyType.substring(10);
            idField = shortName + " id = " + createIdFieldInitialization(idPropertyType, valueVar, jakartaJsfPackages) + ";";
        } else if (idPropertyType.equals("java.math.BigInteger") || "BigInteger".equals(idPropertyType)) {
            idField = "java.math.BigInteger id = " + createIdFieldInitialization(idPropertyType, valueVar, jakartaJsfPackages) + ";";
        } else if (idPropertyType.equals("java.math.BigDecimal") || "BigDecimal".equals(idPropertyType)) {
            idField = "java.math.BigDecimal id = " + createIdFieldInitialization(idPropertyType, valueVar, jakartaJsfPackages) + ";";
        } else {
            idField = idPropertyType + " id = " + createIdFieldInitialization(idPropertyType, valueVar, jakartaJsfPackages) + ";";
        }
        return idField;
    }
    
    /** @param valueVar is name of a String variable */
    private static String createIdFieldInitialization(String idPropertyType, String valueVar, boolean jakartaJsfPackages) {
    	String idField;
        //PENDING cannot assume that key type is Integer, Long, String, int or long
    	if ("char".equals(idPropertyType)) {
            idField = valueVar + ".charAt(0);";
        } else if (PRIMITIVE_TYPES.containsKey(idPropertyType)) {
            String objectType = PRIMITIVE_TYPES.get(idPropertyType);
            String methodName = "parse" + idPropertyType.substring(0,1).toUpperCase() + idPropertyType.substring(1);
            idField = objectType + "." + methodName + "(" + valueVar + ")";
        } else if (idPropertyType.equals("java.math.BigInteger") || "BigInteger".equals(idPropertyType)) {
            idField = "new java.math.BigInteger(" + valueVar + ")";
        } else if (idPropertyType.equals("java.math.BigDecimal") || "BigDecimal".equals(idPropertyType)) {
            idField = "new java.math.BigDecimal(" + valueVar + ")";
        } else if (idPropertyType.equals("java.lang.String") || "String".equals(idPropertyType)) {
            idField = valueVar;
        } else if (idPropertyType.equals("java.lang.Character") || "Character".equals(idPropertyType)) {
            idField = "new Character(" + valueVar + ".charAt(0))";
        } else if (idPropertyType.startsWith("java.lang.")) {
            String shortName = idPropertyType.substring(10);
            idField = "new " + shortName + "(" + valueVar + ")";
        } else if (CONVERTED_TYPES.contains(idPropertyType)) {
            idField = "new " + idPropertyType + "(" + valueVar + ")";
        } else {
            if(jakartaJsfPackages) {
                idField = "(" + idPropertyType + ") jakarta.faces.context.FacesContext.getCurrentInstance().getApplication().\n"
                        + "createConverter(" + idPropertyType + ".class).getAsObject(FacesContext.\n"
                        + "getCurrentInstance(), null, " + valueVar + ")";
            } else {
                idField = "(" + idPropertyType + ") javax.faces.context.FacesContext.getCurrentInstance().getApplication().\n"
                        + "createConverter(" + idPropertyType + ".class).getAsObject(FacesContext.\n"
                        + "getCurrentInstance(), null, " + valueVar + ")";
            }
        }
        return idField;
    }
    
    public static String getManagedBeanName(String simpleEntityName) {
        int len = simpleEntityName.length();
        return len > 1 ? simpleEntityName.substring(0,1).toLowerCase() + simpleEntityName.substring(1) : simpleEntityName.toLowerCase();
    }
    
    private static MethodTree createMethod(WorkingCopy workingCopy, Modifier[] modifiers, String returnType, String name, 
            String[] params, String[] exceptions, String body) {
        if (params.length % 2 != 0) {
            throw new IllegalArgumentException("Number of params can't be odd");
        }
        List<MethodModel.Variable> paramsList = new ArrayList<MethodModel.Variable>();
        for (int i = 0; i < params.length; i++) {
            paramsList.add(MethodModel.Variable.create(params[i], params[i + 1]));
            i++;
        }

        Set<Modifier> modifiersSet = EnumSet.noneOf(Modifier.class);
        modifiersSet.addAll(Arrays.asList(modifiers));

        MethodModel methodModel = MethodModel.create(
                name,
                returnType,
                body,
                paramsList,
                Arrays.asList(exceptions),
                modifiersSet
                );
        return MethodModelSupport.createMethodTree(workingCopy, methodModel);
    }
}