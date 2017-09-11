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

import org.netbeans.modules.websvc.saas.model.jaxb.Group;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import org.openide.util.NbBundle;

/**
 *
 * @author nam
 */
public class SaasGroup implements Comparable<SaasGroup> {
    public static final String PROP_GROUP_NAME = "groupName";

    private final Group delegate;
    private final SaasGroup parent;
    private boolean userDefined = true; //once set to false, remain false.
    private SortedMap<String, Saas> services;
    private SortedMap<String, SaasGroup> children;
    private String icon16Path;
    private String icon32Path;
    
    public SaasGroup(SaasGroup parent, Group group) {
        this.parent = parent;
        this.delegate = group;
        services = Collections.synchronizedSortedMap(new TreeMap<String, Saas>());
    }

    public SaasGroup getParent() {
        return parent;
    }
    
    public Group getDelegate() {
        return delegate;
    }

    public List<Saas> getServices() {
        return new ArrayList<Saas>(services.values());
    }

    public Saas getChildService(String name) {
        getServices();
        return services.get(name);
    }

    /**
     * Not a mutation
     * 
     * @param service saas service
     */
    public void addService(Saas service) {
        getServices();
        services.put(service.getDisplayName(), service);
    }
    
    /**
     * If this is part of model mutation, caller is responsible to ensure 
     * SaasServices persists with proper Group information and eventing.
     * 
     * @param service saas service to remove
     */
    public boolean removeService(Saas service) {
        Saas removed = services.remove(service.getDisplayName());
        if (removed != null) {
            return true;
        }
        return false;
    }

    public void setName(String value) {
        if (getParent() == null) {
            throw new IllegalArgumentException("Cannot rename root group");
        }

        if (value == null || value.equals(getName())) {
            return;
        }
        
        String message = null;
        if (! isUserDefined()) {
            message = NbBundle.getMessage(getClass(), "MSG_GroupNotUserDefined");
        }
        
        for (SaasGroup g : getParent().getChildrenGroups()) {
            if (g.getName().equals(value)) {
                message = NbBundle.getMessage(getClass(), "MSG_DuplicateGroupName");
            }
        }
        
        if (message != null) {
            throw new IllegalArgumentException(message);
        }
        
        delegate.setName(value);
        resetAllServicesGroupPath();
    }
    
    private void resetAllServicesGroupPath() {
        for (Saas s : getServices()) {
            s.computePathFromRoot();
        }
        for (SaasGroup g : getChildrenGroups()) {
            g.resetAllServicesGroupPath();
        }
    }

    public String getName() {
        return delegate.getName();
    }
    
    public boolean isUserDefined() {
        return userDefined;
    }

    void setUserDefined(boolean v) {
        if (userDefined) {
            userDefined = v;
        }
    }

    public String getIcon16Path() {
        return icon16Path;
    }

    protected void setIcon16Path(String icon16Path) {
        this.icon16Path = icon16Path;
    }

    public String getIcon32Path() {
        return icon32Path;
    }

    protected void setIcon32Path(String icon32Path) {
        this.icon32Path = icon32Path;
    }

    
    public List<SaasGroup> getChildrenGroups() {
        if (children == null) {
            children = Collections.synchronizedSortedMap(new TreeMap<String,SaasGroup>());
            for (Group g : delegate.getGroup()) {
                SaasGroup sg = new SaasGroup(this, g);
                children.put(sg.getName(), sg);
            }
        }
        return new ArrayList<SaasGroup>(children.values());
    }

    public SaasGroup getChildGroup(String name) {
        getChildrenGroups();
        return children.get(name);
    }
    
    /**
     * All children services and children groups also removed.
     * Only group created by users could be removed.
     * Caller is responsible for persisting changes.
     * @param group saas group to remove
     */
    protected boolean removeChildGroup(SaasGroup group) {
        if (! group.canRemove()) {
            return false;
        }
        
        _removeChildGroup(group);
        
        return true;
    }
    
    private void _removeChildGroup(SaasGroup child) {
        if (child != null) {
            for (Saas saas : child.getServices()) {
                removeService(saas);
                SaasServicesModel.getInstance()._removeService(saas);
            }
            for (SaasGroup c : child.getChildrenGroups()) {
                _removeChildGroup(c);
            }
            getDelegate().getGroup().remove(child.getDelegate());
            children.remove(child.getName());
        }
    }
    
    public boolean canRemove() {
        if (! isUserDefined()) {
            return false;
        }
        for (Saas s : getServices()) {
            if (! s.isUserDefined()) {
                return false;
            }
        }
        for (SaasGroup child : getChildrenGroups()) {
            if (! child.canRemove()) {
                return false;
            }
        }
        return true;
    }

    /**
     * If this is part of model mutation, caller is responsible to save
     * changes, whether the group is user-defined or pre-installed.
     * 
     * @param group saas group to add
     */
    protected void addChildGroup(SaasGroup group) {
        getChildrenGroups();
        children.put(group.getName(), group);
        getDelegate().getGroup().add(group.getDelegate());
    }
    
    /**
     * Return a clone of group element that has one descendant at each level,
     * excluding root, down to the current group.
     * @return
     */
    public Group getPathFromRoot() {
        SaasGroup parentGroup = getParent();
        if (parentGroup == null) {
            return null;
        }
        
        Group group = new Group();
        group.setName(getName());
        while(parentGroup != SaasServicesModel.getInstance().getRootGroup()) {
            Group p = new Group();
            p.setName(parentGroup.getName());
            p.getGroup().add(group);
            group = p;
            parentGroup = parentGroup.getParent();
        }
        return group;
    }

    /**
     * Check to see if a web service with the given url already exists under
     * the web service manager
     * 
     * TODO: For now, we will only support one service per unique url for the
     * entire web service manager regardless of the group.
     * 
     * @param url url for the service
     * @return true if the service already exists, false otherwise.
     */
    public boolean serviceExists(String url) {
        SaasGroup root = getRoot();
        
        return serviceExists(root, url);
    }
    
    private boolean serviceExists(SaasGroup group, String url) {
        for (Saas service : group.getServices()) {
            if (url.equals(service.getUrl())) {
                return true;
            }
        }
        
        for (SaasGroup g : group.getChildrenGroups()) {
            if (serviceExists(g, url)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Just create a child group node with given name and back parent pointer.
     * Caller should explicitly mutate model, flush, persist and fire event
     * 
     * @param name
     * @return created group
     */
    protected SaasGroup createGroup(String name) {
        List<SaasGroup> childGroups = this.getChildrenGroups();
        
        for (SaasGroup g : childGroups) {
            if (g.getName().equals(name)) {
                throw new IllegalArgumentException(NbBundle.getMessage(SaasGroup.class, "MSG_GroupAlreadyExists"));
            }
        }
        
        Group g = new Group();
        g.setName(name);
        SaasGroup child = new SaasGroup(this, g);
        child.setUserDefined(true);
        return child;
    }
    
    private SaasGroup getRoot() {
        SaasGroup root = this;
        SaasGroup parent = this;
        
        while ((parent = parent.getParent()) != null) {
            root = parent;
        }
        
        return root;
    }
    
    public String toString() {
        return getName();
    }

    public int compareTo(SaasGroup group) {
        return getName().compareTo(group.getName());
    }
}
