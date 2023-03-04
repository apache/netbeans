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

import java.util.HashMap;
import java.util.Map;
import javax.swing.ImageIcon;
import org.openide.util.ImageUtilities;

/**
 *
 * @author sdedic
 */
final class ValueItem extends AbstractCompletionItem {
    private String iconResource;
    private ImageIcon icon;
    private boolean attribute;
    private String valPrefix;
    
    private static final Map<String, ImageIcon> cache = new HashMap<String, ImageIcon>();
    
    public ValueItem(CompletionContext ctx, String text, String valPrefix, String icon) {
        super(ctx, text);
        this.iconResource = icon;
        this.attribute = ctx.isAttribute();
        this.valPrefix = valPrefix;
    }
    
    public ValueItem(CompletionContext ctx, String text, String icon) {
        this(ctx, text, "", icon);
    }
    
    public void setAttribute(boolean attribute) {
        this.attribute = attribute;
    }

    @Override
    protected String getSubstituteText() {
        if (attribute) {
            return "\"" + valPrefix + super.getSubstituteText() + "\"";
        } else {
            return valPrefix + super.getSubstituteText();
        }
    }

    @Override
    protected ImageIcon getIcon() {
        if (icon != null) {
            return icon;
        }
        synchronized (cache) {
            icon = cache.get(iconResource);
        }
        if (icon == null) {
            icon = ImageUtilities.loadImageIcon(iconResource, false);
        }
        synchronized (cache) {
            cache.put(iconResource, icon);
        }
        return icon;
    }

    public String toString() {
        return "value[" + getSubstituteText() + "]";
    }
}
