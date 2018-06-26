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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.websvc.core.jaxws.actions;

import static com.sun.source.tree.Tree.Kind.VARIABLE;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

import javax.lang.model.element.TypeElement;

import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.javaee.injection.api.InjectionTargetQuery;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlOperation;
import org.netbeans.modules.websvc.api.support.java.SourceUtils;
import org.netbeans.modules.websvc.core.JaxWsUtils;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;

/** This is the compilation task that provides various information about the inserting code
 *
 */
class CompilerTask implements CancellableTask<CompilationController> {

    private final boolean[] insertServiceDef = {true};
    private final boolean[] generateWsRefInjection = {false};
    private final String[] printerName = {"System.out"}; // NOI18N
    private final String serviceJavaName;
    private final String[] serviceFName;
    private final String[] argumentDeclPart;
    private final String[] paramNames;
    private final String[] argumentInitPart;
    private final PolicyManager manager;

    public CompilerTask(String serviceJavaName, String[] serviceFName, 
            String[] argumentDeclPart, String[] paramNames, 
            String[] argumentInitPart, PolicyManager manager ) 
    {
        this.serviceJavaName = serviceJavaName;
        this.argumentInitPart = argumentInitPart;
        this.argumentDeclPart = argumentDeclPart;
        this.paramNames = paramNames;
        this.serviceFName = serviceFName;
        this.manager = manager;
    }
    
    public void run(CompilationController controller) throws IOException {
        controller.toPhase(Phase.ELEMENTS_RESOLVED);
        CompilationUnitTree cut = controller.getCompilationUnit();

        TypeElement thisTypeEl = SourceUtils.getPublicTopLevelElement(controller);
        if (thisTypeEl != null) {
            ClassTree javaClass = controller.getTrees().getTree(thisTypeEl);
            // find if class is Injection Target
            generateWsRefInjection[0] = InjectionTargetQuery.isInjectionTarget(
                    controller, thisTypeEl);
            if (generateWsRefInjection[0]) {
                // issue 126014 : check if J2EE Container supports EJBs (e.g. Tomcat 6 doesn't)
                Project project = FileOwnerQuery.getOwner(controller.getFileObject());
                generateWsRefInjection[0] = JaxWsUtils.isEjbSupported(project);
            }

            insertServiceDef[0] = !generateWsRefInjection[0];
            if (isServletClass(controller, thisTypeEl)) {
                // PENDING Need to compute pronter name from the method
                printerName[0] = "out"; //NOI18N
                argumentInitPart[0] = fixNamesInInitializationPart(argumentInitPart[0]);
                argumentDeclPart[0] = fixNamesInDeclarationPart(argumentDeclPart[0]);
                fixNamesMethodParams( paramNames );
            }
            // compute the service field name
            if (generateWsRefInjection[0]) {
                Set<String> serviceFieldNames = new HashSet<String>();
                boolean injectionExists = false;
                int memberOrder = 0;
                for (Tree member : javaClass.getMembers()) {
                    // for the first inner class in top level
                    ++memberOrder;
                    if (VARIABLE == member.getKind()) {
                        // get variable type
                        VariableTree var = (VariableTree) member;
                        Tree typeTree = var.getType();
                        TreePath typeTreePath = controller.getTrees().
                            getPath(cut, typeTree);
                        TypeElement typeEl = (TypeElement) controller.getTrees().
                            getElement(typeTreePath);
                        if (typeEl != null) {
                            String variableType = typeEl.getQualifiedName().toString();
                            if (serviceJavaName.equals(variableType)) {
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
    }

    @Override
    public void cancel() {
    }
    
    public String getMethodBody(String portJavaName, String portGetterMethod, 
            String returnTypeName,String operationJavaName) {

        String methodBody = ""; //NOI18N
        Object[] args = getMethodBodyPortInitArguments(portJavaName, 
                portGetterMethod, 
                returnTypeName, operationJavaName);
        if ("void".equals(returnTypeName)) { //NOI18N
            String body =
                    (insertServiceDef[0] ? JaxWsCodeGenerator.JAVA_SERVICE_DEF : "") +
                    JaxWsCodeGenerator.JAVA_PORT_DEF + 
                    JaxWsCodeGenerator.JAVA_VOID;
            methodBody = MessageFormat.format(body, args);
        } else {
            String body =
                    (insertServiceDef[0] ? JaxWsCodeGenerator.JAVA_SERVICE_DEF : "") +
                    JaxWsCodeGenerator.JAVA_PORT_DEF + 
                    JaxWsCodeGenerator.JAVA_RESULT_1;
            methodBody = MessageFormat.format(body, args);
        }
        return methodBody;
    }
    
    public String getJavaInvocationBody(WsdlOperation operation, String portJavaName,
            String portGetterMethod, String returnTypeName, String operationJavaName, 
            String responseType) 
    {
        String invocationBody = "";
        Object[] args = getInvocationBodyPortInitArguments(portJavaName, 
                portGetterMethod, returnTypeName, operationJavaName);
        switch (operation.getOperationType()) {
            case WsdlOperation.TYPE_NORMAL: {
                if ("void".equals(returnTypeName)) { //NOI18N
                    String body =
                            JaxWsCodeGenerator.JAVA_TRY +
                            (insertServiceDef[0] ? JaxWsCodeGenerator.JAVA_SERVICE_DEF : "") +
                            JaxWsCodeGenerator.JAVA_PORT_DEF +
                            JaxWsCodeGenerator.JAVA_VOID +
                            JaxWsCodeGenerator.JAVA_CATCH;
                    invocationBody = MessageFormat.format(body, args);
                } else {
                    String body =
                            JaxWsCodeGenerator.JAVA_TRY +
                            (insertServiceDef[0] ? JaxWsCodeGenerator.JAVA_SERVICE_DEF : "") +
                            JaxWsCodeGenerator.JAVA_PORT_DEF +
                            JaxWsCodeGenerator.JAVA_RESULT +
                            JaxWsCodeGenerator.JAVA_OUT +
                            JaxWsCodeGenerator.JAVA_CATCH;
                    invocationBody = MessageFormat.format(body, args);
                }
                break;
            }
            case WsdlOperation.TYPE_ASYNC_POLLING: {
                invocationBody = MessageFormat.format(
                        JaxWsCodeGenerator.JAVA_STATIC_STUB_ASYNC_POLLING, args);
                break;
            }
            case WsdlOperation.TYPE_ASYNC_CALLBACK: {
                args[7] = responseType;
                invocationBody = MessageFormat.format(
                        JaxWsCodeGenerator.JAVA_STATIC_STUB_ASYNC_CALLBACK, args);
                break;
            }
            default:
        }
        return invocationBody;
    }

    public boolean containsWsRefInjection() {
        return !generateWsRefInjection[0];
    }

    public boolean isWsRefInjection() {
        return !insertServiceDef[0];
    }
    
    /*
     * 3rd element in the array could be modified with additional code.
     * It will be inserted after port initialization and before call operation on the port.
     */
    protected Object[] getMethodBodyPortInitArguments(String portJavaName, 
            String portGetterMethod, String returnTypeName,String operationJavaName)
    {
        Object[] result =  new Object[] { serviceJavaName, portJavaName,
                portGetterMethod, "", "", operationJavaName,
                argumentDeclPart[0], serviceFName[0], "", ""
            };
        if ( manager.isSupported() ){
            manager.modifyPortCallInitArguments( result );
        }
        return result;
    }
    
    /*
     * 3rd element in the array could be modified with additional code.
     * It will be inserted after port initialization and before call operation on the port.
     */
    protected Object[] getInvocationBodyPortInitArguments(String portJavaName, 
            String portGetterMethod, String returnTypeName,String operationJavaName)
    {
        Object[] result = new Object[] {serviceJavaName, portJavaName,portGetterMethod, 
                argumentInitPart[0],returnTypeName, operationJavaName,
                argumentDeclPart[0], serviceFName[0],printerName[0], ""
            };
        if ( manager.isSupported() ){
            manager.modifyPortInvocationInitArguments( result );
        }
        return result;
    }
    
    private static boolean isServletClass(CompilationController controller, TypeElement typeElement) {
        return SourceUtils.isSubtype(controller, typeElement, "javax.servlet.http.HttpServlet"); // NOI18N
    }

    private static String fixNamesInInitializationPart(String argumentInitializationPart) {
        return argumentInitializationPart.replaceFirst(" request ", //NOI18N
                " request_1 ").replaceFirst(" response ", //NOI18N
                " response_1 ").replaceFirst(" out ", " out_1 "); //NOI18N
    }
    
    private static void fixNamesMethodParams( String[] params ){
        for (int i=0; i<params.length ; i++) {
            if ("request".equals(params[i])) { //NOI18N
                params[i] = "request_1"; //NOI18N
            } else if ("response".equals(params[i])) { //NOI18N
                params[i] = "response_1"; //NOI18N
            } else if ("out".equals(params[i])) { //NOI18N
                params[i] = "out_1"; //NOI18N
            }
        }
    }

    private static String fixNamesInDeclarationPart(String argumentDeclarationPart) {
        StringTokenizer tok = new StringTokenizer(argumentDeclarationPart, " ,"); //NOI18N
        StringBuffer buf = new StringBuffer();
        int i = 0;
        while (tok.hasMoreTokens()) {
            String token = tok.nextToken();
            String newName = null;
            if ("request".equals(token)) { //NOI18N
                newName = "request_1"; //NOI18N
            } else if ("response".equals(token)) { //NOI18N
                newName = "response_1"; //NOI18N
            } else if ("out".equals(token)) { //NOI18N
                newName = "out_1"; //NOI18N
            } else {
                newName = token;
            }
            buf.append(i > 0 ? ", " + newName : newName); //NOI18N
            i++;
        }
        return buf.toString();
    }

    private static String findProperServiceFieldName(Set serviceFieldNames) {
        String name = "service"; //NOI18N
        int i = 0;
        while (serviceFieldNames.contains(name)) {
            name = "service_" + String.valueOf(++i); //NOI18N
        }
        return name;
    }
}
