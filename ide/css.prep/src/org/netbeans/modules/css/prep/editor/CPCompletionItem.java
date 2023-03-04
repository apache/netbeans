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
package org.netbeans.modules.css.prep.editor;

import java.awt.Color;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.modules.csl.api.ElementHandle;
import org.netbeans.modules.csl.api.HtmlFormatter;
import org.netbeans.modules.css.editor.module.spi.CssCompletionItem;
import org.netbeans.modules.css.prep.editor.model.CPElementHandle;
import static org.netbeans.modules.css.prep.editor.model.CPElementType.VARIABLE_GLOBAL_DECLARATION;
import org.netbeans.modules.web.common.ui.api.WebUIUtils;
import org.netbeans.swing.plaf.LFCustoms;

/**
 *
 * @author marekfukala
 */
public abstract class CPCompletionItem extends CssCompletionItem {

    protected static final Color COLOR = new Color(0, 0, 0);
    protected static final Color ORIGIN_COLOR = new Color(99, 99, 99);
    
    protected String origin;
    protected CPElementHandle handle;

    public CPCompletionItem(@NonNull ElementHandle elementHandle, @NonNull CPElementHandle handle, int anchorOffset, @NullAllowed String origin) {
        super(elementHandle, handle.getName(), anchorOffset, false);
        this.handle = handle;
        this.origin = origin;
    }
    
    @Override
    public String getLhsHtml(HtmlFormatter formatter) {
        switch (handle.getType()) {
            case VARIABLE_GLOBAL_DECLARATION:
            case MIXIN_DECLARATION:
                formatter.appendHtml("<font color=");
                formatter.appendHtml(WebUIUtils.toHexCode(LFCustoms.shiftColor(COLOR)));
                formatter.appendHtml("><b>"); //NOI18N
                break;
        }
        
        formatter.appendText(getName());
        
        switch (handle.getType()) {
            case MIXIN_DECLARATION:
            case VARIABLE_GLOBAL_DECLARATION:
                formatter.appendHtml("</b></font>"); //NOI18N);
                break;
        }
        
        return formatter.getText();
    }

    @Override
    public String getRhsHtml(HtmlFormatter formatter) {
        if(origin == null) {
            return super.getRhsHtml(formatter);
        } else {
            formatter.appendHtml("<font color=");
            formatter.appendHtml(WebUIUtils.toHexCode(LFCustoms.shiftColor(ORIGIN_COLOR)));
            formatter.appendHtml(">");
            formatter.appendText(origin);
            formatter.appendHtml("</font>"); //NOI18N
            return formatter.getText();
        }
    }

   
}
