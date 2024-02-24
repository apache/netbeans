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
package org.netbeans.modules.websvc.core.jaxws.saas;

import com.sun.source.tree.ClassTree;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlOperation;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlParameter;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlPort;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlService;
import org.netbeans.modules.websvc.api.support.java.SourceUtils;
import org.netbeans.modules.websvc.rest.model.api.RestConstants;
import org.netbeans.modules.websvc.saas.codegen.java.support.JavaSourceHelper;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.openide.filesystems.FileObject;

/**
 *
 * @author rico
 */
public class RestWrapperForSoapGenerator {

    private FileObject targetFile;
    private WsdlService service;
    private WsdlPort port;
    private WsdlOperation operation;
    private Project project;
    private Map<String, Class> primitiveTypes;
    private static final Modifier[] PUBLIC = new Modifier[]{Modifier.PUBLIC};
    public static final String INDENT = "        ";
    public static final String INDENT_2 = "             ";
    public static final String APP_XML_MIME = "application/xml";
    public static final String TEXT_PLAIN_MIME = "text/plain";
    public static final String APP_JSON_MIMI = "application/json";
    private String[] ANNOTATIONS_GET = new String[]{
        RestConstants.GET,
        RestConstants.PRODUCE_MIME,
        RestConstants.CONSUME_MIME,
        RestConstants.PATH
    };
    private String[] ANNOTATIONS_POST = new String[]{
        RestConstants.POST,
        RestConstants.PRODUCE_MIME,
        RestConstants.CONSUME_MIME,
        RestConstants.PATH
    };
    private String[] ANNOTATIONS_PUT = new String[]{
        RestConstants.PUT,
        RestConstants.CONSUME_MIME,
        RestConstants.PATH
    };
    static final String JAVA_TRY =
            "\ntry '{' // Call Web Service Operation\n"; //NOI18N
    static final String JAVA_SERVICE_DEF =
            "   {0} {5} = new {0}();\n"; //NOI18N
    static final String JAVA_PORT_DEF =
            "   {1} p = {5}.{2}();\n"; //NOI18N
    static final String JAVA_RESULT =
            // "   {3}" + //NOI18N
            // "   // TODO process result here\n" + //NOI18N
            "   {3} result = port.{4}({6});\n"; //NOI18N
    static final String JAVA_VOID =
            // "   {3}" + //NOI18N
            "   port.{4}({6});\n"; //NOI18N
    static final String JAVA_CATCH =
            "'}' catch (Exception ex) '{'\n" + //NOI18N
            "   // TODO handle custom exceptions here\n" + //NOI18N
            "'}'\n"; //NOI18N
    static final String IF_PORT_NOT_NULL = "\nif(port != null)'{'\n"; //NOI18N

    static final String RESPONSE_BLOCK =
            "class {8}_1 extends {7}.{8} '{'\n" +
            "    {8}_1({3} _return) '{'\n" +
            "        this._return = _return;\n" +
            "    '}'\n" +
            "'}'\n" +
            "{7}.{8} response = new {8}_1(result);\n" +
            "return new {7}.ObjectFactory().create{8}(response);\n";

    static final String CLOSE_IF_PORT = "\n'}'\n";//NOI18N
    //private String wsdlUrl;

    public RestWrapperForSoapGenerator(WsdlService service, WsdlPort port,
            WsdlOperation operation, Project project, FileObject targetFile, String wsdlUrl) {
        this.service = service;
        this.port = port;
        this.operation = operation;
        this.targetFile = targetFile;
        this.project = project;
        //this.wsdlUrl = wsdlUrl;
    }

    @org.netbeans.api.annotations.common.SuppressWarnings("NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
    public Set<FileObject> generate() throws IOException {
        JavaSource targetSource = JavaSource.forFileObject(targetFile);
        final String returnType = operation.getReturnTypeName();
        CancellableTask<WorkingCopy> task = new CancellableTask<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.ELEMENTS_RESOLVED);
                ClassTree javaClass = SourceUtils.getPublicTopLevelTree(workingCopy);
                TypeElement classElement = SourceUtils.getPublicTopLevelElement(workingCopy);
                List<? extends AnnotationMirror> anns = classElement.getAnnotationMirrors();
                String pathValue = javaClass.getSimpleName().toString().toLowerCase();
                AnnotationMirror pathAnn = JavaSourceHelper.findAnnotation(anns, RestConstants.PATH + "(\"" + pathValue + "\")");
                if (pathAnn == null) {
                    addPathAnnotation(workingCopy, new String[]{javaClass.getSimpleName().toString().toLowerCase()});
                }
                if (!returnType.equals("void") && getPrimitiveType(returnType) == null) {
                    addQNameImport(workingCopy);
                }
                ClassTree finalJavaClass = addHttpMethod(returnType, workingCopy, javaClass);
                workingCopy.rewrite(javaClass, finalJavaClass);
            }

            public void cancel() {
            }
        };
        targetSource.runModificationTask(task).commit();


        return new HashSet<FileObject>(Collections.<FileObject>emptyList());
    }

    private void addQNameImport(WorkingCopy copy) {
        JavaSourceHelper.addImports(copy, new String[]{"javax.xml.namespace.QName"});
    }

    public List<WsdlParameter> getOutputParameters() {
        ArrayList<WsdlParameter> params = new ArrayList<WsdlParameter>();
        for (WsdlParameter p : operation.getParameters()) {
            if (p.isHolder()) {
                params.add(p);
            }
        }
        return params;
    }

    private void addPathAnnotation(WorkingCopy copy, String[] path) {
        JavaSourceHelper.addClassAnnotation(copy, new String[]{RestConstants.PATH_ANNOTATION}, path);
    }

    private String wrapInJaxbElement(String type) {
        return "javax.xml.bind.JAXBElement<" + type + ">";
    }

    private ClassTree addHttpMethod(String returnType, WorkingCopy copy, ClassTree tree) throws IOException {
        Modifier[] modifiers = PUBLIC;
        String retType = returnType;
        if (retType.equals("void")) {  //if return type is void, find out if there are Holder paramters
            List<WsdlParameter> parms = getOutputParameters();
            for (WsdlParameter parm : parms) {
                if (parm.isHolder()) {//TODO pick the first one right now. 
                    //Should let user pick if there are multiple OUT parameters.

                    String holderType = parm.getTypeName();
                    int leftbracket = holderType.indexOf("<");
                    int rightbracket = holderType.lastIndexOf(">");
                    retType = holderType.substring(leftbracket + 1, rightbracket);

                    break;
                }
            }
        } else if (isList(retType)) {
            retType = wrapInJaxbElement(getPackageFromJava(port.getJavaName())+"."+getResponseType(operation.getName()));
        } else if (getPrimitiveType(retType) != null) {
            retType = "java.lang.String";
        } else {  //find out if need to be wrapped in JAXBElement
            retType = wrapInJaxbElement(retType);
        }

        List<WsdlParameter> queryParams = operation.getParameters();
        String[] parameters = getHttpParamNames(queryParams);
        String[] paramTypes = getHttpParamTypes(queryParams);
        String[][] paramAnnotations = getHttpParamAnnotations(paramTypes);
        Object[][] paramAnnotationAttrs = getHttpParamAnnotationAttrs(queryParams, paramTypes);

        String[] methodAnnotations = getOperationAnnotations(retType, paramTypes);
        Object[] methodAnnotationAttrs = getOperationAnnotationAttrs(operation.getName(), retType, paramTypes);
        String bodyText = getSOAPClientInvocation(retType);

        StringBuilder comment = new StringBuilder("Invokes the SOAP method ");//NOI18N
        comment.append(operation.getName());
        comment.append( '\n' );
        for (String param : parameters) {
            comment.append( "@param ");                     //NOI18N 
            comment.append(param);
            comment.append(" resource URI parameter\n");    //NOI18N
        }
        if (!retType.equals("void")) {                      //NOI18N
            comment.append("@return an instance of ");      //NOI18N
            comment.append( retType);
        }
        int index = methodAnnotations[0].lastIndexOf(".");
        String methodPrefix = methodAnnotations[0].substring(index + 1).toLowerCase();
        return JavaSourceHelper.addMethod(copy, tree,
                modifiers, methodAnnotations, methodAnnotationAttrs,
                getMethodName(methodPrefix), retType, parameters, paramTypes,
                paramAnnotations, paramAnnotationAttrs,
                bodyText, comment.toString());      

    }

    private String[] getHttpParamTypes(List<WsdlParameter> queryParams) {
        List<String> types = new ArrayList<String>();
        for (WsdlParameter queryParam : queryParams) {
            String paramTypeName = queryParam.getTypeName();
            if (!queryParam.isHolder()) {
                if (this.getPrimitiveType(paramTypeName) == null) {
                    types.add(wrapInJaxbElement(paramTypeName));
                } else {
                    types.add(paramTypeName);
                }
            }
        }
        return types.toArray(new String[0]);
    }

    private String[] getHttpParamNames(List<WsdlParameter> queryParams) {
        List<String> names = new ArrayList<String>();
        for (WsdlParameter queryParam : queryParams) {
            if (!queryParam.isHolder()) {
                names.add(queryParam.getName());
            }

        }
        return names.toArray(new String[0]);
    }

    private Object generateDefaultValue(Class type) {
        if (type == Integer.class || type == Short.class || type == Long.class ||
                type == Float.class || type == Double.class) {
            try {
                return type.getConstructor(String.class).newInstance("0"); //NOI18N

            } catch (Exception ex) {
                return null;
            }
        }
        if (type == Boolean.class) {
            return Boolean.FALSE;
        }
        if (type == Character.class) {
            return '\0';
        }
        return null;
    }

    private String[][] getHttpParamAnnotations(String[] paramTypeNames) {
        ArrayList<String[]> annos = new ArrayList<String[]>();
        String[] annotations = null;
        if (!hasComplexTypes(paramTypeNames)) {
            for (int i = 0; i < paramTypeNames.length; i++) {
                Class type = getType(project, paramTypeNames[i]);
                if (generateDefaultValue(type) != null) {
                    annotations = new String[]{
                                RestConstants.QUERY_PARAM,
                                RestConstants.DEFAULT_VALUE
                            };
                } else {
                    annotations = new String[]{RestConstants.QUERY_PARAM};
                }
                annos.add(annotations);
            }
        }
        return annos.toArray(new String[0][]);
    }

    public Class getGenericRawType(String typeName, ClassLoader loader) {
        int i = typeName.indexOf('<');
        if (i < 1) {
            return null;
        }
        String raw = typeName.substring(0, i);
        try {
            return loader.loadClass(raw);
        } catch (ClassNotFoundException ex) {
            Logger.getGlobal().log(Level.INFO, "", ex);
            return null;
        }
    }

    public Class getType(Project project, String typeName) {
        List<ClassPath> classPaths = getClassPath(project);


        for (ClassPath cp : classPaths) {
            try {
                Class ret = getPrimitiveType(typeName);
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

    public Class[] getInputParameterTypes() {
        ArrayList<Class> types = new ArrayList<Class>();

        for (WsdlParameter p : operation.getParameters()) {
            if (!p.isHolder()) {
                int repeatCount = 0;
                Class type = null;

                // This is a hack to wait for the complex type to become
                // available. We will give up after 120 seconds.
                synchronized (this) {
                    try {
                        while (repeatCount < 60) {
                            type = getType(project, p.getTypeName());

                            if (type != null) {
                                break;
                            }

                            repeatCount++;
                            this.wait(2000);
                        }
                    } catch (InterruptedException ex) {
                    }
                }

                // RESOLVE:
                // Need to fail gracefully by displaying an error dialog.
                // For now, set it to Object.class.
                if (type == null) {
                    type = Object.class;
                }

                types.add(type);
            }
        }
        return types.toArray(new Class[0]);
    }

    private Object[][] getHttpParamAnnotationAttrs(List<WsdlParameter> queryParams, String[] typeNames) {
        ArrayList<Object[]> attrs = new ArrayList<Object[]>();

        Object[] annotationAttrs = null;

        if (!hasComplexTypes(typeNames)) {
            for (WsdlParameter param : queryParams) {
                Class type = getType(project, param.getTypeName());
                Object defaultValue = this.generateDefaultValue(type);
                if (generateDefaultValue(type) != null) {
                    annotationAttrs = new Object[]{
                                param.getName(), defaultValue.toString()
                            };
                } else {
                    annotationAttrs = new Object[]{param.getName()};
                }
                attrs.add(annotationAttrs);
            }
        }
        return attrs.toArray(new Object[0][]);
    }

    private String getMethodName(String prefix) {
        String methodName = camelize(operation.getName(), true);
        if (methodName.startsWith(prefix)) {
            return methodName;
        }
        return prefix + camelize(methodName, false);
    }

    private String getSOAPClientInvocation(String typeName) throws IOException {
        String code = "{\n";
        code += getCustomMethodBody() + "\n";
        if (!typeName.equals("void")) {
            code += "return null;\n";  //TODO: will there be a case for primitive return types?

        }
        code += "}\n";
        return code;

    }

    private String getReturnTypeQName(String returnTypeName) {
        int index = returnTypeName.lastIndexOf(".");
        String packageName = returnTypeName.substring(0, index);
        StringTokenizer tokenizer = new StringTokenizer(packageName, ".");
        int tokens = tokenizer.countTokens();
        String[] inverted = new String[tokens];
        while (tokenizer.hasMoreTokens()) {
            inverted[--tokens] = tokenizer.nextToken();
        }
        StringBuffer namespace = new StringBuffer();
        for (int i = 0; i < inverted.length; i++) {
            namespace.append(inverted[i]);
            if (i < inverted.length - 1) {
                namespace.append(".");
            }
        }
        String ns = "http//" + namespace.toString() + "/";
        String localpart = returnTypeName.substring(index + 1).toLowerCase();

        return "new QName(\"" + ns + "\",\"" + localpart + "\")";

    }

    private String getReturnStatement(WsdlOperation operation) {
        String statement = "return result";
        String returnTypeName = operation.getReturnTypeName();
        Class c = getPrimitiveType(returnTypeName);
        if (c != null && !returnTypeName.equals("java.lang.String") && !returnTypeName.equals("String")) {
            statement = "return new " + c.getName() + "(result).toString();";
        } else if (c == null) {
            statement = "return new JAXBElement<" + returnTypeName + ">(" + getReturnTypeQName(returnTypeName) + "," + returnTypeName + ".class, result);";
        }
        return statement;
    }

    private String getCustomMethodBody() throws IOException {
        String methodBody = INDENT;
        methodBody += getWSInvocationCode(targetFile, service, port, operation);

        return methodBody;
    }

    public String getWSInvocationCode(FileObject target, WsdlService service, WsdlPort port, WsdlOperation operation) {


        String serviceFieldName = "service"; //NOI18N

        String operationJavaName = operation.getJavaName();
        String portJavaName = port.getJavaName();
        String portGetterMethod = port.getPortGetter();
        List<WsdlParameter> arguments = operation.getParameters();
        String returnTypeName = operation.getReturnTypeName();
        StringBuffer argumentBuffer = new StringBuffer();

        int i = 0;
        for (WsdlParameter argument : arguments) {
            String argumentTypeName = argument.getTypeName();
            String argumentName = argument.getName();
            if (getPrimitiveType(argumentTypeName) == null) {
                argumentName = argumentName + ".getValue()";
            }
            argumentBuffer.append(i > 0 ? ", " + argumentName : argumentName); //NOI18N
            i++;
        }

        String argumentDeclarationPart = argumentBuffer.toString();

        String javaInvocationBody = getJavaInvocationWithReturnBody(
                operation,
                portJavaName,
                portGetterMethod,
                returnTypeName,
                operationJavaName,
                serviceFieldName,
                argumentDeclarationPart);

        return javaInvocationBody;

    }

    public String getJavaInvocationWithReturnBody(
            WsdlOperation operation, String portJavaName,
            String portGetterMethod, String returnTypeName,
            String operationJavaName, String serviceFName,
            String argumentDeclarationPart) {

        String serviceJavaName = service.getJavaName();
        String invocationBody = "";
        Object[] args = new Object[]{
            serviceJavaName, portJavaName,
            portGetterMethod,
            returnTypeName, operationJavaName,
            serviceFName, argumentDeclarationPart, getPackageFromJava(portJavaName), getResponseType(operationJavaName)
        };
        if ("void".equals(returnTypeName)) { //NOI18N
            String body =
                    JAVA_TRY +
                    IF_PORT_NOT_NULL +
                    JAVA_VOID +
                    CLOSE_IF_PORT +
                    JAVA_CATCH;
            invocationBody = MessageFormat.format(body, args);
        } else {
            String body =
                    JAVA_TRY +
                    IF_PORT_NOT_NULL +
                    JAVA_RESULT +
                    (isList(returnTypeName) ? RESPONSE_BLOCK : getReturnStatement(operation)) +
                    CLOSE_IF_PORT +
                    JAVA_CATCH;
            invocationBody = MessageFormat.format(body, args);
        }
        return invocationBody;
    }

    private Class getPrimitiveType(String typeName) {
        if (primitiveTypes == null) {
            primitiveTypes = new HashMap<String, Class>();
            primitiveTypes.put("int", Integer.class);
            primitiveTypes.put("int[]", Integer[].class);
            primitiveTypes.put("boolean", Boolean.class);
            primitiveTypes.put("boolean[]", Boolean[].class);
            primitiveTypes.put("byte", Byte.class);
            primitiveTypes.put("byte[]", Byte[].class);
            primitiveTypes.put("char", Character.class);
            primitiveTypes.put("char[]", Character[].class);
            primitiveTypes.put("double", Double.class);
            primitiveTypes.put("double[]", Double[].class);
            primitiveTypes.put("float", Float.class);
            primitiveTypes.put("float[]", Float[].class);
            primitiveTypes.put("long", Long.class);
            primitiveTypes.put("long[]", Long[].class);
            primitiveTypes.put("short", Short.class);
            primitiveTypes.put("short[]", Short[].class);
            primitiveTypes.put("java.lang.String", String.class);
            primitiveTypes.put("String", String.class);

        }
        return primitiveTypes.get(typeName);
    }

    private boolean isList(String typeName) {
        if (typeName.startsWith("java.util.List")) {
            return true;
        }
        return false;
    }

    public static List<ClassPath> getClassPath(Project project) {
        List<ClassPath> paths = new ArrayList<ClassPath>();
        List<SourceGroup> groups = new ArrayList<SourceGroup>();
        groups.addAll(Arrays.asList(ProjectUtils.getSources(project).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA)));
        ClassPathProvider cpp = project.getLookup().lookup(ClassPathProvider.class);
        for (SourceGroup group : groups) {
            ClassPath cp = cpp.findClassPath(group.getRootFolder(), ClassPath.COMPILE);
            if (cp != null) {
                paths.add(cp);
            }
            cp = cpp.findClassPath(group.getRootFolder(), ClassPath.SOURCE);
            if (cp != null) {
                paths.add(cp);
            }
        }
        return paths;
    }

    public String camelize(String word, boolean flag) {
        if (word.length() == 0) {
            return word;
        }
        StringBuffer sb = new StringBuffer(word.length());
        if (flag) {
            sb.append(Character.toLowerCase(word.charAt(0)));
        } else {
            sb.append(Character.toUpperCase(word.charAt(0)));
        }
        boolean capitalize = false;
        for (int i = 1; i < word.length(); i++) {
            char ch = word.charAt(i);
            if (capitalize) {
                sb.append(Character.toUpperCase(ch));
                capitalize = false;
            } else if (ch == '_') {
                capitalize = true;
            } else if (ch == '/') {
                capitalize = true;
                sb.append('.');
            } else {
                sb.append(ch);
            }
        }
        return sb.toString();

    }

    private boolean hasComplexTypes(String[] types) {
        for (int i = 0; i < types.length; i++) {
            if (getPrimitiveType(types[i]) == null) {
                return true;
            }
        }
        return false;
    }

    private Object[] getOperationAnnotationAttrs(String operationName, String returnType, String[] parameterTypes) {
        List<Object> attributes = new ArrayList<Object>();
        attributes.add(null);
        if (!returnType.equals("void")) {
            if (getPrimitiveType(returnType) == null) {
                attributes.add(APP_XML_MIME);
            } else {
                attributes.add(TEXT_PLAIN_MIME);
            }
        }
        if (hasComplexTypes(parameterTypes)) {
            attributes.add(APP_XML_MIME);
        } else {
            attributes.add(TEXT_PLAIN_MIME);
        }
        attributes.add(operationName.toLowerCase() + "/");
        return attributes.toArray(new Object[0]);
    }

    private String[] getOperationAnnotations(String returnType, String[] parameterTypes) {
        if (!returnType.equals("void")) {
            if (hasComplexTypes(parameterTypes)) {
                return ANNOTATIONS_POST;
            } else {
                return ANNOTATIONS_GET;
            }
        }
        return ANNOTATIONS_PUT;
    }

    private String getResponseType(String operationName) {
        return operationName.substring(0,1).toUpperCase() + operationName.substring(1)+"Response"; //NOI18N
    }

    private String getPackageFromJava(String seiClass) {
        int index = seiClass.lastIndexOf(".");
        return index > 0 ? seiClass.substring(0,index) : "";
    }
}
