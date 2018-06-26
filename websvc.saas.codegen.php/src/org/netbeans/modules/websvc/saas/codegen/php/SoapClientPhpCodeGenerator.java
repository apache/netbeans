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
package org.netbeans.modules.websvc.saas.codegen.php;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.modules.websvc.saas.codegen.SaasClientCodeGenerator;
import org.netbeans.modules.websvc.saas.codegen.model.ParameterInfo;
import org.netbeans.modules.websvc.saas.codegen.model.SoapClientOperationInfo;
import org.netbeans.modules.websvc.saas.codegen.model.SoapClientSaasBean;
import org.netbeans.modules.websvc.saas.codegen.php.util.PhpUtil;
import org.netbeans.modules.websvc.saas.model.SaasMethod;
import org.netbeans.modules.websvc.saas.model.WsdlSaasMethod;
import org.openide.filesystems.FileObject;

/**
 *
 * @author rico
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.websvc.saas.codegen.spi.SaasClientCodeGenerationProvider.class)
public class SoapClientPhpCodeGenerator extends SaasClientCodeGenerator {

    private static final String FILE = "file://";        // NOI18N

    @Override
    public boolean canAccept(SaasMethod method, Document doc) {
        if (method instanceof WsdlSaasMethod && PhpUtil.isPhp(doc)) {
            return true;
        }
        return false;
    }

    @Override
    public Set<FileObject> generate() throws IOException {
        try {
            insert(PhpUtil.wrapWithTag(getCustomMethodBody(), getTargetDocument(), getStartPosition()), true);

        } catch (BadLocationException ex) {
            throw new IOException(ex.getMessage());
        }
        return super.generate();
    }

    @Override
    public void init(SaasMethod method, Document doc) throws IOException {
        super.init(method, doc);
        setBean(new SoapClientSaasBean((WsdlSaasMethod) method, getProject()));

    }

    @Override
    public SoapClientSaasBean getBean() {
        return (SoapClientSaasBean) super.getBean();
    }

    private String genPhpParms(SoapClientSaasBean bean) {
        StringBuffer params = new StringBuffer("");
        List<ParameterInfo> parameters = bean.getInputParameters();
        for (ParameterInfo parameter : parameters) {
            String parmName = parameter.getName();
            String parmTypeName = parameter.getTypeName();
            Object value = parameter.getDefaultValue();
            String def = null;
            if ( value != null ){
                def= value.toString();
            }
            if (def != null) {
                params.append("'" + parmName + "'" + "=> \"" + def + "\", \n");
            } else {
                params.append("'" + parmName + "'" + "=> \"\",\n");
            }
        }
        return params.toString();
    }

    @Override
    protected String getCustomMethodBody() throws IOException {
        String indent2 = "                    ";
        String wsdlUrl = "";
        String methodName = "";
        SoapClientSaasBean bean = this.getBean();
        SoapClientOperationInfo[] infos = bean.getOperationInfos();
        if (infos.length > 0) {
            wsdlUrl = infos[0].getWsdlURL();
            methodName = infos[0].getOperationName();
        }
        if ( wsdlUrl.startsWith(FILE.substring(0, FILE.length() -1)) && 
                !wsdlUrl.startsWith(FILE.substring(0, FILE.length())))
        {             
            wsdlUrl = FILE + wsdlUrl.substring( FILE.length()-1);
        }
        String paramDecl = "$params = array( " + "\n" + genPhpParms(bean) + ");";

        String methodBody = "\n";
        methodBody += indent2 + "try {\n";
        methodBody += indent2 + "$wsdl_url = '" + wsdlUrl + "';\n";
        methodBody += indent2 + "$client     = new SOAPClient($wsdl_url);\n";
        methodBody += indent2 + paramDecl + "\n";
        methodBody += indent2 + "$return = $client->" + methodName + "($params);\n";
        methodBody += indent2 + "print_r($return);\n";
        methodBody += indent2 + "} catch(Exception $e) {\n";
        methodBody += indent2 + "    echo \"Exception occured: \".$e;\n";
        methodBody += indent2 + "}\n";
        return methodBody;
    }
}
