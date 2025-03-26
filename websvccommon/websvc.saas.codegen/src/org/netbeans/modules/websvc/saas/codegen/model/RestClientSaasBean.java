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

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import org.netbeans.modules.websvc.saas.codegen.SaasClientCodeGenerator;
import org.netbeans.modules.websvc.saas.model.Saas;
import org.netbeans.modules.websvc.saas.model.WadlSaasMethod;
import org.netbeans.modules.websvc.saas.model.wadl.Param;
import org.netbeans.modules.websvc.saas.model.wadl.Representation;
import org.netbeans.modules.websvc.saas.model.wadl.Request;
import org.netbeans.modules.websvc.saas.model.wadl.Resource;
import org.netbeans.modules.websvc.saas.model.wadl.Response;
import org.netbeans.modules.websvc.saas.codegen.Constants.HttpMethodType;
import org.netbeans.modules.websvc.saas.codegen.Constants.MimeType;
import org.netbeans.modules.websvc.saas.codegen.Constants.SaasAuthenticationType;
import org.netbeans.modules.websvc.saas.codegen.model.ParameterInfo.ParamStyle;
import org.netbeans.modules.websvc.saas.codegen.util.Util;
import org.netbeans.modules.websvc.saas.model.jaxb.SaasMetadata.Authentication;
import org.netbeans.modules.websvc.saas.model.jaxb.SaasMetadata.Authentication.SignedUrl;
import org.netbeans.modules.websvc.saas.model.wadl.Method;

/**
 *
 * @author ayubkhan
 */
public class RestClientSaasBean extends SaasBean {

    public static final String SAAS_SERVICE_TEMPLATE = SaasClientCodeGenerator.TEMPLATES_SAAS+"SaasService"; //NOI18N
    public static final String PROTOCOL_SEPERATOR = "://";
    public static final String PROTOCOL_SEPERATOR_ALT = "  ";
    private String url;
    private WadlSaasMethod m;
    private String serviceMethodName = null;
    
    public RestClientSaasBean(WadlSaasMethod m)  throws IOException {
        this(m, false);
    }
    
    public RestClientSaasBean(WadlSaasMethod m, boolean isDropTargetWeb)  throws IOException {
        super(m.getSaas(), Util.deriveResourceName(m.getName()), null, 
                Util.deriveUriTemplate(m.getName()), new MimeType[]{MimeType.XML}, 
                new String[]{"java.lang.String"},       //NOI18N
                new HttpMethodType[]{HttpMethodType.GET});
    
        this.m = m;
        setIsDropTargetWeb(isDropTargetWeb);
        init();
    }
    
    public WadlSaasMethod getMethod() {
        return m;
    }
    
    public String getSaasServiceMethodName() {
        if(serviceMethodName == null) {
            serviceMethodName = Util.deriveMethodName(getMethod().getName());
            serviceMethodName = serviceMethodName.substring(0, 1).toLowerCase() + serviceMethodName.substring(1);
        }
        return serviceMethodName;
    }
    
    private void init() throws IOException { 
        setResourceClassTemplate(RESOURCE_TEMPLATE);
        setHttpMethod(HttpMethodType.valueOf(getMethod().getWadlMethod().getName()));
        findAuthentication(m);
        initUrl();
        getInputParameters();//init parameters
        initMimeTypes();
        setMethodTypes(new HttpMethodType[]{getHttpMethod()});
    }

    private void initUrl() throws IOException {
        Resource[] rArray = m.getResourcePath();
        if(rArray == null || rArray.length == 0)
            throw new IllegalArgumentException("Method do not belong to any resource in the WADL.");
        String url2 = m.getSaas().getBaseURL();

        url2 = url2.replace(PROTOCOL_SEPERATOR, PROTOCOL_SEPERATOR_ALT);//replace now, add :// later
        for(Resource r: rArray){
            String path = r.getPath();
            if(path != null && path.trim().length() > 0) {
                url2 += "/" + path;
            }
        }
        url2 = url2.replace("//", "/");
        url2 = url2.replace("/"+PROTOCOL_SEPERATOR_ALT, PROTOCOL_SEPERATOR_ALT);//special case 
        url2 = url2.replace(PROTOCOL_SEPERATOR_ALT+"/", PROTOCOL_SEPERATOR_ALT);//special case 
        url2 = url2.replace(PROTOCOL_SEPERATOR_ALT, PROTOCOL_SEPERATOR);//put back ://
        this.url = url2;
    }
    
    protected List<ParameterInfo> initInputParameters() {
        ArrayList<ParameterInfo> inputParams = new ArrayList<ParameterInfo>();
        try {
            Resource[] rArray = m.getResourcePath();
            if(rArray == null || rArray.length == 0)
                throw new IllegalArgumentException("Method do not belong to any resource in the WADL.");
            inputParams.addAll(findWadlParams(m));
        } catch (Exception ex) {
        } 
        
        //Further differentiate fixed, api-key for query parameters
        String apiKeyName = null;
        String sessionKeyName = null;
        boolean checkApiKey = false;
        boolean isSessionKey = getAuthenticationType() == SaasAuthenticationType.SESSION_KEY;
        if(isSessionKey)
            sessionKeyName = ((SessionKeyAuthentication)getAuthentication()).getSessionKeyName();
        if(getAuthenticationType() == SaasAuthenticationType.API_KEY)
            apiKeyName = ((ApiKeyAuthentication)getAuthentication()).getApiKeyName();
        if(isSessionKey)
            apiKeyName = ((SessionKeyAuthentication)getAuthentication()).getApiKeyName();
        if(apiKeyName != null)
            checkApiKey = true;
        for (ParameterInfo param : inputParams) {
            String paramName = param.getName();
            if(param.getStyle() == ParamStyle.QUERY) {
                if(checkApiKey && paramName.equals(apiKeyName)) {
                    param.setIsApiKey(true);
                }
                if(isSessionKey && paramName.equals(sessionKeyName)) {
                    param.setIsSessionKey(true);
                }
            }
        }
        return inputParams;
    }
    
    public ArrayList<ParameterInfo> findWadlParams(WadlSaasMethod wsm) {
        ArrayList<ParameterInfo> inputParams = new ArrayList<ParameterInfo>();
        inputParams.addAll(findWadlParams(wsm.getWadlMethod()));
        Resource[] rArray = wsm.getResourcePath();
        if(rArray != null && rArray.length > 0) {
            for(Resource r: rArray){
                findWadlParams(inputParams, r.getParam());
            }
        }
        return inputParams;
    }
    
    public ArrayList<ParameterInfo> findWadlParams(Method wm) {
        ArrayList<ParameterInfo> inputParams = new ArrayList<ParameterInfo>();
        Request req = wm.getRequest();
        findWadlParams(inputParams, req.getParam());
        List<Representation> reps = req.getRepresentation();
        for(Representation rep:reps) {
            findWadlParams(inputParams, rep.getParam());
        }
        return inputParams;
    }

    private void initMimeTypes() {
        List<MimeType> mimeTypes = new LinkedList<MimeType>();
        List<Response> responses = m.getWadlMethod().getResponse();
        for( Response response : responses ){
            findMediaType(response, mimeTypes);
        }
        if(mimeTypes.size() > 0)
            this.setMimeTypes(mimeTypes.toArray(new MimeType[0]));
    }
    
    public static List<QName> findRepresentationTypes(WadlSaasMethod wm) {
        List<QName> repTypes = new LinkedList<QName>();
        List<Response> responses = wm.getWadlMethod().getResponse();
        for(Response response:responses){
            findRepresentationType(response, repTypes);
        }
        return repTypes;
    }
  
    public static List<Representation> findInputRepresentations(WadlSaasMethod m) {
        return m.getWadlMethod().getRequest().getRepresentation();
    }
    
    public String getUrl() {
        return this.url;
    }

    public static void findMediaType(Response response, List<MimeType> mimeTypes) {
        if(response == null)
            return;
        List<Representation> representations = response.getRepresentation();
        for (Representation representation : representations) {
            String mediaType = representation.getMediaType();
            if(mediaType == null)
                continue;
            String[] mTypes = mediaType.split(",");
            for(String m:mTypes) {
                MimeType mType = MimeType.find(m);
                if (mType != null && !mimeTypes.contains(mType)) {
                    mimeTypes.add(mType);
                }
            }
        }
    }

    public static void findMediaType(Request request, List<String> mimeTypes) {
        if(request == null)
            return;
        List<Representation> reps = request.getRepresentation();
        for (Representation rep : reps) {
            String mediaType = rep.getMediaType();
            if(mediaType == null)
                continue;
            String[] mTypes = mediaType.split(",");
            for(String m: mTypes) {
                if (m != null && !mimeTypes.contains(m)) {
                    mimeTypes.add(m);
                }
            }
        }
    }
    
    public static void findWadlParams(List<ParameterInfo> paramInfos, List<Param> params) {
        if (params != null) {
            for (Param param:params) {
                //<param name="replace" type="xsd:boolean" style="query" required="false" default="some value">
                String paramName = param.getName();
                Class paramType = findJavaType(param.getType().getLocalPart());
                Object defaultValue = param.getDefault();
                ParameterInfo paramInfo = new ParameterInfo(paramName, paramType);
                paramInfo.setStyle(ParamStyle.fromValue(param.getStyle().value()));
                paramInfo.setIsRequired(param.isRequired());
                paramInfo.setIsRepeating(param.isRepeating());
                paramInfo.setFixed(param.getFixed());
                paramInfo.setOption(param.getOption());
                paramInfo.setDefaultValue(defaultValue);
                paramInfos.add(paramInfo);
            }
        }
    }
    
    public static void findRepresentationType(Response response, List<QName> repTypes) {
        if(response == null)
            return;
        List<Representation> representations = response.getRepresentation();
        for (Representation representation : representations) {
            QName repType = representation.getElement();
            if(repType == null || repTypes.contains(repType)){
                continue;
            }
            repTypes.add(repType);
        }
    }

    public String getSaasServiceTemplate() {
        return SAAS_SERVICE_TEMPLATE;
    }
    
    @Override
    protected Object getSignedUrl(Authentication auth) {
        Object signedUrl = null;
        if(auth.getSignedUrl() != null && auth.getSignedUrl().size() > 0) {
            String id = m.getWadlMethod().getId();
            signedUrl = getSignedUrlById(auth, id);
            if(signedUrl == null) {
                Resource[] rArray = m.getResourcePath();
                if (rArray == null || rArray.length == 0) {
                    return null;
                }
                id = rArray[rArray.length-1].getId();
                signedUrl = getSignedUrlById(auth, id);
            }
        }
        return signedUrl;
    }
    
    private Object getSignedUrlById(Authentication auth, String id) {
        if(id != null && !id.trim().equals("")) {
            for(SignedUrl s: auth.getSignedUrl()) {
                if(id.equals(s.getId()))
                    return s;
            }
        }
        return null;
    }
    
    @Override
    protected Object getSessionKey(Authentication auth) {
        Object signedUrl = null;
        if(auth.getSignedUrl() != null && auth.getSignedUrl().size() > 0) {
            String id = m.getWadlMethod().getId();
            signedUrl = getSessionKey(auth, id);
            if(signedUrl == null) {
                Resource[] rArray = m.getResourcePath();
                if (rArray == null || rArray.length == 0) {
                    return null;
                }
                id = rArray[rArray.length-1].getId();
                signedUrl = getSessionKey(auth, id);
            }
        }
        return signedUrl;
    }
    
    private Object getSessionKey(Authentication auth, String id) {
        if(id != null && !id.trim().equals("")) {
            for(SignedUrl s: auth.getSignedUrl()) {
                if(id.equals(s.getId()))
                    return s;
            }
        }
        return null;
    }
    
    public boolean canGenerateJAXBUnmarshaller() {
        return !findRepresentationTypes(getMethod()).isEmpty();
    }
}
