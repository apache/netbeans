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

package org.netbeans.installer.product.components;

import java.util.Locale;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.installer.product.RegistryNode;
import org.netbeans.installer.utils.exceptions.InitializationException;
import org.netbeans.installer.utils.StringUtils;
import org.w3c.dom.Element;
import org.netbeans.installer.utils.helper.Status;

/**
 *
 * @author Kirill Sorokin
 * @author Yulia Novozhilova
 */
public class Group extends RegistryNode implements StatusInterface {
    private Status currentStatus;

    public Group() {
        uid = StringUtils.EMPTY_STRING;
        displayNames.put(new Locale(StringUtils.EMPTY_STRING), "Product Tree Root");
        descriptions.put(new Locale(StringUtils.EMPTY_STRING), StringUtils.EMPTY_STRING);
    }
    
    public boolean isEmpty() {
        for (RegistryNode node: getVisibleChildren()) {
            if (node instanceof Group) {
                if (!((Group) node).isEmpty()) {
                    return false;
                }
            } else {
                return false;
            }
        }
        
        return true;
    }
    
    // node <-> dom /////////////////////////////////////////////////////////////////
    protected String getTagName() {
        return "group";
    }
    
    public Group loadFromDom(Element element) throws InitializationException {
        super.loadFromDom(element);
        
        return this;
    }

    public Status getStatus() {
        if(currentStatus == null && !isEmpty()) {                            
            final List<Status> statuses = new ArrayList<Status>();
            for (RegistryNode node: getVisibleChildren()) {
                if (node instanceof Group) {
                   statuses.add(((Group)node).getStatus());
                } 
                if (node instanceof Product) {
                   statuses.add(((Product)node).getStatus());
                }
            }            
            //todo
            currentStatus = statuses.contains(Status.TO_BE_INSTALLED) ||
                   statuses.contains(Status.NOT_INSTALLED)? 
                       Status.TO_BE_INSTALLED : Status.INSTALLED;
        }        
        return currentStatus;
    }
    
    public void setStatus(final Status status) {               
        currentStatus = status;
   
        for (RegistryNode node: getVisibleChildren()) {
            if(node instanceof StatusInterface && 
                    ((StatusInterface)node).getStatus()!= Status.INSTALLED) {
                ((StatusInterface)node).setStatus(status);
            }            
        }        
    }           

}
