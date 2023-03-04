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

package org.netbeans.lib.editor.codetemplates.storage;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.KeyStroke;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.settings.CodeTemplateDescription;
import org.netbeans.api.editor.settings.CodeTemplateSettings;
import org.netbeans.spi.editor.mimelookup.MimeDataProvider;
import org.openide.util.Lookup;
import org.openide.util.WeakListeners;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 *
 *  @author Vita Stejskal
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.spi.editor.mimelookup.MimeDataProvider.class)
public final class SettingsProvider implements MimeDataProvider {

    private static final Logger LOG = Logger.getLogger(SettingsProvider.class.getName());
    
    public SettingsProvider () {
    }
    
    /**
     * Lookup providing mime-type sensitive or global-level data
     * depending on which level this initializer is defined.
     * 
     * @return Lookup or null, if there are no lookup-able objects for mime or global level.
     */
    public Lookup getLookup(MimePath mimePath) {
        return new MyLookup(mimePath);
    }
    
    private static final class MyLookup extends AbstractLookup implements PropertyChangeListener {
        
        private final MimePath mimePath;
        
        private final InstanceContent ic;
        private Object codeTemplateSettings = null;
        private CodeTemplateSettingsImpl [] allCtsi;
        
        public MyLookup(MimePath mimePath) {
            this(mimePath, new InstanceContent());
        }
        
        private MyLookup(MimePath mimePath, InstanceContent ic) {
            super(ic);

            this.mimePath = mimePath;
            this.ic = ic;
            
            // Start listening
            List<MimePath> allPaths = mimePath.getIncludedPaths();
            this.allCtsi = new CodeTemplateSettingsImpl[allPaths.size()];
            
            for(int i = 0; i < allPaths.size(); i++) {
                this.allCtsi[i] = CodeTemplateSettingsImpl.get(allPaths.get(i));
                this.allCtsi[i].addPropertyChangeListener(WeakListeners.propertyChange(this, this.allCtsi[i]));
            }
        }

        protected @Override void initialize() {
            synchronized (this) {
                codeTemplateSettings = new CompositeCTS(allCtsi);
                ic.set(Arrays.asList(new Object [] { codeTemplateSettings }), null);
            }
        }
        
        public void propertyChange(PropertyChangeEvent evt) {
            synchronized (this) {
                // Update lookup contents
                if (codeTemplateSettings != null) {
                    codeTemplateSettings = new CompositeCTS(allCtsi);
                    ic.set(Arrays.asList(new Object [] { codeTemplateSettings }), null);
                }
            }
        }

    } // End of MyLookup class
    
    private static final class CompositeCTS extends CodeTemplateSettings {

        private final CodeTemplateSettingsImpl [] allCtsi;
        private List<CodeTemplateDescription> codeTemplates;
        private KeyStroke expansionKey;

        public CompositeCTS(CodeTemplateSettingsImpl [] allCtsi) {
            this.allCtsi = allCtsi;
        }
        
        public List<CodeTemplateDescription> getCodeTemplateDescriptions() {
            if (codeTemplates == null) {
                Map<String, CodeTemplateDescription> map;
                
                if (allCtsi.length > 1) {
                    map = new HashMap<String, CodeTemplateDescription>();
                    for(int i = allCtsi.length - 1; i >= 0; i--) {
                        map.putAll(allCtsi[i].getCodeTemplates());
                    }
                } else {
                    map = allCtsi[0].getCodeTemplates();
                }
                
                codeTemplates = Collections.unmodifiableList(new ArrayList<CodeTemplateDescription>(map.values()));
            }
            return codeTemplates;
        }

        public KeyStroke getExpandKey() {
            if (expansionKey == null) {
                expansionKey = allCtsi[allCtsi.length - 1].getExpandKey();
            }
            return expansionKey;
        }
        
    } // End of CompositeCTS class
}
