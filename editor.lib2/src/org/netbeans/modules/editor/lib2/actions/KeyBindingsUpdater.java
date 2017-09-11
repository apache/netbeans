/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.editor.lib2.actions;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.Action;
import javax.swing.KeyStroke;
import javax.swing.text.EditorKit;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.settings.KeyBindingSettings;
import org.netbeans.api.editor.settings.MultiKeyBinding;
import org.netbeans.lib.editor.util.ArrayUtilities;
import org.netbeans.spi.editor.AbstractEditorAction;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;

/**
 * Management of keybindings settings for an editor kit ensures that all actions
 * contain a right {@link Action.ACCELERATOR} and {@link AbstractEditorAction.MULTI_ACCELERATOR_KEY} values.
 *
 * @author Miloslav Metelka
 */
public final class KeyBindingsUpdater implements LookupListener {
    
    public static synchronized KeyBindingsUpdater get(String mimeType) {
        KeyBindingsUpdater bindings = mime2bindings.get(mimeType);
        if (bindings == null) {
            bindings = new KeyBindingsUpdater(mimeType);
            mime2bindings.put(mimeType, bindings);
        }
        return bindings;
    }
    
    private static Map<String,KeyBindingsUpdater> mime2bindings = 
            new HashMap<String, KeyBindingsUpdater>();
    
    private final String mimeType;

    private Map<String,List<List<KeyStroke>>> actionName2Binding;
    
    private final ArrayList<KitReference> kitRefs = new ArrayList<KitReference>(2);
    
    private final Lookup.Result<KeyBindingSettings> lookupResult;
    
    private KeyBindingsUpdater(String mimeType) {
        this.mimeType = mimeType;
        lookupResult = MimeLookup.getLookup(mimeType).lookup(
                new Lookup.Template<KeyBindingSettings>(KeyBindingSettings.class));
        lookupResult.addLookupListener(this);
        updateActionsAndKits();
    }
    
    public void addKit(EditorKit kit) {
        Map<String,List<List<KeyStroke>>> actionName2BindingLocal;
        synchronized (this) {
            actionName2BindingLocal = actionName2Binding; // actionName2binding not mutated
            kitRefs.add(new KitReference(kit));
        }
        updateKits(actionName2BindingLocal, Collections.singletonList(new KitReference(kit)));
    }
    
    public synchronized void removeKit(EditorKit kit) {
        for (Iterator<KitReference> it = kitRefs.iterator(); it.hasNext();) {
            KitReference ref = it.next();
            if (ref.get() == kit) {
                it.remove();
            }
        }
        checkEmpty();
    }
    
    synchronized void removeKitRef(KitReference kitRef) {
        kitRefs.remove(kitRef);
        checkEmpty();
    }
    
    
    private void checkEmpty() {
        if (kitRefs.isEmpty()) {
            lookupResult.removeLookupListener(this);
            synchronized (KeyBindingsUpdater.class) {
                mime2bindings.remove(mimeType);
            }
        }
    }
    
    private void updateActionsAndKits() {
        Collection<? extends KeyBindingSettings> instances = lookupResult.allInstances();
        if (!instances.isEmpty()) {
            updateActions(instances.iterator().next());
            Map<String,List<List<KeyStroke>>> actionName2BindingLocal;
            List<KitReference> kitRefsCopy;
            synchronized (this) {
                actionName2BindingLocal = actionName2Binding; // actionName2binding not mutated
                @SuppressWarnings("unchecked")
                List<KitReference> krc = (List<KitReference>) kitRefs.clone();
                kitRefsCopy = krc;
            }
            updateKits(actionName2BindingLocal, kitRefsCopy);
        }
    }
    
    private synchronized void updateActions(KeyBindingSettings settings) {
        List<MultiKeyBinding> multiKeyBindings = settings.getKeyBindings();
        actionName2Binding = new HashMap<String,List<List<KeyStroke>>>(multiKeyBindings.size() << 1);
        for (MultiKeyBinding mkb : multiKeyBindings) {
            String actionName = mkb.getActionName();
            List<List<KeyStroke>> mkbList = actionName2Binding.get(actionName);
            if (mkbList == null) {
                mkbList = Collections.singletonList(mkb.getKeyStrokeList());
            } else {
                @SuppressWarnings("unchecked")
                List<KeyStroke>[] mkbArray = new List[mkbList.size() + 1];
                mkbList.toArray(mkbArray);
                mkbArray[mkbList.size()] = mkb.getKeyStrokeList();
                mkbList = ArrayUtilities.unmodifiableList(mkbArray);
            }
            actionName2Binding.put(actionName, mkbList);
        }
        
        // Update kits
        
    }
    
    private static void updateKits(Map<String,List<List<KeyStroke>>> actionName2binding, List<KitReference> kitRefs) {
        // Update kits without locks (a.putValue() is done)
        for (KitReference kitRef : kitRefs) {
            EditorKit kit = kitRef.get();
            if (kit == null) { // Might be null since this is a copy of orig. list
                continue;
            }
            Action[] actions = kit.getActions();
            for (int i = 0; i < actions.length; i++) {
                Action a = actions[i];
                String actionName = (String) a.getValue(Action.NAME);
                @SuppressWarnings("unchecked")
                List<List<KeyStroke>> origAccels = (List<List<KeyStroke>>) 
                        a.getValue(AbstractEditorAction.MULTI_ACCELERATOR_LIST_KEY);
                List<List<KeyStroke>> accels = actionName2binding.get(actionName);
                if (accels == null) {
                    accels = Collections.emptyList();
                }
                if (origAccels == null || !origAccels.equals(accels)) {
                    a.putValue(AbstractEditorAction.MULTI_ACCELERATOR_LIST_KEY, accels);
                    if (accels.size() > 0) {
                        // First keystroke of first multi-key accelerator in the list
                        a.putValue(Action.ACCELERATOR_KEY, accels.get(0).get(0));
                    }
                }
            }
        }
    }

    @Override
    public void resultChanged(LookupEvent ev) {
        updateActionsAndKits();
    }
    
    private final class KitReference extends WeakReference<EditorKit> implements Runnable {
        
        KitReference(EditorKit kit) {
            super(kit, org.openide.util.Utilities.activeReferenceQueue());
        }

        @Override
        public void run() {
            removeKitRef(this);
        }

    }

}
