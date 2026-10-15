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
                !wsdlUrl.startsWith(FILE))
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
