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
package org.netbeans.modules.websvc.saas.codegen.j2ee;

import com.sun.source.tree.ClassTree;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.Modifier;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.websvc.jaxwsmodelapi.WSParameter;
import org.netbeans.modules.websvc.rest.model.api.RestConstants;
import org.netbeans.modules.websvc.saas.codegen.Constants;
import org.netbeans.modules.websvc.saas.codegen.Constants.HttpMethodType;
import org.netbeans.modules.websvc.saas.codegen.Constants.MimeType;
import org.netbeans.modules.websvc.saas.codegen.util.Inflector;
import org.netbeans.modules.websvc.saas.codegen.java.support.JavaSourceHelper;
import org.netbeans.modules.websvc.saas.codegen.java.support.JavaUtil;
import org.netbeans.modules.websvc.saas.codegen.model.ParameterInfo;
import org.netbeans.modules.websvc.saas.codegen.model.SoapClientOperationInfo;
import org.openide.filesystems.FileObject;

/**
 *
 * @author rico
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.websvc.saas.codegen.spi.SaasClientCodeGenerationProvider.class)
public class RestWrapperForSoapClientGenerator extends SoapClientRestResourceCodeGenerator {

    public static final String COMMENT_END_OF_GET = "TODO return proper representation object";

    public RestWrapperForSoapClientGenerator() {
        super();
    }

    @Override
    public Set<FileObject> generate() throws IOException {
        FileObject implClassFo = this.getTargetFile();
        JavaSource targetSource = JavaSource.forFileObject(implClassFo);
        final String returnType = getBean().getOperationInfos()[0].getOperation().getReturnTypeName();
        CancellableTask<WorkingCopy> task = new CancellableTask<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                ClassTree javaClass = JavaSourceHelper.getTopLevelClassTree(workingCopy);
                ClassTree modifiedJavaClass = addGetMethod(MimeType.XML, returnType, workingCopy, javaClass);
                workingCopy.rewrite(javaClass, modifiedJavaClass);
            }

            public void cancel() {
            }
        };
        targetSource.runModificationTask(task).commit();


        return new HashSet<FileObject>(Collections.<FileObject>emptySet());
    }

    private ClassTree addGetMethod(MimeType mime, String returnType, WorkingCopy copy, ClassTree tree) throws IOException {
        Modifier[] modifiers = JavaUtil.PUBLIC;
        String variableName = "result";  //name of variable that will be returned
        String retType = returnType;
        if (retType.equals(Constants.VOID)) {  //if return type is void, find out if there are Holder paramters
            SoapClientOperationInfo[] info = getBean().getOperationInfos();
            List<WSParameter> parms = info[0].getOutputParameters();
            for (WSParameter parm : parms) {
                if (parm.isHolder()) {//TODO pick the first one right now. 
                                      //Should let user pick if there are multiple OUT parameters.
                    String holderType = parm.getTypeName();
                    int leftbracket = holderType.indexOf("<");
                    int rightbracket = holderType.lastIndexOf(">");
                    retType = holderType.substring(leftbracket + 1, rightbracket);
                    variableName = parm.getName() + ".value";
                    break;
                }
            }
        }
        String[] annotations = new String[]{
            RestConstants.GET_ANNOTATION,
            RestConstants.PRODUCE_MIME_ANNOTATION,
            RestConstants.PATH_ANNOTATION
        };



        Object[] annotationAttrs = new Object[]{
            null,
            mime.value(),
            Inflector.getInstance().camelize(getBean().getShortName(), true) + "/"
        };

        if (returnType == null) {
            returnType = String.class.getName();
        }
        if (retType.equals(Constants.VOID)) {
        }
        String bodyText = getSOAPClientInvocation(retType, variableName);

        List<ParameterInfo> queryParams = getBean().getQueryParameters();
        String[] parameters = getGetParamNames(queryParams);
        Object[] paramTypes = getGetParamTypes(queryParams);
        String[][] paramAnnotations = getGetParamAnnotations(queryParams);
        Object[][] paramAnnotationAttrs = getGetParamAnnotationAttrs(queryParams);

        String comment = "Invokes the SOAP method " + getBean().getShortName() + "\n";
        for (String param : parameters) {
            comment += "@param $PARAM$ resource URI parameter\n".replace("$PARAM$", param);
        }
        comment += "@return an instance of " + retType;

        return JavaSourceHelper.addMethod(copy, tree,
                modifiers, annotations, annotationAttrs,
                getMethodName(HttpMethodType.GET), retType, parameters, paramTypes,
                paramAnnotations, paramAnnotationAttrs,
                bodyText, comment);      //NOI18N

    }

    private String[][] getGetParamAnnotations(List<ParameterInfo> queryParams) {
        ArrayList<String[]> annos = new ArrayList<String[]>();

        for (String uriParam : getBean().getUriParams()) {
            annos.add(new String[]{RestConstants.PATH_PARAM_ANNOTATION});
        }

        String[] annotations = null;
        for (ParameterInfo param : queryParams) {
            if (param.getDefaultValue() != null) {
                annotations = new String[]{
                            RestConstants.QUERY_PARAM_ANNOTATION,
                            RestConstants.DEFAULT_VALUE_ANNOTATION
                        };
            } else {
                annotations = new String[]{RestConstants.QUERY_PARAM_ANNOTATION};
            }
            annos.add(annotations);
        }

        return annos.toArray(new String[annos.size()][]);
    }

    private Object[][] getGetParamAnnotationAttrs(List<ParameterInfo> queryParams) {
        ArrayList<Object[]> attrs = new ArrayList<Object[]>();

        for (String uriParam : getBean().getUriParams()) {
            attrs.add(new Object[]{uriParam});
        }

        Object[] annotationAttrs = null;
        for (ParameterInfo param : queryParams) {
            if (param.getDefaultValue() != null) {
                annotationAttrs = new Object[]{
                            param.getName(), param.getDefaultValue().toString()
                        };
            } else {
                annotationAttrs = new Object[]{param.getName()};
            }
            attrs.add(annotationAttrs);
        }

        return attrs.toArray(new Object[attrs.size()][]);
    }

    private String getMethodName(HttpMethodType methodType) {
        String methodName = Inflector.getInstance().camelize(getBean().getShortName(), true);
        if (methodName.startsWith(methodType.prefix())) {
            return methodName;
        }
        return methodType.prefix() + Inflector.getInstance().camelize(methodName);
    }

    @Override
    protected String getCustomMethodBody() throws IOException {
        String methodBody = INDENT;
        SoapClientOperationInfo[] operations = getBean().getOperationInfos();
        for (SoapClientOperationInfo info : operations) {
            methodBody += getWSInvocationCode(info);
        }

        return methodBody;
    }

    private String getSOAPClientInvocation(String typeName, String variableName) throws IOException {
        String code = "{\n";
        code += INDENT + "try {\n";
        code += getCustomMethodBody() + "\n";
        if (!typeName.equals(Constants.VOID)) {
            code += "return " + variableName + ";\n";
        }
        code += INDENT + "} catch (Exception ex) {\n";
        code += INDENT_2 + "ex.printStackTrace();\n";
        code += INDENT + "}\n";
        if (!typeName.equals(Constants.VOID)) {
            code += "return null;\n";  //TODO: will there be a case for primitive return types?

        }
        code += "}\n";
        return code;

    }
}
