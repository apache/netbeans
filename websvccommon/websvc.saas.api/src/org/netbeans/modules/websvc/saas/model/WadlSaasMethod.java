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
package org.netbeans.modules.websvc.saas.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.netbeans.modules.websvc.saas.model.jaxb.Method;
import org.netbeans.modules.websvc.saas.model.wadl.Application;
import org.netbeans.modules.websvc.saas.model.wadl.Resource;
import org.netbeans.modules.websvc.saas.util.SaasUtil;
import org.openide.util.Exceptions;

/**
 *
 * @author nam
 */
public class WadlSaasMethod extends SaasMethod {

    private static final String GET = "GET";
    private static final String POST = "POST";
    private static final String PUT = "PUT";
    private static final String DELETE = "DELETE";
    private Resource[] path;
    private WadlSaasResource parent;
    private org.netbeans.modules.websvc.saas.model.wadl.Method wadlMethod;
    private String name;
    private String displayName;

    public WadlSaasMethod(WadlSaas wadlSaas, Method method) {
        super(wadlSaas, method);
    }

    public WadlSaasMethod(WadlSaasResource parent, org.netbeans.modules.websvc.saas.model.wadl.Method wadlMethod) {
        this(parent.getSaas(), (Method) null);
        this.parent = parent;
        this.wadlMethod = wadlMethod;
    }

    @Override
    public String getName() {
        if (getMethod() == null) {
            if (name == null) {
                name = wadlMethod.getId();

                if (name == null) {
                    name = wadlMethod.getName();
                    Set<String> medias = null;

                    if (GET.equals(name)) {
                        medias = new HashSet<String>();
                        for( org.netbeans.modules.websvc.saas.model.wadl.Response 
                                response :wadlMethod.getResponse())
                        {
                            medias.addAll(SaasUtil.getMediaTypes(
                                    response.getRepresentation()));
                        }
                    } else if (PUT.equals(name) || POST.equals(name)) {
                        medias = SaasUtil.getMediaTypes(
                                wadlMethod.getRequest().getRepresentation());
                    }

                    name = name.toLowerCase();
                    if (medias != null && medias.size() > 0) {
                        for (String m : medias) {
                            name += "_" + m;
                        }

                        name = name.replaceAll("\\W", "_").replaceAll("_+", "_").replaceAll("_$", "");
                    }
                }
            }
            return name;
        }

        return super.getName();
    }

    @Override
    public String getDisplayName() {
        if (getMethod() == null) {
            if (displayName == null) {
                displayName = wadlMethod.getId();

                if (displayName == null) {
                    displayName = wadlMethod.getName();
                    Set<String> medias = null;

                    if (GET.equals(displayName)) {
                        medias = new HashSet<String>();
                        for( org.netbeans.modules.websvc.saas.model.wadl.Response 
                                response :wadlMethod.getResponse())
                        {
                            medias.addAll(SaasUtil.getMediaTypes(
                                    response.getRepresentation()));
                        }
                    } else if (PUT.equals(displayName) || POST.equals(displayName)) {
                        medias = SaasUtil.getMediaTypes(
                                wadlMethod.getRequest().getRepresentation());
                    }

                    if (medias != null && medias.size() > 0) {
                        displayName += medias;
                    }
                }
            }

            return displayName;
        }

        return super.getDisplayName();
    }

    @Override
    public WadlSaas getSaas() {
        return (WadlSaas) super.getSaas();
    }

    public WadlSaasResource getParentResource() {
        return parent;
    }

    public Resource[] getResourcePath() {
        Application wadl = null;
        try {
            wadl = getSaas().getWadlModel();
        } catch (IOException e) {
            Exceptions.printStackTrace(e);
            return new Resource[0];
        }

        if (path == null || path.length == 0) {
            List<Resource> result = new ArrayList<Resource>();
            if (super.getMethod() == null) {
                WadlSaasResource current = getParentResource();
                while (current != null) {
                    result.add(0, current.getResource());
                    current = current.getParent();
                }
            } else {
                for(org.netbeans.modules.websvc.saas.model.wadl.Resources wadlResources : 
                    wadl.getResources())
                {
                    for (Resource r : wadlResources.getResource()) {
                        findPathToMethod(r, result);
                        if (r.getMethodOrResource().contains(getWadlMethod())) {
                            break;
                        }
                    }
                }
            }
            path = result.toArray(new Resource[0]);
        }
        return path;
    }

    private void findPathToMethod(Resource current, List<Resource> resultPath) {
        if (current.getMethodOrResource().contains(getWadlMethod())) {
            resultPath.add(current);
            return;
        }

        for (Object o : current.getMethodOrResource()) {
            if (o instanceof Resource) {
                findPathToMethod((Resource) o, resultPath);
                if (resultPath.size() > 0) {
                    break;
                }
            }
        }

        resultPath.add(0, current);
    }

    public org.netbeans.modules.websvc.saas.model.wadl.Method getWadlMethod() {
        if (wadlMethod == null) {
            if (getHref() != null && getHref().length() > 0) {
                try {
                    if (getHref().charAt(0) == '/') {
                        wadlMethod = SaasUtil.wadlMethodFromXPath(getSaas().getWadlModel(), getHref());
                    } else {
                        wadlMethod = SaasUtil.wadlMethodFromIdRef(getSaas().getWadlModel(), getHref());
                    }
                } catch (IOException ioe) {
                    Exceptions.printStackTrace(ioe);
                }
            } else {
                throw new IllegalArgumentException("Element method " + getName() + " should define attribute 'href'");
            }
        }
        return wadlMethod;
    }
    
    public String toString() {
        return getDisplayName();
    }
}
