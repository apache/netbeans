/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.websvc.rest.client;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.VariableTree;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.xml.bind.JAXBElement;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import org.apache.tools.ant.module.api.support.ActionUtils;
import org.netbeans.api.java.source.Comment;
import org.netbeans.api.java.source.Comment.Style;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.websvc.rest.client.ClientJavaSourceHelper.HttpMimeType;
import org.netbeans.modules.websvc.rest.model.api.RestConstants;
import org.netbeans.modules.websvc.rest.spi.RestSupport;
import org.netbeans.modules.websvc.rest.support.JavaSourceHelper;
import org.netbeans.modules.websvc.rest.support.SourceGroupSupport;
import org.netbeans.modules.websvc.saas.model.WadlSaas;
import org.netbeans.modules.websvc.saas.model.WadlSaasMethod;
import org.netbeans.modules.websvc.saas.model.WadlSaasResource;
import org.netbeans.modules.websvc.saas.model.jaxb.FieldDescriptor;
import org.netbeans.modules.websvc.saas.model.jaxb.MethodDescriptor;
import org.netbeans.modules.websvc.saas.model.jaxb.ServletDescriptor;
import org.netbeans.modules.websvc.saas.model.wadl.Method;
import org.netbeans.modules.websvc.saas.model.wadl.Representation;
import org.netbeans.modules.websvc.saas.model.wadl.Request;
import org.netbeans.modules.websvc.saas.model.wadl.Response;
import org.netbeans.modules.websvc.saas.util.SaasUtil;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.support.ant.GeneratedFilesHelper;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.execution.ExecutorTask;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.xml.sax.SAXException;

/**
 *
 * @author mkuchtiak
 */
class Wadl2JavaHelper {

    private static final String PROP_XML_SCHEMA="xml_schema"; //NOI18N
    private static final String PROP_PACKAGE_NAME="package_name"; //NOI18N
    private static final String PROP_SOURCE_ROOT="source_root"; //NOI18N
    private static final String SIGN_PARAMS_METHOD="signParams"; //NOI18N
    static final String PROJEC_TYPE_WEB="web"; //NOI18N
    static final String PROJEC_TYPE_WEB_EE7="web-ee7"; //NOI18N
    static final String PROJEC_TYPE_DESKTOP="desktop"; //NOI18N
    static final String PROJEC_TYPE_NB_MODULE="nb-module"; //NOI18N

    static Pair<String> getParamList(List<String> requiredParams,
            Map<String,String> fixedParams) 
    {
        StringBuilder paramNames = new StringBuilder();
        StringBuilder paramValues = new StringBuilder();
        boolean first = true;
        for (String p : requiredParams) {
            if (first) {
                first = false;
            } else {
                paramNames.append(",");
                paramValues.append(",");
            }
            paramNames.append("\"");
            paramNames.append(p);
            paramNames.append("\"");
            paramValues.append(makeJavaIdentifier(p));
        }
        for(Entry<String,String> entry: fixedParams.entrySet()){
            String p = entry.getKey();
            if (first) {
                first = false;
            } else {
                paramNames.append(",");
                paramValues.append(",");
                
            }
            paramNames.append("\"");
            paramNames.append(p);
            paramNames.append("\"");
            paramValues.append("\"");
            paramNames.append(entry.getValue());
            paramNames.append("\"");
        }

        return new Pair<String>(paramNames.toString(), paramValues.toString());
    }
    
    static Pair<String> getParamList(List<String> requiredParams, 
            Map<String,String> fixedParams, SecurityParams securityParams) 
    {
        StringBuffer paramNames = new StringBuffer();
        StringBuffer paramValues = new StringBuffer();
        boolean first = true;
        for (String p : requiredParams) {
            if (!isSignatureParam(p, securityParams)) {
                if (first) {
                    first = false;
                } else {
                    paramNames.append(",");
                    paramValues.append(",");
                }
                paramNames.append("\""+p+"\"");
                if (isSecurityParam(p, securityParams)) {
                    paramValues.append(findGetterForParam(p, securityParams.getMethodDescriptors()));
                } else {
                    paramValues.append(makeJavaIdentifier(p));
                }
            }
        }
        for (String p : fixedParams.keySet()) {
            if (!isSignatureParam(p, securityParams)) {
                if (first) {
                    first = false;
                } else {
                    paramNames.append(",");
                    paramValues.append(",");

                }
                paramNames.append("\""+p+"\"");
                if (isSecurityParam(p, securityParams)) {
                    paramValues.append(findGetterForParam(p, securityParams.getMethodDescriptors()));
                } else {
                    paramValues.append("\""+fixedParams.get(p)+"\"");
                }

            }
        }
        return new Pair<String>(paramNames.toString(), paramValues.toString());
    }

    static String makeJavaIdentifier(String s) {
        int len = s.length();
        String result = s;
        for (int i=0; i<len; i++) {
            char ch = result.charAt(i);
            if (!Character.isJavaIdentifierPart(ch)) {
                result = result.replace(ch, '_');
            }
        }
        if (len>0) {
            if (!Character.isJavaIdentifierStart(result.charAt(0))) {
                result = "_"+result;
            }
            result = result.substring(0,1).toLowerCase()+result.substring(1);
        }
        return result;
    }

    static String getClientClassName(WadlSaasResource saasResource) {
        String path = saasResource.getResource().getPath();
        int len = path.length();
        for (int i=0; i<len; i++) {
            char ch = path.charAt(i);
            if (!Character.isJavaIdentifierPart(ch)) {
                path = path.replace(ch, '_'); //NOI18N
            }
        }
        while (path.startsWith("_")) { //NOI18N
            path = path.substring(1);
        }
        while (path.endsWith("_")) { //NOI18N
            path = path.substring(0, path.length()-1);
        }
        String saasName = saasResource.getSaas().getDisplayName();
        saasName = saasName.replace(" ", "_"); //NOI18N

        if (saasName.length() == 0) {
            saasName = "Resource"; //NOI18N
        } else if (!Character.isJavaIdentifierStart(saasName.charAt(0))) {
            saasName= "Resource_"+saasName; //NOI18N
        } else if (Character.isLowerCase(saasName.charAt(0))) {
            saasName = saasName.substring(0,1).toUpperCase()+saasName.substring(1);
        }

        return saasName+(path.length() == 0 ? "" : "_"+path)+"_JerseyClient"; //NOI18N
    }

    static class Pair<T> {
        private T key;
        private T value;

        public Pair(T key, T value) {
            this.key = key;
            this.value = value;
        }

        public T getKey() {
            return key;
        }

        public T getValue() {
            return value;
        }
    }

    static void generateJaxb(FileObject targetFo, WadlSaas wadlSaas) throws java.io.IOException {
        Project project = FileOwnerQuery.getOwner(targetFo);
        if (project != null) {
            FileObject buildXml = project.getProjectDirectory().getFileObject(GeneratedFilesHelper.BUILD_XML_PATH);
            if (buildXml != null) {
                List<FileObject> schemaFiles = wadlSaas.getLocalSchemaFiles();
                if (schemaFiles.size() > 0) {
                    FileObject srcRoot = findSourceRootForFile(project, targetFo);
                    if (srcRoot != null) {
                        XmlStaxUtils staxUtils = new XmlStaxUtils();
                        String saasDir =  getSourceRootPath(project, srcRoot);
                        String packagePrefix = wadlSaas.getPackageName();
                        String targetName = "saas.xjc."+packagePrefix; //NOI18N
                        try {
                            boolean isXjcTarget = staxUtils.isTarget(buildXml, targetName); //NOI18N
                            if (!isXjcTarget) {
                                NotifyDescriptor dd = new NotifyDescriptor.Confirmation(
                                    NbBundle.getMessage(Wadl2JavaHelper.class, "MSG_CreateJaxbArtifacts", new Object[]{targetName, saasDir}),
                                    NotifyDescriptor.YES_NO_OPTION);
                                DialogDisplayer.getDefault().notify(dd);
                                if (NotifyDescriptor.OK_OPTION.equals(dd.getValue())) {
                                    // create META-INF if missing
                                    FileObject metaInf = srcRoot.getFileObject("META-INF"); //NOI18N
                                    if (metaInf == null) {
                                        metaInf = srcRoot.createFolder("META-INF"); //NOI18N
                                    }
                                    //try {
                                    String[] xmlSchemas = new String[schemaFiles.size()];
                                    String[] packageNames = new String[schemaFiles.size()];
                                    boolean isInitTarget = staxUtils.isTarget(buildXml, "saas-init-xjc"); //NOI18N
                                    int i=0;
                                    for (FileObject schemaFile : schemaFiles) {
                                        if (metaInf != null && metaInf.isFolder() && metaInf.getFileObject(schemaFile.getNameExt()) == null) {
                                            // copy schema to META-INF
                                            FileUtil.copyFile(schemaFile, metaInf, schemaFile.getName());
                                            xmlSchemas[i] = saasDir+"/META-INF/"+schemaFile.getNameExt(); //NOI18N
                                        } else {
                                            xmlSchemas[i] = schemaFile.getPath();
                                        }
                                        packageNames[i++] = packagePrefix+"."+SaasUtil.toValidJavaName(schemaFile.getName()).toLowerCase();
                                    }
                                    XmlDomUtils.addJaxbXjcTargets(buildXml, targetName, saasDir, xmlSchemas, packageNames, isInitTarget, PROJEC_TYPE_NB_MODULE.equals(getProjectType(project)));
                                    for (FileObject schemaFile : schemaFiles) {
                                        ExecutorTask executorTask = ActionUtils.runTarget(buildXml, new String[] {targetName}, null);
                                    }
                                }
                            }
                        } catch (XMLStreamException ex) {
                            Logger.getLogger(Wadl2JavaHelper.class.getName()).log(Level.WARNING, "Can not parse wadl file", ex);
                        } catch (ParserConfigurationException ex) {
                            Logger.getLogger(Wadl2JavaHelper.class.getName()).log(Level.WARNING, "Can not configure parser for wadl file", ex);
                        } catch (SAXException ex) {
                            Logger.getLogger(Wadl2JavaHelper.class.getName()).log(Level.WARNING, "Can not parse wadl file", ex);
                        }
                    }
                }
            }
        }
    }

    static String getProjectType(Project project) {
        RestSupport restSupport = project.getLookup().lookup(RestSupport.class);

        if (restSupport != null) {
            int projectType = restSupport.getProjectType();
            if (projectType == RestSupport.PROJECT_TYPE_WEB) {
                return PROJEC_TYPE_WEB;
            } else if (projectType == RestSupport.PROJECT_TYPE_NB_MODULE) {
                return PROJEC_TYPE_NB_MODULE;
            }
        } else {
            AuxiliaryConfiguration aux = ProjectUtils.getAuxiliaryConfiguration(project);
            for (int i=1;i<10;i++) {
                // be prepared for namespace upgrade
                if (aux.getConfigurationFragment("data", //NOI18N
                        "http://www.netbeans.org/ns/nb-module-project/"+String.valueOf(i), //NOI18N
                        true) != null) {
                    return PROJEC_TYPE_NB_MODULE;
                } else if (aux.getConfigurationFragment("data", //NOI18N
                        "http://www.netbeans.org/ns/web-project/"+String.valueOf(i), //NOI18N
                        true) != null) {
                    return PROJEC_TYPE_WEB;
                }
            }
        }
        return PROJEC_TYPE_DESKTOP;
    }

    private static FileObject findSourceRootForFile(Project project, FileObject fo) {
        SourceGroup[] sourceGroups = SourceGroupSupport.getJavaSourceGroups(project);
        for (SourceGroup sourceGroup : sourceGroups) {
            FileObject srcRoot = sourceGroup.getRootFolder();
            if (FileUtil.isParentOf(srcRoot, fo)) {
                return srcRoot;
            }
        }
        return null;
    }

    private static String getSourceRootPath(Project project, FileObject srcRoot) {
        return FileUtil.getRelativePath(project.getProjectDirectory(), srcRoot);
    }

    static boolean isSecurityParam(String param, SecurityParams securityParams) {
        if (securityParams.getParams().contains(param)) {
            return true;
        }
        return false;
    }

    static boolean isSignatureParam(String param, SecurityParams securityParams) {
        if (param.equals(securityParams.getSignature())) {
            return true;
        }
        return false;
    }

    private static String findGetterForParam(String param, List<MethodDescriptor> methodDescriptors) {
        for (MethodDescriptor method : methodDescriptors) {
            if (param.equals(method.getId())) {
                return method.getName()+"()"; //NOI18N
            }
        }
        return makeJavaIdentifier(param);
    }

    static ClassTree addSessionAuthMethods(WorkingCopy copy, ClassTree originalClass, SecurityParams securityParams) {
        ClassTree modifiedClass = originalClass;
        TreeMaker maker = copy.getTreeMaker();

        for (FieldDescriptor field : securityParams.getFieldDescriptors()) {

            ModifiersTree fieldModifiers = maker.Modifiers(getModifiers(field.getModifiers()));
            ExpressionTree fieldType = JavaSourceHelper.createTypeTree(copy, field.getType());
            VariableTree fieldTree = maker.Variable(fieldModifiers, field.getName(), fieldType, null); //NOI18N
            modifiedClass = maker.addClassMember(modifiedClass, fieldTree);
        }

        for (MethodDescriptor m : securityParams.getMethodDescriptors()) {
            ModifiersTree methodModifiers = maker.Modifiers(getModifiers(m.getModifiers()));
            // add params
            List<VariableTree> paramList = new ArrayList<VariableTree>();
            String pList = m.getParamNames();
            if (pList != null) {
                List<String> paramN = getList(pList);
                List<String> paramT = getList(m.getParamTypes());
                ModifiersTree paramModifier = maker.Modifiers(Collections.<Modifier>emptySet());
                for (int i=0; i<paramN.size(); i++) {
                    Tree paramTypeTree = JavaSourceHelper.createTypeTree(copy, paramT.get(i)); //NOI18N
                    VariableTree paramTree = maker.Variable(paramModifier, paramN.get(i), paramTypeTree, null); //NOI18N
                    paramList.add(paramTree);
                }
            }

            // add throws
            List<ExpressionTree> throwsList = new ArrayList<ExpressionTree>();
            String tList = m.getThrows();
            if (tList != null) {
                for (String thr : getList(tList)) {
                    throwsList.add(JavaSourceHelper.createTypeTree(copy, thr));
                }
            }

            String body = m.getBody();
            if (body == null) {
                String bodyTemplate = m.getBodyRef();
                if (bodyTemplate != null) {
                    body = getMethodBody(bodyTemplate);
                }
                if (body == null) {
                    body = ("void".equals(m.getReturnType())? "{}" : "{return null;}"); //NOI18N
                }
            }
            MethodTree methodTree = maker.Method (
                    methodModifiers,
                    m.getName(), //NOI18N
                    JavaSourceHelper.createTypeTree(copy, m.getReturnType()), //NOI18N
                    Collections.<TypeParameterTree>emptyList(),
                    paramList,
                    throwsList,
                    body,
                    null); //NOI18N
            modifiedClass = maker.addClassMember(modifiedClass, methodTree);

        }
        return modifiedClass;
    }

    private static List<String> getList(String s) {
        List<String> list = new ArrayList<String>();
        StringTokenizer tokens = new StringTokenizer(s, ",");
        while (tokens.hasMoreTokens()) {
            list.add(tokens.nextToken().trim());
        }
        return list;
    }

    static String getMethodBody(String templatePath) {
        FileObject templateFo = FileUtil.getConfigFile(templatePath);
        if (templateFo != null) {
            try {
                InputStreamReader is = null;
                StringWriter writer = null;
                try {
                    is = new InputStreamReader(templateFo.getInputStream(), 
                            Charset.forName("UTF-8"));          // NOI18N
                    writer = new StringWriter();
                    char[] buffer = new char[1024];
                    int b;
                    while((b=is.read(buffer)) != -1) {
                        writer.write(buffer,0,b);
                    }
                    return writer.toString();
                } finally {
                    if (is != null) is.close();
                    if (writer != null) writer.close();
                }
            } catch(java.io.IOException ex) {
                return null;
            }
        }
        return null;
    }

    private static Set<Modifier> getModifiers(String modif) {
        Set<Modifier> modifs = new HashSet<Modifier>();
        if (modif != null) {
            if (modif.contains("public")) { //NOI18N
                modifs.add(Modifier.PUBLIC);
            } else if (modif.contains("protected")) { //NOI18N
                modifs.add(Modifier.PROTECTED);
            } else if (modif.contains("private")) { //NOI18N
                modifs.add(Modifier.PRIVATE);
            }
            if (modif.contains("static")) { //NOI18N
                modifs.add(Modifier.STATIC);
            }
            if (modif.contains("final")) { //NOI18N
                modifs.add(Modifier.FINAL);
            }
        }
        return modifs;
    }
    
    static ClassTree addSessionAuthServlets(WorkingCopy copy, ClassTree originalClass, SecurityParams securityParams, boolean annotateServlet) {
        ClassTree modifiedClass = originalClass;
        TreeMaker maker = copy.getTreeMaker();
        TypeElement servletAn = copy.getElements().getTypeElement("javax.servlet.annotation.WebServlet");
        for (ServletDescriptor classDescriptor : securityParams.getServletDescriptors()) {
            String className = classDescriptor.getClassName();
            ModifiersTree classModifiers = maker.Modifiers(getModifiers(classDescriptor.getModifiers()));
            if (annotateServlet && servletAn != null) {
                List<ExpressionTree> attrs = new ArrayList<ExpressionTree>();
                attrs.add(
                        maker.Assignment(maker.Identifier("name"), maker.Literal(className))); //NOI18N
                attrs.add(
                        maker.Assignment(maker.Identifier("urlPatterns"), maker.Literal(classDescriptor.getServletMapping()))); //NOI18N

                AnnotationTree servletAnnotation = maker.Annotation(
                        maker.QualIdent(servletAn),
                        attrs);
                classModifiers =
                    maker.addModifiersAnnotation(classModifiers, servletAnnotation);
            }
            Tree extendsTree = JavaSourceHelper.createTypeTree(copy, "javax.servlet.http.HttpServlet"); //NOI18N
            ClassTree innerClass = maker.Class (
                    classModifiers,
                    className,
                    Collections.<TypeParameterTree>emptyList(),
                    extendsTree,
                    Collections.<Tree>emptyList(),
                    Collections.<Tree>emptyList());

            ClassTree modifiedInnerClass = innerClass;
            for (MethodDescriptor m : classDescriptor.getMethodDescriptor()) {
                ModifiersTree methodModifiers = maker.Modifiers(getModifiers(m.getModifiers()));
                // add params
                List<VariableTree> paramList = new ArrayList<VariableTree>();
                String pList = m.getParamNames();
                if (pList != null) {
                    List<String> paramN = getList(pList);
                    List<String> paramT = getList(m.getParamTypes());
                    ModifiersTree paramModifier = maker.Modifiers(Collections.<Modifier>emptySet());
                    for (int i=0; i<paramN.size(); i++) {
                        Tree paramTypeTree = JavaSourceHelper.createTypeTree(copy, paramT.get(i)); //NOI18N
                        VariableTree paramTree = maker.Variable(paramModifier, paramN.get(i), paramTypeTree, null); //NOI18N
                        paramList.add(paramTree);
                    }
                }

                // add throws
                List<ExpressionTree> throwsList = new ArrayList<ExpressionTree>();
                String tList = m.getThrows();
                if (tList != null) {
                    for (String thr : getList(tList)) {
                        throwsList.add(JavaSourceHelper.createTypeTree(copy, thr));
                    }
                }

                String body = m.getBody();
                if (body == null) {
                    body = getMethodBody(m.getBodyRef());
                    if (body == null) {
                        body = ("void".equals(m.getReturnType())? "{}" : "{return null;}"); //NOI18N
                    }
                }
                MethodTree methodTree = maker.Method (
                        methodModifiers,
                        m.getName(), //NOI18N
                        JavaSourceHelper.createTypeTree(copy, m.getReturnType()), //NOI18N
                        Collections.<TypeParameterTree>emptyList(),
                        paramList,
                        throwsList,
                        body,
                        null); //NOI18N
                modifiedInnerClass = maker.addClassMember(modifiedInnerClass, methodTree);


            }
            modifiedClass = maker.addClassMember(modifiedClass, modifiedInnerClass);
        }
        return modifiedClass;
    }

}
