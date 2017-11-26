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
package org.netbeans.modules.hibernate.loaders.cfg.multiview;

import org.netbeans.modules.hibernate.loaders.cfg.*;
import org.netbeans.modules.hibernate.cfg.model.Event;
import org.netbeans.modules.xml.multiview.ui.InnerPanelFactory;
import org.netbeans.modules.xml.multiview.ui.SectionInnerPanel;
import org.netbeans.modules.xml.multiview.ui.SectionView;
import org.netbeans.modules.xml.multiview.ui.ToolBarDesignEditor;

/**
 * Factory for creating section panels for displaying and/or editing Hibernate configuration file
 * 
 * @author Dongmei Cao
 */
public class HibernateCfgPanelFactory implements InnerPanelFactory {

    private HibernateCfgDataObject dObj;
    private ToolBarDesignEditor editor;

    /** Creates a new instance of ServletPanelFactory */
    HibernateCfgPanelFactory(ToolBarDesignEditor editor, HibernateCfgDataObject dObj) {
        this.dObj = dObj;
        this.editor = editor;
    }

    public SectionInnerPanel createInnerPanel(Object key) {
        
        // Two types of key object: Event and String for the rest
        if( key instanceof Event ) {
            return new EventPanel((SectionView) editor.getContentView(), dObj, (Event)key );
        } else if( key instanceof String ) {

            String keyStr = (String) key;
            if (keyStr.equals(HibernateCfgToolBarMVElement.JDBC_PROPS) ||
                keyStr.equals(HibernateCfgToolBarMVElement.DATASOURCE_PROPS) ||
                keyStr.equals(HibernateCfgToolBarMVElement.CONFIGURATION_PROPS) ||
                keyStr.equals(HibernateCfgToolBarMVElement.JDBC_CONNECTION_PROPS) ||
                keyStr.equals(HibernateCfgToolBarMVElement.TRANSACTION_PROPS) ||
                keyStr.equals(HibernateCfgToolBarMVElement.CACHE_PROPS) ||
                keyStr.equals(HibernateCfgToolBarMVElement.MISCELLANEOUS_PROPS)) {
                return new PropertiesPanel((SectionView) editor.getContentView(), dObj, (String)key);
            } else if (keyStr.equals(HibernateCfgToolBarMVElement.MAPPINGS)) {
                return new MappingsPanel((SectionView) editor.getContentView(), dObj );
            }else if( keyStr.equals( HibernateCfgToolBarMVElement.CLASS_CACHE)) {
                return new ClassCachesPanel((SectionView) editor.getContentView(), dObj );
            }else if( keyStr.equals( HibernateCfgToolBarMVElement.COLLECTION_CACHE)) {
                return new CollectionCachesPanel((SectionView) editor.getContentView(), dObj );
            }else if(keyStr.equals(HibernateCfgToolBarMVElement.SECURITY)) {
                return new SecurityPanel((SectionView) editor.getContentView(), dObj );
            } else // Should never get here {
            {
                return null;
            }
        } else // Should never be here
            return null;
    }

}
