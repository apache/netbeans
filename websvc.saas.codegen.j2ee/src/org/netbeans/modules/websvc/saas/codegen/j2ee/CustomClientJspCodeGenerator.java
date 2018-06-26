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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.websvc.saas.codegen.j2ee;

import org.netbeans.modules.websvc.saas.codegen.Constants;
import java.io.IOException;
import java.util.List;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.modules.websvc.saas.codegen.j2ee.support.J2eeUtil;
import org.netbeans.modules.websvc.saas.codegen.model.ParameterInfo;
import org.netbeans.modules.websvc.saas.codegen.util.Inflector;
import org.netbeans.modules.websvc.saas.codegen.model.SaasBean;
import org.netbeans.modules.websvc.saas.codegen.util.Util;
import org.netbeans.modules.websvc.saas.model.CustomSaasMethod;
import org.netbeans.modules.websvc.saas.model.SaasMethod;

/**
 * Code generator for Accessing Saas services.
 *
 * @author ayubskhan
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.websvc.saas.codegen.spi.SaasClientCodeGenerationProvider.class)
public class CustomClientJspCodeGenerator extends CustomClientServletCodeGenerator {

    public CustomClientJspCodeGenerator() throws IOException {
        setDropFileType(Constants.DropFileType.JSP);
    }

    @Override
    public boolean canAccept(SaasMethod method, Document doc) {
        if (SaasBean.canAccept(method, CustomSaasMethod.class, getDropFileType()) &&
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
        clearVariablePatterns();
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
    
    @Override
    protected String getCustomMethodBody() throws IOException {
        String paramUse = "";
        String paramDecl = "";
        String indent2 = "                 ";
        
        //Evaluate parameters (query(not fixed or apikey), header, template,...)
        List<ParameterInfo> params = getServiceMethodParameters();
        updateVariableNames(params);
        paramUse += Util.getHeaderOrParameterUsage(renameParameterNames(params));
        
        List<ParameterInfo> filterParams = J2eeUtil.filterJspParameters(super.getServiceMethodParameters());
        filterParams = renameParameterNames(filterParams);
        paramDecl += getHeaderOrParameterDeclaration(filterParams);
        return getCustomMethodBody(paramDecl, paramUse, getResultPattern(), indent2);
    }
    
    private ParameterInfo clone(ParameterInfo p, String name, Class type) {
        ParameterInfo clone = new ParameterInfo(name, type);
        clone.setFixed(p.getFixed());
        clone.setStyle(p.getStyle());
        clone.setDefaultValue(p.getDefaultValue());
        clone.setIsApiKey(p.isApiKey());  
        clone.setId(p.getId());  
        clone.setIsRequired(p.isRequired());
        clone.setIsRepeating(p.isRepeating());
        clone.setIsSessionKey(p.isSessionKey());
        clone.setOption(p.getOption());
        return clone;
    }
    
    public String findSubresourceLocatorUriTemplate() {
        String subresourceLocatorUriTemplate = getAvailableUriTemplate();
        if (!subresourceLocatorUriTemplate.endsWith("/")) {
            //NOI18N
            subresourceLocatorUriTemplate += "/"; //NOI18N
        }
        return subresourceLocatorUriTemplate;
    }
    
    private String getAvailableUriTemplate() {
        String uriTemplate = Inflector.getInstance().camelize(getBean().getShortName(), true);
        return uriTemplate;
    }
}
