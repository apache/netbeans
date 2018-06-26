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
package org.netbeans.modules.websvc.core.jaxws.actions;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ImportTree;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import javax.swing.JEditorPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.StyledDocument;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.modules.j2ee.api.ejbjar.Car;
import org.netbeans.modules.websvc.api.support.InvokeOperationCookie;
import org.openide.util.Lookup;
import static org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.progress.ProgressUtils;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.editor.BaseDocument;

import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.editor.indent.api.Indent;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.websvc.api.jaxws.client.JAXWSClientSupport;
import org.netbeans.modules.websvc.api.jaxws.project.config.Client;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlOperation;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlParameter;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlPort;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlService;
import org.netbeans.modules.websvc.core.JaxWsUtils;
import org.netbeans.modules.websvc.core.jaxws.nodes.JaxWsClientNode;
import org.netbeans.modules.websvc.core.jaxws.nodes.OperationNode;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.text.NbDocument;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/** JaxWsCodeGenerator.java
 *
 * Created on March 2, 2006
 *
 * @author mkuchtiak
 */
public class JaxWsCodeGenerator  {
    
    private static final String POLICY_MANAGER = "policyManager";   // NOI18N
    
    private static final JaxWsCodeGenerator INSTANCE = new JaxWsCodeGenerator();

    private static final List IMPLICIT_JSP_OBJECTS = Arrays.asList(new String[]{
                "request", "response", "session", "out", "page", "config", "application", "pageContext" //NOI18N
            });
    private static final String HINT_INIT_ARGUMENTS = " // TODO initialize WS operation arguments here\n"; //NOI18N

    // {0} = service java name (as variable, e.g. "AddNumbersService")
    // {1} = port java name (e.g. "AddNumbersPort")
    // {2} = port getter method (e.g. "getAddNumbersPort")
    // {3} = argument initialization part (e.g. "int x=0; int y=0;")
    // {4} = java result type (e.g. "int")
    // {5} = operation java method name (e.g. "add")
    // {6} = java method arguments (e.g. "int x, int y")
    // {7} = service field name
    // {8} = PrintStream instance identifier
    // {9} = array of WebServiceFeatures
    static final String JAVA_TRY =
            "\ntry '{' // Call Web Service Operation\n"; //NOI18N
    static final String JAVA_SERVICE_DEF =
            "   {0} {7} = new {0}();\n"; //NOI18N
    static final String JAVA_PORT_DEF =
            "   {1} port = {7}.{2}({9});\n"; //NOI18N
    static final String JAVA_RESULT =
            "   {3}" + //NOI18N
            "   // TODO process result here\n" + //NOI18N
            "   {4} result = port.{5}({6});\n"; //NOI18N
    static final String JAVA_VOID =
            "   {3}" + //NOI18N
            "   port.{5}({6});\n"; //NOI18N
    static final String JAVA_RESULT_1 =
            "   {3}" + //NOI18N
            "   return port.{5}({6});\n"; //NOI18N
    static final String JAVA_OUT =
            "   {8}.println(\"Result = \"+result);\n"; //NOI18N
    static final String JAVA_CATCH =
            "'}' catch (Exception ex) '{'\n" + //NOI18N
            "   // TODO handle custom exceptions here\n" + //NOI18N
            "'}'\n"; //NOI18N
    // {0} = service java name (as variable, e.g. "AddNumbersService")
    // {1} = port java name (e.g. "AddNumbersPort")
    // {2} = port getter method (e.g. "getAddNumbersPort")
    // {3} = argument initialization part (e.g. "int x=0; int y=0;")
    // {4} = java result type (e.g. "int")
    // {5} = operation java method name (e.g. "add")
    // {6} = java method arguments (e.g. "int x, int y")
    // {7}-{8} is not used 
    // {9} = array of WebServiceFeatures
    static final String JAVA_STATIC_STUB_ASYNC_POLLING =
            "\ntry '{' // Call Web Service Operation(async. polling)\n" + //NOI18N
            "   {0} service = new {0}();\n" + //NOI18N
            "   {1} port = service.{2}({9});\n" + //NOI18N
            "   {3}" + //NOI18N
            "   // TODO process asynchronous response here\n" + //NOI18N
            "   {4} resp = port.{5}({6});\n" + //NOI18N
            "   while(!resp.isDone()) '{'\n" + //NOI18N
            "       // do something\n" + //NOI18N
            "       Thread.sleep(100);\n" + //NOI18N
            "   '}'\n" + //NOI18N
            "   System.out.println(\"Result = \"+resp.get());\n" + //NOI18N
            "'}' catch (Exception ex) '{'\n" + //NOI18N
            "   // TODO handle custom exceptions here\n" + //NOI18N
            "'}'\n"; //NOI18N
    // {0} = service java name (as variable, e.g. "AddNumbersService")
    // {1} = port java name (e.g. "AddNumbersPort")
    // {2} = port getter method (e.g. "getAddNumbersPort")
    // {3} = argument initialization part (e.g. "int x=0; int y=0;")
    // {4} = java result type (e.g. "int")
    // {5} = operation java method name (e.g. "add")
    // {6} = java method arguments (e.g. "int x, int y")
    // {7} = response type (e.g. FooResponse)
    // {8} is not used
    // {9} = array of WebServiceFeatures
    static final String JAVA_STATIC_STUB_ASYNC_CALLBACK =
            "\ntry '{' // Call Web Service Operation(async. callback)\n" + //NOI18N
            "   {0} service = new {0}();\n" + //NOI18N
            "   {1} port = service.{2}({9});\n" + //NOI18N
            "   {3}" + //NOI18N
            "       public void handleResponse(javax.xml.ws.Response<{7}> response) '{'\n" + //NOI18N
            "           try '{'\n" + //NOI18N
            "               // TODO process asynchronous response here\n" + //NOI18N
            "               System.out.println(\"Result = \"+ response.get());\n" + //NOI18N
            "           '}' catch(Exception ex) '{'\n" + //NOI18N
            "               // TODO handle exception\n" + //NOI18N
            "           '}'\n" + //NOI18N
            "       '}'\n" + //NOI18N
            "   '}';\n" + //NOI18N
            "   {4} result = port.{5}({6});\n" + //NOI18N
            "   while(!result.isDone()) '{'\n" + //NOI18N
            "       // do something\n" + //NOI18N
            "       Thread.sleep(100);\n" + //NOI18N
            "   '}'\n" + //NOI18N
            "'}' catch (Exception ex) '{'\n" + //NOI18N
            "   // TODO handle custom exceptions here\n" + //NOI18N
            "'}'\n"; //NOI18N
    // {0} = service java name (as variable, e.g. "AddNumbersService")
    // {1} = port java name (e.g. "AddNumbersPort")
    // {2} = port getter method (e.g. "getAddNumbersPort")
    // {3} = argument initialization part (e.g. "int x=0; int y=0;")
    // {4} = java result type (e.g. "int")
    // {5} = operation java method name (e.g. "add")
    // {6} = java method arguments (e.g. "int x, int y")
    private static final String JSP_STATIC_STUB =
            "    <%-- start web service invocation --%><hr/>\n" + //NOI18N
            "    <%\n" + //NOI18N
            "    try '{'\n" + //NOI18N
            "\t{0} service = new {0}();\n" + //NOI18N
            "\t{1} port = service.{2}();\n" + //NOI18N
            "{3}" + //NOI18N
            "\t// TODO process result here\n" + //NOI18N
            "\t{4} result = port.{5}({6});\n" + //NOI18N
            "\tout.println(\"Result = \"+result);\n" + //NOI18N
            "    '}' catch (Exception ex) '{'\n" + //NOI18N
            "\t// TODO handle custom exceptions here\n" + //NOI18N
            "    '}'\n" + //NOI18N
            "    %>\n" + //NOI18N
            "    <%-- end web service invocation --%><hr/>\n"; //NOI18N
    // {0} = service java name (as variable, e.g. "AddNumbersService")
    // {1} = port java name (e.g. "AddNumbersPort")
    // {2} = port getter method (e.g. "getAddNumbersPort")
    // {3} = argument initialization part (e.g. "int x=0; int y=0;")
    // {5} = operation java method name (e.g. "add")
    // {6} = java method arguments (e.g. "int x, int y")
    private static final String JSP_STATIC_STUB_VOID =
            "    <%-- start web service invocation --%><hr/>\n" + //NOI18N
            "    <%\n" + //NOI18N
            "    try '{'\n" + //NOI18N
            "\t{0} service = new {0}();\n" + //NOI18N
            "\t{1} port = service.{2}();\n" + //NOI18N
            "{3}" + //NOI18N
            "\tport.{5}({6});\n" + //NOI18N
            "    '}' catch (Exception ex) '{'\n" + //NOI18N
            "\t// TODO handle custom exceptions here\n" + //NOI18N
            "    '}'\n" + //NOI18N
            "    %>\n" + //NOI18N
            "    <%-- end web service invocation --%><hr/>\n"; //NOI18N
    // {0} = service java name (as variable, e.g. "AddNumbersService")
    // {1} = port java name (e.g. "AddNumbersPort")
    // {2} = port getter method (e.g. "getAddNumbersPort")
    // {3} = argument initialization part (e.g. "int x=0; int y=0;")
    // {4} = java result type (e.g. "int")
    // {5} = operation java method name (e.g. "add")
    // {6} = java method arguments (e.g. "int x, int y")
    private static final String JSP_STATIC_STUB_ASYNC_POLLING =
            "    <%-- start web service invocation(async. polling) --%><hr/>\n" + //NOI18N
            "    <%\n" + //NOI18N
            "    try '{'\n" + //NOI18N
            "\t{0} service = new {0}();\n" + //NOI18N
            "\t{1} port = service.{2}();\n" + //NOI18N
            "{3}" + //NOI18N
            "\t// TODO process asynchronous response here\n" + //NOI18N
            "\t{4} resp = port.{5}({6});\n" + //NOI18N
            "\twhile(!resp.isDone()) '{'\n" + //NOI18N
            "\t\t// do something\n" + //NOI18N
            "\t\tThread.sleep(100);\n" + //NOI18N
            "\t'}'\n" + //NOI18N
            "\tout.println(\"Result = \"+resp.get());\n" + //NOI18N
            "    '}' catch (Exception ex) '{'\n" + //NOI18N
            "\t// TODO handle custom exceptions here\n" + //NOI18N
            "    '}'\n" + //NOI18N
            "    %>\n" + //NOI18N
            "    <%-- end web service invocation(async. polling) --%><hr/>\n"; //NOI18N            
    // {0} = service java name (as variable, e.g. "AddNumbersService")
    // {1} = port java name (e.g. "AddNumbersPort")
    // {2} = port getter method (e.g. "getAddNumbersPort")
    // {3} = argument initialization part (e.g. "int x=0; int y=0;")
    // {4} = java result type (e.g. "int")
    // {5} = operation java method name (e.g. "add")
    // {6} = java method arguments (e.g. "int x, int y")
    private static final String JSP_STATIC_STUB_ASYNC_CALLBACK =
            "    <%-- start web service invocation(async. callback) --%><hr/>\n" + //NOI18N
            "    <%\n" + //NOI18N
            "    try '{'\n" + //NOI18N
            "\t{0} service = new {0}();\n" + //NOI18N
            "\t{1} port = service.{2}();\n" + //NOI18N
            "{3}" + //NOI18N
            "\t// TODO process asynchronous response here\n" + //NOI18N
            "\t{4} result = port.{5}({6});\n" + //NOI18N
            "\twhile(!result.isDone()) '{'\n" + //NOI18N
            "\t\t// do something\n" + //NOI18N
            "\t\tThread.sleep(100);\n" + //NOI18N
            "\t'}'\n" + //NOI18N
            "\tout.println(\"Result = \"+asyncHandler.getResponse());\n" + //NOI18N
            "    '}' catch (Exception ex) '{'\n" + //NOI18N
            "\t// TODO handle custom exceptions here\n" + //NOI18N
            "    '}'\n" + //NOI18N
            "    %>\n" + //NOI18N
            "    <%-- end web service invocation(async. callback) --%><hr/>\n"; //NOI18N
    // {0} = handler name (as type, e.g. "FooCallbackHandler")
    // {1} = response type (e.g. FooResponse)
    private static final String JSP_CALLBACK_HANDLER =
            "<%!\n" + //NOI18N
            "class {0} implements javax.xml.ws.AsyncHandler<{1}> '{'\n" + //NOI18N
            "    private {1} output;\n" + //NOI18N
            "\n" + //NOI18N
            "    public void handleResponse(javax.xml.ws.Response<{1}> response) '{'\n" + //NOI18N
            "        try '{'\n" + //NOI18N
            "            output = response.get();\n" + //NOI18N
            "        '}' catch(Exception ex) '{'\n" + //NOI18N
            "            // TODO handle exception\n" + //NOI18N
            "        '}'\n" + //NOI18N
            "    '}'\n" + //NOI18N
            "\n" + //NOI18N
            "    {1} getResponse() '{'\n" + //NOI18N
            "         return output;\n" + //NOI18N
            "    '}'\n" + //NOI18N
            "'}'\n" + //NOI18N
            "%>\n"; //NOI18N
    private static final String QNAME =
            "\nQName portQName = new QName(\"{0}\" , \"{1}\"); ";
    // {0} = service java name (as variable, e.g. "AddNumbersService")
    // {1} =namespace URI of port
    // {2} = java port name 
    // {3} = XML message string
    private static final String JSP_DISPATCH =
            "    <%-- start web service invocation --%><hr/>\n" + //NOI18N
            "    <%\n" + //NOI18N
            "    try '{'\n" + //NOI18N
            "\t{0} service = new {0}();\n" + //NOI18N
            "\tjavax.xml.namespace.QName portQName = new javax.xml.namespace.QName(\"{1}\", \"{2}\");\n" +
            "\tString req = \"{3}\";\n" +
            "\tjavax.xml.ws.Dispatch<javax.xml.transform.Source> sourceDispatch = null;\n" +
            "\tsourceDispatch = service.createDispatch(portQName, javax.xml.transform.Source.class, javax.xml.ws.Service.Mode.PAYLOAD);\n" +
            "\tjavax.xml.transform.Source result = sourceDispatch.invoke(new javax.xml.transform.stream.StreamSource(new java.io.StringReader(req)));\n" +
            "    '}' catch (Exception ex) '{'\n" + //NOI18N
            "\t// TODO handle custom exceptions here\n" + //NOI18N
            "    '}'\n" + //NOI18N
            "    %>\n" + //NOI18N
            "    <%-- end web service invocation --%><hr/>\n"; //NOI18N


    public static void insertMethodCall(InvokeOperationCookie.TargetSourceType targetSourceType,
            DataObject dataObj, Lookup sourceNodeLookup) {

        EditorCookie cookie = dataObj.getCookie(EditorCookie.class);
        OperationNode opNode = sourceNodeLookup.lookup(OperationNode.class);
        boolean inJsp = InvokeOperationCookie.TargetSourceType.JSP == targetSourceType;
        Node portNode = opNode.getParentNode();
        Node serviceNode = portNode.getParentNode();
        addProjectReference(serviceNode, dataObj);
        final Document document;
        int position = -1;
        if (inJsp) {
            //TODO:
            //this should be handled differently, see issue 60609
            document = cookie.getDocument();
            try {
                String content = document.getText(0, document.getLength());
                position = content.lastIndexOf("</body>"); //NOI18N
                if (position < 0) {
                    position = content.lastIndexOf("</html>"); //NOI18N
                }
                if (position >= 0) { //find where line begins
                    while (position > 0 && content.charAt(position - 1) != '\n' && content.charAt(position - 1) != '\r') {
                        position--;
                    }
                } else {
                    position = document.getLength();
                }
            } catch (BadLocationException ble) {
                Exceptions.printStackTrace(ble);
            }
        } else {
            EditorCookie ec = dataObj.getCookie(EditorCookie.class);
            JEditorPane pane = ec.getOpenedPanes()[0];
            document = pane.getDocument();
            position = pane.getCaretPosition();
        }
        final int pos = position;
        insertMethod(document, pos, opNode);
    }

    private static void addProjectReference(Node serviceNode, DataObject dObj) {
        Node clientNode = serviceNode.getParentNode();
        FileObject srcRoot = clientNode.getLookup().lookup(FileObject.class);
        Project clientProject = FileOwnerQuery.getOwner(srcRoot);
        if (dObj != null) {
            FileObject targetFo = dObj.getPrimaryFile();
            JaxWsUtils.addProjectReference(clientProject, targetFo);
        }
    }

    /**
     * Determines the initialization value of a variable of type "type"
     * @param type Type of the variable
     * @param targetFile FileObject containing the class that declares the type
     */
    private static String resolveInitValue(final String type, FileObject targetFile) {
        if ("int".equals(type)) { //NOI18N
            return "0;"; //NOI18N
        } else if ("long".equals(type)) { //NOI18N
            return "0L;"; //NOI18N
        } else if ("float".equals(type)) { //NOI18N
            return "0.0f;"; //NOI18N
        } else if ("double".equals(type)) { //NOI18N
            return "0.0d;"; //NOI18N
        } else if ("short".equals(type)) { //NOI18N
            return "(short)0;"; //NOI18N
        } else if ("byte".equals(type)) { //NOI18N
            return "(byte)0;"; //NOI18N
        } else if ("boolean".equals(type)) { //NOI18N
            return "false;"; //NOI18N
        } else if ("java.lang.String".equals(type)) { //NOI18N
            return "\"\";"; //NOI18N
        } else if ("java.lang.Integer".equals(type)) { //NOI18N
            return "Integer.valueOf(0);"; //NOI18N
        } else if ("java.lang.Long".equals(type)) { //NOI18N
            return "Long.valueOf(0L);"; //NOI18N
        } else if ("java.lang.Float".equals(type)) { //NOI18N
            return "Float.valueOf(0.0f);"; //NOI18N
        } else if ("java.lang.Double".equals(type)) { //NOI18N
            return "Double.valueOf(0.0d);"; //NOI18N
        } else if ("java.lang.Short".equals(type)) { //NOI18N
            return "Short.valueOf((short)0);"; //NOI18N
        } else if ("java.lang.Byte".equals(type)) { //NOI18N
            return "Byte.valueOf((byte)0);"; //NOI18N
        } else if ("java.lang.Boolean".equals(type)) { //NOI18N
            return "Boolean.FALSE;"; //NOI18N
        } else if (type.endsWith("CallbackHandler")) { //NOI18N
            return "new " + type + "();"; //NOI18N
        } else if (type.startsWith("javax.xml.ws.AsyncHandler")) { //NOI18N
            return "new " + type + "() {"; //NOI18N
        }

        ResultHolder<String> result = new ResultHolder<String>("");
        getInitValue(type, targetFile, result);
        String returnText = result.getResult();
        if (!returnText.equals("")) {
            return returnText;
        }

        return "null;"; //NOI18N
    }

    private static void getInitValue(final String type, FileObject targetFile, final ResultHolder<String> result) {
        if (targetFile == null) {
            return;
        }
        JavaSource targetSource = JavaSource.forFileObject(targetFile);

        if (targetSource == null) {
            result.setResult("null;"); //NOI18N
            return;
        }
        
        CancellableTask<CompilationController> task = new CancellableTask<CompilationController>() {

            public void run(CompilationController controller) throws IOException {
                controller.toPhase(Phase.ELEMENTS_RESOLVED);
                if (!isEnum(controller, type)) {
                    if (hasNoArgConstructor(controller, type)) {
                        result.setResult("new " + type + "();");//NOI18N
                    }
                }
            }

            public void cancel() {
            }
        };
        try {
            targetSource.runUserActionTask(task, true);
        } catch (IOException e) {
            ErrorManager.getDefault().notify(e);
        }
    }

    private static boolean isEnum(CompilationController controller, String type) {
        TypeElement classEl = controller.getElements().getTypeElement(getCanonicalClassName(type));
        if (classEl != null) {
            return classEl.getKind() == ElementKind.ENUM;
        }
        return false;
    }

    private static boolean hasNoArgConstructor(CompilationController controller, String type) {
        TypeElement classEl = controller.getElements().getTypeElement(getCanonicalClassName(type));
        if (classEl != null) {
            List<ExecutableElement> constructors = ElementFilter.constructorsIn(classEl.getEnclosedElements());
            for (ExecutableElement c : constructors) {
                if (c.getParameters().size() == 0) {
                    return true;
                }
            }
        }
        return false;
    }

    /** Holder class for result
     */
    private static class ResultHolder<E> {

        private E result;

        public ResultHolder(E result) {
            this.result = result;
        }

        public E getResult() {
            return result;
        }

        public void setResult(E result) {
            this.result = result;
        }
    }

    private static String getCanonicalClassName(String genericClassName) {
        int index = genericClassName.indexOf("<");
        if (index != -1) {
            return genericClassName.substring(0, index);
        }
        return genericClassName;
    }

    private static String resolveResponseType(String argumentType) {
        int start = argumentType.indexOf("<");
        int end = argumentType.indexOf(">");
        if (start > 0 && end > 0 && start < end) {
            return argumentType.substring(start + 1, end);
        } else {
            return "javax.xml.ws.Response";
        } //NOI18N
    }

    private static String pureJavaName(String javaNameWithPackage) {
        int index = javaNameWithPackage.lastIndexOf(".");
        return index >= 0 ? javaNameWithPackage.substring(index + 1) : javaNameWithPackage;
    }
    
    public static void insertMethod(final Document document, final int pos, 
            final OperationNode operationNode) 
    {
        WsdlOperation operation = operationNode.getLookup().lookup(WsdlOperation.class);
        Runnable runnable = new Runnable() {

            @Override
            public void run() {
                doInsertMethod(document, pos, operationNode);
            }
        };
        ProgressUtils.showProgressDialogAndRun(runnable, 
                NbBundle.getMessage(JaxWsCodeGenerator.class, 
                "MSG_GenerateMethod", operation.getName()));              // NOI18N
    }

    public static void doInsertMethod(final Document document, final int pos, 
            OperationNode operationNode) 
    {
        Node portNode = operationNode.getParentNode();
        Node serviceNode = portNode.getParentNode();
        Node wsdlNode = serviceNode.getParentNode();
        WsdlOperation operation = operationNode.getLookup().lookup(WsdlOperation.class);
        WsdlPort port = portNode.getLookup().lookup(WsdlPort.class);
        WsdlService service = serviceNode.getLookup().lookup(WsdlService.class);
        Client client = wsdlNode.getLookup().lookup(Client.class);
        JaxWsClientNode wsClientNode = wsdlNode.getLookup().lookup( JaxWsClientNode.class );
        FileObject srcRoot = wsdlNode.getLookup().lookup(FileObject.class);
        
        JAXWSClientSupport clientSupport = JAXWSClientSupport.getJaxWsClientSupport(srcRoot);
        FileObject wsdlFileObject = null;
        
        FileObject localWsdlocalFolder = clientSupport.getLocalWsdlFolderForClient(
                client.getName(),false);
        if (localWsdlocalFolder!=null) {
            String relativePath = client.getLocalWsdlFile();
            if (relativePath != null) {
                wsdlFileObject=localWsdlocalFolder.getFileObject(relativePath);
            }
        }
        
        FileObject documentFileObject = NbEditorUtilities.getFileObject(document);
        String wsdlUrl = findWsdlLocation(client, documentFileObject);
        
        Project targetProject = FileOwnerQuery.getOwner(documentFileObject);

        Map<String,Object> context = (Map<String,Object>)wsClientNode.getValue(
                JaxWsClientNode.CONTEXT);
        if ( context == null ){
            context = new HashMap<String, Object>();
            wsClientNode.setValue(JaxWsClientNode.CONTEXT, context);
        }

        insertMethod(client , document, pos, service, port, operation, 
                wsdlFileObject, wsdlUrl, context, targetProject );
    }
    
    private static void insertMethod(final Document document, final int pos,
            WsdlService service, WsdlPort port, WsdlOperation operation, 
            FileObject wsdlFileObject, String wsdlUrl, PolicyManager manager) 
    {

        boolean inJsp = "text/x-jsp".equals(document.getProperty("mimeType")); //NOI18N
        // First, collect name of method, port, and service:

        final String serviceJavaName;
        String serviceFieldName;
        String portJavaName, portGetterMethod, operationJavaName, returnTypeName;
        String responseType = "Object"; //NOI18N
        String callbackHandlerName = "javax.xml.ws.AsyncHandler"; //NOI18N
        String argumentInitializationPart, argumentDeclarationPart;
        String[] paramNames;
        String[] paramTypes;
        String[] exceptionTypes;

        try {
            serviceFieldName = "service"; //NOI18N
            operationJavaName = operation.getJavaName();
            portJavaName = port.getJavaName();
            portGetterMethod = port.getPortGetter();
            serviceJavaName = service.getJavaName();
            List<WsdlParameter> arguments = operation.getParameters();
            returnTypeName = operation.getReturnTypeName();
            Iterator<String> it = operation.getExceptions();
            List<String> exceptionList = new ArrayList<String>();
            while (it.hasNext()) {
                exceptionList.add(it.next());
            }
            StringBuffer argumentBuffer1 = new StringBuffer();
            StringBuffer argumentBuffer2 = new StringBuffer();
            List<String> paramTypesList = new ArrayList<String>();
            List<String> paramNamesList = new ArrayList<String>();
            int argSize = arguments.size();
            for (int i = 0; i < argSize; i++) {
                String argumentTypeName = ((WsdlParameter) arguments.get(i)).getTypeName();
                if (argumentTypeName.startsWith("javax.xml.ws.AsyncHandler")) { //NOI18N
                    responseType = resolveResponseType(argumentTypeName);
                    if (inJsp) {
                        argumentTypeName = pureJavaName(portJavaName) + "CallbackHandler";
                    } //NOI18N
                    callbackHandlerName = argumentTypeName;
                }
                String argumentName = ((WsdlParameter) arguments.get(i)).getName();
                if (inJsp && IMPLICIT_JSP_OBJECTS.contains(argumentName)) {
                    argumentName = argumentName + "_1"; //NOI18N
                }
                argumentBuffer1.append("\t" + argumentTypeName + " " + argumentName +
                        " = " + resolveInitValue(argumentTypeName,
                        NbEditorUtilities.getFileObject(document)) + "\n"); //NOI18N
                argumentBuffer2.append(i > 0 ? ", " + argumentName : argumentName); //NOI18N
                paramTypesList.add(argumentTypeName);
                paramNamesList.add(argumentName);
            }
            paramTypes = new String[argSize];
            paramTypesList.toArray(paramTypes);
            paramNames = new String[argSize];
            paramNamesList.toArray(paramNames);
            exceptionTypes = new String[exceptionList.size()];
            exceptionList.toArray(exceptionTypes);

            argumentInitializationPart = (argumentBuffer1.length() > 0 ? "\t" + 
                    HINT_INIT_ARGUMENTS + argumentBuffer1.toString() : "");
            argumentDeclarationPart = argumentBuffer2.toString();

        } catch (NullPointerException npe) {
            // !PW notify failure to extract service information.
            npe.printStackTrace();
            String message = NbBundle.getMessage(JaxWsCodeGenerator.class, 
                    "ERR_FailedUnexpectedWebServiceDescriptionPattern"); // NOI18N
            NotifyDescriptor desc = new NotifyDescriptor.Message(message, 
                    NotifyDescriptor.Message.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(desc);
            return;
        }

        manager.chosePolicy();
        // including code to JSP
        if (inJsp) {
            insertJspMethod(document, pos, operation, serviceJavaName,
                    portJavaName, portGetterMethod, operationJavaName,
                    returnTypeName, responseType, callbackHandlerName,
                    argumentInitializationPart, argumentDeclarationPart);
        }
        else {
            insertJavaMethod(document, pos, operation, wsdlUrl, manager,
                serviceJavaName, serviceFieldName, portJavaName,
                portGetterMethod, operationJavaName, returnTypeName,
                responseType, argumentInitializationPart,
                argumentDeclarationPart, paramNames, paramTypes, exceptionTypes);
        }
    }

    @org.netbeans.api.annotations.common.SuppressWarnings("NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
    private static void insertJavaMethod( final Document document,
            final int pos, WsdlOperation operation, String wsdlUrl,
            PolicyManager manager, final String serviceJavaName,
            String serviceFieldName, String portJavaName,
            String portGetterMethod, String operationJavaName,
            String returnTypeName, String responseType,
            String argumentInitializationPart, String argumentDeclarationPart,
            String[] paramNames, String[] paramTypes, String[] exceptionTypes )
    {
        // including code to java class
        final FileObject targetFo = NbEditorUtilities.getFileObject(document);

        JavaSource targetSource = JavaSource.forFileObject(targetFo);
        String respType = responseType;
        final String[] argumentInitPart = {argumentInitializationPart};
        final String[] argumentDeclPart = {argumentDeclarationPart};
        final String[] serviceFName = {serviceFieldName};

        try {
            CompilerTask task = getCompilerTask(serviceJavaName, serviceFName,
                    argumentDeclPart, paramNames, argumentInitPart, manager);
            targetSource.runUserActionTask(task, true);
            
            if (WsdlOperation.TYPE_NORMAL == operation.getOperationType()) {
                String body = task.getMethodBody(portJavaName, 
                        portGetterMethod, returnTypeName, operationJavaName);
                // generate static method when @WebServiceRef injection is missing, or for J2ee Client application
                boolean isStatic = !task.isWsRefInjection() || 
                    (Car.getCar(targetFo) != null);
                
                String webServiceRefWarning = task.isWsRefInjection() ? 
                        NbBundle.getMessage(JaxWsCodeGenerator.class, "WRN_WebServiceRef") : ""; //NOI18N
                JaxWsClientMethodGenerator clientMethodGenerator =
                    new JaxWsClientMethodGenerator (
                            isStatic,
                            operationJavaName,
                            returnTypeName,
                            paramTypes,
                            paramNames,
                            exceptionTypes,
                            "{ "+webServiceRefWarning+body+"}"); //NOI18N

                targetSource.runModificationTask(clientMethodGenerator).commit();
            } else {
                // create & format inserted text
                final String invocationBody = task.getJavaInvocationBody(
                        operation,
                        portJavaName,
                        portGetterMethod,
                        returnTypeName,
                        operationJavaName,
                        respType);

                final Indent indent = Indent.get(document);
                indent.lock();
                try {
                    ((BaseDocument)document).runAtomic(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                System.out.println("............. 11111111111 ............. ");
                                document.insertString(pos, invocationBody, null);
                                indent.reindent(pos, pos+invocationBody.length());
                            } catch (BadLocationException ex) {
                                Exceptions.printStackTrace(ex);
                            }
                        }
                    }); 
                } finally {
                    indent.unlock();
                }
            }

            // modify Class f.e. @insert WebServiceRef injection
            InsertTask modificationTask = getClassModificationTask(serviceJavaName, 
                    serviceFName, wsdlUrl, manager , task.containsWsRefInjection());
            targetSource.runModificationTask(modificationTask).commit();
        } catch (IOException ioe) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ioe);
        }
    }

    private static void insertJspMethod( final Document document,
            final int pos, WsdlOperation operation,
            final String serviceJavaName, String portJavaName,
            String portGetterMethod, String operationJavaName,
            String returnTypeName, String responseType,
            String callbackHandlerName, String argumentInitializationPart,
            String argumentDeclarationPart )
    {
        // invocation
        Object[] args = new Object[]{
            serviceJavaName,
            portJavaName,
            portGetterMethod,
            argumentInitializationPart,
            returnTypeName,
            operationJavaName,
            argumentDeclarationPart
        };
        final String invocationBody = getJSPInvocationBody(operation, args);
        try {
            if (WsdlOperation.TYPE_ASYNC_CALLBACK == operation.getOperationType()) {
                Object[] args1 = new Object[]{
                    callbackHandlerName,
                    responseType
                };
                final String methodBody = MessageFormat.format(JSP_CALLBACK_HANDLER, args1);
                // insert 2 parts in one atomic action
                NbDocument.runAtomic((StyledDocument) document, new Runnable() {

                    public void run() {
                        try {
                            document.insertString(document.getLength(), methodBody, null);
                            document.insertString(pos, invocationBody, null);
                        } catch (javax.swing.text.BadLocationException ex) {
                        }
                    }
                });
            } else {
                document.insertString(pos, invocationBody, null);
            }


        } catch (javax.swing.text.BadLocationException ex) {
        }
    }
    
    private static InsertTask getClassModificationTask(String serviceJavaName, 
            String[] serviceFName, String wsdlUrl , PolicyManager manager , 
            boolean  containsWsRefInjection )
    {
        return new InsertTask(serviceJavaName, serviceFName[0], wsdlUrl, 
                manager, containsWsRefInjection );
    }
    
    private static CompilerTask getCompilerTask(String serviceJavaName, 
            String[] serviceFName, String[] argumentDeclPart, String[] paramNames, 
            String[] argumentInitPart, PolicyManager manager )
    {
        return new CompilerTask(serviceJavaName, serviceFName,argumentDeclPart, 
                paramNames, argumentInitPart, manager );
    }

    private static String findWsdlLocation(Client client, FileObject targetFo) {
        Project targetProject = FileOwnerQuery.getOwner(targetFo);
        J2eeModuleProvider moduleProvider = targetProject.getLookup().lookup(J2eeModuleProvider.class);
        if (moduleProvider != null && J2eeModule.Type.WAR.equals(moduleProvider.getJ2eeModule().getType())) {
            return "WEB-INF/wsdl/" + client.getLocalWsdlFile(); //NOI18N
        } else {
            return "META-INF/wsdl/" + client.getLocalWsdlFile(); //NOI18N
        }
    }
    
    private static void insertMethod(Client client, Document document, int pos, 
            WsdlService service, WsdlPort port, WsdlOperation operation, 
            FileObject wsdlFileObject, String wsdlUrl, 
            Map<String, Object> context ,  Project project )
    {
        PolicyManager manager  = (PolicyManager)context.get( POLICY_MANAGER );
        if ( manager == null || !client.getWsdlUrl().equals( manager.getWsdlUrl())){
            manager = new PolicyManager(client.getWsdlUrl(), wsdlFileObject , 
                    project );
            context.put(POLICY_MANAGER, manager);
        }
        manager.init( client );
        if (client.getUseDispatch()) {
            insertDispatchMethod(document, pos, service, port, operation, 
                    wsdlFileObject , wsdlUrl, manager);
        } else {
            insertMethod(document, pos, service, port, operation,
                    wsdlFileObject , wsdlUrl, manager);
        }
    }

    private static String getJSPInvocationBody(WsdlOperation operation, Object[] args) {
        String invocationBody = "";
        switch (operation.getOperationType()) {
            case WsdlOperation.TYPE_NORMAL: {
                if ("void".equals(args[4])) {
                    invocationBody = MessageFormat.format(JSP_STATIC_STUB_VOID, args);
                } else {
                    invocationBody = MessageFormat.format(JSP_STATIC_STUB, args);
                }
                break;
            }
            case WsdlOperation.TYPE_ASYNC_POLLING: {
                invocationBody = MessageFormat.format(JSP_STATIC_STUB_ASYNC_POLLING, args);
                break;
            }
            case WsdlOperation.TYPE_ASYNC_CALLBACK: {
                invocationBody = MessageFormat.format(JSP_STATIC_STUB_ASYNC_CALLBACK, args);
                break;
            }
            default:
        }
        return invocationBody;
    }

    static boolean foundImport(String importStatement, CompilationUnitTree tree) {
        List<? extends ImportTree> imports = tree.getImports();
        for (ImportTree imp : imports) {
            if (importStatement.equals(imp.getQualifiedIdentifier().toString())) {
                return true;
            }
        }
        return false;
    }

    public static String generateXMLMessage(WsdlPort port, WsdlOperation operation) {
        StringBuffer message = new StringBuffer("");

        String operationName = operation.getOperationName();
        String namespace = port.getNamespaceURI();
        message.append("<");
        message.append(operationName);
        message.append("  xmlns=\\\"");
        message.append(namespace);
        message.append("\\\">");
        List<WsdlParameter> parameters = operation.getParameters();
        for (WsdlParameter parameter : parameters) {
            String name = parameter.getName();
            message.append("<");
            message.append(name);
            message.append(">");
            message.append("ENTER VALUE");
            message.append("</");
            message.append(name);
            message.append(">");
        }
        message.append("</");
        message.append(operationName);
        message.append(">");
        return message.toString();
    }

    private static String getDispatchInvocationMethod(WsdlPort port, WsdlOperation operation) {
        StringBuffer invoke = new StringBuffer("");
        invoke.append(MessageFormat.format(QNAME, new Object[]{port.getNamespaceURI(), port.getName()}));
        invoke.append("\n");
        invoke.append("String req = ");
        invoke.append("\"");
        invoke.append(generateXMLMessage(port, operation));
        invoke.append("\";\n");
        invoke.append(MessageFormat.format(JAVA_TRY, new Object[]{}));
        invoke.append("\n");
        invoke.append("Dispatch<Source> sourceDispatch = null;\n");
        invoke.append("sourceDispatch = service.createDispatch(portQName, Source.class, Service.Mode.PAYLOAD);\n");
        invoke.append("Source result = sourceDispatch.invoke(new StreamSource(new StringReader(req)));\n");
        invoke.append(MessageFormat.format(JAVA_CATCH, new Object[]{}));
        invoke.append("\n");
        return invoke.toString();
    }

    private static String getJSPDispatchBody(Object[] args) {
        return MessageFormat.format(JSP_DISPATCH, args);
    }
    
/*     public static String getWSInvocationCode(FileObject target, boolean inJsp,
            WsdlService service, WsdlPort port, WsdlOperation operation) {

        // First, collect name of method, port, and service:

        final String serviceJavaName;
        String serviceFieldName;
        String portJavaName, portGetterMethod, operationJavaName, returnTypeName;
        String responseType = "Object"; //NOI18N

        String callbackHandlerName = "javax.xml.ws.AsyncHandler"; //NOI18N

        String argumentInitializationPart, argumentDeclarationPart;

        try {
            serviceFieldName = "service"; //NOI18N

            operationJavaName = operation.getJavaName();
            portJavaName = port.getJavaName();
            portGetterMethod = port.getPortGetter();
            serviceJavaName = service.getJavaName();
            List arguments = operation.getParameters();
            returnTypeName = operation.getReturnTypeName();
            StringBuffer argumentBuffer1 = new StringBuffer();
            StringBuffer argumentBuffer2 = new StringBuffer();
            for (int i = 0; i < arguments.size(); i++) {
                String argumentTypeName = ((WsdlParameter) arguments.get(i)).getTypeName();
                if (argumentTypeName.startsWith("javax.xml.ws.AsyncHandler")) { //NOI18N

                    responseType = resolveResponseType(argumentTypeName);
                    if (inJsp) {
                        argumentTypeName = pureJavaName(portJavaName) + "CallbackHandler";
                    } //NOI18N

                    callbackHandlerName = argumentTypeName;
                }
                String argumentName = ((WsdlParameter) arguments.get(i)).getName();
                if (inJsp && IMPLICIT_JSP_OBJECTS.contains(argumentName)) {
                    argumentName = argumentName + "_1"; //NOI18N

                }
                argumentBuffer1.append("\t" + argumentTypeName + " " + argumentName + " = " + resolveInitValue(argumentTypeName,
                        target) + "\n"); //NOI18N

                argumentBuffer2.append(i > 0 ? ", " + argumentName : argumentName); //NOI18N

            }
            argumentInitializationPart = (argumentBuffer1.length() > 0 ? "\t" + HINT_INIT_ARGUMENTS + argumentBuffer1.toString() : "");
            argumentDeclarationPart = argumentBuffer2.toString();

        } catch (NullPointerException npe) {
            // !PW notify failure to extract service information.
            npe.printStackTrace();
            String message = NbBundle.getMessage(JaxWsCodeGenerator.class, "ERR_FailedUnexpectedWebServiceDescriptionPattern"); // NOI18N

            NotifyDescriptor desc = new NotifyDescriptor.Message(message, NotifyDescriptor.Message.ERROR_MESSAGE);
            Displayer.getDefault().notify(desc);
            return "";
        }

        // including code to JSP
        if (inJsp) {
            // invocation
            Object[] args = new Object[]{
                serviceJavaName,
                portJavaName,
                portGetterMethod,
                argumentInitializationPart,
                returnTypeName,
                operationJavaName,
                argumentDeclarationPart
            };
            String invocationBody = getJSPInvocationBody(operation, args);
            if (WsdlOperation.TYPE_ASYNC_CALLBACK == operation.getOperationType()) {
                Object[] args1 = new Object[]{
                    callbackHandlerName,
                    responseType
                };
                String methodBody = MessageFormat.format(JSP_CALLBACK_HANDLER, args1);
                invocationBody = invocationBody + "\n\n" + methodBody;

                
            }
            return invocationBody;
        }   


            final JavaSource targetSource = JavaSource.forFileObject(target);
            String respType = responseType;
            final String[] argumentInitPart = {argumentInitializationPart};
            final String[] argumentDeclPart = {argumentDeclarationPart};
            final String[] serviceFName = {serviceFieldName};

            RequestProcessor rp = new RequestProcessor(JaxWsCodeGenerator.class.getName());
            //try {
            final CompilerTask task = new CompilerTask(serviceJavaName, serviceFName,
                    argumentDeclPart, argumentInitPart);
            rp.post(new Runnable() {

                public void run() {
                    try {
                        targetSource.runUserActionTask(task, true);
                    } catch (IOException ex) {
                        Logger.getLogger(JaxWsCodeGenerator.class.getName()).log(Level.FINE, "cannot parse " + serviceJavaName + " class", ex); //NOI18N

                    }
                }
            });

            // create the inserted text
            String javaInvocationBody = task.getJavaInvocationBody(
                    operation,
                    portJavaName,
                    portGetterMethod,
                    returnTypeName,
                    operationJavaName,
                    respType);
            
            return javaInvocationBody;

        //}
    }*/

    private static void insertDispatchMethod(final Document document, final int pos,
            WsdlService service, WsdlPort port, WsdlOperation operation, 
            FileObject wsdlFileObject, String wsdlUrl, PolicyManager manager ) 
    {
        boolean inJsp = "text/x-jsp".equals(document.getProperty("mimeType")); //NOI18N
        if (inJsp) {
            Object[] args = new Object[]{service.getJavaName(), 
                    port.getNamespaceURI(), port.getJavaName(), generateXMLMessage(port, operation)};
            final String invocationBody = getJSPDispatchBody(args);
            try {
                document.insertString(pos, invocationBody, null);
            } catch (javax.swing.text.BadLocationException ex) {
                ErrorManager.getDefault().notify(ex);
            }
            return;
        }
        try {
            insertDispatchJavaMethod(document, pos, service, port, operation,
                    wsdlUrl, manager);

        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    @org.netbeans.api.annotations.common.SuppressWarnings("NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
    private static void insertDispatchJavaMethod( final Document document,
            final int pos, WsdlService service, WsdlPort port,
            WsdlOperation operation, String wsdlUrl, PolicyManager manager )
            throws IOException
    {
        final FileObject targetFo = NbEditorUtilities.getFileObject(document);
        JavaSource targetSource = JavaSource.forFileObject(targetFo);

        String serviceJavaName = service.getJavaName();
        String[] serviceFName = new String[]{"service"};
        String[] argumentDeclPart = new String[]{""};
        String[] argumentInitPart = new String[]{""};
        CompilerTask compilerTask = getCompilerTask(serviceJavaName, serviceFName, 
                argumentDeclPart, new String[0], argumentInitPart, manager);
        targetSource.runUserActionTask(compilerTask, true);
        
        StringBuilder methodBody = new StringBuilder();

        String serviceDeclForJava = "";
        if (compilerTask.containsWsRefInjection()) { //if in J2SE
            Object[] args = new Object[]{service.getJavaName(), null, null, 
                    null, null, null, null, "service"}; //TODO: compute proper var name
            serviceDeclForJava = MessageFormat.format(JAVA_SERVICE_DEF, args);
            methodBody.append( serviceDeclForJava );
        } 
        // create the inserted text
        String invocationBody = getDispatchInvocationMethod(port, operation);
        
        DispatchClientMethodGenerator generator = new DispatchClientMethodGenerator(
                operation.getJavaName(), 
                methodBody.append( invocationBody ).toString(), pos );
        ModificationResult result = targetSource.runModificationTask(generator);
        if (generator.isMethodBody()) {
            final String insideMethodBody = serviceDeclForJava+invocationBody;
            final Indent indent = Indent.get(document);
            indent.lock();
            try {
                ((BaseDocument)document).runAtomic(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            document.insertString(pos, insideMethodBody, null);
                            indent.reindent(pos, pos+insideMethodBody.length());
                        } catch (BadLocationException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                }); 
            } finally {
                indent.unlock();
            }
        }
        else {
            result.commit();
        }

        // modify Class f.e. @insert WebServiceRef injection
        InsertTask modificationTask = getClassModificationTask(serviceJavaName, 
                serviceFName, wsdlUrl, manager, compilerTask.containsWsRefInjection());
        targetSource.runModificationTask(modificationTask).commit();

        DispatchCompilerTask task = new DispatchCompilerTask();
        targetSource.runModificationTask(task).commit();
    }

}
