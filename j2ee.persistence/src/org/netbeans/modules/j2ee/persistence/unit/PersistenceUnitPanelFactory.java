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

package org.netbeans.modules.j2ee.persistence.unit;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.modules.j2ee.persistence.dd.common.PersistenceUnit;
import org.netbeans.modules.j2ee.persistence.provider.Provider;
import org.netbeans.modules.xml.multiview.XmlMultiViewDataObject;
import org.netbeans.modules.xml.multiview.ui.InnerPanelFactory;
import org.netbeans.modules.xml.multiview.ui.SectionView;
import org.netbeans.modules.xml.multiview.ui.ToolBarDesignEditor;
import org.netbeans.modules.xml.multiview.ui.SectionInnerPanel;

/**
 * Factory for creating persistence unit panels.
 *
 * @author mkuchtiak
 * @author Erno Mononen
 */
public class PersistenceUnitPanelFactory implements InnerPanelFactory, PropertyChangeListener {
    
    private PUDataObject dObj;
    private ToolBarDesignEditor editor;
    /**
     * A naive cache for preventing reinitialization of persistence unit panels
     * if nothing has changed.
     */
    private Map<PersistenceUnit, PersistenceUnitPanel> cache = new HashMap<PersistenceUnit, PersistenceUnitPanel>(10);
    
    /** Creates a new instance of PersistenceUnitPanelFactory */
    PersistenceUnitPanelFactory(ToolBarDesignEditor editor, PUDataObject dObj) {
        this.dObj=dObj;
        this.editor=editor;
        dObj.addPropertyChangeListener(this);
    }
    
    /**
     * Gets the inner panel associated with the given key or creates a new inner
     * panel if the key had no associated panel yet.
     * @param key the persistence unit whose associated panel should be retrieved.
     */ 
    public SectionInnerPanel createInnerPanel(Object key) {
        SectionInnerPanel panel = null;
        if (key instanceof PersistenceUnit){
            PersistenceUnit punit = (PersistenceUnit) key;
            panel = cache.get(punit);
            if (panel == null){
                panel = new PersistenceUnitPanel((SectionView)editor.getContentView(), dObj, punit);
                cache.put(punit, (PersistenceUnitPanel)panel);
            }
        } else if(key instanceof PropertiesPanel.PropertiesParamHolder){
            panel = new PropertiesPanel((SectionView)editor.getContentView(), dObj, (PropertiesPanel.PropertiesParamHolder)key);
        } else {
            throw new IllegalArgumentException("The given key must be an instance of PersistenceUnit or PropertiesParamHolder"); //NOI18N
        }
        return panel;
    }
    
    public void propertyChange(PropertyChangeEvent evt) {
        if (!XmlMultiViewDataObject.PROPERTY_DATA_MODIFIED.equals(evt.getPropertyName())){
            cache.clear();
        }
    }
}

