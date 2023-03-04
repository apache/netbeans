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
package org.netbeans.modules.xml.text.completion;

import java.awt.Color;

import java.beans.BeanInfo;
import javax.swing.text.JTextComponent;
import org.netbeans.modules.xml.api.model.*;
import org.netbeans.spi.editor.completion.CompletionTask;
import org.netbeans.swing.plaf.LFCustoms;


/**
 * Represents value option (attribute one or element content one).
 * <p>
 * It takes advatage of replacent text vs. display name. Providers
 * should use shorted display name for list values. e.g. for
 * <code>&lt;example enums="one two three fo|"</code>
 * provider can return nodeValue <code>"one two three four"</code>
 * and display name <code>"four"</code> to denote that it actually
 * completed only the suffix.
 * 
 * @author  sands
 * @author  Petr Kuzel
 */
class ValueResultItem extends XMLResultItem {
    private static final Color COLOR = new Color(64, 64, 255);
    
    private final String replText;
    
    private final GrammarResult res;
    
    private int delLen;

    public ValueResultItem(int position, GrammarResult res, int delLen, String suffix) {
        super(position, res.getDisplayName(), res.getDisplayName());
        this.res = res;
        foreground = Color.magenta;
        selectionForeground = Color.magenta.darker();
        String t = res.getNodeValue();
        if (suffix != null) {
            replText = t + suffix;
        } else {
            replText = t;
        }
        icon = res.getIcon(BeanInfo.ICON_COLOR_16x16);
        this.delLen = delLen;
    }

    @Override
    int getDeleteLength(String currentText, String replaceToText, int len) {
        return delLen; 
    }

    @Override
    public String getReplacementText(int modifiers) {
        return replText;
    }
    
    @Override
    Color getPaintColor() { return LFCustoms.shiftColor(COLOR); }

    @Override
    public CompletionTask createDocumentationTask() {
        return doCreateDocumentationTask(res);
    }
}
