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
