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
package org.netbeans.modules.websvc.saas.model;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.websvc.saas.model.wadl.Method;
import org.netbeans.modules.websvc.saas.model.wadl.Resource;

/**
 *
 * @author nam
 */
public class WadlSaasResource implements Comparable<WadlSaasResource> {

    private final WadlSaas saas;
    private final WadlSaasResource parent;
    private final Resource resource;
    private List<WadlSaasMethod> methods;
    private List<WadlSaasResource> childResources;

    public WadlSaasResource(WadlSaas saas, WadlSaasResource parent, Resource resource) {
        this.saas = saas;
        this.parent = parent;
        this.resource = resource;
    }

    public WadlSaasResource getParent() {
        return parent;
    }

    public Resource getResource() {
        return resource;
    }

    public WadlSaas getSaas() {
        return saas;
    }

    private void initChildren() {
        methods = new ArrayList<WadlSaasMethod>();
        childResources = new ArrayList<WadlSaasResource>();
        for (Object o : resource.getMethodOrResource()) {
            if (o instanceof Method) {
                Method m = (Method) o;
                methods.add(new WadlSaasMethod(this, m));
            } else if (o instanceof Resource) {
                Resource r = (Resource) o;
                childResources.add(new WadlSaasResource(saas, this, r));
            }
        }
    }

    public List<WadlSaasMethod> getMethods() {
        if (methods == null) {
            initChildren();
        }
        return new ArrayList<WadlSaasMethod>(methods);
    }

    public List<WadlSaasResource> getChildResources() {
        if (childResources == null) {
            initChildren();
        }
        return new ArrayList<WadlSaasResource>(childResources);
    }
  
    @Override
    public String toString() {
        return resource.getPath();
    }
    
    @Override
    public int compareTo(WadlSaasResource saasResource) {
        String thisPath = resource.getPath();
        String thatPath = saasResource.getResource().getPath();
        if (thisPath == null) {
            return (thatPath == null) ? 0 : -1;
        } else {
            return (thatPath == null) ? 1 : thisPath.compareTo(thatPath);
        }
    }
}
