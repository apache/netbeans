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

/*
 * WSDesignNavigatorPanel.java
 *
 * Created on April 9, 2007, 5:51 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.websvc.design.navigator;

import java.beans.PropertyChangeEvent;
import java.util.Collection;
import javax.swing.JComponent;
import org.netbeans.spi.navigator.NavigatorPanel;
import org.openide.loaders.DataObject;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;

/**
 *
 * @author rico
 */
public class WSDesignNavigatorPanel implements NavigatorPanel, LookupListener{
    
    private WSDesignViewNavigatorContent navigator;
    private Lookup.Result<DataObject> selection;
    
    /** Creates a new instance of WSDesignNavigatorPanel */
    public WSDesignNavigatorPanel() {
    }
    
    public String getDisplayName() {
        return NbBundle.getMessage(WSDesignNavigatorPanel.class,
                "LBL_WSDesignNavigatorPanel_Name");
    }
    
    public String getDisplayHint() {
        return NbBundle.getMessage(WSDesignNavigatorPanel.class,
                "LBL_WSDesignNavigatorPanel_Hint");
    }
    
    public JComponent getComponent() {
        if(navigator == null){
            navigator = new WSDesignViewNavigatorContent();
        }
        return navigator;
    }
    
    public void panelActivated(Lookup context) {
        getComponent();
        TopComponent.getRegistry().addPropertyChangeListener(navigator);
        selection = context.lookup(new Lookup.Template<DataObject>(DataObject.class));
        selection.addLookupListener(this);
        resultChanged(null);
    }
    
    public void panelDeactivated() {
        TopComponent.getRegistry().removePropertyChangeListener(navigator);
        selection.removeLookupListener(this);
        selection = null;
    }
    
    public Lookup getLookup() {
        return null;
    }
    
    public void resultChanged(LookupEvent ev) {
        Collection<? extends DataObject> selected = selection.allInstances();
        if (selected.size() == 1) {
            DataObject dobj = selected.iterator().next();
            navigator.navigate(dobj);
        }
    }
    
}
