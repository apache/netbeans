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
package org.netbeans.modules.hibernate.loaders;

import org.netbeans.modules.xml.multiview.XmlMultiViewDataObject;
import org.netbeans.modules.xml.multiview.XmlMultiViewEditorSupport;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.windows.TopComponent;

/**
 * This class is to workaround the rename bug logged in 
 * http://www.netbeans.org/issues/show_bug.cgi?id=128211
 * 
 * @author Dongmei Cao
 */
public class HbXmlMultiViewEditorSupport extends XmlMultiViewEditorSupport {

    private XmlMultiViewDataObject dObj;
    
    public HbXmlMultiViewEditorSupport(XmlMultiViewDataObject dObj) {
        super(dObj);
        this.dObj = dObj;
    }

    /**
     * Updates the display name of the associated top component.
     */
    @Override
    public void updateDisplayName() {
        final TopComponent mvtc = getMVTC();

        if (mvtc != null) {
            org.netbeans.modules.xml.multiview.Utils.runInAwtDispatchThread(new Runnable() {

                public void run() {
                    String displayName = dObj.getPrimaryFile().getNameExt();
                    if (!displayName.equals(mvtc.getDisplayName())) {
                        mvtc.setDisplayName(displayName);
                    }
                    mvtc.setToolTipText(FileUtil.getFileDisplayName(dObj.getPrimaryFile()));
                }
                });
        }
    }
    }
