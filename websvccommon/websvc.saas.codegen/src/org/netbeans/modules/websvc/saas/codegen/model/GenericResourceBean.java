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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.netbeans.modules.websvc.saas.codegen.Constants.HttpMethodType;
import org.netbeans.modules.websvc.saas.codegen.Constants.MimeType;
import org.netbeans.modules.websvc.saas.codegen.Constants.SaasAuthenticationType;
import org.netbeans.modules.websvc.saas.codegen.model.ParameterInfo.ParamFilter;
import org.netbeans.modules.websvc.saas.codegen.model.SaasBean.SaasAuthentication;
import org.netbeans.modules.websvc.saas.codegen.util.Util;

/**
 * Meta model for generic REST resource class.
 *
 * @author nam
 */
public class GenericResourceBean {

    public static final String RESOURCE_SUFFIX = "Resource";
    public static final MimeType[] supportedMimeTypes = new MimeType[]{MimeType.XML, MimeType.JSON, MimeType.TEXT, MimeType.HTML};
    public static final HttpMethodType[] CONTAINER_METHODS = new HttpMethodType[]{HttpMethodType.GET, HttpMethodType.POST};
    public static final HttpMethodType[] ITEM_METHODS = new HttpMethodType[]{HttpMethodType.GET, HttpMethodType.PUT, HttpMethodType.DELETE};
    public static final HttpMethodType[] STAND_ALONE_METHODS = new HttpMethodType[]{HttpMethodType.GET, HttpMethodType.PUT};
    public static final HttpMethodType[] CLIENT_CONTROL_CONTAINER_METHODS = new HttpMethodType[]{HttpMethodType.GET};
    private String name;
    private String packageName;
    private String uriTemplate;
    private MimeType[] mimeTypes;
    private String[] representationTypes;
    private Set<HttpMethodType> methodTypes;
    private boolean privateFieldForQueryParam;
    private boolean generateUriTemplate = true;
    private List<GenericResourceBean> subResources;
    private HttpMethodType httpMethod;
   
    public GenericResourceBean(String name, String packageName, String uriTemplate) {
        this(name, packageName, uriTemplate, supportedMimeTypes, HttpMethodType.values());
    }

    public GenericResourceBean(String name, String packageName, String uriTemplate, 
            MimeType[] mediaTypes, HttpMethodType[] methodTypes) {
        this(name, packageName, uriTemplate, mediaTypes, null, methodTypes);
    }

    public GenericResourceBean(String name, String packageName, String uriTemplate, 
            MimeType[] mediaTypes, String[] representationTypes, HttpMethodType[] methodTypes) {
        this.name = name;
        this.packageName = packageName;
        this.uriTemplate = uriTemplate;
        this.methodTypes = new HashSet<HttpMethodType>(Arrays.asList(methodTypes));
        this.subResources = new ArrayList<GenericResourceBean>();

        if (representationTypes == null) {
            representationTypes = new String[mediaTypes.length];
            for (int i = 0; i < representationTypes.length; i++) {
                representationTypes[i] = String.class.getName();
            }
        }
        if (mediaTypes.length != representationTypes.length) {
            throw new IllegalArgumentException("Unmatched media types and representation types");
        }
        this.mimeTypes = mediaTypes;
        this.representationTypes = representationTypes == null ? new String[0] : representationTypes;
    }

    public static MimeType[] getSupportedMimeTypes() {
        return supportedMimeTypes;
    }

    public static String getDefaultRepresetationClass(MimeType mime) {
        if (mime == MimeType.XML || mime == MimeType.TEXT || mime == MimeType.HTML || mime == MimeType.JSON) {
            return String.class.getName();
        }
        return String.class.getName();
    }

    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }

    public String getShortName() {
        return getShortName(name);
    }

    public static String getShortName(String name) {
        if (name.endsWith(RESOURCE_SUFFIX)) {
            return name.substring(0, name.length() - 8);
        }
        return name;
    }

    public String getUriWhenUsedAsSubResource() {
        return Util.lowerFirstChar(getShortName()) + "/";
    }

    public void setPackageName(String name) {
        packageName = name;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getUriTemplate() {
        return uriTemplate;
    }

    public void setUriTemplate(String uriTemplate) {
        this.uriTemplate = uriTemplate;
    }

    public MimeType[] getMimeTypes() {
        return mimeTypes;
    }

    public void setMimeTypes(MimeType[] mimeTypes) {
        this.mimeTypes = mimeTypes;
    }

    public String[] getRepresentationTypes() {
        return representationTypes;
    }

    public Set<HttpMethodType> getMethodTypes() {
        return methodTypes;
    }

    public void setMethodTypes(HttpMethodType[] types) {
        methodTypes = new HashSet<>(Arrays.asList(types));
    }
    
    private String[] uriParams = null;

    public String[] getUriParams() {
        if (uriParams == null) {
            uriParams = getUriParams(uriTemplate);
        }
        return uriParams;
    }

    public static String[] getUriParams(String template) {
        if (template == null) {
            return new String[0];
        }
        
        String[] segments = template.split("/");
        List<String> res = new ArrayList<String>();
        
        for (String segment : segments) {
            if (segment.startsWith("{")) {
                if (segment.length() > 2 && segment.endsWith("}")) {
                    res.add(segment.substring(1, segment.length() - 1));
                } else {
                    throw new IllegalArgumentException(template);
                }
            }
        }
        
        return res.toArray(new String[0]);
    }

    public String getQualifiedClassName() {
        return getPackageName() + "." + getName();
    }
    
    public void addSubResource(GenericResourceBean bean) {
        this.subResources.add(bean);
    }

    public List<GenericResourceBean> getSubResources() {
        return subResources;
    }

    public boolean isPrivateFieldForQueryParam() {
        return privateFieldForQueryParam;
    }

    public void setPrivateFieldForQueryParam(boolean privateFieldForQueryParam) {
        this.privateFieldForQueryParam = privateFieldForQueryParam;
    }

    public boolean isGenerateUriTemplate() {
        return generateUriTemplate;
    }

    public void setGenerateUriTemplate(boolean flag) {
        this.generateUriTemplate = flag;
    }
    
    public List<ParameterInfo> getInputParameters() {
        return Collections.emptyList();
    }
    
    public List<ParameterInfo> getQueryParameters() {
        return Collections.emptyList();
    }
    
    public static String getGetMethodName(MimeType mime) {
        return "get"+mime.suffix(); //NOI18N
    }
    
    public HttpMethodType getHttpMethod() {
        return this.httpMethod;
    }
    
    public void setHttpMethod(HttpMethodType httpMethod) {
        this.httpMethod = httpMethod;
    }
    
    public static String getHttpMethodName(HttpMethodType httpMethod, MimeType mime) {
        return httpMethod.value().toLowerCase()+mime.suffix(); //NOI18N
    }

    public SaasAuthenticationType getAuthenticationType() {
        return SaasAuthenticationType.PLAIN;
    }
    
    public SaasAuthentication getAuthentication() {
        return null;
    }
    
    public List<ParameterInfo> filterParametersByAuth(List<ParameterInfo> params) {
        return Util.filterParametersByAuth(getAuthenticationType(), getAuthentication(), params);
    }
    
    public List<ParameterInfo> filterParameters(ParamFilter[] filters) {
        return filterParameters(getInputParameters(), filters);
    }
    public List<ParameterInfo> filterParameters(List<ParameterInfo> params, ParamFilter[] filters) {
        return Util.filterParameters(params, filters);
    }
}
