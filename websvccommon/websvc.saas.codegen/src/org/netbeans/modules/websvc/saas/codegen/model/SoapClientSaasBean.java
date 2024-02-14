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
        
        return infos.toArray(new SoapClientOperationInfo[0]);
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
