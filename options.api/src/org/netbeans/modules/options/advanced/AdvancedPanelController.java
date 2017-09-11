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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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

package org.netbeans.modules.options.advanced;

import java.beans.PropertyChangeListener;
import java.util.List;
import javax.swing.JComponent;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;


/**
 * Implementation of one panel in Options Dialog.
 *
 * @author Jan Jancura
 */
public final class AdvancedPanelController extends OptionsPanelController {

    private String subpath = null;

    /* Creates new AdvancedPanelController.
     * @param subpath path to folder under OptionsDialog folder containing 
     * instances of AdvancedOption class. Path is composed from registration 
     * names divided by slash.
     */
    public AdvancedPanelController(String subpath) {
        this.subpath = subpath;
    }

    public void update () {
        getAdvancedPanel ().update ();
    }

    public void applyChanges () {
        getAdvancedPanel ().applyChanges ();
    }
    
    public void cancel () {
        getAdvancedPanel ().cancel ();
    }
    
    public boolean isValid () {
        return getAdvancedPanel ().dataValid ();
    }
    
    public boolean isChanged () {
        return getAdvancedPanel ().isChanged ();
    }
        
    @Override
    public Lookup getLookup () {
        return getAdvancedPanel ().getLookup ();
    }
    
    public JComponent getComponent (Lookup masterLookup) {
	AdvancedPanel p = getAdvancedPanel();
	p.init();
	return p;
    }
    
    @Override
    public void setCurrentSubcategory(String subpath) {
        getAdvancedPanel().setCurrentSubcategory(subpath);
    }
    
    public HelpCtx getHelpCtx () {
        return getAdvancedPanel ().getHelpCtx ();
    }
    
    public void addPropertyChangeListener (PropertyChangeListener l) {
        getAdvancedPanel().addPropertyChangeListener(l);
        getAdvancedPanel().addModelPropertyChangeListener(l);
    }

    public void removePropertyChangeListener (PropertyChangeListener l) {
        getAdvancedPanel().removePropertyChangeListener(l);
        getAdvancedPanel().removeModelPropertyChangeListener(l);
    }

    @Override
    public void handleSuccessfulSearch(String searchText, List<String> matchedKeywords) {
        getAdvancedPanel().handleSearch(searchText, matchedKeywords);
    }

    private AdvancedPanel advancedPanel;
    
    private synchronized AdvancedPanel getAdvancedPanel () {
        if (advancedPanel == null)
            advancedPanel = new AdvancedPanel(subpath);
        return advancedPanel;
    }
}
