/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.j2ee.persistence.unit;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.modules.j2ee.persistence.dd.common.PersistenceUnit;
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
    private Map<PersistenceUnit, PersistenceUnitPanel> cache = new HashMap<>(10);
    
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
    @Override
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
    
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (!XmlMultiViewDataObject.PROPERTY_DATA_MODIFIED.equals(evt.getPropertyName())){
            cache.clear();
        }
    }
}

