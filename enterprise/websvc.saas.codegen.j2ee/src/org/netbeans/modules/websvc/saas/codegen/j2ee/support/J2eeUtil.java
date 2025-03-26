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
package org.netbeans.modules.websvc.saas.codegen.j2ee.support;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.project.Project;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.j2ee.dd.api.common.NameAlreadyUsedException;
import org.netbeans.modules.j2ee.dd.api.web.DDProvider;
import org.netbeans.modules.j2ee.dd.api.web.Listener;
import org.netbeans.modules.j2ee.dd.api.web.Servlet;
import org.netbeans.modules.j2ee.dd.api.web.ServletMapping;
import org.netbeans.modules.j2ee.dd.api.web.WebApp;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.jsps.parserapi.JspParserAPI;
import org.netbeans.modules.web.jsps.parserapi.JspParserFactory;
import org.netbeans.modules.web.jsps.parserapi.PageInfo;
import org.netbeans.modules.websvc.saas.codegen.Constants;
import org.netbeans.modules.websvc.saas.codegen.Constants.SaasAuthenticationType;
import org.netbeans.modules.websvc.saas.codegen.SaasClientCodeGenerator;
import org.netbeans.modules.websvc.saas.codegen.model.SaasBean.SessionKeyAuthentication;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.NbBundle;
import org.netbeans.modules.websvc.saas.codegen.Constants.DropFileType;
import org.netbeans.modules.websvc.saas.codegen.java.support.AbstractTask;
import org.netbeans.modules.websvc.saas.codegen.java.support.JavaSourceHelper;
import org.netbeans.modules.websvc.saas.codegen.java.support.JavaUtil;
import org.netbeans.modules.websvc.saas.codegen.model.ParameterInfo;
import org.netbeans.modules.websvc.saas.codegen.model.SaasBean;
import org.netbeans.modules.websvc.saas.codegen.model.SaasBean.HttpBasicAuthentication;
import org.netbeans.modules.websvc.saas.codegen.model.SaasBean.SaasAuthentication.UseTemplates;
import org.netbeans.modules.websvc.saas.codegen.model.SaasBean.SaasAuthentication.UseTemplates.Template;
import org.netbeans.modules.websvc.saas.codegen.ui.CodeSetupPanel;
import org.netbeans.modules.websvc.saas.codegen.util.Util;
import org.netbeans.modules.websvc.saas.model.WsdlSaasMethod;
import org.openide.filesystems.FileUtil;
import org.openide.util.Parameters;

/**
 * Copy of j2ee/utilities Util class
 *  
 * TODO: Should move some of the methods into o.n.m.w.r.support.Utils class
 * since that's the package used for sharing all the utility classes.
 * 
 */
public class J2eeUtil {

    public static final String JSP_NAMES_PAGE = "page";

    /*
     * Check if the primary file of d is a REST Resource
     */
    public static boolean isRestJavaFile(DataObject d) {
        try {
            if (!JavaUtil.isJava(d)) {
                return false;
            }
            EditorCookie ec = d.getCookie(EditorCookie.class);
            if (ec == null) {
                return false;
            }
            javax.swing.text.Document doc = ec.getDocument();
            if (doc != null) {
                String docText = doc.getText(0, doc.getLength());

                return (docText.indexOf(Util.APATH) != -1) ||
                        (docText.indexOf(Util.AGET) != -1) ||
                        (docText.indexOf(Util.APOST) != -1) ||
                        (docText.indexOf(Util.APUT) != -1) ||
                        (docText.indexOf(Util.ADELETE) != -1);
            }
        } catch (BadLocationException ex) {
        }
        return false;
    }

    public static boolean isServlet(DataObject d) {
        try {
            if (!JavaUtil.isJava(d)) {
                return false;
            }
            EditorCookie ec = d.getCookie(EditorCookie.class);
            if (ec == null) {
                return false;
            }
            javax.swing.text.Document doc = ec.getDocument();
            if (doc != null) {
                String docText = doc.getText(0, doc.getLength());

                return (docText.indexOf("extends HttpServlet") != -1);
            }
        } catch (BadLocationException ex) {
        }
        return false;
    }

    public static boolean isJsp(DataObject d) {
        if (d != null && "jsp".equals(d.getPrimaryFile().getExt())) //NOI18N
        {
            return true;
        }
        return false;
    }

    /**
     *  Return target and generated file objects
     */
    public static void addServletMethod(final SaasBean bean,
            String groupName, final String methodName, final JavaSource source,
            final String[] parameters, final Object[] paramTypes,
            final String bodyText) throws IOException {

        if (JavaSourceHelper.isContainsMethod(source, methodName, parameters, paramTypes)) {
            return;
        }
        ModificationResult result = source.runModificationTask(new AbstractTask<WorkingCopy>() {

            public void run(WorkingCopy copy) throws IOException {
                copy.toPhase(JavaSource.Phase.RESOLVED);

                javax.lang.model.element.Modifier[] modifiers = JavaUtil.PROTECTED;

                String type = Constants.VOID;

                String comment = "\n";// NOI18N
                for (String param : parameters) {
                    comment += "@param $PARAM$ resource URI parameter\n".replace("$PARAM$", param);// NOI18N
                }
                comment += "@return an instance of " + type;// NOI18N
                ClassTree initial = JavaSourceHelper.getTopLevelClassTree(copy);
                ClassTree tree = JavaSourceHelper.addMethod(copy, initial,
                        modifiers, null, null,
                        methodName, type, parameters, paramTypes,
                        null, null, new String[]{"javax.servlet.ServletException", "java.io.IOException"},
                        bodyText, comment);      //NOI18N
                copy.rewrite(initial, tree);
            }
        });
        result.commit();
    }
    
    public static void createSessionKeyAuthorizationClassesForWeb(
            SaasBean bean, Project project,
            String groupName, String saasServicePackageName, FileObject targetFolder,
            JavaSource loginJS, FileObject loginFile,
            JavaSource callbackJS, FileObject callbackFile,
            final String[] parameters, final Object[] paramTypes, boolean isUseTemplates,
            DropFileType dropFileType) throws IOException {
        FileObject[] loginFiles = new FileObject[1];
        JavaSource[] loginJavaSources = new JavaSource[0];
        FileObject[] callbackFiles = new FileObject[1];
        JavaSource[] callbackJavaSources = new JavaSource[0];
        createSessionKeyAuthorizationClassesForWeb(bean, project, groupName,
                saasServicePackageName, targetFolder, 
                loginJavaSources, loginFiles,
                callbackJavaSources, callbackFiles, 
                parameters, paramTypes, isUseTemplates, false, dropFileType);

        //Make entry into web.xml for login and callback servlets
        if (loginFiles[0] != null && callbackFiles[0] != null) {
            loginFile = loginFiles[0];
            callbackFile = callbackFiles[0];
            Map<String, String> filesMap = new HashMap<String, String>();
            filesMap.put(loginFile.getName(), saasServicePackageName + "." + loginFile.getName());
            filesMap.put(callbackFile.getName(), saasServicePackageName + "." + callbackFile.getName());
            addAuthorizationClassesToWebDescriptor(project, filesMap);
        } else {
            Logger.getLogger(J2eeUtil.class.getName()).log(Level.INFO, "Cannot add login and callback servlets" +
                    "to web descriptor");
        }
    }
    
    public static void createSessionKeyAuthorizationClassesForWeb(
            SaasBean bean, Project project,
            String groupName, String saasServicePackageName, FileObject targetFolder,
            JavaSource[] loginJS, FileObject[] loginFile,
            JavaSource[] callbackJS, FileObject[] callbackFile,
            final String[] parameters, final Object[] paramTypes, boolean isUseTemplates,
            boolean skipWebDescEntry, DropFileType dropFileType) throws IOException {
        SaasAuthenticationType authType = bean.getAuthenticationType();
        if (authType == SaasAuthenticationType.SESSION_KEY ||
                authType == SaasAuthenticationType.HTTP_BASIC) {
            if (!isUseTemplates) {
                String fileId = Util.upperFirstChar(Constants.LOGIN);// NoI18n
                String methodName = "processRequest";// NoI18n
                String authFileName = groupName + fileId;
                loginJS[0] = JavaSourceHelper.createJavaSource(
                        SaasClientCodeGenerator.TEMPLATES_SAAS + authType.getClassIdentifier() + fileId + "." + Constants.JAVA_EXT,
                        targetFolder, saasServicePackageName, authFileName);// NOI18n
                Set<FileObject> files = new HashSet<FileObject>(loginJS[0].getFileObjects());
                if (files != null && files.size() > 0) {
                    loginFile[0] = files.iterator().next();
                }

                if (!JavaSourceHelper.isContainsMethod(loginJS[0], methodName, parameters, paramTypes)) {
                    addServletMethod(bean, groupName, methodName, loginJS[0],
                            parameters, paramTypes,
                            "{ \n" + Util.getServletLoginBody(bean, groupName) + "\n }");
                }

                fileId = Util.upperFirstChar(Constants.CALLBACK);// NOI18n
                authFileName = groupName + fileId;
                callbackJS[0] = JavaSourceHelper.createJavaSource(
                        SaasClientCodeGenerator.TEMPLATES_SAAS + authType.getClassIdentifier() + fileId + "." + Constants.JAVA_EXT,
                        targetFolder, saasServicePackageName, authFileName);// NOI18n
                files = new HashSet<FileObject>(callbackJS[0].getFileObjects());
                if (files != null && files.size() > 0) {
                    callbackFile[0] = files.iterator().next();
                }

                if (!JavaSourceHelper.isContainsMethod(callbackJS[0], methodName, parameters, paramTypes)) {
                    addServletMethod(bean, groupName, methodName, callbackJS[0],
                            parameters, paramTypes,
                            "{ \n" + Util.getServletCallbackBody(bean, groupName) + "\n }");
                }
            } else {
                UseTemplates useTemplates = null;
                if (bean.getAuthentication() instanceof SessionKeyAuthentication) {
                    SessionKeyAuthentication sessionKey = (SessionKeyAuthentication) bean.getAuthentication();
                    useTemplates = sessionKey.getUseTemplates();
                } else if (bean.getAuthentication() instanceof HttpBasicAuthentication) {
                    HttpBasicAuthentication httpBasic = (HttpBasicAuthentication) bean.getAuthentication();
                    useTemplates = httpBasic.getUseTemplates();
                }
                if (useTemplates != null) {
                    String dropType = dropFileType.prefix();
                    for (Template template : useTemplates.getTemplates()) {
                        if(!template.getDropTypeList().contains(dropType))
                            continue;           
                        String id = template.getId();
                        String type = template.getType() == null ? "" : template.getType();
                        String templateUrl = template.getUrl();
                        if (templateUrl == null || templateUrl.trim().equals("")) {
                            throw new IOException("Authentication template is empty.");
                        }
                        //FIXME - Hack
                        if(templateUrl.contains("Desktop"))
                            continue;
                        String fileName = null;
//                        if (type.equals(Constants.LOGIN)) {
                        if (templateUrl.contains("Login")) {
                            fileName = bean.getSaasName() + Util.upperFirstChar(Constants.LOGIN);
//                        } else if (type.equals(Constants.CALLBACK)) {
                        } else if (templateUrl.contains("Callback")) {
                            fileName = bean.getSaasName() + Util.upperFirstChar(Constants.CALLBACK);
                        } else if (templateUrl.contains("Authenticator")) {
//                        } else if (type.equals(Constants.AUTH)) {
                            continue;
                        }
                        FileObject fObj = null;
                        if (templateUrl.endsWith("." + Constants.JAVA_EXT)) {
                            JavaSource source = JavaSourceHelper.createJavaSource(templateUrl, targetFolder,
                                    bean.getSaasServicePackageName(), fileName);

                            if (source != null && getDeploymentDescriptor(project) == null) {
                                addServletAnnotation(source, fileName, "/"+fileName); //NOI18N
                            }

                            Set<FileObject> files = new HashSet<FileObject>(source.getFileObjects());
                            if (files != null && files.size() > 0) {
                                fObj = files.iterator().next();
                            }
                        } else {
                            if (fileName != null) {
                                fObj = targetFolder.getFileObject(fileName);
                                if (fObj == null) {
                                    DataObject d = Util.createDataObjectFromTemplate(templateUrl, targetFolder,
                                            fileName);
                                    if (d != null) {
                                        fObj = d.getPrimaryFile();
                                    }
                                }
                            }
                        }
                        if (fObj != null) {
                            if (templateUrl.contains("Login")) {
                                loginFile[0] = fObj;
                            } else if (templateUrl.contains("Callback")) {
                                callbackFile[0] = fObj;
                            }
                        }
                    }
                }
            }
        }
    }

    public static FileObject getWebXmlFile(Project p) {
        SourceGroup[] groups = ProjectUtils.getSources(p).getSourceGroups("web");
        for (SourceGroup group : groups) {
            FileObject root = group.getRootFolder();
            java.util.Enumeration<? extends FileObject> files = root.getData(true);
            while (files.hasMoreElements()) {
                FileObject fobj = files.nextElement();
                if (fobj.getNameExt().equals("web.xml")) {
                    return fobj;
                }
            }
        }
        return null;
    }

    public static void addAuthorizationClassesToWebDescriptor(Project p,
            Map<String, String> filesMap) throws IOException {
        for (Map.Entry e : filesMap.entrySet()) {
            String name = (String) e.getKey();
            String qName = (String) e.getValue();
            addServiceEntriesToDD(p, name, qName);
        }
    }

    /**
     * This is to support non-JSR 109 containers. In this case, a regular jaxws web service
     * is created and the deployment descriptor is updated with the jaxws-ri servlet and
     * listener.
     */
    public static void addServiceEntriesToDD(Project p, String servletName,
            String servletClassName) {
        WebApp webApp = getWebApp(p);
        if (webApp != null) {
            Servlet servlet = null;
            Listener listener = null;
            try {
                servlet = (Servlet) webApp.addBean("Servlet", new String[]{"ServletName", "ServletClass"},
                        new Object[]{servletName, servletClassName}, "ServletName");
                servlet.setLoadOnStartup(new java.math.BigInteger("1"));
                ServletMapping servletMapping = (ServletMapping) webApp.addBean("ServletMapping", new String[]{"ServletName", "UrlPattern"},
                        new Object[]{servletName, "/" + servletName}, "ServletName");
                // This also saves server specific configuration, if necessary.
                webApp.write(getDeploymentDescriptor(p));
            } catch (ClassNotFoundException exc) {
                Logger.getLogger("global").log(Level.INFO, exc.getLocalizedMessage());
            } catch (NameAlreadyUsedException exc) {
                Logger.getLogger("global").log(Level.INFO, exc.getLocalizedMessage());
            } catch (IOException exc) {
                Logger.getLogger("global").log(Level.INFO, exc.getLocalizedMessage());
            }
        }
    }

    public static FileObject getDeploymentDescriptor(Project p) {
        FileObject webInfFo = getWebInf(p);
        if (webInfFo == null) {
            if (JavaUtil.isProjectOpened(p)) {
                DialogDisplayer.getDefault().notify(
                        new NotifyDescriptor.Message(NbBundle.getMessage(
                            CodeSetupPanel.class, "MSG_WebInfCorrupted",
                            new Object[] {p.getProjectDirectory().getPath()}), // NOI18N
                            NotifyDescriptor.ERROR_MESSAGE));
            }
            return null;
        }
        return getWebInf(p).getFileObject("web.xml");//NoI18n
    }

    public static FileObject getWebInf(Project p) {
        WebModule webModule = getWebModule(p.getProjectDirectory());
        if (webModule != null) {
            return webModule.getWebInf();
        }
        return null;
    }

    public static WebApp getWebApp(Project p) {
        try {
            FileObject deploymentDescriptor = getDeploymentDescriptor(p);
            if (deploymentDescriptor != null) {
                return DDProvider.getDefault().getDDRoot(deploymentDescriptor);
            }
        } catch (java.io.IOException e) {
            Logger.getLogger("global").log(Level.INFO, e.getLocalizedMessage());
        }
        return null;
    }
    
    public static WebModule getWebModule(FileObject fo) {
        return getWebModule(fo, false);
    }
    
    public static WebModule getWebModule(FileObject fo, boolean checkParent) {
        WebModule wm =  WebModule.getWebModule(fo);
        if(checkParent && wm != null) {
            FileObject wmRoot = wm.getDocumentBase();
            if (fo == wmRoot || FileUtil.isParentOf(wmRoot, fo)) {
                return WebModule.getWebModule(fo);
            }
        }
        return wm;
    }

    public static String wrapWithTag(String content, Document doc, int insertStart) {
        String str = "";
        boolean addTag = !isWithinTag(doc, 0, insertStart);
        if(addTag)
            str += "\n<%\n";
        str += content;
        if(addTag)
            str += "\n%>\n";
        return str;
    }
    
    public static boolean isWithinTag(Document doc, int start, int end) {
        try {
            String str = doc.getText(start, end - start);
            return str.lastIndexOf("<%") > str.lastIndexOf("%>");
        } catch (BadLocationException ex) {
            return false;
        }
    }
    
    public static String getJspImports(Document doc, int start, String svcPkg) throws IOException {
        String[] imports = new String[] {SaasClientCodeGenerator.REST_CONNECTION_PACKAGE+".*", svcPkg+".*"};
        List<String> importsToAdd = new ArrayList<String>();
        String code = "";
        List<String> existingImports = getExistingJspImports(
                NbEditorUtilities.getFileObject(doc));
        for(String imp:imports) {
            if(!existingImports.contains(imp))
                importsToAdd.add(imp);
        }
        if(importsToAdd.size() > 0)
            code += "\n<%@ page import=\"";
        for(String imp:importsToAdd) {
            code +=  imp + ", ";
        }
        if(importsToAdd.size() > 0)
            code = code.substring(0, code.length()-2) + "\" %>\n";
        if(code.length() > 0 && isWithinTag(doc, 0, start)) {
            code = "%>\n"+code+"\n<%";
        }
        return code;
    }
    
    public static List<String> getExistingJspImports(FileObject fo) throws IOException {
        WebModule webModule = J2eeUtil.getWebModule(fo, true);
        JspParserAPI jspParser = JspParserFactory.getJspParser();
        JspParserAPI.ParseResult result = jspParser.analyzePage(fo, 
                webModule, JspParserAPI.ERROR_IGNORE);
        PageInfo pInfo = result.getPageInfo();
        return pInfo.getImports();
    }

    public static List<ParameterInfo> filterJspParameters(List<ParameterInfo> params) {
        List<ParameterInfo> returnParams = new ArrayList<ParameterInfo>();
        for (ParameterInfo p : params) {
            String name = Util.getParameterName(p);
            if (Constants.HTTP_SERVLET_REQUEST_VARIABLE.equals(name) ||
                    Constants.HTTP_SERVLET_RESPONSE_VARIABLE.equals(name)) {
                continue;
            }
            returnParams.add(p);
        }
        return returnParams;
    }

    public static SoapClientJ2eeOperationInfo[] toJaxwsOperationInfos(WsdlSaasMethod m, 
            Project project) {
        List<SoapClientJ2eeOperationInfo> infos = new ArrayList<SoapClientJ2eeOperationInfo>();
        infos.add(new SoapClientJ2eeOperationInfo(m, project));
        
        return infos.toArray(new SoapClientJ2eeOperationInfo[0]);
    }

    public static void addServletAnnotation(JavaSource javaSource, final String servletName, final String urlPattern)
        throws IOException {
        final CancellableTask<WorkingCopy> modificationTask = new CancellableTask<WorkingCopy>() {

            public void cancel() {
            }

            public void run(WorkingCopy workingCopy) throws Exception {
                workingCopy.toPhase(Phase.RESOLVED);
                TypeElement servletAn = workingCopy.getElements().getTypeElement("javax.servlet.annotation.WebServlet"); //NOI18N
                if (servletAn != null) {
                    boolean found = false;
                    TypeElement classEl = getPublicTopLevelElement(workingCopy);
                    if (classEl != null) {
                        List<? extends AnnotationMirror> annotations = classEl.getAnnotationMirrors();

                        for (AnnotationMirror m : annotations) {
                            Name qualifiedName = ((TypeElement)m.getAnnotationType().asElement()).getQualifiedName();
                            if (qualifiedName.contentEquals("javax.servlet.annotation.WebServlet")) { //NOI18N
                                found = true;
                                break;
                            }
                        }
                    }
                    if (!found) {
                        ClassTree classTree = getPublicTopLevelTree(workingCopy);
                        if (classTree != null) {
                            TreeMaker make = workingCopy.getTreeMaker();
                            List<ExpressionTree> attrs = new ArrayList<ExpressionTree>();
                            attrs.add(
                                    make.Assignment(make.Identifier("name"), make.Literal(servletName))); //NOI18N
                            attrs.add(
                                    make.Assignment(make.Identifier("urlPatterns"), make.Literal(urlPattern))); //NOI18N

                            AnnotationTree servletAnnotation = make.Annotation(
                                    make.QualIdent(servletAn),
                                    attrs);
                            ClassTree modifiedClass = make.Class(
                                        make.addModifiersAnnotation(classTree.getModifiers(), servletAnnotation),
                                        classTree.getSimpleName(),
                                        classTree.getTypeParameters(),
                                        classTree.getExtendsClause(),
                                        classTree.getImplementsClause(),
                                        classTree.getMembers());
                            workingCopy.rewrite(classTree, modifiedClass);
                        }
                    }
                }
            }

        };
        javaSource.runModificationTask(modificationTask).commit();
    }

    public static ClassTree getPublicTopLevelTree(CompilationController controller) {
        Parameters.notNull("controller", controller); // NOI18N

        TypeElement typeElement = getPublicTopLevelElement(controller);
        if (typeElement != null) {
            return controller.getTrees().getTree(typeElement);
        }
        return null;
    }

    public static TypeElement getPublicTopLevelElement(CompilationController controller) {
        Parameters.notNull("controller", controller); // NOI18N

        FileObject mainFileObject = controller.getFileObject();
        if (mainFileObject == null) {
            throw new IllegalStateException();
        }
        String mainElementName = mainFileObject.getName();
        List<? extends TypeElement> elements = controller.getTopLevelElements();
        if (elements != null) {
            for (TypeElement element : elements) {
                if (element.getModifiers().contains(Modifier.PUBLIC) && element.getSimpleName().contentEquals(mainElementName)) {
                    return element;
                }
            }
        }
        return null;
    }
}
