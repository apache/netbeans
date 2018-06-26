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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
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
