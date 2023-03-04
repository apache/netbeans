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

package org.netbeans.modules.j2ee.ddloaders.web.multiview;

import org.netbeans.modules.xml.multiview.ui.SectionView;
import org.netbeans.modules.xml.multiview.ui.ToolBarDesignEditor;
import org.netbeans.modules.xml.multiview.ui.SectionInnerPanel;
import org.netbeans.modules.j2ee.ddloaders.web.DDDataObject;

/** ReferencesFactory - factory for creating references' tables
 *
 * @author mkuchtiak
 * Created on April 11, 2005
 */
public class ReferencesFactory implements org.netbeans.modules.xml.multiview.ui.InnerPanelFactory {
    private DDDataObject dObj;
    ToolBarDesignEditor editor;
    
    /** Creates a new instance of ServletPanelFactory */
    ReferencesFactory(ToolBarDesignEditor editor, DDDataObject dObj) {
        this.dObj=dObj;
        this.editor=editor;
    }
    
    public SectionInnerPanel createInnerPanel(Object key) {
        if ("env_entries".equals(key)) return new EnvEntriesPanel((SectionView)editor.getContentView(), dObj);
        else if ("res_refs".equals(key)) return new ResRefsPanel((SectionView)editor.getContentView(), dObj);
        else if ("res_env_refs".equals(key)) return new ResEnvRefsPanel((SectionView)editor.getContentView(), dObj);
        else if ("ejb_refs".equals(key)) return new EjbRefsPanel((SectionView)editor.getContentView(), dObj);
        else if ("message_dest_refs".equals(key)) return new MessageDestRefsPanel((SectionView)editor.getContentView(), dObj);
        else return null;
    }
}
