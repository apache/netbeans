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

package org.netbeans.modules.websvc.saas.codegen.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.xml.namespace.QName;
import org.netbeans.api.project.Project;
import org.netbeans.modules.websvc.saas.codegen.Constants.HttpMethodType;
import org.netbeans.modules.websvc.saas.codegen.model.ParameterInfo.ParamStyle;
import org.netbeans.modules.websvc.saas.codegen.util.Util;
import org.netbeans.modules.websvc.saas.model.Saas;
import org.netbeans.modules.websvc.saas.model.WsdlSaasMethod;

/**
 * Model bean for code generation of JAXWS operation wrapper resource class.
 * 
 * @author nam
 */
public class SoapClientSaasBean extends SaasBean {
    
    private SoapClientOperationInfo[] jaxwsInfos;
    private WsdlSaasMethod m;
    
    public SoapClientSaasBean(WsdlSaasMethod m, Project project) {
        this(m, project, toJaxwsOperationInfos(m, project));
    }
    
    public SoapClientSaasBean(WsdlSaasMethod m, Project project, SoapClientOperationInfo[] jaxwsInfos) {
        this(m.getSaas(), Util.deriveResourceName(m.getName()), jaxwsInfos);
    }
  
    /**
     * Create a resource model bean for wrapper resource generation.
     * Note that the last JAXWS info is the principal one from which resource name, 
     * URI template and representation class is derived from.
     * @param jaxwsInfos array of JAXWS info objects.
     * @param packageName name of package
     */ 
    private SoapClientSaasBean(Saas saas, String name, SoapClientOperationInfo[] jaxwsInfos) {
        super(saas, name, 
              null,
              Util.deriveUriTemplate(jaxwsInfos[jaxwsInfos.length-1].getOperationName()),
              Util.deriveMimeTypes(jaxwsInfos), 
              new String[] { jaxwsInfos[jaxwsInfos.length-1].getOutputType() }, 
              new HttpMethodType[] { HttpMethodType.GET });
        this.jaxwsInfos = jaxwsInfos;
    }

    private static SoapClientOperationInfo[] toJaxwsOperationInfos(WsdlSaasMethod m, 
            Project project) {
        List<SoapClientOperationInfo> infos = new ArrayList<SoapClientOperationInfo>();
        infos.add(new SoapClientOperationInfo(m, project));
        
        return infos.toArray(new SoapClientOperationInfo[infos.size()]);
    }
    
    protected List<ParameterInfo> initInputParameters() {
        List<ParameterInfo> inputParams = new ArrayList<ParameterInfo>();
        
        for(SoapClientOperationInfo info : jaxwsInfos) {
            String[] names = info.getInputParameterNames();
            Class[] types = info.getInputParameterTypes();
            
            for (int i=0; i<names.length; i++) {
                ParameterInfo p = new ParameterInfo(names[i], types[i]);
                p.setStyle(ParamStyle.QUERY);
                inputParams.add(p);
            }
        }
        
        return inputParams;
    }
    
    @Override
    public String[] getOutputTypes() {
        String[] types = new String[jaxwsInfos.length];
        for (int i=0; i<jaxwsInfos.length; i++) {
            types[i] = jaxwsInfos[i].getOutputType();
        }
        return types;
    }
    
    public SoapClientOperationInfo[] getOperationInfos() {
        return jaxwsInfos;
    }

    @Override
    public List<ParameterInfo> getHeaderParameters() {
        HashMap<QName,ParameterInfo> params = new HashMap<QName,ParameterInfo>();
        for (SoapClientOperationInfo info : getOperationInfos()) {
            for (ParameterInfo pinfo : info.getSoapHeaderParameters()) {
                params.put(pinfo.getQName(), pinfo);
            }
        }
        return new ArrayList<ParameterInfo>(params.values());
    }

    @Override
    public List<String> getOutputWrapperNames() {
        if (needsHtmlRepresentation()) {
            return null;
        }
        return super.getOutputWrapperNames();
    }

    @Override
    public List<String> getOutputWrapperPackageNames() {
        if (needsHtmlRepresentation()) {
            return null;
        }
        return super.getOutputWrapperPackageNames();
    }
    
    public boolean needsHtmlRepresentation() {
        return getOperationInfos().length > 0 && 
               String.class.getName().equals(lastOperationInfo().getOperation().getReturnTypeName());
    }
    
    public SoapClientOperationInfo lastOperationInfo() {
        return getOperationInfos()[getOperationInfos().length-1];
    }

    public String getResourceClassTemplate() {
        return RESOURCE_TEMPLATE;
    }
}
