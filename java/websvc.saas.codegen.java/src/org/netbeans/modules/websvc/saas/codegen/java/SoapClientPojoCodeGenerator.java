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
package org.netbeans.modules.websvc.saas.codegen.java;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.websvc.jaxwsmodelapi.WSOperation;
import org.netbeans.modules.websvc.jaxwsmodelapi.WSParameter;
import org.netbeans.modules.websvc.saas.codegen.Constants;
import org.netbeans.modules.websvc.saas.codegen.SaasClientCodeGenerator;
import org.netbeans.modules.websvc.saas.codegen.java.support.JavaSourceHelper;
import org.netbeans.modules.websvc.saas.codegen.java.support.JavaUtil;
import org.netbeans.modules.websvc.saas.codegen.java.support.LibrariesHelper;
import org.netbeans.modules.websvc.saas.codegen.model.ParameterInfo;
import org.netbeans.modules.websvc.saas.codegen.model.SaasBean;
import org.netbeans.modules.websvc.saas.codegen.model.SoapClientOperationInfo;
import org.netbeans.modules.websvc.saas.codegen.model.SoapClientSaasBean;
import org.netbeans.modules.websvc.saas.codegen.ui.CodeSetupPanel;
import org.netbeans.modules.websvc.saas.codegen.util.Util;
import org.netbeans.modules.websvc.saas.model.SaasMethod;
import org.netbeans.modules.websvc.saas.model.WsdlSaas;
import org.netbeans.modules.websvc.saas.model.WsdlSaasMethod;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 * Code generator for REST services wrapping WSDL-based web {10}.
 *
 * @author ayubkhan
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.websvc.saas.codegen.spi.SaasClientCodeGenerationProvider.class)
public class SoapClientPojoCodeGenerator extends SaasClientCodeGenerator {

    public static final String QNAME = "javax.xml.namespace.QName";
    public static final String WS_BINDING_PROVIDER = "com.sun.xml.ws.developer.WSBindingProvider";
    public static final String HEADERS = "com.sun.xml.ws.api.message.Headers";
    public static final String SET_HEADER_PARAMS = "setHeaderParameters";
    public static final String VAR_NAMES_SERVICE = "service";
    public static final String VAR_NAMES_PORT = "port";

    public SoapClientPojoCodeGenerator() {
        setDropFileType(Constants.DropFileType.JAVA_CLIENT);
    }

    @Override
    public boolean canAccept(SaasMethod method, Document doc) {
        if (SaasBean.canAccept(method, WsdlSaasMethod.class, getDropFileType()) &&
                Util.isJava(doc)) {
            try {
                WsdlSaasMethod wsm = (WsdlSaasMethod) method;
                Project p = FileOwnerQuery.getOwner(NbEditorUtilities.getFileObject(doc));
                new SoapClientSaasBean(wsm, p, JavaUtil.toJaxwsOperationInfos(wsm, p));
                return true;
            } catch (Exception e) {
                Logger.getLogger(this.getClass().getName()).log(Level.WARNING, null, 
                    NbBundle.getMessage(CodeSetupPanel.class, "WARN_UnsupportedDropTarget")
                );
            }
        }
        return false;
    }

    @Override
    public void init(SaasMethod m, Document doc) throws IOException {
        super.init(m, doc);
        WsdlSaasMethod wsm = (WsdlSaasMethod) m;
        Project p = FileOwnerQuery.getOwner(NbEditorUtilities.getFileObject(doc));
        SaasBean bean = new SoapClientSaasBean(wsm, p, JavaUtil.toJaxwsOperationInfos(wsm, p));
        setBean(bean);
        clearVariablePatterns();
    }

    @Override
    public SoapClientSaasBean getBean() {
        return (SoapClientSaasBean) super.getBean();
    }

    @Override
    protected void preGenerate() throws IOException {
        super.preGenerate();

        SoapClientOperationInfo[] operations = getBean().getOperationInfos();
        for (SoapClientOperationInfo info : operations) {
            if (info.isRPCEncoded()) {
                LibrariesHelper.addDefaultJaxRpcClientJars(getProject(), null, info.getMethod().getSaas());
            } else {
                LibrariesHelper.addDefaultJaxWsClientJars(getProject(), null, info.getMethod().getSaas());
            }
        }
    }

    @Override
    public Set<FileObject> generate() throws IOException {
        preGenerate();

        insertSaasServiceAccessCode(isInBlock(getTargetDocument()));
        //addImportsToTargetFile();

        finishProgressReporting();

        return new HashSet<FileObject>(Collections.<FileObject>emptyList());
    }

    /**
     *  Insert the Saas client call
     */
    protected void insertSaasServiceAccessCode(boolean isInBlock) throws IOException {
        try {
            String code = "";
            if (isInBlock) {
                code = getCustomMethodBody();
            } else {
                code = "\nprivate String call" + getBean().getName() + "Service() {\n";
                code += getCustomMethodBody() + "\n";
                code += "return " + getResultPattern() + ";\n";
                code += "}\n";
            }
            insert(code, true);
        } catch (BadLocationException ex) {
            throw new IOException(ex.getMessage());
        }
    }

    @Override
    protected String getCustomMethodBody() throws IOException {
        String methodBody = "\n" + INDENT + "try {\n";
        List<ParameterInfo> params = getBean().getQueryParameters();
        updateVariableNames(params);
        List<ParameterInfo> renamedParams = renameParameterNames(params);
        for (ParameterInfo param : renamedParams) {
            String name = param.getName();
            methodBody += INDENT_2 + param.getType().getName() + " " + name + " = " +
                    resolveInitValue(param) + "\n";
        }
        SoapClientOperationInfo[] operations = getBean().getOperationInfos();
        for (SoapClientOperationInfo info : operations) {
            if (!info.isRPCEncoded()) {
                methodBody += getWSInvocationCode(info);
            } else {
                methodBody += getWSInvocationCodeForJaxrpc(info);
            }
        }
        methodBody += INDENT + "} catch (Exception ex) {\n";
        methodBody += INDENT_2 + "ex.printStackTrace();\n";
        methodBody += INDENT + "}\n";
        return methodBody;
    }

    private static String varFromName(final String name) {
        if (name.length() > 0) {
            StringBuffer buf = new StringBuffer(name);

            // If the first character is uppercase, make it lowercase for the variable name,
            // otherwise, prefix an underscore.
            if (Character.isUpperCase(buf.charAt(0))) {
                buf.setCharAt(0, Character.toLowerCase(buf.charAt(0)));
            } else {
                buf.insert(0, '_');
            }
            return removeDots(buf).toString();
        } else {
            return "unknown"; // NOI18N
        }
    }
    // replace dots in a class/var name
    private static StringBuffer removeDots(final StringBuffer name) {
        int dotIndex;
        while ((dotIndex = name.indexOf(".")) > -1) { //NOI18N
            name.deleteCharAt(dotIndex); //delete the dot
            name.setCharAt(dotIndex, Character.toUpperCase(name.charAt(dotIndex))); // make the letter after dot uppercase
        }
        return name;
    }

    // {0} = fully qualified service class name ( e.g. "org.netbeans.FooSeWebService")
    // {1} = service variable name ( e.g. "fooService")
    // {2} = fully qualified port name (as type, e.g. com.service.FooPort)
    // {3} = port varialbe name (e.g., "port")
    // {4} = port getter method (e.g., getFooPort)
    // {5} = operation name
    // {6} = argument array
    // {7} = result statement (return type and variable, e.g., MyClass myvar =
    private String getWSInvocationCodeForJaxrpc(SoapClientOperationInfo info) throws IOException {
        String serviceClassName = info.getService().getJavaName();
        String serviceLookup = serviceClassName.substring(serviceClassName.lastIndexOf(".") + 1);
        String serviceName = info.getServiceName();
        if (serviceName == null || serviceName.length() == 0) {
            serviceName = "service";  //NOI18N
        }
        String serviceVarName = varFromName(serviceName);
        String servicePortClassName = info.getPort().getJavaName();
        String portVarName = varFromName(info.getPortName());
        String portGetterName = info.getPort().getPortGetter();
        String serviceOperationName = info.getOperation().getJavaMethod().getName();
    
        List<WSParameter> outArguments = info.getOutputParameters();
        updateVariableNamesForWS(outArguments);
        String responseType = "Object"; //NOI18N
        String argumentInitializationPart = "";
        String argumentDeclarationPart = "";
        String resultStatement = "";
        String resultTypeName = "";
        String resultVariable = "";
        StringBuffer argumentBuffer2 = new StringBuffer("");
        try {
            StringBuffer argumentBuffer1 = new StringBuffer();
            for (int i = 0; i < outArguments.size(); i++) {
                String argumentTypeName = outArguments.get(i).getTypeName();
                String argumentName = findNewName(getVariableDecl(outArguments.get(i)), outArguments.get(i).getName());
                argumentBuffer1.append(INDENT_2 + argumentTypeName + " " + argumentName +
                        " = " + resolveInitValue(argumentTypeName) + "\n"); //NOI18N
            }

            List<? extends WSParameter> parameters = info.getOperation().getParameters();
            updateVariableNamesForWS(parameters);
            for (int i = 0; i < parameters.size(); i++) {
                String argument = findNewName(getVariableDecl(parameters.get(i)), parameters.get(i).getName());
                argumentBuffer2.append(i > 0 ? ", " + argument : argument); //NOI18N
            }
            argumentInitializationPart = (argumentBuffer1.length() > 0 ? "\t" +
                    HINT_INIT_ARGUMENTS + argumentBuffer1.toString() : "");
            resultTypeName = info.getOperation().getReturnTypeName();
            resultVariable = "result";  //NOI18N
            argumentDeclarationPart = argumentBuffer2.toString();
            if(!resultTypeName.equals("void")){  //NOI18N
                resultStatement = resultTypeName + " " + resultVariable + " =";  //NOI18N
            }
        } catch (Exception e) {
            ErrorManager.getDefault().notify(e);
        }

        Object[] args = new String[]{serviceClassName, serviceVarName, servicePortClassName, portVarName,
            portGetterName, serviceOperationName, argumentDeclarationPart, serviceLookup, resultStatement
        };
        String operationInvocation = MessageFormat.format(INVOKE_JAXRPC_BODY, args);
        return operationInvocation;
    }

    /**
     * Add JAXWS client code for invoking the given operation at current position.
     */
    protected String getWSInvocationCode(SoapClientOperationInfo info) throws IOException {
        //Collect java names for invocation code
        String serviceJavaName = info.getService().getJavaName();
        String portJavaName = info.getPort().getJavaName();
        String operationJavaName = info.getOperation().getJavaName();
        String portGetterMethod = info.getPort().getPortGetter();
        String pkgName = (((WsdlSaas)getBean().getSaas()).getWsdlData()).getJaxWsDescriptor().getPackageName();
        if(pkgName != null) {
            if(serviceJavaName.lastIndexOf(".") != -1)
                serviceJavaName = serviceJavaName.substring(serviceJavaName.lastIndexOf(".")+1);
            serviceJavaName = pkgName+"."+serviceJavaName;
            if(portJavaName.lastIndexOf(".") != -1)
                portJavaName = portJavaName.substring(portJavaName.lastIndexOf(".")+1);
            portJavaName = pkgName+"."+portJavaName;
        }
        String serviceFieldName = findNewName(serviceJavaName + " " + VAR_NAMES_SERVICE, VAR_NAMES_SERVICE); //NOI18N
        String returnTypeName = info.getOperation().getReturnTypeName();
        List<WSParameter> outArguments = info.getOutputParameters();
        updateVariableNamesForWS(outArguments);
        String responseType = "Object"; //NOI18N
        String callbackHandlerName = "javax.xml.ws.AsyncHandler"; //NOI18N
        String argumentInitializationPart = "";
        String argumentDeclarationPart = "";
        try {
            StringBuffer argumentBuffer1 = new StringBuffer();
            StringBuffer argumentBuffer2 = new StringBuffer();
            for (int i = 0; i < outArguments.size(); i++) {
                String argumentTypeName = outArguments.get(i).getTypeName();
                if (argumentTypeName.startsWith("javax.xml.ws.AsyncHandler")) {
                    //NOI18N
                    responseType = resolveResponseType(argumentTypeName);
                    callbackHandlerName = argumentTypeName;
                }
                String argumentName = findNewName(getVariableDecl(outArguments.get(i)), outArguments.get(i).getName());
                argumentBuffer1.append(INDENT_2 + argumentTypeName + " " + argumentName +
                        " = " + resolveInitValue(argumentTypeName) + "\n"); //NOI18N
            }

            List<? extends WSParameter> parameters = info.getOperation().getParameters();
            updateVariableNamesForWS(parameters);
            for (int i = 0; i < parameters.size(); i++) {
                String argument = findNewName(getVariableDecl(parameters.get(i)), parameters.get(i).getName());
                argumentBuffer2.append(i > 0 ? ", " + argument : argument); //NOI18N
            }
            argumentInitializationPart = (argumentBuffer1.length() > 0 ? "\t" +
                    HINT_INIT_ARGUMENTS + argumentBuffer1.toString() : "");
            argumentDeclarationPart = argumentBuffer2.toString();
        } catch (NullPointerException npe) {
            // !PW notify failure to extract service information.
            npe.printStackTrace();
            String message = NbBundle.getMessage(SoapClientPojoCodeGenerator.class,
                    "ERR_FailedUnexpectedWebServiceDescriptionPattern"); // NOI18N
            NotifyDescriptor desc = new NotifyDescriptor.Message(message,
                    NotifyDescriptor.Message.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(desc);
        }

        final boolean[] insertServiceDef = {true};
        final String[] printerName = {"System.out"}; // NOI18N
        final String[] argumentInitPart = {argumentInitializationPart};
        final String[] argumentDeclPart = {argumentDeclarationPart};
        final String[] serviceFName = {serviceFieldName};
        final boolean[] generateWsRefInjection = {false};
        final String serviceJName = serviceJavaName;
        CancellableTask<CompilationController> task = new CancellableTask<CompilationController>() {

            private Kind VARIABLE;

            public void run(CompilationController controller) throws IOException {
                controller.toPhase(Phase.ELEMENTS_RESOLVED);
                CompilationUnitTree cut = controller.getCompilationUnit();
                ClassTree classTree = JavaSourceHelper.findPublicTopLevelClass(controller);
                generateWsRefInjection[0] = JavaSourceHelper.isInjectionTarget(controller);
                insertServiceDef[0] = !generateWsRefInjection[0];

                // compute the service field name
                if (generateWsRefInjection[0]) {
                    Set<String> serviceFieldNames = new HashSet<String>();
                    boolean injectionExists = false;
                    int memberOrder = 0;
                    for (Tree member : classTree.getMembers()) {
                        // for the first inner class in top level
                        ++memberOrder;
                        if (VARIABLE == member.getKind()) {
                            // get variable type
                            VariableTree var = (VariableTree) member;
                            Tree typeTree = var.getType();
                            TreePath typeTreePath = controller.getTrees().getPath(cut, typeTree);
                            TypeElement typeEl = JavaSourceHelper.getTypeElement(controller, typeTreePath);
                            if (typeEl != null) {
                                String variableType = typeEl.getQualifiedName().toString();
                                if (serviceJName.equals(variableType)) {
                                    serviceFName[0] = var.getName().toString();
                                    generateWsRefInjection[0] = false;
                                    injectionExists = true;
                                    break;
                                }
                            }
                            serviceFieldNames.add(var.getName().toString());
                        }
                    }
                    if (!injectionExists) {
                        serviceFName[0] = findProperServiceFieldName(serviceFieldNames);
                    }
                }
            }

            public void cancel() {
            }
        };

        String invocationBody = getJavaInvocationBody(info.getOperation(),
                insertServiceDef[0], serviceJavaName, portJavaName,
                portGetterMethod, argumentInitPart[0], returnTypeName,
                operationJavaName, argumentDeclPart[0], serviceFName[0],
                printerName[0], responseType);

        return invocationBody;
    }
    public static final String HINT_INIT_ARGUMENTS = " // TODO initialize WS operation arguments here\n"; //NOI18N
    // {0} = service java name (as variable, e.g. "AddNumbersService")
    // {1} = port java name (e.g. "AddNumbersPort")
    // {2} = port getter method (e.g. "getAddNumbersPort")
    // {3} = argument initialization part (e.g. "int x=0; int y=0;")
    // {4} = java result type (e.g. "int")
    // {5} = operation java method name (e.g. "add")
    // {6} = java method arguments (e.g. "int x, int y")
    // {7} = service field name
    private static final String JAVA_SERVICE_DEF = "   {0} {7} = new {0}();\n"; //NOI18N
    private static final String JAVA_PORT_DEF = "   {1} {11} = {7}.{2}();\n"; //NOI18N
    private static final String JAVA_RESULT = "   {3}" + "   // TODO process result here\n" + "   {4} {9} = {11}.{5}({6});\n"; //NOI18N
    private static final String JAVA_VOID = "   {3}" + "   {11}.{5}({6});\n"; //NOI18N
    private static final String JAVA_OUT = "   {8}.println(\"Result = \"+{9});\n"; //NOI18N
    // {0} = service java name (as variable, e.g. "AddNumbersService")
    // {1} = port java name (e.g. "AddNumbersPort")
    // {2} = port getter method (e.g. "getAddNumbersPort")
    // {3} = argument initialization part (e.g. "int x=0; int y=0;")
    // {4} = java result type (e.g. "int")
    // {5} = operation java method name (e.g. "add")
    // {6} = java method arguments (e.g. "int x, int y")
    private static final String JAVA_STATIC_STUB_ASYNC_POLLING = "\ntry '{' // Call Web Service Operation(async. polling)\n" + "   {0} {10} = new {0}();\n" + "   {1} {11} = {10}.{2}();\n" + "   {3}" + "   // TODO process asynchronous response here\n" + "   {4} {9} = {11}.{5}({6});\n" + "   while(!{9}.isDone()) '{'\n" + "       // do something\n" + "       Thread.sleep(100);\n" + "   '}'\n" + "   System.out.println(\"Result = \"+{9}.get());\n" + "'}' catch (Exception ex) '{'\n" + "   // TODO handle custom exceptions here\n" + "'}'\n"; //NOI18N
    // {0} = service java name (as variable, e.g. "AddNumbersService")
    // {1} = port java name (e.g. "AddNumbersPort")
    // {2} = port getter method (e.g. "getAddNumbersPort")
    // {3} = argument initialization part (e.g. "int x=0; int y=0;")
    // {4} = java result type (e.g. "int")
    // {5} = operation java method name (e.g. "add")
    // {6} = java method arguments (e.g. "int x, int y")
    // {7} = response type (e.g. FooResponse)
    private static final String JAVA_STATIC_STUB_ASYNC_CALLBACK = "\ntry '{' // Call Web Service Operation(async. callback)\n" + "   {0} {10} = new {0}();\n" + "   {1} {11} = {10}.{2}();\n" + "   {3}" + "       public void handleResponse(javax.xml.ws.Response<{7}> {9}Resp) '{'\n" + "           try '{'\n" + "               // TODO process asynchronous response here\n" + "               System.out.println(\"Result = \"+ {9}Resp.get());\n" + "           '}' catch(Exception ex) '{'\n" + "               // TODO handle exception\n" + "           '}'\n" + "       '}'\n" + "   '}';\n" + "   {4} {9} = {11}.{5}({6});\n" + "   while(!{9}.isDone()) '{'\n" + "       // do something\n" + "       Thread.sleep(100);\n" + "   '}'\n" + "'}' catch (Exception ex) '{'\n" + "   // TODO handle custom exceptions here\n" + "'}'\n"; //NOI18N

    // {0} = fully qualified service class name ( e.g. "org.netbeans.FooSeWebService")
    // {1} = service variable name ( e.g. "fooService")
    // {2} = fully qualified port name (as type, e.g. com.service.FooPort)
    // {3} = port varialbe name (e.g., "port")
    // {4} = port getter method (e.g., getFooPort)
    // {5} = operation name
    // {6} = argument array
    // {7} = lookup object
    // {8} = result statement (return type and variable, e.g., MyClass myvar =
    private static final String INVOKE_JAXRPC_BODY =
            //"\t\tjavax.naming.InitialContext ic = new javax.naming.InitialContext();\n" +
            //"\t\t{0} {1} = ({0}) ic.lookup(\"java:comp/env/service/{7}\");\n" +
            "\t\t{0} {1} = new {0}_Impl();\n" +
            "\t\t{2} {3} = {1}.{4}();\n" +
            "\t\t{8} {3}.{5}({6});\n";

    /**
     * Determines the initialization value of a variable of type "type"
     * @param type Type of the variable
     * @param targetFile FileObject containing the class that declares the type
     */
    protected static String resolveInitValue(String type) {
        if (type.startsWith("javax.xml.ws.Holder")) {
            //NOI18N
            return "new " + type + "();";
        }
        if ("int".equals(type) || "long".equals(type) || "short".equals(type) || "byte".equals(type)) {
            //NOI18N
            return "0;"; //NOI18N
        }
        if ("boolean".equals(type)) {
            //NOI18N
            return "false;"; //NOI18N
        }
        if ("float".equals(type) || "double".equals(type)) {
            //NOI18N
            return "0.0;"; //NOI18N
        }
        if ("java.lang.String".equals(type)) {
            //NOI18N
            return "\"\";"; //NOI18N
        }
        if (type.endsWith("CallbackHandler")) {
            //NOI18N
            return "new " + type + "();"; //NOI18N
        }
        if (type.startsWith("javax.xml.ws.AsyncHandler")) {
            //NOI18N
            return "new " + type + "() {"; //NOI18N
        }

        return "null;"; //NOI18N
    }

    /**
     * Determines the initialization value of a variable of type "type"
     * @param type Type of the variable
     * @param targetFile FileObject containing the class that declares the type
     */
    protected static String resolveInitValue(ParameterInfo p) {
        String type = p.getTypeName();
        Object defaultVal = p.getDefaultValue();
        if (type.startsWith("javax.xml.ws.Holder")) {
            //NOI18N
            return "new " + type + "();";
        }
        if ("int".equals(type) || "long".equals(type) || "short".equals(type) || "byte".equals(type) ||
                "java.lang.Integer".equals(type) || "java.lang.Long".equals(type) ||
                "java.lang.Short".equals(type) || "java.lang.Byte".equals(type)) {
            //NOI18N
            try {
                int val = Integer.parseInt((String) defaultVal);
                return String.valueOf(val) + ";";
            } catch (Exception ex) {
            }
            return "0;"; //NOI18N
        }
        if ("boolean".equals(type) || "java.lang.Boolean".equals(type)) {
            //NOI18N
            return defaultVal.toString() + ";";
        }
        if ("float".equals(type) || "double".equals(type) ||
                "java.lang.Float".equals(type) || "java.lang.Double".equals(type)) {
            //NOI18N
            try {
                double val = Double.parseDouble((String) defaultVal);
                return String.valueOf(val) + ";";
            } catch (Exception ex) {
            }
            return "0.0;"; //NOI18N
        }
        if ("java.lang.String".equals(type)) {
            //NOI18N
            if (defaultVal != null && defaultVal instanceof String) {
                return "\"" + (String) defaultVal + "\";";
            }
            return "\"\";"; //NOI18N
        }
        if (type.endsWith("CallbackHandler")) {
            //NOI18N
            return "new " + type + "();"; //NOI18N
        }
        if (type.startsWith("javax.xml.ws.AsyncHandler")) {
            //NOI18N
            return "new " + type + "() {"; //NOI18N
        }

        return "null;"; //NOI18N
    }

    protected static String resolveResponseType(String argumentType) {
        int start = argumentType.indexOf("<");
        int end = argumentType.indexOf(">");
        if (start > 0 && end > 0 && start < end) {
            return argumentType.substring(start + 1, end);
        } else {
            return "javax.xml.ws.Response"; //NOI18N
        }
    }
    public static final String SET_HEADER_PARAMS_CALL = SET_HEADER_PARAMS + "(port); \n";

    protected String getJavaInvocationBody(WSOperation operation,
            boolean insertServiceDef, String serviceJavaName, String portJavaName,
            String portGetterMethod, String argumentInitializationPart,
            String returnTypeName, String operationJavaName, String argumentDeclarationPart,
            String serviceFieldName, String printerName, String responseType) {

        String invocationBody = INDENT_2;
        String setHeaderParams = getBean().getHeaderParameters().size() > 0 ? SET_HEADER_PARAMS_CALL : "";
        Object[] args = new Object[]{
            serviceJavaName, portJavaName, portGetterMethod,
            argumentInitializationPart, returnTypeName, operationJavaName,
            argumentDeclarationPart, serviceFieldName, printerName,
            getResultPattern(), findNewName(serviceJavaName + " " + VAR_NAMES_SERVICE, VAR_NAMES_SERVICE),
            findNewName(portJavaName + " " + VAR_NAMES_PORT, VAR_NAMES_PORT)
        };
        switch (operation.getOperationType()) {
            case WSOperation.TYPE_NORMAL: {
                if ("void".equals(returnTypeName)) {
                    //NOI18N
                    String body = (insertServiceDef ? JAVA_SERVICE_DEF : "") + setHeaderParams + JAVA_PORT_DEF + JAVA_VOID;
                    invocationBody += MessageFormat.format(body, args);
                } else {
                    String body = (insertServiceDef ? JAVA_SERVICE_DEF : "") + JAVA_PORT_DEF + setHeaderParams + JAVA_RESULT + JAVA_OUT;
                    invocationBody += MessageFormat.format(body, args);
                }
                break;
            }
            case WSOperation.TYPE_ASYNC_POLLING: {
                invocationBody += MessageFormat.format(JAVA_STATIC_STUB_ASYNC_POLLING, args);
                break;
            }
            case WSOperation.TYPE_ASYNC_CALLBACK: {
                args[7] = responseType;
                invocationBody += MessageFormat.format(JAVA_STATIC_STUB_ASYNC_CALLBACK, args);
                break;
            }
        }
        return invocationBody;
    }

    protected static String findProperServiceFieldName(Set serviceFieldNames) {
        String name = "service";
        int i = 0;
        while (serviceFieldNames.contains(name)) {
            name = "service_" + String.valueOf(++i);
        }
        return name; //NOI18N
    }

    private ClassTree addSetHeaderParamsMethod(WorkingCopy copy, ClassTree tree, String portJavaType) {
        Modifier[] modifiers = JavaUtil.PRIVATE;
        String[] annotations = new String[0];
        Object[] annotationAttrs = new Object[0];
        Object returnType = Constants.VOID;
        String bodyText = "{ WSBindingProvider bp = (WSBindingProvider)port;";
        bodyText += "bp.setOutboundHeaders(";
        boolean first = true;
        for (ParameterInfo pinfo : getBean().getHeaderParameters()) {
            if (pinfo.getDefaultValue() == null) {
                continue;
            }
            if (first) {
                first = false;
            } else {
                bodyText += ", \n ";
            }
            String namespaceUri = pinfo.getQName().getNamespaceURI();
            bodyText += "Headers.create(new QName(";
            if (namespaceUri != null) {
                bodyText += "\"" + namespaceUri + "\",";
            }
            bodyText += "\"" + pinfo.getName() + "\"), \"" + pinfo.getDefaultValue() + "\")";
        }
        bodyText += ");";
        String[] parameters = new String[]{findNewName(portJavaType + " " + VAR_NAMES_PORT, VAR_NAMES_PORT)};
        Object[] paramTypes = new Object[]{portJavaType};
        String[] paramAnnotations = new String[0];
        Object[] paramAnnotationAttrs = new String[0];
        String comment = null;

        return JavaSourceHelper.addMethod(copy, tree,
                modifiers, annotations, annotationAttrs,
                SET_HEADER_PARAMS, returnType, parameters, paramTypes, //NOI18N
                paramAnnotations, paramAnnotationAttrs,
                bodyText, comment);      //NOI18N
    }
}
