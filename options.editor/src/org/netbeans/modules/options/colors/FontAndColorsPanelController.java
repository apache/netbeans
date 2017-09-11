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
