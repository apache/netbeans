/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.websvc.saas.codegen.j2ee;

import java.io.IOException;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.modules.websvc.saas.codegen.Constants;
import org.netbeans.modules.websvc.saas.codegen.j2ee.support.J2eeUtil;
import org.netbeans.modules.websvc.saas.codegen.model.SaasBean;
import org.netbeans.modules.websvc.saas.codegen.util.Util;
import org.netbeans.modules.websvc.saas.model.SaasMethod;
import org.netbeans.modules.websvc.saas.model.WsdlSaasMethod;

/**
 * Code generator for Accessing Saas services.
 *
 * @author ayubskhan
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.websvc.saas.codegen.spi.SaasClientCodeGenerationProvider.class)
public class SoapClientJspCodeGenerator extends SoapClientServletCodeGenerator {

    public SoapClientJspCodeGenerator() {
        setDropFileType(Constants.DropFileType.JSP);
    }
    
    @Override
    public boolean canAccept(SaasMethod method, Document doc) {
        if (SaasBean.canAccept(method, WsdlSaasMethod.class, getDropFileType()) &&
                Util.isJsp(doc)) {
            return true;
        }
        return false;
    }

    /**
     *  Insert the Saas client call
     */
    @Override
    protected void insertSaasServiceAccessCode(boolean isInBlock) throws IOException {
        addVariablePattern(J2eeUtil.JSP_NAMES_PAGE, 1);
        try {
            String code = "";
            code += J2eeUtil.getJspImports(getTargetDocument(), getStartPosition(), 
                    getBean().getSaasServicePackageName());
            code += J2eeUtil.wrapWithTag(getCustomMethodBody(), getTargetDocument(), getStartPosition()) + "\n";
            insert(code, true);
        } catch (BadLocationException ex) {
            throw new IOException(ex.getMessage());
        }
    }
    
}
