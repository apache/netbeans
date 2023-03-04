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

package org.netbeans.modules.websvc.rest.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.websvc.rest.client.ClientJavaSourceHelper.HttpMimeType;
import org.netbeans.modules.websvc.saas.model.WadlSaasMethod;
import org.netbeans.modules.websvc.saas.model.WadlSaasResource;
import org.netbeans.modules.websvc.saas.model.wadl.Param;
import org.netbeans.modules.websvc.saas.model.wadl.ParamStyle;
import org.netbeans.modules.websvc.saas.model.wadl.Representation;
import org.netbeans.modules.websvc.saas.model.wadl.Request;

/** Information about Query, Form and Header parameters
 *
 * @author mkuchtiak
 */
public class HttpParams {
    private boolean hasFormParams = false;

    private boolean hasHeaderParams = false;

    private boolean hasQueryParams = false;
    private boolean hasRequiredQueryParams = false;
    private boolean hasOptionalQueryParams = false;
    private boolean hasDefaultQueryParams = false;

    private List<String> formParams = new ArrayList<String>();
    private Map<String, String> fixedFormParams = new HashMap<String,String>();

    private List<String> headerParams = new ArrayList<String>();
    private Map<String, String> fixedHeaderParams = new HashMap<String,String>();

    private Map<String, String> defaultQueryParams = new HashMap<String,String>();
    private List<String> requiredQueryParams = new ArrayList<String>();
    private List<String> optionalQueryParams = new ArrayList<String>();
    private Map<String, String> fixedQueryParams = new HashMap<String,String>();


    HttpParams(WadlSaasResource saasResource) {
        initParams(saasResource.getResource().getParam(), null);
    }

    HttpParams(WadlSaasMethod saasMethod) {
        Request request = saasMethod.getWadlMethod().getRequest();
        if (request != null) {
            Representation formType = null;
            List<Representation> representations = request.getRepresentation();
            for (Representation repr: representations) {
                if (HttpMimeType.FORM.getMimeType().equals(repr.getMediaType())) {
                    formType = repr;
                    break;
                }
            }
            initParams(request.getParam(), formType);
        }
    }

    private void initParams (List<Param> params, Representation formType) {
        // form params
        if (formType != null) {
            List<Param> formParamList = formType.getParam();
            if (formParamList.size() > 0) {
                hasFormParams = true;
                for (Param param : formParamList) {
                    String fixedValue = param.getFixed();
                    if (fixedValue != null) {
                        fixedFormParams.put(param.getName(), fixedValue);
                    } else {
                        formParams.add(param.getName());
                    }
                }
            }
        }

        // header and query params
        for (Param param : params) {

            if (ParamStyle.HEADER == param.getStyle()) {
                hasHeaderParams = true;
                String fixedValue = param.getFixed();
                    if (fixedValue != null) {
                        fixedHeaderParams.put(param.getName(), fixedValue);
                    } else {
                        headerParams.add(param.getName());
                    }
            }

            if (ParamStyle.QUERY == param.getStyle()) {
                hasQueryParams = true;
                if (param.isRequired() && param.getFixed() == null && param.getDefault() == null) {
                    hasRequiredQueryParams = true;
                    requiredQueryParams.add(param.getName());
                } else if (param.getFixed() != null) {
                    hasRequiredQueryParams = true;
                    fixedQueryParams.put(param.getName(), param.getFixed());
                } else if (param.getDefault() != null) {
                    hasDefaultQueryParams = true;
                    defaultQueryParams.put(param.getName(), param.getDefault());
                } else {
                    hasOptionalQueryParams = true;
                    optionalQueryParams.add(param.getName());
                }
            }
        }
    }

    boolean hasMultipleParamsInList() {
        if (!hasQueryParams) return false;
        return (requiredQueryParams.size() + fixedQueryParams.size() > 1) || (!hasRequiredQueryParams && optionalQueryParams.size() > 0);
    }

    boolean hasFormParams() {
        return hasFormParams;
    }
    boolean hasQueryParams() {
        return hasQueryParams;
    }
    boolean hasHeaderParams() {
        return hasHeaderParams;
    }

    boolean hasRequiredQueryParams() {
        return hasRequiredQueryParams;
    }

    boolean hasOptionalQueryParams() {
        return hasOptionalQueryParams;
    }

    boolean hasDefaultQueryParams() {
        return hasDefaultQueryParams;
    }

    Map<String, String> getDefaultQueryParams() {
        return defaultQueryParams;
    }

    Map<String, String> getFixedQueryParams() {
        return fixedQueryParams;
    }

    List<String> getOptionalQueryParams() {
        return optionalQueryParams;
    }

    List<String> getRequiredQueryParams() {
        return requiredQueryParams;
    }

    List<String> getFormParams() {
        return formParams;
    }

    Map<String, String> getFixedFormParams() {
        return fixedFormParams;
    }

    List<String> getHeaderParams() {
        return headerParams;
    }

    Map<String, String> getFixedHeaderParams() {
        return fixedHeaderParams;
    }

    void mergeQueryandHeaderParams(HttpParams params) {
        hasQueryParams = hasQueryParams || params.hasQueryParams;
        hasRequiredQueryParams = hasRequiredQueryParams || params.hasRequiredQueryParams;
        hasOptionalQueryParams = hasOptionalQueryParams || params.hasOptionalQueryParams;
        
        hasHeaderParams = hasHeaderParams || params.hasHeaderParams;

        requiredQueryParams.addAll(params.requiredQueryParams);
        optionalQueryParams.addAll(params.optionalQueryParams);
        fixedQueryParams.putAll(params.fixedQueryParams);
        defaultQueryParams.putAll(params.defaultQueryParams);

        headerParams.addAll(params.headerParams);
        fixedHeaderParams.putAll(params.fixedHeaderParams);
    }

}
