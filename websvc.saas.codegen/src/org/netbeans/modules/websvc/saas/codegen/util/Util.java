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
package org.netbeans.modules.websvc.saas.codegen.util;

import java.awt.Component;
import javax.swing.JLabel;
import java.awt.Container;
import java.awt.Dialog;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import javax.swing.JComponent;
import java.util.Vector;
import java.util.Iterator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.java.classpath.ClassPath;
import org.openide.util.Utilities;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import javax.swing.text.Document;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.websvc.saas.codegen.Constants;
import org.netbeans.modules.websvc.saas.codegen.Constants.MimeType;
import org.netbeans.modules.websvc.saas.codegen.Constants.SaasAuthenticationType;
import org.netbeans.modules.websvc.saas.codegen.SaasClientCodeGenerator;
import org.netbeans.modules.websvc.saas.codegen.model.GenericResourceBean;
import org.netbeans.modules.websvc.saas.codegen.model.SoapClientOperationInfo;
import org.netbeans.modules.websvc.saas.codegen.model.ParameterInfo;
import org.netbeans.modules.websvc.saas.codegen.model.ParameterInfo.ParamFilter;
import org.netbeans.modules.websvc.saas.codegen.model.ParameterInfo.ParamStyle;
import org.netbeans.modules.websvc.saas.codegen.model.SaasBean.ApiKeyAuthentication;
import org.netbeans.modules.websvc.saas.codegen.model.SaasBean.SaasAuthentication;
import org.netbeans.modules.websvc.saas.codegen.model.SaasBean.SessionKeyAuthentication;
import org.netbeans.modules.websvc.saas.codegen.model.SaasBean.SaasAuthentication.UseGenerator.Login;
import org.netbeans.modules.websvc.saas.codegen.model.SaasBean.SignedUrlAuthentication;
import org.netbeans.modules.websvc.saas.codegen.model.RestClientSaasBean;
import org.netbeans.modules.websvc.saas.util.SaasUtil;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.netbeans.modules.websvc.saas.codegen.Constants.DropFileType;
import org.netbeans.modules.websvc.saas.codegen.Constants.HttpMethodType;
import org.netbeans.modules.websvc.saas.codegen.model.SaasBean;
import org.netbeans.modules.websvc.saas.codegen.model.SaasBean.SaasAuthentication.UseGenerator;
import org.netbeans.modules.websvc.saas.codegen.model.SaasBean.SaasAuthentication.UseGenerator.Token;
import org.netbeans.modules.websvc.saas.codegen.model.SaasBean.Time;
import org.netbeans.modules.websvc.saas.codegen.ui.CodeSetupPanel;
import org.netbeans.modules.websvc.saas.codegen.ui.ProgressDialog;
import org.netbeans.modules.websvc.saas.model.wadl.Application;
import org.netbeans.modules.websvc.saas.model.wadl.Representation;
import org.netbeans.modules.websvc.saas.model.wadl.Resource;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.BindingInput;
import org.netbeans.modules.xml.wsdl.model.BindingOperation;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPBinding;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPBinding.Style;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPBody;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPMessageBase.Use;
import org.openide.DialogDescriptor;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObjectNotFoundException;

/**
 * Copy of j2ee/utilities Util class
 *  
 * TODO: Should move some of the methods into o.n.m.w.r.support.Utils class
 * since that's the package used for sharing all the utility classes.
 * 
 */
public class Util {

    public static final String TYPE_DOC_ROOT = "doc_root"; //NOI18N
    public static final String AT = "@"; //NOI18N
    public static final String APATH = AT + Constants.PATH_ANNOTATION;      //NOI18N
    public static final String AGET = AT + Constants.GET_ANNOTATION;      //NOI18N
    public static final String APOST = AT + Constants.POST_ANNOTATION;      //NOI18N
    public static final String APUT = AT + Constants.PUT_ANNOTATION;      //NOI18N
    public static final String ADELETE = AT + Constants.DELETE_ANNOTATION;      //NOI18N
    public static final String SCANNING_IN_PROGRESS = "ScanningInProgress";//NOI18N
    public static final String BUILD_XML_PATH = "build.xml"; // NOI18N
    public static final String VAR_NAMES_RESULT = "result";
    public static final String WIZARD_PANEL_CONTENT_DATA = WizardDescriptor.PROP_CONTENT_DATA; // NOI18N
    public static final String WIZARD_PANEL_CONTENT_SELECTED_INDEX = WizardDescriptor.PROP_CONTENT_SELECTED_INDEX; //NOI18N;
    
    public static boolean isJsp(Document doc) {
        if(doc == null)
            return false;
        Object mimeType = doc.getProperty("mimeType"); //NOI18N
        if (mimeType != null && "text/x-jsp".equals(mimeType)) { //NOI18N
            return true;
        }
        return false;
    }
    
    public static boolean isJava(Document doc) {
        if(doc == null)
            return false;
        Object mimeType = doc.getProperty("mimeType"); //NOI18N
        if (mimeType != null && "text/x-java".equals(mimeType)) { //NOI18N
            return true;
        }
        return false;
    }
    
    /*
     * Changes the text of a JLabel in component from oldLabel to newLabel
     */
    public static void changeLabelInComponent(JComponent component, String oldLabel, String newLabel) {
        JLabel label = findLabel(component, oldLabel);
        if (label != null) {
            label.setText(newLabel);
        }
    }

    /*
     * Hides a JLabel and the component that it is designated to labelFor, if any
     */
    public static void hideLabelAndLabelFor(JComponent component, String lab) {
        JLabel label = findLabel(component, lab);
        if (label != null) {
            label.setVisible(false);
            Component c = label.getLabelFor();
            if (c != null) {
                c.setVisible(false);
            }
        }
    }

    /*
     * Recursively gets all components in the components array and puts it in allComponents
     */
    public static void getAllComponents(Component[] components, Collection allComponents) {
        for (int i = 0; i < components.length; i++) {
            if (components[i] != null) {
                allComponents.add(components[i]);
                if (((Container) components[i]).getComponentCount() != 0) {
                    getAllComponents(((Container) components[i]).getComponents(), allComponents);
                }
            }
        }
    }

    /*
     *  Recursively finds a JLabel that has labelText in comp
     */
    public static JLabel findLabel(JComponent comp, String labelText) {
        Vector allComponents = new Vector();
        getAllComponents(comp.getComponents(), allComponents);
        Iterator iterator = allComponents.iterator();
        while (iterator.hasNext()) {
            Component c = (Component) iterator.next();
            if (c instanceof JLabel) {
                JLabel label = (JLabel) c;
                if (label.getText().equals(labelText)) {
                    return label;
                }
            }
        }
        return null;
    }

    /**
     * Returns the simple class for the passed fully-qualified class name.
     */
    public static String getClassName(String fqClassName) {
        int dot = fqClassName.lastIndexOf("."); // NOI18N
        if (dot >= 0 && dot < fqClassName.length() - 1) {
            return fqClassName.substring(dot + 1);
        } else {
            return fqClassName;
        }
    }

    /**
     * Returns the package name of the passed fully-qualified class name.
     */
    public static String getPackageName(String fqClassName) {
        int dot = fqClassName.lastIndexOf("."); // NOI18N
        if (dot >= 0 && dot < fqClassName.length() - 1) {
            return fqClassName.substring(0, dot);
        } else {
            return ""; // NOI18N
        }
    }

    public static String lowerFirstChar(String name) {
        if (name.length() == 0) {
            return name;
        }
        StringBuilder sb = new StringBuilder(name);
        sb.setCharAt(0, Character.toLowerCase(name.charAt(0)));
        return sb.toString();
    }

    public static String upperFirstChar(String name) {
        if (name.length() == 0) {
            return name;
        }
        StringBuilder sb = new StringBuilder(name);
        sb.setCharAt(0, Character.toUpperCase(name.charAt(0)));
        return sb.toString();
    }

    public static String deriveResourceClassName(String resourceName) {
        return upperFirstChar(resourceName) + SaasClientCodeGenerator.RESOURCE_SUFFIX;
    }

    public static String deriveUri(String resourceName, String currentUri) {
        if (resourceName.length() == 0 || currentUri == null || currentUri.length() == 0 || currentUri.charAt(0) != '/') {
            return currentUri;
        }
        resourceName = lowerFirstChar(resourceName);
        resourceName = pluralize(resourceName);
        String root = currentUri;
        String params = null;
        int lastIndex = currentUri.indexOf('{');
        if (lastIndex > -1) {
            params = root.substring(lastIndex - 1);
            root = root.substring(0, lastIndex - 1); /* ../{id} we are excluding the ending '/' */
            if (root.length() == 0) {
                return currentUri;
            }
        }


        lastIndex = root.lastIndexOf('/');
        if (lastIndex == -1) {
            return currentUri;
        }

        root = root.substring(0, lastIndex);
        String ret = root + "/" + resourceName;
        if (params != null) {
            ret += params;
        }
        return ret;
    }

    public static String deriveContainerClassName(String resourceName) {
        return deriveResourceClassName(Inflector.getInstance().pluralize((resourceName)));
    }

    public static String singularize(String name) {
        // get around inflector bug:  'address' -> 'addres'
        if (name.endsWith("ss")) {
            String plural = Inflector.getInstance().pluralize(name);
            if (!name.equals(plural)) {
                return name;
            }
        }
        return Inflector.getInstance().singularize(name);
    }

    public static String pluralize(String name) {
        return Inflector.getInstance().pluralize(singularize(name));
    }
    
    public static String camelize(String name, boolean lower) {
        return Inflector.getInstance().camelize(name.replace('-', '_'), lower);
    }
 
    public static String[] ensureTypes(String[] types) {
        if (types == null || types.length == 0 || types[0].length() == 0) {
            types = new String[]{String.class.getName()                    };
        }
        return types;
    }

    private static Map<String, Class> primitiveTypes;
    private static Map<String, Class> primitiveClassTypes;
    
    private static HashSet<String> keywords;

    public static Class getType(Project project, String typeName) {
        List<ClassPath> classPaths = new ArrayList<ClassPath>();
                
        //hack for PHP
        if(classPaths.size() == 0){
            try {
                Class ret = getPrimitiveClassType(typeName);
                if(ret != null){
                    return ret;
                }
                return Class.forName(typeName);
            } catch (ClassNotFoundException ex) {
                return java.lang.Object.class;
            }
        }

        for (ClassPath cp : classPaths) {
            try {
                Class ret = Util.getPrimitiveType(typeName);
                if (ret != null) {
                    return ret;
                }
                ClassLoader cl = cp.getClassLoader(true);
                ret = getGenericRawType(typeName, cl);
                if (ret != null) {
                    return ret;
                }
                if (cl != null) {
                    return cl.loadClass(typeName);
                }
            } catch (ClassNotFoundException ex) {
                //Logger.global.log(Level.INFO, ex.getLocalizedMessage(), ex);
            }
        }
        return null;
    }
    
    
    public static Class getPrimitiveClassType(String type) {
        if (primitiveClassTypes == null) {
            primitiveClassTypes = new HashMap<String, Class>();
            primitiveClassTypes.put("int", Integer.TYPE);
            primitiveClassTypes.put("boolean", Boolean.TYPE);
            primitiveClassTypes.put("byte", Byte.TYPE);
            primitiveClassTypes.put("char", Character.TYPE);
            primitiveClassTypes.put("double", Double.TYPE);
            primitiveClassTypes.put("float", Float.TYPE);
            primitiveClassTypes.put("long", Long.TYPE);
            primitiveClassTypes.put("short", Short.TYPE);
        }
        return primitiveClassTypes.get(type);
    }

    public static Class getPrimitiveType(String typeName) {
        if (primitiveTypes == null) {
            primitiveTypes = new HashMap<String, Class>();
            primitiveTypes.put("int", Integer.TYPE);
            primitiveTypes.put("int[]", int[].class);
            primitiveTypes.put("boolean", Boolean.TYPE);
            primitiveTypes.put("boolean[]", boolean[].class);
            primitiveTypes.put("byte", Byte.TYPE);
            primitiveTypes.put("byte[]", byte[].class);
            primitiveTypes.put("char", Character.TYPE);
            primitiveTypes.put("char[]", char[].class);
            primitiveTypes.put("double", Double.TYPE);
            primitiveTypes.put("double[]", double[].class);
            primitiveTypes.put("float", Float.TYPE);
            primitiveTypes.put("float[]", float[].class);
            primitiveTypes.put("long", Long.TYPE);
            primitiveTypes.put("long[]", long[].class);
            primitiveTypes.put("short", Short.TYPE);
            primitiveTypes.put("short[]", short[].class);
        }
        return primitiveTypes.get(typeName);
    }

    public static boolean isKeyword(String name) {
        if (keywords == null) {
            keywords = new HashSet<String>();

            keywords.add("abstract");
            keywords.add("assert");
            keywords.add("boolean");
            keywords.add("break");
            keywords.add("byte");
            keywords.add("case");
            keywords.add("catch");
            keywords.add("char");
            keywords.add("class");
            keywords.add("const");
            keywords.add("continue");
            keywords.add("default");
            keywords.add("do");
            keywords.add("double");
            keywords.add("else");
            keywords.add("enum");
            keywords.add("extends");
            keywords.add("final");
            keywords.add("finally");
            keywords.add("float");
            keywords.add("for");
            keywords.add("goto");
            keywords.add("if");
            keywords.add("implements");
            keywords.add("import");
            keywords.add("instanceof");
            keywords.add("int");
            keywords.add("interface");
            keywords.add("long");
            keywords.add("native");
            keywords.add("new");
            keywords.add("package");
            keywords.add("private");
            keywords.add("protected");
            keywords.add("public");
            keywords.add("return");
            keywords.add("short");
            keywords.add("static");
            keywords.add("strictfp");
            keywords.add("super");
            keywords.add("switch");
            keywords.add("synchronized");
            keywords.add("this");
            keywords.add("throw");
            keywords.add("throws");
            keywords.add("transient");
            keywords.add("try");
            keywords.add("void");
            keywords.add("volatile");
            keywords.add("while");
        }

        return keywords.contains(name);
    }

    public static Class getGenericRawType(String typeName, ClassLoader loader) {
        int i = typeName.indexOf('<');
        if (i < 1) {
            return null;
        }
        String raw = typeName.substring(0, i);
        try {
            return loader.loadClass(raw);
        } catch (ClassNotFoundException ex) {
            Logger.global.log(Level.INFO, "", ex);
            return null;
        }
    }

    public static boolean isValidPackageName(String packageName) {
        if (packageName == null || packageName.endsWith(".")) {
            return false;
        }

        String[] segments = packageName.split("\\.");
        for (String s : segments) {
            if (!Utilities.isJavaIdentifier(s)) {
                return false;
            }
        }
        return true;
    }

    public static String stripPackageName(String name) {
        int index = name.lastIndexOf(".");          //NOI18N

        if (index > 0) {
            return name.substring(index + 1);
        }
        return name;
    }

    public static Collection<String> sortKeys(Collection<String> keys) {
        Collection<String> sortedKeys = new TreeSet<String>(
                new Comparator<String>() {

                    public int compare(String str1, String str2) {
                        return str1.compareTo(str2);
                    }
                });

        sortedKeys.addAll(keys);
        return sortedKeys;
    }

    public static Method getValueOfMethod(Class type) {
        try {
            Method method = type.getDeclaredMethod("valueOf", String.class);
            if (method == null || !Modifier.isStatic(method.getModifiers())) {
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
            if (p != null) {
                result.add(p);
            }
        }
        Project[] projectsArray = result.toArray(new Project[result.size()]);
        return projectsArray;
    }

    public static FileObject findBuildXml(Project project) {
        return project.getProjectDirectory().getFileObject(BUILD_XML_PATH);
    }
    
    public static DataObject createDataObjectFromTemplate(String template,
            FileObject targetFolder, String targetName)  throws IOException 
    {
        return createDataObjectFromTemplate(template, targetFolder, targetName, null);
    }

    public static DataObject createDataObjectFromTemplate(String template,
            FileObject targetFolder, String targetName, Map<String,Object> map) 
                    throws IOException 
    {
        assert template != null;
        assert targetFolder != null;

        FileObject templateFO = FileUtil.getConfigFile(template);
        DataObject templateDO = DataObject.find(templateFO);
        DataFolder dataFolder = DataFolder.findFolder(targetFolder);

        //Check if already exists
        String fileName = targetName;
        if (fileName == null) {
            fileName = templateFO.getName();
        }
        FileObject targetFO = targetFolder.getFileObject(fileName, templateFO.getExt());
        if (targetFO != null) {
            return DataFolder.find(targetFO);
        }

        if ( map == null ){
            return templateDO.createFromTemplate(dataFolder, targetName);
        }
        else {
            return templateDO.createFromTemplate(dataFolder, targetName, map);
        }
    }

    public static String deriveResourceName(final String name) {
        String resourceName = Inflector.getInstance().camelize(normailizeName(name) + GenericResourceBean.RESOURCE_SUFFIX);
        return resourceName.substring(0, 1).toUpperCase() + resourceName.substring(1);
    }

    public static String deriveMethodName(final String name) {
        return Inflector.getInstance().camelize(normailizeName(name), true);
    }

    public static String deriveUriTemplate(final String name) {
        return Inflector.getInstance().camelize(normailizeName(name), true) + "/"; //NOI18N
    }

    public static MimeType[] deriveMimeTypes(SoapClientOperationInfo[] operations) {
        if (String.class.getName().equals(operations[operations.length - 1].getOperation().getReturnTypeName())) {
            return new MimeType[]{MimeType.HTML                    };
        } else {
            return new MimeType[]{MimeType.XML                    };//TODO  MimeType.JSON };
        }
    }

    public static String normailizeName(final String name) {
        //String normalized = name;
        //normalized = normalized.replaceAll("\\p{Punct}", "_");
        //normalized = normalized.replaceAll("\\p{Space}", "_");
        //return normalized;
        return SaasUtil.toValidJavaName(name);
    }

    public static List<ParameterInfo> filterParametersByAuth(SaasAuthenticationType authType,
            SaasAuthentication auth, List<ParameterInfo> params) {
        List<ParameterInfo> filterParams = new ArrayList<ParameterInfo>();
        if (params != null) {
            for (ParameterInfo param : params) {
                if (authType == SaasAuthenticationType.API_KEY) {
                    ApiKeyAuthentication apiKey = (ApiKeyAuthentication) auth;
                    if (param.getName().equals(apiKey.getApiKeyName())) {
                        continue;
                    }
                } else if (authType == SaasAuthenticationType.SESSION_KEY) {
                    SessionKeyAuthentication sessionKey = (SessionKeyAuthentication) auth;
                    if (param.getName().equals(sessionKey.getApiKeyName()) ||
                            param.getName().equals(sessionKey.getSessionKeyName()) ||
                            param.getName().equals(sessionKey.getSigKeyName())) {
                        continue;
                    }
                } else if (authType == SaasAuthenticationType.SIGNED_URL) {
                    SignedUrlAuthentication signedUrl = (SignedUrlAuthentication) auth;
                    if (param.getName().equals(signedUrl.getSigKeyName())) {
                        continue;
                    }
                }
                filterParams.add(param);
            }
        }
        return filterParams;
    }

    public static List<ParameterInfo> filterParameters(List<ParameterInfo> params, ParamFilter[] filters) {
        List<ParameterInfo> filterParams = new ArrayList<ParameterInfo>();
        if (params != null) {
            for (ParameterInfo param : params) {
                for (ParamFilter filter : filters) {
                    if (filter == ParamFilter.FIXED && param.getFixed() != null) {
                        continue;
                    }
                    filterParams.add(param);
                }
            }
        }
        return filterParams;
    }

    public static ParameterInfo findParameter(List<ParameterInfo> params, String paramName) {
        for (ParameterInfo p : params) {
            if (p.getName().equals(paramName)) {
                return p;
            }
        }

        return null;
    }

    public static String createSessionKeyLoginBodyForWeb(SaasBean bean,
            String groupName, String paramVariableName) throws IOException {
        String methodBody = "";
        if (bean.getAuthenticationType() != SaasAuthenticationType.SESSION_KEY) {
            return null;
        }
        SessionKeyAuthentication sessionKey = (SessionKeyAuthentication) bean.getAuthentication();
        UseGenerator useGenerator = sessionKey.getUseGenerator();
        if (useGenerator != null) {
            Login login = useGenerator.getLogin();
            if (login != null) {
                String methodName = null;
                SessionKeyAuthentication.UseGenerator.Method method = login.getMethod();
                if (method != null) {
                    methodName = method.getHref();
                    if (methodName == null) {
                        return methodBody;
                    } else {
                        methodName = methodName.startsWith("#") ? methodName.substring(1) : methodName;
                    }
                }
                String tokenName = getTokenName(useGenerator);
                String tokenMethodName = getTokenMethodName(useGenerator);
                methodBody += "    try {\n";
                methodBody += "        javax.servlet.http.HttpSession session = request.getSession(true);\n";
                methodBody += "        if (" + getVariableName(sessionKey.getSessionKeyName()) + " != null) \n";
                methodBody += "            return;\n";
                methodBody += "        String " + tokenName + " = " + tokenMethodName + "(" + getLoginArgumentsForWeb() + ");\n";

                methodBody += "        if (" + tokenName + " != null) {\n";

                methodBody += "           session.removeAttribute(\"" + groupName + "_auth_token\");\n";
                Map<String, String> tokenMap = new HashMap<String, String>();
                methodBody += getLoginBody(login, bean, groupName, tokenMap);
                for (Entry e : tokenMap.entrySet()) {
                    String name = (String) e.getKey();
                    String val = (String) e.getValue();
                    methodBody += "              session.setAttribute(\"" + groupName + "_" + val + "\", " + name + ");\n";
                }

                methodBody += "              String returnUrl = (String) session.getAttribute(\"" + groupName + "_return_url\");\n";

                methodBody += "              if (returnUrl != null) {\n";
                methodBody += "                session.removeAttribute(\"" + groupName + "_return_url\");\n";
                methodBody += "                response.sendRedirect(returnUrl);\n";
                methodBody += "              }\n";
                methodBody += "            } else {\n";
                methodBody += "                session.setAttribute(\"" + groupName + "_return_url\", request.getRequestURI());\n";
                methodBody += "                response.sendRedirect(\"" + groupName + "Login\");\n";
                methodBody += "            }\n";
                methodBody += "        } catch (IOException ex) {\n";
                methodBody += "            Logger.getLogger(" + groupName + Constants.SERVICE_AUTHENTICATOR + ".class.getName()).log(Level.SEVERE, null, ex);\n";
                methodBody += "        }\n\n";
            }
        }
        return methodBody;
    }

    public static String getLoginBody(Login login, SaasBean bean, String groupName,
            Map<String, String> tokenMap) throws IOException {
        SessionKeyAuthentication.UseGenerator.Method method = login.getMethod();
        String methodName = null;
        if (method != null) {
            methodName = method.getHref();
            if (methodName == null) {
                return "";
            } else {
                methodName = methodName.startsWith("#") ? methodName.substring(1) : methodName;
            }
        }
        String methodBody = "";
        methodBody += "                    String method = \"" + methodName + "\";\n";
        methodBody += "                    String v = \"1.0\";\n\n";
        String sigId = "sig";
        if (login.getSignId() != null) {
            sigId = login.getSignId();
        }
        List<ParameterInfo> signParams = login.getParameters();
        if (signParams != null && signParams.size() > 0) {
            String paramStr = "";
            paramStr += "        String " + sigId + " = sign(secret, \n";
            paramStr += getSignParamUsage(signParams, groupName, bean.isDropTargetWeb());
            paramStr += ");\n\n";
            methodBody += paramStr;
        }

        String queryParamsCode = "";
        if (method != null) {
            String id = method.getId();
            if (id != null) {
                String[] tokens = id.split(",");
                for (String token : tokens) {
                    String[] tokenElem = token.split("=");
                    if (tokenElem.length == 2) {
                        String val = tokenElem[1];
                        if (val.startsWith("{")) {
                            val = val.substring(1);
                        }
                        if (val.endsWith("{")) {
                            val = val.substring(val.length() - 1);
                        }
                        tokenMap.put(getVariableName(tokenElem[0]), val);
                    }
                }
            }
            String href = method.getHref();
            if (href != null && bean instanceof RestClientSaasBean) {
                Application app = ((RestClientSaasBean)bean).getMethod().getSaas().getWadlModel();
                org.netbeans.modules.websvc.saas.model.wadl.Method wadlMethod =
                        SaasUtil.wadlMethodFromIdRef(app, href);
                if (wadlMethod != null) {
                    ArrayList<ParameterInfo> params = ((RestClientSaasBean)bean).findWadlParams(wadlMethod);
                    Resource parentResource = SaasUtil.getParentResource(app, wadlMethod);
                    if (parentResource != null) {
                        ((RestClientSaasBean)bean).findWadlParams(params, parentResource.getParam());
                    }
                    if (params != null &&
                            params.size() > 0) {
                        queryParamsCode = Util.getHeaderOrParameterDefinition(params, Constants.QUERY_PARAMS, false);
                    }
                }
            }
        }

        //Insert parameter declaration
        methodBody += "        " + queryParamsCode;

        String url = "";
        if (bean instanceof RestClientSaasBean) {
            url = ((RestClientSaasBean)bean).getUrl();
        }
        methodBody += "             " + Constants.REST_CONNECTION + " conn = new " + Constants.REST_CONNECTION + "(\"" + url + "\"";
        if (!queryParamsCode.trim().equals("")) {
            methodBody += ", " + Constants.QUERY_PARAMS;
        }
        methodBody += ");\n";

        methodBody += "                    String result = conn.get();\n";

        for (Entry e : tokenMap.entrySet()) {
            String name = (String) e.getKey();
            String val = (String) e.getValue();
            methodBody += "                    " + name + " = result.substring(result.indexOf(\"<" + val + ">\") + 13,\n";
            methodBody += "                            result.indexOf(\"</" + val + ">\"));\n\n";
        }
        return methodBody;
    }

    /*
     */
    public static String getSignParamUsage(List<ParameterInfo> signParams,
            String groupName, boolean isDropTargetWeb) {
        String paramStr = "                new String[][] {\n";
        for (ParameterInfo p : signParams) {
            String name = p.getName();
            String varName = getVariableName(name);
            String[] pIds = getParamIds(p, groupName, isDropTargetWeb);
            if (pIds != null) {//process special case
                varName = pIds[1];
            }
            paramStr += "                    {\"" + name + "\", " + varName + "},\n";
        }
        paramStr += "        }\n";
        return paramStr;
    }

    public static String[] getParamIds(ParameterInfo p, String groupName,
            boolean isDropTargetWeb) {
        if (p.getId() != null) {//process special case
            String[] pElems = p.getId().split("=");
            if (pElems.length == 2) {
                String val = pElems[1];
                if (val.startsWith("{")) {
                    val = val.substring(1);
                }
                if (val.endsWith("}")) {
                    val = val.substring(0, val.length() - 1);
                }
                val = getVariableName(val);
                val = getAuthenticatorClassName(groupName) + "." +
                        "get" + val.substring(0, 1).toUpperCase() + val.substring(1);
                if (isDropTargetWeb) {
                    val += "(request, response)";
                } else {
                    val += "()";
                }
                return new String[]{pElems[0], val};
            }
        }
        return null;
    }

    public static String getAuthenticatorClassName(String groupName) {
        return groupName + Constants.SERVICE_AUTHENTICATOR;
    }

    public static String getAuthorizationFrameClassName(String groupName) {
        return groupName + Constants.SERVICE_AUTHORIZATION_FRAME;
    }

    public static String createSessionKeyTokenBodyForWeb(SaasBean bean,
            String groupName, String paramVariableName, String saasServicePackageName) throws IOException {
        String methodBody = "";
        methodBody += "        javax.servlet.http.HttpSession session = request.getSession(true);\n";
        methodBody += "        return (String) session.getAttribute(\"" + groupName + "_auth_token\");\n";
        return methodBody;
    }

    public static String getTokenMethodName(SessionKeyAuthentication.UseGenerator useGenerator) {
        String methodName = getTokenName(useGenerator);
        return methodName = "get" + methodName.substring(0, 1).toUpperCase() + methodName.substring(1);
    }

    public static String getTokenName(SessionKeyAuthentication.UseGenerator useGenerator) {
        String methodName = "token";
        Token token = useGenerator.getToken();
        if (token != null && token.getId() != null) {
            methodName = token.getId();
        }
        return getVariableName(methodName);
    }

    public static String getHeaderOrParameterUsage(List<ParameterInfo> params) {
        String paramUsage = "";
        for (ParameterInfo param : params) {
            String name = getParameterName(param, true, true, true);
            paramUsage += name + ", ";
        }
        if (params.size() > 0) {
            paramUsage = paramUsage.substring(0, paramUsage.length() - 2);
        }
        return paramUsage;
    }

    public static String getHeaderOrParameterDefinition(List<ParameterInfo> params, String varName, boolean evaluate, HttpMethodType httpMethod) {
        String part = getHeaderOrParameterDefinitionPart(params, evaluate);
        if (httpMethod == HttpMethodType.PUT ||
                httpMethod == HttpMethodType.POST) {
            if (!Util.isContains(params, new ParameterInfo(Constants.CONTENT_TYPE, String.class))) {
                part += ", {\"" + Constants.CONTENT_TYPE + "\", " + getVariableName(Constants.CONTENT_TYPE) + "}";
            }
        }
        String paramCode = "";
        paramCode += "             String[][] " + varName + " = new String[][]{\n";
        paramCode += "                 " + part + "\n";
        paramCode += "             };\n";
        return paramCode;
    }

    public static String getHeaderOrParameterDefinition(List<ParameterInfo> params, String varName, boolean evaluate) {
        String paramCode = "";
        paramCode += "             String[][] " + varName + " = new String[][]{\n";
        paramCode += "                 " + getHeaderOrParameterDefinitionPart(params, evaluate) + "\n";
        paramCode += "             };\n";
        return paramCode;
    }

    public static String getHeaderOrParameterDefinitionPart(List<ParameterInfo> params, boolean evaluate) {
        String paramsStr = null;
        StringBuffer sb = new StringBuffer();
        for (ParameterInfo param : params) {
            String paramName = getParameterName(param);
            String paramVal = null;
            if (evaluate || param.isApiKey()) {
                paramVal = findParamValue(param);
                if (param.getType() != String.class) {
                    sb.append("{\"" + paramName + "\", " + paramVal + ".toString()},\n");
                } else {
                    if (paramVal != null) {
                        sb.append("{\"" + paramName + "\", \"" + paramVal + "\"},\n");
                    } else {
                        sb.append("{\"" + paramName + "\", null},\n");
                    }
                }
            } else {
                if (param.getType() != String.class) {
                    sb.append("{\"" + paramName + "\", " + getVariableName(param.getName()) + ".toString()},\n");
                } else {
                    sb.append("{\"" + paramName + "\", " + getVariableName(param.getName()) + "},\n");
                }
            }
        }
        paramsStr = sb.toString();
        if (params.size() > 0) {
            paramsStr = paramsStr.substring(0, paramsStr.length() - 2);
        }
        return paramsStr;
    }

    public static String getParameterName(ParameterInfo param) {
        return param.getName();
    }

    public static String getParameterName(ParameterInfo param,
            boolean camelize, boolean normalize) {
        return getParameterName(param, camelize, normalize, false);
    }

    public static String getParameterName(ParameterInfo param,
            boolean camelize, boolean normalize, boolean trimBraces) {
        String name = param.getName();
        if (Util.isKeyword(name)) {
            name += "Param";
        }

        if (trimBraces && param.getStyle() == ParamStyle.TEMPLATE && name.startsWith("{") && name.endsWith("}")) {
            name = name.substring(0, name.length() - 1);
        }
        return getParameterName(name, camelize, normalize);
    }

    public static String getParameterName(String name,
            boolean camelize, boolean normalize) {
        if (normalize) {
            name = Util.normailizeName(name);
        }
        if (camelize) {
            name = Inflector.getInstance().camelize(name, true);
        }
        return name;
    }

    public static String getVariableName(String name) {
        return getVariableName(name, true, true, true);
    }

    public static String getVariableName(final String name,
            boolean camelize, boolean normalize, boolean trimBraces) {
        String varName = name;
        if (trimBraces && varName.startsWith("{") && varName.endsWith("}")) {
            varName = varName.substring(0, varName.length() - 1);
        }
        if (normalize) {
            varName = Util.normailizeName(varName);
        }
        if (camelize) {
            varName = Inflector.getInstance().camelize(varName, true);
        }
        if (Util.isKeyword(varName)) {
            varName += "Param";
        }
        return varName;
    }

    public static String findParamValue(ParameterInfo param) {
        String paramVal = null;
        if (param.isApiKey()) {
            paramVal = "\"+apiKey+\"";
        } else if (param.getStyle() == ParamStyle.TEMPLATE) {
            if (param.getDefaultValue() != null) {
                paramVal = param.getDefaultValue().toString();
            } else {
                paramVal = "";
            }
        } else if (param.getStyle() == ParamStyle.HEADER) {
            if (param.isFixed()) {
                paramVal = param.getFixed();
            } else if (param.getDefaultValue() != null) {
                paramVal = param.getDefaultValue().toString();
            } else {
                paramVal = "";
            }
        } else {
            if (param.isFixed()) {
                paramVal = param.getFixed();
            } else {
                if (param.isRequired()) {
                    paramVal = "";
                }
                if (param.getDefaultValue() != null) {
                    paramVal = param.getDefaultValue().toString();
                }
            }
        }
        return paramVal;
    }

    public static String getServletLoginBody(SaasBean bean,
            String groupName) throws IOException {
        String methodBody = "";
        SessionKeyAuthentication sessionKey = (SessionKeyAuthentication) bean.getAuthentication();
        UseGenerator useGenerator = sessionKey.getUseGenerator();
        if (useGenerator != null) {
            SessionKeyAuthentication.UseGenerator.Token token = useGenerator.getToken();
            SessionKeyAuthentication.UseGenerator.Token.Prompt prompt = token.getPrompt();
            String url = prompt.getWebUrl();
            String tokenName = "authToken";
            String tokenId = "auth_token";
            if (token != null) {
                tokenName = Util.getTokenName(useGenerator);
                tokenId = token.getId() != null ? token.getId() : tokenId;
            }
            methodBody += "        response.setContentType(\"text/html;charset=UTF-8\");\n";
            methodBody += "        PrintWriter out = response.getWriter();\n";
            methodBody += "        try {\n";
            methodBody += "            out.println(\"<html>\");\n";
            methodBody += "            out.println(\"<head>\");\n";
            methodBody += "            out.println(\"<title>Servlet " + groupName + "Login</title>\");\n";
            methodBody += "            out.println(\"</head>\");\n";
            methodBody += "            out.println(\"<body>\");\n";
            methodBody += "            out.println(\"<h1>Servlet " + groupName + "Login at \" + request.getContextPath() + \"</h1>\");\n";

            methodBody += "            HttpSession session = request.getSession(true);\n";

            methodBody += "            String " + tokenName + " = (String) session.getAttribute(\"" + groupName + "_" + tokenId + "\");\n";

            methodBody += "            if (" + tokenName + " != null) {\n";
            methodBody += "                out.println(\"<p>Already logged in.</b>\");\n";
            methodBody += "            } else {\n";
            String apiKeyName = getVariableName(sessionKey.getApiKeyName());
            methodBody += "                String apiKey = " + groupName +
                    Constants.SERVICE_AUTHENTICATOR + ".get" +
                    apiKeyName.substring(0, 1).toUpperCase() + apiKeyName.substring(1) + "();\n";
            methodBody += "                String loginUrl = \"<a href=" + Util.getTokenPromptUrl(token, url) + ">" + groupName + " Login</a>\";\n";
            methodBody += "                out.println(loginUrl);\n";
            methodBody += "            }\n";
            methodBody += "            out.println(\"</body>\");\n";
            methodBody += "            out.println(\"</html>\");\n";
            methodBody += "        } finally {\n";
            methodBody += "            out.close();\n";
            methodBody += "        }\n";
        }
        return methodBody;
    }

    public static String getServletCallbackBody(SaasBean bean,
            String groupName) throws IOException {
        String methodBody = "";
        SessionKeyAuthentication sessionKey = (SessionKeyAuthentication) bean.getAuthentication();
        String sessionKeyName = getVariableName(sessionKey.getSessionKeyName());
        String tokenName = "authToken";
        String tokenId = "auth_token";
        UseGenerator useGenerator = sessionKey.getUseGenerator();
        if (useGenerator != null) {
            Token token = useGenerator.getToken();
            if (token != null) {
                tokenName = Util.getTokenName(useGenerator);
                tokenId = token.getId() != null ? token.getId() : tokenId;
            }
            String name = Util.getParameterName(sessionKey.getSessionKeyName(), true, true);
            methodBody += "        response.setContentType(\"text/html;charset=UTF-8\");\n";
            methodBody += "        PrintWriter out = response.getWriter();\n";
            methodBody += "        try {\n";
            methodBody += "            HttpSession session = request.getSession(true);\n";
            methodBody += "            String " + tokenName + " = request.getParameter(\"" + tokenId + "\");\n";
            methodBody += "            session.setAttribute(\"" + groupName + "_" + tokenId + "\", " + tokenName + ");\n";

            methodBody += "            " + groupName + Constants.SERVICE_AUTHENTICATOR + ".login(" + getLoginArgumentsForWeb() + ");\n";
            methodBody += "            String " + sessionKeyName + " = " + groupName + Constants.SERVICE_AUTHENTICATOR + "." + Util.getSessionKeyMethodName(name) + "();\n";

            methodBody += "            out.println(\"<html>\");\n";
            methodBody += "            out.println(\"<head>\");\n";
            methodBody += "            out.println(\"<title>Servlet " + groupName + "Callback</title>\");\n";
            methodBody += "            out.println(\"</head>\");\n";
            methodBody += "            out.println(\"<body>\");\n";
            methodBody += "            out.println(\"<h1>Servlet " + groupName + "Callback at \" + request.getContextPath() + \"</h1>\");\n";
            methodBody += "            out.println(\"<p> Your Session Key is \" + " + sessionKeyName + " + \"</p>\");\n";
            methodBody += "            out.println(\"</body>\");\n";
            methodBody += "            out.println(\"</html>\");\n";

            methodBody += "        } finally {\n";
            methodBody += "            out.close();\n";
            methodBody += "        }\n";
        }
        return methodBody;
    }

    public static String getSessionKeyMethodName(String name) {
        String methodName = getVariableName(name);
        methodName = "get" + methodName.substring(0, 1).toUpperCase() + methodName.substring(1);
        return methodName;
    }

    public static String getTokenPromptUrl(SessionKeyAuthentication.UseGenerator.Token token,
            String url) {
        String loginUrl = "";
        if (token.getPrompt() != null) {
            int index = url.indexOf("?");
            loginUrl = url.substring(0, index + 1);
            String params = url.substring(index + 1);
            String[] tokens = params.split("&");
            for (String tokenE : tokens) {
                String[] tokenElem = tokenE.split("=");
                if (tokenElem.length == 2) {
                    String paramVal = tokenElem[1];
                    if (paramVal.startsWith("{")) {
                        paramVal = paramVal.substring(1);
                    }
                    if (paramVal.endsWith("}")) {
                        paramVal = paramVal.substring(0, paramVal.length() - 1);
                    }
                    if (paramVal.indexOf(":") != -1) {
                        loginUrl += tokenElem[0] + "=" + getVariableName(paramVal.substring(paramVal.indexOf(":") + 1)) + "&";
                    } else {
                        loginUrl += tokenElem[0] + "=\"+" + getVariableName(paramVal) + "+\"&";
                    }

                }
            }
            if (loginUrl.endsWith("+\"&")) {
                loginUrl = loginUrl.substring(0, loginUrl.length() - 3);
            } else if (loginUrl.endsWith("&")) {
                loginUrl = loginUrl.substring(0, loginUrl.length() - 1);
            }
        }
        return loginUrl;
    }

    public static List<ParameterInfo> getAuthenticatorMethodParametersForWeb() {
        List<ParameterInfo> params = new ArrayList<ParameterInfo>();
        params.add(new ParameterInfo(Constants.HTTP_SERVLET_REQUEST_VARIABLE, Object.class,
                Constants.HTTP_SERVLET_REQUEST_CLASS));
        params.add(new ParameterInfo(Constants.HTTP_SERVLET_RESPONSE_VARIABLE, Object.class,
                Constants.HTTP_SERVLET_RESPONSE_CLASS));
        return params;
    }

    public static List<ParameterInfo> getServiceMethodParametersForWeb(RestClientSaasBean bean) {
        List<ParameterInfo> params = new ArrayList<ParameterInfo>();
        params.addAll(getAuthenticatorMethodParametersForWeb());
        params.addAll(bean.filterParametersByAuth(bean.filterParameters(
                new ParamFilter[]{ParamFilter.FIXED})));
        return params;
    }

    public static String getLoginArgumentsForWeb() {
        return getHeaderOrParameterUsage(getAuthenticatorMethodParametersForWeb());
    }

    public static List<String> getJaxBClassImports() {
        List<String> imports = new ArrayList<String>();
        ClassLoader orig = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(Util.class.getClassLoader());
        try {
            imports.add(JAXBContext.class.getName());
            imports.add(Unmarshaller.class.getName());
            imports.add(StreamSource.class.getName());
            imports.add(StringReader.class.getName());
        } finally {
          Thread.currentThread().setContextClassLoader(orig);
        }
        return imports;
    }

    public static boolean isContains(List<ParameterInfo> params, ParameterInfo pInfo) {
        boolean found = false;
        String name = getVariableName(pInfo.getName());
        for (ParameterInfo p : params) {
            if (getVariableName(p.getName()).equals(name)) {
                found = true;
                break;
            }
        }
        return found;
    }

    public static void showUnsupportedDropMessage(Object[] args) {
        String message = NbBundle.getMessage(CodeSetupPanel.class,
                "WARN_UnsupportedDropTarget", args); // NOI18N
        NotifyDescriptor desc = new NotifyDescriptor.Message(message,
                NotifyDescriptor.Message.WARNING_MESSAGE);
        DialogDisplayer.getDefault().notify(desc);
    }
    
    public static String createPrintStatement(List<String> pkgs, List<String> typeNames,
            DropFileType dropFileType, HttpMethodType methodType,
            boolean canGenerateJaxb, String indent) {
        return createPrintStatement(pkgs, typeNames, dropFileType, methodType,
            canGenerateJaxb, VAR_NAMES_RESULT, indent);
    }
    
    public static String createPrintStatement(List<String> pkgs, List<String> typeNames,
            DropFileType dropFileType, HttpMethodType methodType,
            boolean canGenerateJaxb, String resultVarName, String indent) {
        String methodBody = "";
        String commentStr = "//";
        if (canGenerateJaxb) {
            for(int i=0;i<typeNames.size();i++) {
                String pkg = pkgs.get(i);
                String typeName = typeNames.get(i);
                if(i>0) {
                    methodBody += "else ";
                }
                if (!isPrimitive(typeName)) {
                    String resultClass = pkg + "." + Util.camelize(typeName, false);
                    String lft = resultClass;
                    String rht = resultVarName+".getDataAsObject(" + resultClass + ".class)";
                    methodBody += indent + "if(" + rht + " instanceof " + lft + ") {\n";
                    methodBody += indent + "    " + lft + " "+resultVarName+"Obj = " + rht + ";\n}\n";
                } else {
                    String resultClass = Util.camelize(typeName, false);
                    String lft = resultClass;
                    String rht = resultVarName+".getDataAsObject(" + resultClass + ".class, \"java.lang\")";
                    methodBody += indent + "if(" + rht + " instanceof " + lft + ") {\n";
                    methodBody += indent + resultClass + " "+resultVarName+"Obj = " + rht +";\n}\n";
                }
            }
        }
        methodBody += indent + "//TODO - Uncomment the print Statement below to print result.\n";
        methodBody += indent + commentStr + dropFileType.getPrintWriterType() +
                ".println(\"The SaasService returned: \"+"+resultVarName+".getDataAsString());\n";

        return methodBody;
    }

    private static boolean isPrimitive(String typeName) {
        return typeName.equals("integer") || typeName.equals("string") || typeName.equals("boolean") ||
                typeName.equals("float") || typeName.equals("long");  //NOI18N
    }

    public static String[] getParamNames(List<ParameterInfo> params) {
        List<String> results = new ArrayList<String>();

        for (ParameterInfo param : params) {
            results.add(getParameterName(param, true, true, true));
        }

        return results.toArray(new String[results.size()]);
    }

    public static String[] getParamTypeNames(List<ParameterInfo> params) {
        List<String> results = new ArrayList<String>();

        for (ParameterInfo param : params) {
            results.add(param.getTypeName());
        }

        return results.toArray(new String[results.size()]);
    }

    public static Object[] getParamValues(List<ParameterInfo> params) {
        List<Object> results = new ArrayList<Object>();

        for (ParameterInfo param : params) {
            results.add(getParamValue(param));
        }

        return results.toArray(new Object[results.size()]);
    }

    public static Object getParamValue(ParameterInfo p) {
        Object defaultValue = null;
        if (p.getStyle() != ParamStyle.QUERY) {
            defaultValue = p.getDefaultValue();
        }
        return defaultValue;
    }

    /*
     * Generates something like 
    String apiKey = FacebookAuthenticator.getApiKey();
    String sessionKey = FacebookAuthenticator.getSessionKey();
    String method = "facebook.friends.get";
    String v = "1.0";
    String callId = String.valueOf(System.currentTimeMillis());
     */
    public static String getSignParamDeclaration(SaasBean bean,
            List<ParameterInfo> signParams, List<ParameterInfo> filterParams) {
        String paramStr = "";
        for (ParameterInfo p : signParams) {
            String[] pIds = Util.getParamIds(p, bean.getSaasName(),
                    bean.isDropTargetWeb());
            if (pIds != null) {//process special case
                paramStr += "        String " + getVariableName(pIds[0]) + " = " + pIds[1] + ";\n";
                continue;
            }
            if (isContains(p, filterParams)) {
                continue;
            }

            paramStr += "        String " + getVariableName(p.getName()) + " = ";
            if (p.getFixed() != null) {
                paramStr += "\"" + p.getFixed() + "\";\n";
            } else if (p.getType() == Date.class) {
                paramStr += "conn.getDate();\n";
            } else if (p.getType() == Time.class) {
                paramStr += "String.valueOf(System.currentTimeMillis());\n";
            } else if (p.getType() == HttpMethodType.class) {
                paramStr += "\"" + bean.getHttpMethod().value() + "\";\n";
            } else if (p.isRequired()) {
                if (p.getDefaultValue() != null) {
                    paramStr += getQuotedValue(p.getDefaultValue().toString()) + ";\n";
                } else {
                    paramStr += "\"\";\n";
                }
            } else {
                if (p.getDefaultValue() != null) {
                    paramStr += getQuotedValue(p.getDefaultValue().toString()) + ";\n";
                } else {
                    paramStr += "null;\n";
                }
            }
        }
        paramStr += "\n";
        return paramStr;
    }

    public static boolean isContains(ParameterInfo pInfo, List<ParameterInfo> params) {
        String name = getVariableName(pInfo.getName());
        for (ParameterInfo p : params) {
            if (name.equals(getVariableName(p.getName()))) {
                return true;
            }
        }

        return false;
    }

    public static String getQuotedValue(String value) {
        String normalized = value;
        if (normalized.startsWith("\"")) {
            normalized = normalized.substring(1);
        }
        if (normalized.endsWith("\"")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return "\"" + normalized + "\"";
    }

    public static String[] getGetParamNames(List<ParameterInfo> queryParams) {
        ArrayList<String> params = new ArrayList<String>();
        params.addAll(Arrays.asList(getParamNames(queryParams)));
        return params.toArray(new String[params.size()]);
    }

    public static String[] getGetParamTypes(List<ParameterInfo> queryParams) {
        ArrayList<String> types = new ArrayList<String>();
        types.addAll(Arrays.asList(getParamTypeNames(queryParams)));
        return types.toArray(new String[types.size()]);
    }
    
    public static List<ParameterInfo> getRestClientMethodParameters(RestClientSaasBean bean) {
        List<ParameterInfo> params = bean.filterParametersByAuth(bean.filterParameters(
                new ParamFilter[]{ParamFilter.FIXED}));
        getRestClientPutPostParameters(bean, params);
        return params;
    }
    
    public static List<ParameterInfo> getRestClientPutPostParameters(RestClientSaasBean bean, List<ParameterInfo> params) {
        HttpMethodType httpMethod = bean.getHttpMethod();

        if (httpMethod == HttpMethodType.PUT || httpMethod == HttpMethodType.POST) {

            ParameterInfo contentTypeParam = Util.findParameter(bean.getInputParameters(), Constants.CONTENT_TYPE);
            Class contentType = InputStream.class;

            if (contentTypeParam != null) {
                if (!contentTypeParam.isFixed() && !params.contains(contentTypeParam)) {
                    params.add(contentTypeParam);
                } else {
                    String value = findParamValue(contentTypeParam);
                    if (value.equals("text/plain") || value.equals("application/xml") ||
                            value.equals("text/xml")) {     //NOI18N

                        contentType = String.class;
                    }
                }
            }
            if (hasInputRepresentations(bean) && !isPutPostFormParams(bean)) {
                params.add(new ParameterInfo(Constants.PUT_POST_CONTENT, contentType));
            }
        }
        return params;
    }

    public static boolean isPutPostFormParams(RestClientSaasBean bean) {
        HttpMethodType httpMethod = bean.getHttpMethod();
        if(httpMethod == HttpMethodType.PUT || httpMethod == HttpMethodType.POST) {
            List<Representation> reps = bean.findInputRepresentations(bean.getMethod());
            for(Representation rep: reps) {
                if (rep.getParam() != null && rep.getParam().size() > 0)
                    return true;
            }
        }
        return false;
    }
    
    public static boolean hasInputRepresentations(RestClientSaasBean bean) {
       List<Representation> reps = bean.findInputRepresentations(bean.getMethod());
       
       if (reps == null || reps.size() == 0) 
           return false;
       
       return true;
    }

    public static Document getDocument(FileObject f) throws IOException {
        try {
            DataObject d = DataObject.find(f);
            EditorCookie ec = d.getCookie(EditorCookie.class);
            Document doc = ec.openDocument();
            if (doc == null) {
                throw new IOException("Document cannot be opened for : " + f.getPath());
            }
            return doc;
        } catch (DataObjectNotFoundException ex) {
            throw new IOException("DataObject does not exist for : " + f.getPath());
        }
    }

    public static boolean isRPCEncoded(WSDLModel wsdlModel){

        Definitions definitions = wsdlModel.getDefinitions();
        Collection<Binding> bindings = definitions.getBindings();
        for (Binding binding : bindings) {
            List<SOAPBinding> soapBindings = binding.getExtensibilityElements(SOAPBinding.class);
            for (SOAPBinding soapBinding : soapBindings) {
                if (soapBinding.getStyle() == Style.RPC) {
                    Collection<BindingOperation> bindingOperations = binding.getBindingOperations();
                    for (BindingOperation bindingOperation : bindingOperations) {
                        BindingInput bindingInput = bindingOperation.getBindingInput();
                        if (bindingInput != null) {
                            List<SOAPBody> soapBodies = bindingInput.getExtensibilityElements(SOAPBody.class);
                            if (soapBodies != null && soapBodies.size() > 0) {
                                SOAPBody soapBody = soapBodies.get(0);
                                if (soapBody.getUse() == Use.ENCODED) {
                                    return true;
                                }
                            }
                        }
                    }
                }
            }
        }
        return false;
    }
    
    public static boolean showDialog(String displayName, List<ParameterInfo> allParams, 
            Document targetDoc) {
        if (!allParams.isEmpty()) {
            boolean showParamTypes = Util.isJava(targetDoc) || Util.isJsp(targetDoc);
            CodeSetupPanel panel = new CodeSetupPanel(allParams, showParamTypes);

            DialogDescriptor desc = new DialogDescriptor(panel,
                    NbBundle.getMessage(CodeSetupPanel.class,
                    "LBL_CustomizeSaasService", displayName));

            Dialog d = DialogDisplayer.getDefault().createDialog(desc);
            panel.setDialog(d);
            d.setVisible(true);
            Object response = (desc.getValue() != null) ? desc.getValue() : NotifyDescriptor.CLOSED_OPTION;
            if (response.equals(NotifyDescriptor.CANCEL_OPTION) ||
                    response.equals(NotifyDescriptor.CLOSED_OPTION)) {
                return false;
            }
        }
        return true;
    }
    
    public static void doGenerateCode(SaasClientCodeGenerator codegen,
            ProgressDialog progress, List<Exception> errors) {
        try {
            codegen.initProgressReporting(progress.getProgressHandle());
            codegen.generate();
        } catch (IOException ex) {
            if (!ex.getMessage().equals(Util.SCANNING_IN_PROGRESS)) {
                errors.add(ex);
            }
        }
    }

}
