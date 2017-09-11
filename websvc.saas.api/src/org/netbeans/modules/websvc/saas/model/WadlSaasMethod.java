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
package org.netbeans.modules.websvc.saas.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.xml.ws.Response;

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
            path = result.toArray(new Resource[result.size()]);
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
