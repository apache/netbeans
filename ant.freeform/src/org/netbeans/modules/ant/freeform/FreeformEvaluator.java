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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
                        PropertyEvaluator intermediate = PropertyUtils.sequentialPropertyEvaluator(preprovider, defs.toArray(new PropertyProvider[defs.size()]));
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
        return PropertyUtils.sequentialPropertyEvaluator(preprovider, defs.toArray(new PropertyProvider[defs.size()]));
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
