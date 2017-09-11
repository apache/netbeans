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

package org.netbeans.modules.websvc.saas.codegen.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.websvc.saas.model.CustomSaasMethod;
import org.netbeans.modules.websvc.saas.codegen.Constants.HttpMethodType;
import org.netbeans.modules.websvc.saas.codegen.Constants.MimeType;
import org.netbeans.modules.websvc.saas.codegen.model.ParameterInfo.ParamStyle;
import org.netbeans.modules.websvc.saas.codegen.util.Inflector;
import org.netbeans.modules.websvc.saas.codegen.util.Util;
import org.netbeans.modules.websvc.saas.model.jaxb.Artifact;
import org.netbeans.modules.websvc.saas.model.jaxb.Artifacts;
import org.netbeans.modules.websvc.saas.model.jaxb.Method.Input;
import org.netbeans.modules.websvc.saas.model.jaxb.Method.Output;
import org.netbeans.modules.websvc.saas.model.jaxb.Params.Param;
import org.netbeans.modules.websvc.saas.model.jaxb.SaasMetadata.CodeGen;

/**
 *
 * @author ayubkhan
 */
public class CustomClientSaasBean extends SaasBean {

    public static final String ARTIFACT_TYPE_TEMPLATE = "template";
    public static final String ARTIFACT_TYPE_LIB = "lib";
    private String url;
    private CustomSaasMethod m;
    private Map<String, Map<String, String>> templates;
    private Map<String, String> libs;
    private boolean canGenerateJaxb;
    private String serviceMethodName;
    
    public CustomClientSaasBean(CustomSaasMethod m)  throws IOException {
        this(m, false);
    }
    
    public CustomClientSaasBean(CustomSaasMethod m, boolean isDropTargetWeb)  throws IOException {
        super(m.getSaas(), deriveResourceName(m), null, 
                deriveUriTemplate(m), new MimeType[]{MimeType.XML}, 
                new String[]{"java.lang.String"},       //NOI18N
                new HttpMethodType[]{HttpMethodType.GET});
    
        this.m = m;
        setIsDropTargetWeb(isDropTargetWeb);
        init();
    }
    
    private void init() throws IOException {
        
        setHttpMethod(HttpMethodType.GET);
        
        if(m.getHref() != null)
            setResourceClassTemplate(m.getHref());
        
        findAuthentication(m);
        
        List<MimeType> mimeTypes = new ArrayList<MimeType>();
        try {
            Output out = m.getOutput();
            findSaasMediaType(mimeTypes, out.getMedia());

            if(mimeTypes.size() > 0)
                setMimeTypes(mimeTypes.toArray(new MimeType[mimeTypes.size()]));
        } catch (Exception ex) {
            throw new IOException(ex.getMessage());
        } 
        
        templates = new HashMap<String, Map<String, String>>();
        libs = new HashMap<String, String>();
        CodeGen codegen = m.getSaas().getSaasMetadata().getCodeGen();
        if(codegen != null) {
            List<Artifacts> artifactsList = codegen.getArtifacts();
            if(artifactsList != null) {
                for(Artifacts artifacts: artifactsList) {
                    Map<String, String> artifactMap = new HashMap<String, String>();
                    String targets = artifacts.getTargets();
                    for(String target:targets.split(",")) {
                        addArtifactTemplates(target, artifactMap);
                    }
                    List<Artifact> artifactList = artifacts.getArtifact();
                    if(artifactList != null) {
                        for(Artifact artifact: artifactList) {
                            if(artifact.getRequires() != null) {
                                //TODO
                            }
                            String type = artifact.getType();
                            if(type == null)
                                throw new IOException("saas-metadata/code-gen/artifacts/artifact/@type value is null.");
                            String id = artifact.getId();
                            String artifactUrl = artifact.getUrl();
                            if(id == null || artifactUrl == null)
                                throw new IOException("saas-metadata/code-gen/artifacts/artifact/@id|@url value is null.");
                            if(type.equals(ARTIFACT_TYPE_TEMPLATE)) {
                                if(artifactMap.get(id) != null)
                                    throw new IOException("saas-metadata/code-gen/artifacts/artifact/@"+id+" already exists.");
                                artifactMap.put(id, artifactUrl);
                            } else if(type.equals(ARTIFACT_TYPE_LIB)) {
                                if(libs.get(id) != null)
                                    throw new IOException("saas-metadata/code-gen/artifacts/artifact/@"+id+" already exists.");
                                libs.put(id, artifactUrl);
                            }
                        }
                    }
                }
            }
        }
    }

    public String getSaasServiceMethodName() {
        if(serviceMethodName == null) {
            serviceMethodName = Util.deriveMethodName(getMethod().getName());
            serviceMethodName = serviceMethodName.substring(0, 1).toLowerCase() + serviceMethodName.substring(1);
        }
        return serviceMethodName;
    }
    
    public CustomSaasMethod getMethod() {
        return m;
    }
    
    protected List<ParameterInfo> initInputParameters() {
        List<ParameterInfo> inputParams = new ArrayList<ParameterInfo>();
        Input in = m.getInput();
        if(in != null && in.getParams() != null && in.getParams().getParam() != null) {
            List<Param> params = in.getParams().getParam();
            findSaasParams(inputParams, params);
            for(ParameterInfo p:inputParams) {
                p.setStyle(ParamStyle.QUERY);
            }
        }
        return inputParams;
    }
    
    public String getUrl() {
        return this.url;
    }

    protected static String deriveResourceName(CustomSaasMethod m) {
        String name = m.getName();
        if(m.getHref() != null && !m.getHref().trim().equals(""))
            name = m.getHref();
        return Inflector.getInstance().camelize(name + GenericResourceBean.RESOURCE_SUFFIX);
    }

    protected static String deriveUriTemplate(CustomSaasMethod m) {
        String name = m.getName();
        if(m.getHref() != null && !m.getHref().trim().equals(""))
            name = m.getHref();
        return Inflector.getInstance().camelize(name, true) + "/"; //NOI18N
    }
    
    public Map<String, String> getArtifactLibs() {
        return libs;
    }
    
    public void setArtifactLibs(Map<String, String> libs) {
        this.libs = libs;
    }
    
    public Map<String, String> getArtifactTemplates(String target) {
        return this.templates.get(target);
    }
    
    public void addArtifactTemplates(String target, Map<String, String> templates) {
        this.templates.put(target, templates);
    }
    
    public boolean canGenerateJAXBUnmarshaller() {
        return canGenerateJaxb;
    }
    
    public void setCanGenerateJAXBUnmarshaller(boolean canGenerateJaxb) {
        this.canGenerateJaxb = canGenerateJaxb;
    }
}
