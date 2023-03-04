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

package org.netbeans.modules.options.colors;

import org.netbeans.modules.options.colors.spi.FontsColorsController;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import org.netbeans.api.options.OptionsDisplayer;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.WeakListeners;
import org.openide.util.lookup.Lookups;


/**
 * Implementation of one panel in Options Dialog.
 *
 * @author Jan Jancura
 */
@OptionsPanelController.TopLevelRegistration(
    id=OptionsDisplayer.FONTSANDCOLORS,
    categoryName="#CTL_Font_And_Color_Options",
    iconBase="org/netbeans/modules/options/colors/colors.png",
    keywords="#KW_FontsAndColorsOptions",
    keywordsCategory="FontsAndColors",
    position=400
//    title="#CTL_Font_And_Color_Options_Title",
//    description="#CTL_Font_And_Color_Options_Description"
)
public final class FontAndColorsPanelController extends OptionsPanelController {
    
    private final Lookup.Result<? extends FontsColorsController> lookupResult;
    private final LookupListener lookupListener = new LookupListener() {
        @Override
        public void resultChanged(LookupEvent ev) {
            rebuild();
        }
    };
    
    private Collection<? extends FontsColorsController> delegates;
    private FontAndColorsPanel component;
    
    public FontAndColorsPanelController() {
        Lookup lookup = Lookups.forPath("org-netbeans-modules-options-editor/OptionsDialogCategories/FontsColors"); //NOI18N
        lookupResult = lookup.lookupResult(FontsColorsController.class);
        lookupResult.addLookupListener(WeakListeners.create(
            LookupListener.class,
            lookupListener,
            lookupResult
        ));
        rebuild();
    }
    
    @Override
    public void update() {
        if (getFontAndColorsPanel() != null) {
            getFontAndColorsPanel().update();
        }
    }
    
    @Override
    public void applyChanges() {
        if (getFontAndColorsPanel() != null) {
            getFontAndColorsPanel().applyChanges();
        }
    }
    
    @Override
    public void cancel() {
        if (getFontAndColorsPanel() != null) {
            getFontAndColorsPanel().cancel();
        }
    }
    
    @Override
    public boolean isValid() {
        if (getFontAndColorsPanel() != null) {
            return getFontAndColorsPanel().dataValid();
        } else {
            return true;
        }
    }
    
    @Override
    public boolean isChanged() {
        if (getFontAndColorsPanel() != null) {
            return getFontAndColorsPanel().isChanged();
        } else {
            return false;
        }
    }

    
    @Override
    public JComponent getComponent(Lookup masterLookup) {
        return getFontAndColorsPanel();
    }
    
    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("netbeans.optionsDialog.fontAndColorsPanel");
    }
    
    @Override
    public void addPropertyChangeListener(PropertyChangeListener l) {
        if (getFontAndColorsPanel() != null) {
            getFontAndColorsPanel().addPropertyChangeListener(l);
        }
    }
    
    @Override
    public void removePropertyChangeListener(PropertyChangeListener l) {
        if (getFontAndColorsPanel() != null) {
            getFontAndColorsPanel().removePropertyChangeListener(l);
        }
    }
    
    private synchronized FontAndColorsPanel getFontAndColorsPanel() {
        if (component == null && SwingUtilities.isEventDispatchThread()) {
            assert !delegates.isEmpty() : "Font and Colors Panel is empty."; //NOI18N
            component = new FontAndColorsPanel(delegates);
        }
        return component;
    }
    
    private void rebuild() {
        this.delegates = lookupResult.allInstances();
        this.component = null;
    }
}
