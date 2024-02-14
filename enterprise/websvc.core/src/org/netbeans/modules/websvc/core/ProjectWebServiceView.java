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
package org.netbeans.modules.websvc.core;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.netbeans.api.project.Project;

/**
 * This API displays the web service and client nodes in this project.
 * @author Ajit Bhate
 */
public final class ProjectWebServiceView {

    private static final Lookup.Result<ProjectWebServiceViewProvider> implementations;
//    private static final LookupListener lookupListener;

    static {
        implementations = Lookup.getDefault().lookup(new Lookup.Template<ProjectWebServiceViewProvider>(ProjectWebServiceViewProvider.class));
//        lookupListener = new LookupListener() {
//
//            @Override
//            public void resultChanged(LookupEvent ev) {
//                if (views == null) {
//                    return;
//                }
//                for (ProjectWebServiceView view : views.values()) {
//                    view.updateImpls(true);
//                }
//            }
//        };
//        implementations.addLookupListener(lookupListener);
    }

    /**
     * @return the project
     */
    private Project getProject() {
        return project.get();
    }

    /** 
     * View Type: Service or Client
     */
    public static enum ViewType {

        SERVICE, CLIENT
    }
    private Reference<Project> project;
    private List<ProjectWebServiceViewImpl> impls;
    private List<ChangeListener> serviceListeners,  clientListeners;
    private ChangeListener serviceListener,  clientListener;

    private ProjectWebServiceView(Project project) {
        this.project = new WeakReference<Project>(project);
        serviceListener = new ChangeListenerDelegate(ViewType.SERVICE);
        clientListener = new ChangeListenerDelegate(ViewType.CLIENT);       
    }

    List<ProjectWebServiceViewImpl> getWebServiceViews() {
        return impls != null ? Collections.unmodifiableList(impls) : impls;
    }

    final void addChangeListener(ChangeListener l, ViewType viewType) {
        switch (viewType) {
            case SERVICE:
                if (serviceListeners == null) {
                    serviceListeners = new ArrayList<ChangeListener>();
                }
                serviceListeners.add(l);
                break;
            case CLIENT:
                if (clientListeners == null) {
                    clientListeners = new ArrayList<ChangeListener>();
                }
                clientListeners.add(l);
                break;
        }
    }

    final void removeChangeListener(ChangeListener l, ViewType viewType) {
        switch (viewType) {
            case SERVICE:
                if (serviceListeners != null) {
                    serviceListeners.remove(l);
                }
                break;
            case CLIENT:
                if (clientListeners != null) {
                    clientListeners.remove(l);
                }
                break;
        }
    }

    final void fireChange(ChangeEvent evt, ViewType viewType) {
        if (evt == null) {
            evt = new ChangeEvent(this);
        }
        try {
            switch (viewType) {
                case SERVICE:
                    for (ChangeListener l : serviceListeners) {
                        l.stateChanged(evt);
                    }
                    return;
                case CLIENT:
                    for (ChangeListener l : clientListeners) {
                        l.stateChanged(evt);
                    }
                    return;
            }
        } catch (RuntimeException x) {
        }
    }

    /** 
     * Create view for given type (service or client)
     */
    final Node[] createView(ViewType viewType) {
        initImpls();
        List<Node> result = new ArrayList<Node>();
        for (ProjectWebServiceViewImpl view : getWebServiceViews()) {
            if (!view.isViewEmpty(viewType)) {
                result.addAll(Arrays.<Node>asList(view.createView(viewType)));
            }
        }
        return result.toArray(new Node[0]);
    }

    /** 
     * If a view for given type (service or client) is empty.
     */
    final boolean isViewEmpty(ViewType viewType) {
        initImpls();
        for (ProjectWebServiceViewImpl view : getWebServiceViews()) {
            if (!view.isViewEmpty(viewType)) {
                return false;
            }
        }
        return true;
    }

    /** 
     * Notify that this view is in use.
     * Subclasses may add listeners here
     */
    void addNotify() {
        initImpls();
        for (ProjectWebServiceViewImpl impl : getWebServiceViews()) {
            impl.addNotify();
            impl.addChangeListener(serviceListener, ViewType.SERVICE);
            impl.addChangeListener(clientListener, ViewType.CLIENT);
        }
    }

    /** 
     * Notify that this view is not in use.
     * Subclasses may remove listeners here.
     */
    void removeNotify() {
        if (getWebServiceViews() != null) {
            for (ProjectWebServiceViewImpl impl : getWebServiceViews()) {
                impl.removeChangeListener(serviceListener, ViewType.SERVICE);
                impl.removeChangeListener(clientListener, ViewType.CLIENT);
                impl.removeNotify();
            }
        }
    }

    private void callImplAddNotify(ProjectWebServiceViewImpl impl) {
        impl.addNotify();
        impl.addChangeListener(serviceListener, ViewType.SERVICE);
        impl.addChangeListener(clientListener, ViewType.CLIENT);
    }

    private void callImplRemoveNotify(ProjectWebServiceViewImpl impl) {
        impl.removeChangeListener(serviceListener, ViewType.SERVICE);
        impl.removeChangeListener(clientListener, ViewType.CLIENT);
        impl.removeNotify();
    }

    private void initImpls() {
        if (getWebServiceViews() == null) {
            impls = new ArrayList<ProjectWebServiceViewImpl>(createWebServiceViews(getProject()));
        }
    }

    /*private void updateImpls(boolean fireEvents) {
        if (getWebServiceViews() == null) {
            initImpls();
            if (fireEvents) {
                for (ProjectWebServiceViewImpl impl : getWebServiceViews()) {
                    fireChange(new ChangeEvent(impl), ViewType.SERVICE);
                    fireChange(new ChangeEvent(impl), ViewType.CLIENT);
                    callImplAddNotify(impl);
                }
            }
            return;
        }
        List<ProjectWebServiceViewImpl> oldImpls = new ArrayList<ProjectWebServiceViewImpl>(getWebServiceViews());
        List<ProjectWebServiceViewImpl> newImpls = new ArrayList<ProjectWebServiceViewImpl>(createWebServiceViews(getProject()));
        if (oldImpls.containsAll(newImpls)) {
            oldImpls.removeAll(newImpls);
            for (ProjectWebServiceViewImpl impl : oldImpls) {
                if (fireEvents) {
                    fireChange(new ChangeEvent(impl), ViewType.SERVICE);
                    fireChange(new ChangeEvent(impl), ViewType.CLIENT);
                    callImplRemoveNotify(impl);
                }
                impls.remove(impl);
            }
            return;
        }
        if (newImpls.containsAll(oldImpls)) {
            newImpls.removeAll(oldImpls);
            for (ProjectWebServiceViewImpl impl : newImpls) {
                if (fireEvents) {
                    fireChange(new ChangeEvent(impl), ViewType.SERVICE);
                    fireChange(new ChangeEvent(impl), ViewType.CLIENT);
                    callImplAddNotify(impl);
                }
                impls.add(impl);
            }
            return;
        }
        oldImpls.removeAll(newImpls);
        newImpls.removeAll(getWebServiceViews());
        for (ProjectWebServiceViewImpl impl : oldImpls) {
            if (fireEvents) {
                fireChange(new ChangeEvent(impl), ViewType.SERVICE);
                fireChange(new ChangeEvent(impl), ViewType.CLIENT);
                callImplRemoveNotify(impl);
            }
            impls.remove(impl);
        }
        for (ProjectWebServiceViewImpl impl : newImpls) {
            if (fireEvents) {
                fireChange(new ChangeEvent(impl), ViewType.SERVICE);
                fireChange(new ChangeEvent(impl), ViewType.CLIENT);
                callImplAddNotify(impl);
            }
            impls.add(impl);
        }
    }*/

    private final class ChangeListenerDelegate implements ChangeListener {

        private final ViewType viewType;

        private ChangeListenerDelegate(ViewType viewType) {
            this.viewType = viewType;
        }

        public void stateChanged(ChangeEvent e) {
            fireChange(e, viewType);
        }
    }

    /**
     * Returns lookup.result for ProjectWebServiceViewProviders
     * @return Lookup.Result<ProjectWebServiceViewProvider>.
     */
    static Lookup.Result<ProjectWebServiceViewProvider> getProviders() {
        return implementations;
    }

    static ProjectWebServiceView getProjectWebServiceView(Project project) {    
        return new ProjectWebServiceView(project);
    }

    /**
     * Creates WebServiceViews for given project.
     * @param project Project for which WebServiceViews are to be created.
     * @return list of WebServiceViews.
     */
    private List<ProjectWebServiceViewImpl> createWebServiceViews(Project project) {
        Collection<? extends ProjectWebServiceViewProvider> providers = getProviders().allInstances();
        if (providers == null || providers.isEmpty()) {
            return Collections.<ProjectWebServiceViewImpl>emptyList();
        }
        List<ProjectWebServiceViewImpl> viewImpls = new ArrayList<ProjectWebServiceViewImpl>();
        for (ProjectWebServiceViewProvider provider : providers) {
            viewImpls.add(provider.createProjectWebServiceView(project));
        }
        return viewImpls;
    }

    /**
     * Get the web service nodes that are in the project. 
     * @param project Project that contains the web service nodes
     * @return Array of web service nodes in the project.
     */
    public Node[] getServiceNodes(Project project) {
        return createWebServiceNodes(project, ViewType.SERVICE);
    }

    /**
     * Get the web service client nodes that are in the project. 
     * @param project Project that contains the web service nodes
     * @return Array of web service client nodes in the project.
     */
    public Node[] getClientNodes(Project project) {
        return createWebServiceNodes(project, ViewType.CLIENT);
    }

    /**
     * Creates Web Service/client nodes for given project.
     * @param project Project for which Web service /client nodes to be created.
     * @param viewType type of nodes (service or client).
     * @return array of nodes representing Web Services/Clients in given project.
     */
    private Node[] createWebServiceNodes(Project project, ViewType viewType) {
        List<ProjectWebServiceViewImpl> viewImpls = createWebServiceViews(project);
        List<Node> result = new ArrayList<Node>();
        for (ProjectWebServiceViewImpl view : viewImpls) {
            if (!view.isViewEmpty(viewType)) {
                result.addAll(Arrays.<Node>asList(view.createView(viewType)));
            }
        }
        return result.toArray(new Node[0]);
    }
}
