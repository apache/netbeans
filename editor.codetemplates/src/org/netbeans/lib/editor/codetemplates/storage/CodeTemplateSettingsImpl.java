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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.lib.editor.codetemplates.storage;

import java.awt.event.KeyEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.KeyStroke;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.settings.CodeTemplateDescription;
import org.netbeans.modules.editor.settings.storage.api.EditorSettingsStorage;
import org.openide.util.Utilities;

/**
 *
 * @author Vita Stejskal
 */
public final class CodeTemplateSettingsImpl {

    private static final Logger LOG = Logger.getLogger(CodeTemplateSettingsImpl.class.getName());
    
    public static final String PROP_CODE_TEMPLATES = "CodeTemplateSettingsImpl.PROP_CODE_TEMPLATES"; //NOI18N
    public static final String PROP_EXPANSION_KEY = "CodeTemplateSettingsImpl.PROP_EXPANSION_KEY"; //NOI18N
    public static final String PROP_ON_EXPAND_ACTION = "CodeTemplateSettingsImpl.PROP_ON_EXPAND_ACTION"; //NOI18N
    
    public static synchronized CodeTemplateSettingsImpl get(MimePath mimePath) {
        WeakReference<CodeTemplateSettingsImpl> reference = INSTANCES.get(mimePath);
        CodeTemplateSettingsImpl result = reference == null ? null : reference.get();
        
        if (result == null) {
            result = new CodeTemplateSettingsImpl(mimePath);
            INSTANCES.put(mimePath, new WeakReference<CodeTemplateSettingsImpl>(result));
        }
        
        return result;
    }
    
    public Map<String, CodeTemplateDescription> getCodeTemplates() {
        EditorSettingsStorage<String, CodeTemplateDescription> ess = EditorSettingsStorage.<String, CodeTemplateDescription>get(CodeTemplatesStorage.ID);
        try {
            return ess.load(mimePath, null, false);
        } catch (IOException ioe) {
            LOG.log(Level.WARNING, null, ioe);
            return Collections.<String, CodeTemplateDescription>emptyMap();
        }
    }

    public void setCodeTemplates(Map<String, CodeTemplateDescription> map) {
        EditorSettingsStorage<String, CodeTemplateDescription> ess = EditorSettingsStorage.<String, CodeTemplateDescription>get(CodeTemplatesStorage.ID);
        
        try {
            if (map == null) {
                ess.delete(mimePath, null, false);
            } else {
                ess.save(mimePath, null, false, map);
            }
        } catch (IOException ioe) {
            LOG.log(Level.WARNING, null, ioe);
        }
        
        pcs.firePropertyChange(PROP_CODE_TEMPLATES, null, null);
    }

    public KeyStroke getExpandKey() {
        Preferences prefs = MimeLookup.getLookup(MimePath.EMPTY).lookup(Preferences.class);
        String ks = prefs.get(CODE_TEMPLATE_EXPAND_KEY, null);
        if (ks != null) {
            KeyStroke keyStroke = Utilities.stringToKey(ks);
            if (keyStroke != null) {
                return keyStroke;
            }
        }
        return DEFAULT_EXPANSION_KEY;
    }

    public void setExpandKey(KeyStroke expansionKey) {
        Preferences prefs = MimeLookup.getLookup(MimePath.EMPTY).lookup(Preferences.class);
        prefs.put(CODE_TEMPLATE_EXPAND_KEY, Utilities.keyToString(expansionKey));
        
        try {
            Thread.sleep(100);
        } catch (InterruptedException ex) {
            // ignore
        }
        
        // notify all lookups for all mime types that the expansion key has changed
        List<CodeTemplateSettingsImpl> all = new ArrayList<CodeTemplateSettingsImpl>();
        synchronized (CodeTemplateSettingsImpl.class) {
            for(Reference<CodeTemplateSettingsImpl> r : INSTANCES.values()) {
                CodeTemplateSettingsImpl ctsi = r.get();
                if (ctsi != null) {
                    all.add(ctsi);
                }
            }
        }
        
        for(CodeTemplateSettingsImpl ctsi : all) {
            ctsi.pcs.firePropertyChange(PROP_EXPANSION_KEY, null, null);
        }
    }

    public OnExpandAction getOnExpandAction() {
        Preferences prefs = MimeLookup.getLookup(MimePath.EMPTY).lookup(Preferences.class);
        String action = prefs.get(CODE_TEMPLATE_ON_EXPAND_ACTION, null);
        if (action != null) {
            return OnExpandAction.valueOf(action);
        }
        return OnExpandAction.FORMAT;
    }

    public void setOnExpandAction(OnExpandAction action) {
        Preferences prefs = MimeLookup.getLookup(MimePath.EMPTY).lookup(Preferences.class);
        prefs.put(CODE_TEMPLATE_ON_EXPAND_ACTION, action.name());

        try {
            Thread.sleep(100);
        } catch (InterruptedException ex) {
            // ignore
        }
        
        // notify all lookups for all mime types that the on expand action has changed
        List<CodeTemplateSettingsImpl> all = new ArrayList<CodeTemplateSettingsImpl>();
        synchronized (CodeTemplateSettingsImpl.class) {
            for(Reference<CodeTemplateSettingsImpl> r : INSTANCES.values()) {
                CodeTemplateSettingsImpl ctsi = r.get();
                if (ctsi != null) {
                    all.add(ctsi);
                }
            }
        }
        
        for(CodeTemplateSettingsImpl ctsi : all) {
            ctsi.pcs.firePropertyChange(PROP_CODE_TEMPLATES, null, null);
        }
    }

    public void addPropertyChangeListener(PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }
    
    public void removePropertyChangeListener(PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }

    public static enum OnExpandAction {
        FORMAT, INDENT, NOOP
    }
    
    // ---------------------------------------------
    // Private implementation
    // ---------------------------------------------

    private static final String CODE_TEMPLATE_EXPAND_KEY = "code-template-expand-key"; // NOI18N
    private static final KeyStroke DEFAULT_EXPANSION_KEY = KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0);
    private static final String CODE_TEMPLATE_ON_EXPAND_ACTION = "code-template-on-expand-action"; // NOI18N

    private static final Map<MimePath, WeakReference<CodeTemplateSettingsImpl>> INSTANCES =
        new WeakHashMap<MimePath, WeakReference<CodeTemplateSettingsImpl>>();
    
//    private static final CodeTemplateDescriptionComparator CTC = new CodeTemplateDescriptionComparator();
    
    private final MimePath mimePath;
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    
    private CodeTemplateSettingsImpl(MimePath mimePath) {
        this.mimePath = mimePath;
    }
}    
