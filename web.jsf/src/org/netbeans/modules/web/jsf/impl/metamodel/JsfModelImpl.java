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
package org.netbeans.modules.web.jsf.impl.metamodel;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationModelHelper;
import org.netbeans.modules.web.jsf.api.facesmodel.FacesConfig;
import org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigComponent;
import org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigModel;
import org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigModelFactory;
import org.netbeans.modules.web.jsf.api.facesmodel.ManagedBean;
import org.netbeans.modules.web.jsf.api.metamodel.Behavior;
import org.netbeans.modules.web.jsf.api.metamodel.Component;
import org.netbeans.modules.web.jsf.api.metamodel.FacesConverter;
import org.netbeans.modules.web.jsf.api.metamodel.FacesManagedBean;
import org.netbeans.modules.web.jsf.api.metamodel.JsfModel;
import org.netbeans.modules.web.jsf.api.metamodel.JsfModelElement;
import org.netbeans.modules.web.jsf.api.metamodel.ModelUnit;
import org.netbeans.modules.web.jsf.api.metamodel.SystemEventListener;
import org.netbeans.modules.web.jsf.api.metamodel.Validator;
import org.netbeans.modules.web.jsf.impl.facesmodel.AnnotationBehaviorRenderer;
import org.netbeans.modules.web.jsf.impl.facesmodel.AnnotationRenderer;
import org.netbeans.modules.web.jsf.impl.facesmodel.JSFConfigQNames;
import org.netbeans.modules.xml.retriever.catalog.Utilities;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.locator.CatalogModelException;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 *
 */
public class JsfModelImpl extends JsfModelManagers implements JsfModel {

    private static final Logger LOG = Logger.getLogger(JsfModelImpl.class.getName());
    private final PropertyChangeSupport changeSupport;
    private ModelUnit unit;
    private List<JSFConfigModel> models;
    private JSFConfigModel facesConfigModel;
    private Map<FileObject, WeakReference<JSFConfigModel>> modelCache = new HashMap<FileObject, WeakReference<JSFConfigModel>>();
    private final static Map<Class<? extends JsfModelElement>, ElementFinder<? extends JsfModelElement>> FINDERS =
            new HashMap<Class<? extends JsfModelElement>, ElementFinder<? extends JsfModelElement>>();

    static {
        FINDERS.put(Behavior.class, new BehaviorFinder());
        FINDERS.put(Component.class, new ComponentFinder());
        FINDERS.put(FacesConverter.class, new ConverterFinder());
        FINDERS.put(FacesManagedBean.class, new ManagedBeanFinder());
        FINDERS.put(Validator.class, new ValidatorFinder());
    }

    JsfModelImpl(ModelUnit unit, AnnotationModelHelper helper) {
        super(helper);
        this.unit = unit;
        changeSupport = new PropertyChangeSupport(this);
        models = new LinkedList<JSFConfigModel>();
        registerChangeListeners();
        initModels();
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.metamodel.JsfModel#getElement(java.lang.Class)
     */
    @Override
    public <T extends JsfModelElement> List<T> getElements(Class<T> clazz) {
        ElementFinder<T> finder = getFinder(clazz);
        Class<? extends JSFConfigComponent> type =
                (finder == null) ? (Class) clazz : finder.getConfigType();
        List<T> result = new LinkedList<T>();
        for (FacesConfig config : getFacesConfigs()) {
            List<? extends JSFConfigComponent> children = config.getChildren(type);
            for (JSFConfigComponent component : children) {
                if (clazz.isAssignableFrom(FacesManagedBean.class)
                        && ((ManagedBean) component).getManagedBeanClass() == null) {
                    continue;
                }
                result.add((T) component);
            }
        }

        JSFConfigModel model = getMainModel();
        boolean metadataComplete = false;
        if (model != null) {
            FacesConfig config = model.getRootComponent();
            if (config != null) {
                Boolean isComplete = config.isMetaDataComplete();
                if (isComplete != null) {
                    metadataComplete = isComplete;
                }
            }
        }
        if (finder != null && !metadataComplete) {
            result.addAll(finder.getAnnotations(this));
        }

        return result;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.metamodel.JsfModel#getFacesConfigs()
     */
    @Override
    public List<FacesConfig> getFacesConfigs() {
        List<FacesConfig> configModels = new ArrayList<FacesConfig>();
        for (JSFConfigModel model : getModelsVariable()) {
            try {
                model.sync();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
            FacesConfig fc = model.getRootComponent();
            if (fc != null) {
                configModels.add(fc);
            }
        }
        return configModels;
    }

    private void synch(JSFConfigModel model) {
        if (model != null) {
            try {
                model.sync();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.metamodel.JsfModel#getMainConfig()
     */
    @Override
    public FacesConfig getMainConfig() {
        JSFConfigModel model = getMainModel();
        synch(model);
        return model != null ? model.getRootComponent() : null;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.metamodel.JsfModel#getModels()
     */
    @Override
    public List<JSFConfigModel> getModels() {
        List<JSFConfigModel> mods = getModelsVariable();
        for (JSFConfigModel model : mods) {
            try {
                model.sync();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return Collections.unmodifiableList(mods);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.metamodel.JsfModel#addPropertyChangeListener(java.beans.PropertyChangeListener)
     */
    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        getChangeSupport().addPropertyChangeListener(listener);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.metamodel.JsfModel#removePropertyChangeListener(java.beans.PropertyChangeListener)
     */
    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        getChangeSupport().removePropertyChangeListener(listener);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.impl.facesmodel.AbstractJsfModel#getClientBehaviorRenderer(java.lang.String)
     */
    @Override
    protected List<AnnotationBehaviorRenderer> getClientBehaviorRenderers(String renderKitId) {
        FacesConfig config = getMainConfig();
        if (config != null) {
            Boolean complete = config.isMetaDataComplete();
            if (complete != null && complete) {
                return Collections.emptyList();
            }
        }
        Collection<ClientBehaviorRendererImpl> collection = getClientBehaviorManager().getObjects();
        List<AnnotationBehaviorRenderer> result =
                new ArrayList<AnnotationBehaviorRenderer>(collection.size());
        for (ClientBehaviorRendererImpl renderer : collection) {
            String id = renderer.getRenderKitId();
            if (renderKitId.equals(id)) {
                result.add(renderer);
            }
        }
        return result;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.impl.facesmodel.AbstractJsfModel#getRenderers(java.lang.String)
     */
    @Override
    protected List<AnnotationRenderer> getRenderers(String renderKitId) {
        FacesConfig config = getMainConfig();
        if (config != null) {
            Boolean complete = config.isMetaDataComplete();
            if (complete != null && complete) {
                return Collections.emptyList();
            }
        }
        Collection<RendererImpl> collection = getRendererManager().getObjects();
        List<AnnotationRenderer> result = new ArrayList<AnnotationRenderer>(collection.size());
        for (RendererImpl renderer : collection) {
            String id = renderer.getRenderKitId();
            if (renderKitId.equals(id)) {
                result.add(renderer);
            }
        }
        return result;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.impl.facesmodel.AbstractJsfModel#getSystemEventListeners()
     */
    @Override
    protected List<SystemEventListener> getSystemEventListeners() {
        // Notion : one don't need to check metadata-complete attribute.
        // Listeners annotations works in any case.
        Collection<SystemEventListenerImpl> collection = getSystemEventManager().getObjects();
        List<SystemEventListener> listeners = ObjectProviders.findApplicationSystemEventListeners(getHelper());
        List<SystemEventListener> result = new ArrayList<SystemEventListener>(collection.size() + listeners.size());
        result.addAll(collection);
        result.addAll(listeners);
        return result;
    }

    private PropertyChangeSupport getChangeSupport() {
        return changeSupport;
    }

    private synchronized void setModelsVariable(List<JSFConfigModel> models, JSFConfigModel model) {
        this.models = models;
        this.facesConfigModel = model;
        // events of this change is fiured outside of synch section
    }

    private synchronized List<JSFConfigModel> getModelsVariable() {
        return models;
    }

    private synchronized JSFConfigModel getMainModel() {
        return facesConfigModel;
    }

    /**
     * Can return null if the created model is not faces-config model.
     */
    protected JSFConfigModel createModel(FileObject fo) {
        if (fo.isFolder()) {
            LOG.log(Level.WARNING, "Creation of faces-config model for directory: {0}", fo.getPath());
            return null;
        }
        JSFConfigModel model = getCachedModel(fo);
        if (model == null) {
            try {
                ModelSource source = Utilities.createModelSource(fo, true);
                Lookup lookup = source.getLookup();
                lookup = new ProxyLookup(lookup, Lookups.singleton(this));
                source = new ModelSource(lookup, true);
                model = JSFConfigModelFactory.getInstance().getModel(source);
                Node rootElement = model.getDocument().getDocumentElement();
                if (rootElement == null || !JSFConfigQNames.areSameQName(JSFConfigQNames.FACES_CONFIG, (Element) rootElement)) {
                    return null;
                } else {
                    putCachedModel(fo, model);
                }
            } catch (CatalogModelException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return model;
    }

    private synchronized JSFConfigModel getCachedModel(FileObject fo) {
        WeakReference<JSFConfigModel> ref = modelCache.get(fo);
        if (ref != null) {
            return ref.get();
        }
        return null;
    }

    private synchronized void putCachedModel(FileObject fo, JSFConfigModel model) {
        modelCache.put(fo, new WeakReference(model));
    }

    private void initModels() {
        List<JSFConfigModel> mods = new LinkedList<JSFConfigModel>();
        JSFConfigModel facesConfigMod = null;
        FileObject facesConfig = getUnit().getApplicationFacesConfig();
        if (facesConfig != null) {
            facesConfigMod = createModel(facesConfig);
            if (facesConfigMod != null) {
                mods.add(facesConfigMod);
            }
        }
        for (FileObject fo : getUnit().getApplicationConfigurationResources()) {
            JSFConfigModel model = createModel(fo);
            if (model != null) {
                mods.add(model);
            }
        }
        setModelsVariable(mods, facesConfigMod);
        // TODO: nobody seems to be listening and there is not property defined in JsfModel api class
        // firing something anyway.
        getChangeSupport().firePropertyChange("model-changed", null, null);
    }

    private ModelUnit getUnit() {
        return unit;
    }

    private <T extends JsfModelElement> ElementFinder<T> getFinder(Class<T> clazz) {
        return (ElementFinder<T>) FINDERS.get(clazz);
    }
    private RequestProcessor requestProcessor = new RequestProcessor();

    private void registerChangeListeners() {
        unit.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                // if unit has changed then update model in some other thread:
                requestProcessor.post(new Runnable() {
                    @Override
                    public void run() {
                        initModels();
                    }
                });
            }
        });
    }
}
