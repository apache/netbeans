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
package org.netbeans.modules.javafx2.editor.css;

import java.lang.ref.SoftReference;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.css.editor.module.spi.*;
import org.netbeans.modules.css.lib.api.CssModule;
import org.netbeans.modules.css.lib.api.properties.PropertyCategory;
import org.netbeans.modules.css.lib.api.properties.PropertyDefinition;
import org.netbeans.modules.javafx2.project.api.JavaFXProjectUtils;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 * Java FX CSS editor
 *
 * @author Anton Chechel, Marek Fukala, Petr Somol
 * @version 2.0
 */
@NbBundle.Messages({
    "JavaFXCSSModule.displayName=JavaFX"
})
@ServiceProvider(service = CssEditorModule.class)
public class JavaFXCSSModule extends CssEditorModule implements CssModule {

    static ElementKind JFX_CSS_ELEMENT_KIND = ElementKind.GLOBAL;
    private static final String PROPERTIES_DEFINITION_PATH = "org/netbeans/modules/javafx2/editor/css/javafx2"; // NOI18N
    private static Map<String, PropertyDefinition> propertyDescriptors;
    private static SoftReference<Map<String, Boolean>> fileTypeCache;
    private static final String PSEUDO_CLASSES_PROPERTY = "@pseudo-classes"; // NOI18N
    private static Collection<String> pseudoClasses;
    private static Browser FX_BROWSER = new FxBrowser();
    
    @Override
    public Collection<String> getPseudoClasses(EditorFeatureContext context) {
        if(pseudoClasses == null) {
            pseudoClasses = new ArrayList<String>();
            PropertyDefinition prop = getJavaFXProperties().get(PSEUDO_CLASSES_PROPERTY);
            if(prop != null) {
                String grammar = prop.getGrammar();
                StringTokenizer tokenizer = new StringTokenizer(grammar, "| "); //NOI18N
                while(tokenizer.hasMoreTokens()) {
                    pseudoClasses.add(tokenizer.nextToken());
                }
            }
        }
        return pseudoClasses;
    }

    @Override
    public Collection<Browser> getExtraBrowsers(FileObject file) {
        return isJavaFXContext(file) ? Collections.singleton(FX_BROWSER) : null;
    }

    @Override
    public Collection<String> getPropertyNames(FileObject file) {
        return isJavaFXContext(file) ? getJavaFXProperties().keySet() : Collections.<String>emptyList();
    }

    @Override
    public PropertyDefinition getPropertyDefinition(String propertyName) {
        return getJavaFXProperties().get(propertyName);
    }

    private synchronized Map<String, PropertyDefinition> getJavaFXProperties() {
        if (propertyDescriptors == null) {
            propertyDescriptors = Utilities.parsePropertyDefinitionFile(PROPERTIES_DEFINITION_PATH, this);
        }
        return propertyDescriptors;
    }

    /**
     * Checks whether the file is standard CSS or FX CSS. Unfortunately this
     * can not be easily determined by file extension nor file contents as FX CSS
     * is a superset of CSS; a valid FX CSS file can be also a valid standard CSS file.
     * Here we decide based on file location - CSS files within a FX project are considered
     * to be FX CSS, all others are considered to be standard CSS.
     * 
     * @param file file context - may be null!
     * @return
     */
    private boolean isJavaFXContext(FileObject file) {
        if(file != null) {
            Map<String, Boolean> m;
            if(fileTypeCache == null) {
                m = new HashMap<String, Boolean>();
                fileTypeCache = new SoftReference<Map<String, Boolean>>( m );
            } else {
                m = fileTypeCache.get();
            }
            if(m != null) {
                Boolean b = m.get(file.getPath());
                if(b != null) {
                    return b.booleanValue();
                } else {
                    Project p = FileOwnerQuery.getOwner(file);
                    if(p != null) {
                        boolean isFX = JavaFXProjectUtils.isJavaFxEnabled(p) || JavaFXProjectUtils.isMavenFxProject(p);
                        m.put(file.getPath(), isFX);
                        return isFX;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public String getName() {
        return "javafx2_css"; //NOI18N
    }

    @Override
    public String getDisplayName() {
        return Bundle.JavaFXCSSModule_displayName();
        
    }

    @Override
    public String getSpecificationURL() {
        return "http://docs.oracle.com/javafx/2/api/javafx/scene/doc-files/cssref.html"; // NOI18N
    }
    
    private static class FxBrowser extends Browser {
        
        private static final String VENDOR = "Oracle"; // NOI18N
        private static final String NAME = "JavaFX"; // NOI18N
        private static final String RENDERING_ENGINE = "javafx"; // NOI18N
        private static final String PREFIX = "fx"; // NOI18N
        
        private static final String ICONS_LOCATION = "/org/netbeans/modules/javafx2/editor/resources/"; //NOI18N
        private static final String iconBase = "javafxicon"; // NOI18N
        private URL active, inactive;
      
        @Override
        public PropertyCategory getPropertyCategory() {
            return PropertyCategory.UNKNOWN;
        }

        @Override
        public String getVendor() {
            return VENDOR;
        }

        @Override
        public String getName() {
            return NAME;
        }

        @Override
        public String getDescription() {
            return new StringBuilder().append(getVendor()).append(' ').append(getName()).toString(); // NOI18N
        }

        @Override
        public String getRenderingEngineId() {
            return RENDERING_ENGINE;
        }

        @Override
        public String getVendorSpecificPropertyId() {
            return PREFIX;
        }

        //why icon by an URL??? - its put to the generated html source this way:
        //         sb.append("<img src=\""); //NOI18N
        //         sb.append(browserIcon.toExternalForm());
        //         sb.append("\">"); // NOI18N

        @Override
        public synchronized URL getActiveIcon() {
            if(active == null) {
                active = FxBrowser.class.getResource(
                    ICONS_LOCATION + iconBase + ".png"); //NOI18N
            }
            return active;
        }

        @Override
        public synchronized URL getInactiveIcon() {
            if(inactive == null) {
                inactive = FxBrowser.class.getResource(
                    ICONS_LOCATION + iconBase + "-disabled.png"); //NOI18N
            }
            return inactive;
        }
    
    }
}
