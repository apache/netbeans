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

package org.netbeans.modules.ant.freeform;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.spi.project.support.ant.AntProjectEvent;
import org.netbeans.spi.project.support.ant.AntProjectListener;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.PropertyProvider;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.ErrorManager;
import org.openide.util.Parameters;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Element;

/**
 * Manages property evaluation for the freeform project.
 * Refreshes properties if (1) project.xml changes; (2) some *.properties changes.
 * @author Jesse Glick
 */
final class FreeformEvaluator implements PropertyEvaluator, AntProjectListener, PropertyChangeListener {

    private final FreeformProject project;
    private PropertyEvaluator delegate;
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private final Set<PropertyEvaluator> intermediateEvaluators = new HashSet<PropertyEvaluator>();
    private final Object privateLock = new Object();
    
    public FreeformEvaluator(FreeformProject project) throws IOException {
        this.project = project;
        init();
        project.helper().addAntProjectListener(this);
    }
    
    private void init() throws IOException {
        PropertyEvaluator newDelegate = initEval();
        synchronized (privateLock) {
            if (delegate != null) {
                delegate.removePropertyChangeListener(this);
            }
            newDelegate.addPropertyChangeListener(this);
            delegate = newDelegate;
        }
        if (org.netbeans.modules.ant.freeform.Util.err.isLoggable(ErrorManager.INFORMATIONAL)) {
            org.netbeans.modules.ant.freeform.Util.err.log("properties for " + project.getProjectDirectory() + ": " + delegate.getProperties());
        }
    }
    
    private PropertyEvaluator initEval() throws IOException {
        // Stop listening to old intermediate evaluators.
        Iterator<PropertyEvaluator> ieIt = intermediateEvaluators.iterator();
        while (ieIt.hasNext()) {
            ieIt.next().removePropertyChangeListener(this);
            ieIt.remove();
        }
        PropertyProvider preprovider = project.helper().getStockPropertyPreprovider();
        List<PropertyProvider> defs = new ArrayList<PropertyProvider>();
        Element genldata = project.getPrimaryConfigurationData();
        Element properties = XMLUtil.findElement(genldata, "properties", FreeformProjectType.NS_GENERAL); // NOI18N
        if (properties != null) {
            for (Element e : XMLUtil.findSubElements(properties)) {
                if (e.getLocalName().equals("property")) { // NOI18N
                    String val = XMLUtil.findText(e);
                    if (val == null) {
                        val = "";
                    }
                    defs.add(PropertyUtils.fixedPropertyProvider(Collections.singletonMap(e.getAttribute("name"), val))); // NOI18N
                } else {
                    assert e.getLocalName().equals("property-file") : e;
                    String fname = XMLUtil.findText(e);
                    if (fname.contains("${")) { // NOI18N
                        // Tricky (#48230): need to listen to changes in the location of the file as well as its contents.
                        PropertyEvaluator intermediate = PropertyUtils.sequentialPropertyEvaluator(preprovider, defs.toArray(new PropertyProvider[0]));
                        fname = intermediate.evaluate(fname);
                        if (fname == null) {
                            continue;
                        }
                        // Listen to changes in it, too.
                        intermediate.addPropertyChangeListener(this);
                        intermediateEvaluators.add(intermediate);
                    }
                    defs.add(PropertyUtils.propertiesFilePropertyProvider(project.helper().resolveFile(fname)));
                }
            }
        }
        return PropertyUtils.sequentialPropertyEvaluator(preprovider, defs.toArray(new PropertyProvider[0]));
    }
    
    public String getProperty(String prop) {
        return delegate.getProperty(prop);
    }
    
    public String evaluate(String text) {
        return delegate.evaluate(text);
    }
    
    public Map<String,String> getProperties() {
        return delegate.getProperties();
    }
    
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        Parameters.notNull("listener", listener); // NOI18N
        pcs.addPropertyChangeListener(listener);
    }
    
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        Parameters.notNull("listener", listener); // NOI18N
        pcs.removePropertyChangeListener(listener);
    }
    
    public void configurationXmlChanged(AntProjectEvent ev) {
        fireAnyChange();
    }
    
    private void fireAnyChange() {
        try {
            init();
        } catch (IOException ex) {
            ErrorManager.getDefault().notify(ErrorManager.ERROR, ex);
        } catch (RuntimeException ex) {
            // Something else? E.g. IAE when parsing <properties> block.
            ErrorManager.getDefault().notify(ErrorManager.ERROR, ex);
        }
        pcs.firePropertyChange(null, null, null);
    }
    
    public void propertiesChanged(AntProjectEvent ev) {
        // ignore
    }
    
    public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
        Object source = propertyChangeEvent.getSource();
        assert source instanceof PropertyEvaluator : source;
        if (intermediateEvaluators.contains(source)) {
            // A <property-file> may have changed location. Generally need to rebuild the list of definers.
            fireAnyChange();
        } else {
            // If a properties file changes on disk, we refire that from the delegate.
            assert source == delegate : "Got change from " + source + " rather than current delegate " + delegate;
            pcs.firePropertyChange(propertyChangeEvent.getPropertyName(), null, null);
        }
    }
    
}
