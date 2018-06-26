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
package org.netbeans.modules.maven.jaxws.actions;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.ImportTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.StyledDocument;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.modules.editor.indent.api.Indent;
import org.netbeans.modules.maven.jaxws.nodes.OperationNode;
import org.netbeans.modules.websvc.api.support.java.SourceUtils;
import org.netbeans.modules.websvc.jaxws.light.api.JaxWsService;
import static org.netbeans.api.java.source.JavaSource.Phase;
import static com.sun.source.tree.Tree.Kind.*;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.java.source.ui.ScanDialog;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.editor.BaseDocument;

import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.javaee.injection.api.InjectionTargetQuery;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlOperation;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlParameter;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlPort;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlService;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
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
public class JaxWsCodeGenerator {

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
    private static final String JAVA_TRY =
            "\ntry '{' // Call Web Service Operation\n"; //NOI18N
    private static final String JAVA_SERVICE_DEF =
            "   {0} {7} = new {0}();\n"; //NOI18N
    private static final String JAVA_PORT_DEF =
            "   {1} port = {7}.{2}();\n"; //NOI18N
    private static final String JAVA_RESULT =
            "   {3}" + //NOI18N
            "   // TODO process result here\n" + //NOI18N
            "   {4} result = port.{5}({6});\n"; //NOI18N
    private static final String JAVA_VOID =
            "   {3}" + //NOI18N
            "   port.{5}({6});\n"; //NOI18N
    private static final String JAVA_OUT =
            "   {8}.println(\"Result = \"+result);\n"; //NOI18N
    private static final String JAVA_CATCH =
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
    private static final String JAVA_STATIC_STUB_ASYNC_POLLING =
            "\ntry '{' // Call Web Service Operation(async. polling)\n" + //NOI18N
            "   {0} service = new {0}();\n" + //NOI18N
            "   {1} port = service.{2}();\n" + //NOI18N
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
    private static final String JAVA_STATIC_STUB_ASYNC_CALLBACK =
            "\ntry '{' // Call Web Service Operation(async. callback)\n" + //NOI18N
            "   {0} service = new {0}();\n" + //NOI18N
            "   {1} port = service.{2}();\n" + //NOI18N
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

//    public static void insertMethodCall(int targetSourceType, DataObject dataObj, Node sourceNode, Node operationNode) {
//        EditorCookie cookie = sourceNode.getCookie(EditorCookie.class);
//        OperationNode opNode = operationNode.getLookup().lookup(OperationNode.class);
//        boolean inJsp = InvokeOperationCookie.TARGET_SOURCE_JSP == targetSourceType;
//        Node portNode = operationNode.getParentNode();
//        Node serviceNode = portNode.getParentNode();
////        addProjectReference(serviceNode, sourceNode);
//        final Document document;
//        int position = -1;
//        if (inJsp) {
//            //TODO:
//            //this should be handled differently, see issue 60609
//            document = cookie.getDocument();
//            try {
//                String content = document.getText(0, document.getLength());
//                position = content.lastIndexOf("</body>"); //NOI18N
//                if (position < 0) {
//                    position = content.lastIndexOf("</html>"); //NOI18N
//                }
//                if (position >= 0) { //find where line begins
//                    while (position > 0 && content.charAt(position - 1) != '\n' && content.charAt(position - 1) != '\r') {
//                        position--;
//                    }
//                } else {
//                    position = document.getLength();
//                }
//            } catch (BadLocationException ble) {
//                Exceptions.printStackTrace(ble);
//            }
//        } else {
//            EditorCookie ec = dataObj.getCookie(EditorCookie.class);
//            JEditorPane pane = ec.getOpenedPanes()[0];
//            document = pane.getDocument();
//            position = pane.getCaretPosition();
//        }
//        final int pos = position;
//        insertMethod(document, pos, opNode);
//    }

//    private static void addProjectReference(Node serviceNode, Node sourceNode) {
//        Node clientNode = serviceNode.getParentNode();
//        FileObject srcRoot = clientNode.getLookup().lookup(FileObject.class);
//        Project clientProject = FileOwnerQuery.getOwner(srcRoot);
//        DataObject dObj = sourceNode.getCookie(DataObject.class);
//        if (dObj != null) {
//            FileObject targetFo = dObj.getPrimaryFile();
//            JaxWsUtils.addProjectReference(clientProject, targetFo);
//        }
//    }

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

            @Override
            public void run(CompilationController controller) throws IOException {
                controller.toPhase(Phase.ELEMENTS_RESOLVED);
                TypeElement typeElement = SourceUtils.getPublicTopLevelElement(
                        controller);
                if ( typeElement == null ){
                    return;
                }
                if (!isEnum(controller, typeElement)) {
                    if (hasNoArgConstructor(controller, typeElement)) {
                        result.setResult("new " + type + "();");//NOI18N
                    }
                }
            }

            @Override
            public void cancel() {
            }
        };
        try {
            targetSource.runUserActionTask(task, true);
        } catch (IOException e) {
            ErrorManager.getDefault().notify(e);
        }
    }

    private static boolean isEnum(CompilationController controller, 
            TypeElement classEl) 
    {
        if (classEl != null) {
            return classEl.getKind() == ElementKind.ENUM;
        }
        return false;
    }

    private static boolean hasNoArgConstructor(CompilationController controller, 
            TypeElement classEl) 
    {
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

    public static void insertMethod(final Document document, final int pos, OperationNode operationNode) {
        Node portNode = operationNode.getParentNode();
        Node serviceNode = portNode.getParentNode();
        Node wsdlNode = serviceNode.getParentNode();
        WsdlOperation operation = operationNode.getLookup().lookup(WsdlOperation.class);
        WsdlPort port = portNode.getLookup().lookup(WsdlPort.class);
        WsdlService service = serviceNode.getLookup().lookup(WsdlService.class);
        JaxWsService client = wsdlNode.getLookup().lookup(JaxWsService.class);

        String wsdlUrl = findWsdlLocation(client, NbEditorUtilities.getFileObject(document));
        
//        if (client.getUseDispatch()) {
//            insertDispatchMethod(document, pos, service, port, operation, wsdlUrl);
//        } else {
            insertMethod(document, pos, service, port, operation, wsdlUrl);
//        }
    }

    public static void insertMethod(final Document document, final int pos,
            WsdlService service, WsdlPort port, WsdlOperation operation, String wsdlUrl) {

        boolean inJsp = "text/x-jsp".equals(document.getProperty("mimeType")); //NOI18N
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
                        NbEditorUtilities.getFileObject(document)) + "\n"); //NOI18N
                argumentBuffer2.append(i > 0 ? ", " + argumentName : argumentName); //NOI18N
            }
            argumentInitializationPart = (argumentBuffer1.length() > 0 ? "\t" + HINT_INIT_ARGUMENTS + argumentBuffer1.toString() : "");
            argumentDeclarationPart = argumentBuffer2.toString();

        } catch (NullPointerException npe) {
            // !PW notify failure to extract service information.
            npe.printStackTrace();
            String message = NbBundle.getMessage(JaxWsCodeGenerator.class, "ERR_FailedUnexpectedWebServiceDescriptionPattern"); // NOI18N
            NotifyDescriptor desc = new NotifyDescriptor.Message(message, NotifyDescriptor.Message.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(desc);
            return;
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
            final String invocationBody = getJSPInvocationBody(operation, args);
            
            if (WsdlOperation.TYPE_ASYNC_CALLBACK == operation.getOperationType()) {
                Object[] args1 = new Object[]{
                    callbackHandlerName,
                    responseType
                };
                final String methodBody = MessageFormat.format(JSP_CALLBACK_HANDLER, args1);
                // insert 2 parts in one atomic action
                NbDocument.runAtomic((StyledDocument) document, new Runnable() {

                    @Override
                    public void run() {
                        try {
                            document.insertString(document.getLength(), methodBody, null);
                            document.insertString(pos, invocationBody, null);
                        } catch (javax.swing.text.BadLocationException ex) {
                        }
                    }
                });
            } else {
                final Indent indent = Indent.get(document);
                indent.lock();
                try {
                    ((BaseDocument)document).runAtomic(new Runnable() {
                        @Override
                        public void run() {
                            try {
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

            return;
        }

        // including code to java class
        final FileObject targetFo = NbEditorUtilities.getFileObject(document);

        final JavaSource targetSource = JavaSource.forFileObject(targetFo);
        String respType = responseType;
        final String[] argumentInitPart = {argumentInitializationPart};
        final String[] argumentDeclPart = {argumentDeclarationPart};
        final String[] serviceFName = {serviceFieldName};

        try {
            final CompilerTask task = new CompilerTask(serviceJavaName, 
                    serviceFName,
                    argumentDeclPart, 
                    argumentInitPart);
            final Runnable runnable = new Runnable() {

                @Override
                public void run() {
                    try {
                        targetSource.runUserActionTask(task, true);
                    }
                    catch (IOException ex) {
                        Logger.getLogger(JaxWsCodeGenerator.class.getName()).log(
                            Level.FINE,
                            "cannot parse " + serviceJavaName + " class", ex); // NOI18N

                    }
                }
            };
            final String title = NbBundle.getMessage(JaxWsCodeGenerator.class, 
                    "LBL_ParseWsClass");            // NOI18N
            if ( SwingUtilities.isEventDispatchThread() ){
                ScanDialog.runWhenScanFinished( runnable, title);
            }
            else {
                try {
                    SwingUtilities.invokeAndWait( new Runnable() {
                    
                        @Override
                        public void run() {
                            ScanDialog.runWhenScanFinished( runnable, title);                        
                        }
                    });
                }
                catch ( InvocationTargetException e ){
                    Logger.getLogger(JaxWsCodeGenerator.class.getName()).log(
                            Level.WARNING, null, e );
                }
                catch( InterruptedException e ){
                    Logger.getLogger(JaxWsCodeGenerator.class.getName()).log(
                            Level.WARNING, null, e );
                }
            }
            // create and format inserted text
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
                            document.insertString(pos, invocationBody, null);
                            indent.reindent(pos, pos+invocationBody.length());
                        } catch (BadLocationException badLoc) {
                            Exceptions.printStackTrace(badLoc);
                        }
                    }
                });
            } finally {
                indent.unlock();
            }

            // @insert WebServiceRef injection
            if (!task.containsWsRefInjection()) {        
                // scan should be finished already due previous scan task
                InsertTask modificationTask = new InsertTask(serviceJavaName, serviceFName[0], wsdlUrl);
                targetSource.runModificationTask(modificationTask).commit();
            }
        } catch (IOException ioe) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ioe);
        }
    }

    private static String findWsdlLocation(JaxWsService client, FileObject targetFo) {
        Project targetProject = FileOwnerQuery.getOwner(targetFo);
        J2eeModuleProvider moduleProvider = targetProject.getLookup().lookup(J2eeModuleProvider.class);
        if (moduleProvider != null && J2eeModule.Type.WAR.equals(moduleProvider.getJ2eeModule().getType())) {
            return "WEB-INF/wsdl/"+ client.getLocalWsdl(); //NOI18N
        } else {
            return "META-INF/wsdl/"+client.getLocalWsdl(); //NOI18N
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
                break;
        }
        return invocationBody;
    }

    static final class CompilerTask implements CancellableTask<CompilationController> {

        private final boolean[] insertServiceDef = {true};
        private final boolean[] generateWsRefInjection = {false};
        private final String[] printerName = {"System.out"}; // NOI18N
        private final String serviceJavaName;
        private final String[] serviceFName;
        private final String[] argumentDeclPart;
        private final String[] argumentInitPart;

        public CompilerTask(String serviceJavaName, String[] serviceFName, 
                String[] argumentDeclPart, String[] argumentInitPart) 
        {
            this.serviceJavaName = serviceJavaName;
            this.argumentInitPart = argumentInitPart;
            this.argumentDeclPart = argumentDeclPart;
            this.serviceFName = serviceFName;
        }

        @Override
        public void run(CompilationController controller) throws IOException {
            controller.toPhase(Phase.ELEMENTS_RESOLVED);
            CompilationUnitTree cut = controller.getCompilationUnit();

            TypeElement thisTypeEl = SourceUtils.getPublicTopLevelElement(controller);
            if (thisTypeEl != null) {
                ClassTree javaClass = controller.getTrees().getTree(thisTypeEl);
                // find if class is Injection Target
                generateWsRefInjection[0] = InjectionTargetQuery.isInjectionTarget(controller, thisTypeEl);
//                if (generateWsRefInjection[0]) {
//                    // issue 126014 : check if J2EE Container supports EJBs (e.g. Tomcat 6 doesn't)
//                    Project project = FileOwnerQuery.getOwner(controller.getFileObject());
//                    generateWsRefInjection[0] = JaxWsUtils.isEjbSupported(project);
//                }

                insertServiceDef[0] = !generateWsRefInjection[0];
                if (isServletClass(controller, thisTypeEl)) {
                    // PENDING Need to compute pronter name from the method
                    printerName[0] = "out"; //NOI18N
                    argumentInitPart[0] = fixNamesInInitializationPart(argumentInitPart[0]);
                    argumentDeclPart[0] = fixNamesInDeclarationPart(argumentDeclPart[0]);
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
                            TreePath typeTreePath = controller.getTrees().getPath(cut, typeTree);
                            TypeElement typeEl = (TypeElement) controller.getTrees().getElement(typeTreePath);
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

        public String getJavaInvocationBody(
                WsdlOperation operation, String portJavaName,
                String portGetterMethod, String returnTypeName,
                String operationJavaName, String responseType) {
            String invocationBody = "";
            Object[] args = new Object[]{
                serviceJavaName, portJavaName,
                portGetterMethod, argumentInitPart[0],
                returnTypeName, operationJavaName,
                argumentDeclPart[0], serviceFName[0],
                printerName[0]
            };
            switch (operation.getOperationType()) {
                case WsdlOperation.TYPE_NORMAL: {
                    if ("void".equals(returnTypeName)) { //NOI18N
                        String body =
                                JAVA_TRY +
                                (insertServiceDef[0] ? JAVA_SERVICE_DEF : "") +
                                JAVA_PORT_DEF +
                                JAVA_VOID +
                                JAVA_CATCH;
                        invocationBody = MessageFormat.format(body, args);
                    } else {
                        String body =
                                JAVA_TRY +
                                (insertServiceDef[0] ? JAVA_SERVICE_DEF : "") +
                                JAVA_PORT_DEF +
                                JAVA_RESULT +
                                JAVA_OUT +
                                JAVA_CATCH;
                        invocationBody = MessageFormat.format(body, args);
                    }
                    break;
                }
                case WsdlOperation.TYPE_ASYNC_POLLING: {
                    invocationBody = MessageFormat.format(JAVA_STATIC_STUB_ASYNC_POLLING, args);
                    break;
                }
                case WsdlOperation.TYPE_ASYNC_CALLBACK: {
                    args[7] = responseType;
                    invocationBody = MessageFormat.format(JAVA_STATIC_STUB_ASYNC_CALLBACK, args);
                    break;
                }
            }
            return invocationBody;
        }

        public boolean containsWsRefInjection() {
            return !generateWsRefInjection[0];
        }

        private static boolean isServletClass(CompilationController controller, TypeElement typeElement) {
            return SourceUtils.isSubtype(controller, typeElement, "javax.servlet.http.HttpServlet"); // NOI18N
        }

        private static String fixNamesInInitializationPart(String argumentInitializationPart) {
            return argumentInitializationPart.replaceFirst(" request ", //NOI18N
                    " request_1 ").replaceFirst(" response ", //NOI18N
                    " response_1 ").replaceFirst(" out ", " out_1 "); //NOI18N
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

    static class InsertTask implements CancellableTask<WorkingCopy> {

        private final String serviceJavaName;
        private final String serviceFName;
        private final String wsdlUrl;

        public InsertTask(String serviceJavaName, String serviceFName, String wsdlUrl) {
            this.serviceJavaName = serviceJavaName;
            this.serviceFName = serviceFName;
            this.wsdlUrl = wsdlUrl;
        }

        @Override
        public void run(WorkingCopy workingCopy) throws IOException {
            workingCopy.toPhase(Phase.RESOLVED);
            TreeMaker make = workingCopy.getTreeMaker();
            ClassTree javaClass = SourceUtils.getPublicTopLevelTree(workingCopy);
            if (javaClass != null) {
                TypeElement wsRefElement = workingCopy.getElements().getTypeElement(
                        "javax.xml.ws.WebServiceRef"); //NOI18N
                AnnotationTree wsRefAnnotation = make.Annotation(
                        make.QualIdent(wsRefElement),
                        Collections.<ExpressionTree>singletonList(
                                make.Assignment(make.Identifier("wsdlLocation"), 
                                        make.Literal(wsdlUrl)))); //NOI18N
                // create field modifier: private(static) with @WebServiceRef annotation
//                FileObject targetFo = workingCopy.getFileObject();
                Set<Modifier> modifiers = new HashSet<Modifier>();
//                if (Car.getCar(targetFo) != null) {
//                    modifiers.add(Modifier.STATIC);
//                }
                modifiers.add(Modifier.PRIVATE);
                ModifiersTree methodModifiers = make.Modifiers(
                        modifiers,
                        Collections.<AnnotationTree>singletonList(wsRefAnnotation));
                TypeElement typeElement = workingCopy.getElements().getTypeElement(serviceJavaName);
                Tree type;
                if ( typeElement != null ){
                    type = make.Type(typeElement.asType());
                }
                else {
                    type = make.Type(serviceJavaName);
                }
                VariableTree serviceRefInjection = make.Variable(methodModifiers,
                            serviceFName,type,null);
                
                ClassTree modifiedClass = make.insertClassMember(javaClass, 0, serviceRefInjection);
                workingCopy.rewrite(javaClass, modifiedClass);
            }
        }

        @Override
        public void cancel() {
        }
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

    static final class DispatchCompilerTask implements CancellableTask<WorkingCopy> {

        @Override
        public void run(WorkingCopy workingCopy) throws Exception {
            boolean changed = false;
            workingCopy.toPhase(Phase.RESOLVED);
            TreeMaker make = workingCopy.getTreeMaker();
            CompilationUnitTree cut = workingCopy.getCompilationUnit();
            CompilationUnitTree copy = cut;
            if (!foundImport("javax.xml.namespace.QName", copy)) {
                copy = make.addCompUnitImport(copy,
                        make.Import(make.Identifier("javax.xml.namespace.QName"), false));
                changed = true;
            }
            if (!foundImport("javax.xml.transform.Source", copy)) {
                copy = make.addCompUnitImport(copy,
                        make.Import(make.Identifier("javax.xml.transform.Source"), false));
                changed = true;
            }
            if (!foundImport("javax.xml.ws.Dispatch", copy)) {
                copy = make.addCompUnitImport(copy,
                        make.Import(make.Identifier("javax.xml.ws.Dispatch"), false));
                changed = true;
            }
            if (!foundImport("javax.xml.transform.stream.StreamSource", copy)) {
                copy = make.addCompUnitImport(copy,
                        make.Import(make.Identifier("javax.xml.transform.stream.StreamSource"), false));
                changed = true;
            }
            if (!foundImport("javax.xml.ws.Service", copy)) {
                copy = make.addCompUnitImport(copy,
                        make.Import(make.Identifier("javax.xml.ws.Service"), false));
                changed = true;
            }
            if (!foundImport("java.io.StringReader", copy)) {
                copy = make.addCompUnitImport(copy,
                        make.Import(make.Identifier("java.io.StringReader"), false));
                changed = true;
            }
            if (changed) {
                workingCopy.rewrite(cut, copy);
            }
        }

        @Override
        public void cancel() {
        }
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
    
     public static String getWSInvocationCode(FileObject target, boolean inJsp,
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
            DialogDisplayer.getDefault().notify(desc);
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

            //try {
            final CompilerTask task = new CompilerTask(serviceJavaName, serviceFName,
                    argumentDeclPart, argumentInitPart);
            final Runnable runnable = new Runnable() {

                @Override
                public void run() {
                    try {
                        targetSource.runWhenScanFinished(task, true);
                    }
                    catch (IOException ex) {
                        Logger.getLogger(JaxWsCodeGenerator.class.getName()).log(
                            Level.FINE,
                            "cannot parse " + serviceJavaName + " class", ex); // NOI18N

                    }
                }
            };
            final String title = NbBundle.getMessage(JaxWsCodeGenerator.class, 
                    "LBL_ParseWsClass");            // NOI18N
            if ( SwingUtilities.isEventDispatchThread() ){
                ScanDialog.runWhenScanFinished( runnable, title);
            }
            else {
                try {
                    SwingUtilities.invokeAndWait( new Runnable() {
                    
                        @Override
                        public void run() {
                            ScanDialog.runWhenScanFinished( runnable, title);                        
                        }
                    });
                }
                catch ( InvocationTargetException e ){
                    Logger.getLogger(JaxWsCodeGenerator.class.getName()).log(
                            Level.WARNING, null, e );
                }
                catch( InterruptedException e ){
                    Logger.getLogger(JaxWsCodeGenerator.class.getName()).log(
                            Level.WARNING, null, e );
                }
            }
            

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
    }


//    public static void insertDispatchMethod(final Document document, final int pos,
//            WsdlService service, WsdlPort port, WsdlOperation operation, String wsdlUrl) {
//        boolean inJsp = "text/x-jsp".equals(document.getProperty("mimeType")); //NOI18N
//        if (inJsp) {
//            Object[] args = new Object[]{service.getJavaName(), port.getNamespaceURI(), port.getJavaName(), generateXMLMessage(port, operation)};
//            final String invocationBody = getJSPDispatchBody(args);
//            try {
//                document.insertString(pos, invocationBody, null);
//            } catch (javax.swing.text.BadLocationException ex) {
//                ErrorManager.getDefault().notify(ex);
//            }
//            return;
//        }
//        try {
//
//            final FileObject targetFo = NbEditorUtilities.getFileObject(document);
//            JavaSource targetSource = JavaSource.forFileObject(targetFo);
//
//            String serviceJavaName = service.getJavaName();
//            String[] serviceFName = new String[]{"service"};
//            String[] argumentDeclPart = new String[]{""};
//            String[] argumentInitPart = new String[]{""};
//            CompilerTask compilerTask = new CompilerTask(serviceJavaName, serviceFName, argumentDeclPart, argumentInitPart);
//            targetSource.runUserActionTask(compilerTask, true);
//
//
//            IndentEngine eng = IndentEngine.find(document);
//            StringWriter textWriter = new StringWriter();
//            Writer indentWriter = eng.createWriter(document, pos, textWriter);
//
//            if (compilerTask.containsWsRefInjection()) { //if in J2SE
//                Object[] args = new Object[]{service.getJavaName(), null, null, null, null, null, null, "service"}; //TODO: compute proper var name
//                String serviceDeclForJava = MessageFormat.format(JAVA_SERVICE_DEF, args);
//                indentWriter.write(serviceDeclForJava);
//            }
//            // create the inserted text
//            String invocationBody = getDispatchInvocationMethod(port, operation);
//            indentWriter.write(invocationBody);
//            indentWriter.close();
//            String textToInsert = textWriter.toString();
//
//            try {
//                document.insertString(pos, textToInsert, null);
//            } catch (BadLocationException badLoc) {
//                try {
//                    document.insertString(pos + 1, textToInsert, null);
//                } catch (BadLocationException ex) {
//                    ErrorManager.getDefault().notify(ex);
//                }
//            }
//
//            // @insert WebServiceRef injection
//            if (!compilerTask.containsWsRefInjection()) {
//                InsertTask modificationTask = new InsertTask(serviceJavaName, serviceFName[0], wsdlUrl);
//                targetSource.runModificationTask(modificationTask).commit();
//            }
//
//            DispatchCompilerTask task = new DispatchCompilerTask();
//            targetSource.runModificationTask(task).commit();
//
//
//        } catch (IOException ex) {
//            Exceptions.printStackTrace(ex);
//        }
//    }
}
