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
package org.netbeans.modules.javascript2.debug.ui.breakpoints;

import javax.swing.JComponent;
import org.netbeans.modules.javascript2.debug.ui.JSUtils;
import org.netbeans.modules.javascript2.debug.ui.breakpoints.JSLineBreakpointCustomizerPanel;
import org.netbeans.spi.debugger.ui.BreakpointType;
import org.netbeans.spi.debugger.ui.Controller;
import org.netbeans.spi.debugger.ui.EditorContextDispatcher;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;


@NbBundle.Messages({"JavaScriptBreakpointTypeCategory=JavaScript",
    "LineBreakpointTypeName=Line"})
@BreakpointType.Registration(displayName="#LineBreakpointTypeName")
public class JSLineBreakpointType extends BreakpointType {
    
    private JSLineBreakpointCustomizerPanel cust;
    
    /* (non-Javadoc)
     * @see org.netbeans.spi.debugger.ui.BreakpointType#getCategoryDisplayName()
     */
    @Override
    public String getCategoryDisplayName() {
        return Bundle.JavaScriptBreakpointTypeCategory();
    }

    /* (non-Javadoc)
     * @see org.netbeans.spi.debugger.ui.BreakpointType#getCustomizer()
     */
    @Override
    public JComponent getCustomizer() {
        if (cust == null) {
            cust = new JSLineBreakpointCustomizerPanel();
        }
        return cust;
    }

    @Override
    public Controller getController() {
        getCustomizer();
        return cust.getController();
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.spi.debugger.ui.BreakpointType#getTypeDisplayName()
     */
    @Override
    public String getTypeDisplayName() {
        return Bundle.LineBreakpointTypeName();
    }

    /* (non-Javadoc)
     * @see org.netbeans.spi.debugger.ui.BreakpointType#isDefault()
     */
    @Override
    public boolean isDefault() {
        FileObject mostRecentFile = EditorContextDispatcher.getDefault().getMostRecentFile();
        if (mostRecentFile == null) {
            return false;
        }
        String mimeType = mostRecentFile.getMIMEType();
        return JSUtils.JS_MIME_TYPE.equals(mimeType);
    }

}
