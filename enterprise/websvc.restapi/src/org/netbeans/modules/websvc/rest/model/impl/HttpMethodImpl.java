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

package org.netbeans.modules.websvc.rest.model.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import org.netbeans.modules.websvc.rest.model.api.HttpMethod;
import org.netbeans.modules.websvc.rest.model.impl.RestServicesImpl.Status;

/**
 *
 * @author Peter Liu
 */
public class HttpMethodImpl extends RestMethodDescriptionImpl implements HttpMethod {
    
    private String type;
    private String consumeMime;
    private String produceMime;
    private String path;
    private Map<String,String> queryParams;
    
    public HttpMethodImpl(ExecutableElement methodElement) {
        super(methodElement);   
        
        this.type = Utils.getHttpMethod(methodElement);
        this.consumeMime = Utils.getConsumeMime(methodElement);
        this.produceMime = Utils.getProduceMime(methodElement);
        this.path = Utils.hasUriTemplate(methodElement) ? Utils.getUriTemplate(methodElement) : ""; //NOI18N
        this.queryParams = new HashMap<String, String>();
        Utils.fillQueryParams( queryParams , methodElement);
    }

    public String getType() {
        return type;
    }
    
    public String getConsumeMime() {
        return consumeMime;
    }
    
    public String getProduceMime() {
        return produceMime;
    }
    
    public String getPath() {
        return path;
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.websvc.rest.model.api.HttpMethod#getQueryParams()
     */
    @Override
    public Map<String, String> getQueryParams() {
        return queryParams;
    }
    
    public Status refresh(Element element) {    
        boolean isModified = false;
        
        if (super.refresh(element) == Status.MODIFIED) {
            isModified = true;
        }
        
        if (!Utils.hasHttpMethod(element)) {
            return Status.REMOVED;
        }
    
        String newValue = Utils.getConsumeMime(element);
        if (!consumeMime.equals(newValue)) {
            consumeMime = newValue;
            isModified = true;
        }
        
        newValue = Utils.getProduceMime(element);
        if (!produceMime.equals(newValue)) {
            produceMime = newValue;
            isModified = true;
        }
        
        Map<String,String> map = new HashMap<String, String>();
        Utils.fillQueryParams( map , element);
        Set<String> current = map.keySet();
        Set<String> original = queryParams.keySet();
        if ( current.containsAll(original) && current.size() != original.size()){
            for( Entry<String, String> entry : queryParams.entrySet()){
                String key = entry.getKey();
                String value = entry.getValue();
                if ( value == null && map.get(key) != null ){
                    queryParams = map;
                    isModified = true;
                    break;
                }
                else if ( value!= null && !value.equals( map.get(key))){
                    queryParams = map;
                    isModified = true;
                    break;
                }
            }
        }
        else {
            queryParams = map;
            isModified = true;
        }
        
        String newPath = Utils.hasUriTemplate(element) ? 
                Utils.getUriTemplate(element) : ""; //NOI18N
        if ( !path.equals(newPath) ){
            path = newPath;
            isModified = true;
        }
        
        if (isModified) {
            return Status.MODIFIED;
        }
        
        return Status.UNMODIFIED;
    }

}
