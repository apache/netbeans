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

package org.netbeans.modules.websvc.api.webservices;

import java.util.List;
import java.util.Collections;

import javax.swing.JPanel;

import org.openide.WizardValidationException;

/**
 *
 * @author Peter Williams
 */
public interface WsCompileEditorSupport {

    /** Editor fires this property event when the user edits a feature list
     */
    public static final String PROP_FEATURES_CHANGED = "featuresChanged";
    public static final String PROP_DEBUG_CHANGED = "debugChanged";
    public static final String PROP_OPTIMIZE_CHANGED = "optimizeChanged";
    public static final String PROP_VERBOSE_CHANGED = "verboseChanged";
    
    public WsCompileEditorSupport.Panel getWsCompileSupport();
    
    public interface Panel {

        /** The panel for the host
         */
        public JPanel getComponent();
        
        /** Call to initialize the properties in the editor panel
         */
        public void initValues(List/*ServiceSettings*/ settings);
        
        /** Validation entry point.
         */
        public void validatePanel() throws WizardValidationException;
    }
    
    public final class ServiceSettings {
        private String name;
        private StubDescriptor stubType;
        private List/*String*/ availableFeatures;
        private List/*String*/ importantFeatures;
        private String currentFeatures;
        private String newFeatures;
        
        public ServiceSettings(String sn, StubDescriptor st, String c, List a, List i) {
            name = sn;
            stubType = st;
            currentFeatures = newFeatures = c;
            availableFeatures = Collections.unmodifiableList(a);
            importantFeatures = Collections.unmodifiableList(i);
        }
        
        public String getServiceName() {
            return name;
        }
        
        public StubDescriptor getStubDescriptor() {
            return stubType;
        }
        
        public String getCurrentFeatures() {
            return currentFeatures;
        }
        
        public String getNewFeatures() {
            return newFeatures;
        }
        
        public List/*String*/ getAvailableFeatures() {
            return availableFeatures;
        }
        
        public List/*String*/ getImportantFeatures() {
            return importantFeatures;
        }
        
        public String toString() {
            return getServiceName();
        }
        
        public void setNewFeatures(String nf) {
            newFeatures = nf;
        }
    }
    
    public final class FeatureDescriptor {
        
        private String serviceName;
        private String features;
        
        public FeatureDescriptor(String serviceName, String features) {
            this.serviceName = serviceName;
            this.features = features;
        }
        
        public String getServiceName() {
            return serviceName;
        }
        
        public String getFeatures() {
            return features;
        }
    }
}
