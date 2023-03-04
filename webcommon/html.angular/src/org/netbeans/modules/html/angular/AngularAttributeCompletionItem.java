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
package org.netbeans.modules.html.angular;

import java.awt.Color;
import javax.swing.ImageIcon;
import org.netbeans.modules.html.angular.model.DirectiveConvention;
import org.netbeans.modules.html.editor.api.completion.HtmlCompletionItem;
import org.netbeans.modules.html.editor.api.gsf.CustomAttribute;

/**
 *
 * @author marekfukala
 */
public class AngularAttributeCompletionItem extends HtmlCompletionItem.Attribute {

    private boolean isInAngularPage;
    
//    public AngularAttributeCompletionItem(String value, int offset, boolean required, String helpId, boolean autoCompleteValue, boolean isInAngularPage) {
    public AngularAttributeCompletionItem(CustomAttribute ca, int offset, boolean isInAngularPage) {
        super(ca.getName(), offset, true /* not required, but makes them bold */, ca.getHelp());
        this.isInAngularPage = isInAngularPage;
    }

    @Override
    protected ImageIcon getIcon() {
        return Constants.ANGULAR_ICON;
    }

    @Override
    protected Color getAttributeColor() {
        return Constants.ANGULAR_COLOR;
    }

    //move the angular results to the bottom of the completion list if the page
    //doesn't contain angular stuff
    @Override
    public int getSortPriority() {
        return super.getSortPriority() - (isInAngularPage ? 0 : -10);
    }

    @Override
    public boolean hasHelp() {
        return true;
    }
    
}
