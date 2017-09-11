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

package org.netbeans.modules.java.j2seproject.api;

import java.util.Map;
import javax.swing.JComponent;

import org.netbeans.api.project.Project;

/**
 * Provider of component that will be added to customizer panel of category specified
 * by getCategory() return value. Component will be used for additional customization 
 * of a set of additional properties not customized by standard J2SE Project controls.
 * Implementation of the interface should be registered using {@link org.netbeans.spi.project.ProjectServiceProvider}.
 * 
 * @author Petr Somol
 * @author Milan Kubec
 * @since 1.46
 */
public interface J2SECategoryExtensionProvider {
    
    /**
     * Enumeration of categories for which extension is currently allowed
     */
    enum ExtensibleCategory { PACKAGING, RUN, APPLICATION, DEPLOYMENT }
            
    /**
     * Provides identifier of category whose panel should be extended by this component provider
     * 
     * @returns identifier of the category to be extended
     */
    ExtensibleCategory getCategory();
    
    /**
     * Provides component that is added to the customizer panel of j2seproject
     * selected by getCategory() return value
     * 
     * @param proj project to create the customizer component for
     * @param listener listener to be notified when properties should be updated
     * @returns extension panel to be added to the specified category
     */
    JComponent createComponent(Project proj, ConfigChangeListener listener);
    
    /**
     * Method is called when properties exposed by the provided component
     * get changed externally and the component needs to be updated accordingly
     * 
     * @param props all properties (shared + private);
     *        properites are not evaluated
     */
    void configUpdated(Map<String,String> props);
    
    /**
     * Callback listener for setting properties that are changed by interaction 
     * with the component
     */
    interface ConfigChangeListener {
        /**
         * Method is called when properties should be updated, null prop value 
         * means property will be removed from the property file, only shared 
         * properties are updated; properties are not evaluated
         * 
         * @param updates map holding updated properties
         */
        void propertiesChanged(Map<String,String> updates);
    }
    
}
