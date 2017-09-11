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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
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
