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
package org.netbeans.modules.javafx2.editor.completion.impl;

import java.beans.BeanInfo;
import javax.swing.ImageIcon;
import javax.swing.text.Document;
import org.openide.loaders.DataObject;
import org.openide.util.ImageUtilities;

/**
 *
 * @author sdedic
 */
final  class ResourcePathItem extends AbstractCompletionItem {
    private DataObject  target;
    private ImageIcon   icon;
    private String      right;
    
    public ResourcePathItem(DataObject target, CompletionContext ctx, String text, String right) {
        super(ctx, text);
        this.target = target;
        this.right = right;
    }

    @Override
    protected String getLeftHtmlText() {
        String tn = target.getName();
        if (target.getPrimaryFile().isFolder()) {
            return "<i>" + tn + "/</i>";
        } else {
            return tn;
        }
    }

    @Override
    protected int getCaretShift(Document d) {
        int pos = super.getCaretShift(d);
        if (!target.getPrimaryFile().isData()) {
            // skip the closing " in the value.
            pos -= 1;
        }
        return pos;
    }
    
    @Override
    protected ImageIcon getIcon() {
        if (icon == null) {
            icon = new ImageIcon(target.getNodeDelegate().getIcon(BeanInfo.ICON_COLOR_16x16));
        }
        return icon;
    }


    public String toString() {
        return "resource[" + super.getSubstituteText() + "]";
    }
}
