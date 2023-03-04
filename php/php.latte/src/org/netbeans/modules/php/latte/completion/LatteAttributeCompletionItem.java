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
package org.netbeans.modules.php.latte.completion;

import java.awt.Color;
import javax.swing.ImageIcon;
import org.netbeans.modules.html.editor.api.completion.HtmlCompletionItem;
import org.netbeans.modules.html.editor.api.gsf.CustomAttribute;
import org.openide.util.ImageUtilities;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public class LatteAttributeCompletionItem extends HtmlCompletionItem.Attribute {
    public static final ImageIcon LATTE_ICON = ImageUtilities.loadImageIcon("org/netbeans/modules/php/latte/resources/latte_cc_icon.png", false); //NOI18N

    public LatteAttributeCompletionItem(CustomAttribute customAttribute, int offset) {
        super(customAttribute.getName(), offset, customAttribute.isValueRequired(), customAttribute.getHelp());
    }

    @Override
    protected ImageIcon getIcon() {
        return LATTE_ICON;
    }

    @Override
    protected Color getAttributeColor() {
        return new Color(102, 58, 19);
    }

    @Override
    public int getSortPriority() {
        return super.getSortPriority() - 10;
    }

    @Override
    public boolean hasHelp() {
        return true;
    }

}
