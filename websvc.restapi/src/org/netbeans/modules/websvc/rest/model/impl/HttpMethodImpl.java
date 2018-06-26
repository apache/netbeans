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
