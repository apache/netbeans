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

import javax.swing.ImageIcon;
import javax.swing.text.Document;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

import static org.netbeans.modules.javafx2.editor.completion.impl.Bundle.*;

/**
 * TODO - consolidate with PropertyElementItem
 * 
 * @author sdedic
 */
final class EventCompletionItem extends AbstractCompletionItem {
    private static final String ICON_RESOURCE = "org/netbeans/modules/javafx2/editor/resources/event.png"; // NOI18N
    private static ImageIcon ICON;
    
    /**
     * type in a printable form
     */
    private String  eventType;
    
    private boolean attribute;
    
    private boolean inherited;

    public EventCompletionItem(String eventType, boolean attribute, CompletionContext ctx, String text) {
        super(ctx, text);
        this.eventType = eventType;
        this.attribute = attribute;
    }

    public void setInherited(boolean inherited) {
        this.inherited = inherited;
    }

    @NbBundle.Messages({
        "# {0} - event name",
        "FMT_ownEvent=<b>{0}</b>"
    })
    @Override
    protected String getLeftHtmlText() {
        if (!inherited) {
            return FMT_ownEvent(super.getLeftHtmlText());
        } else {
            return super.getLeftHtmlText();
        }
    }

    @Override
    protected String getSubstituteText() {
        boolean replace = ctx.isReplaceExisting();
        if (attribute) {
            if (replace) {
                return super.getSubstituteText();
            } else {
                return super.getSubstituteText() + "=\"\" ";
            }
        } else {
            if (replace) {
                return "<" + super.getSubstituteText();
            } else {
                return "<" + super.getSubstituteText() + "></" + super.getSubstituteText() + ">";
            }
        }
    }
    
    @Override
    protected int getCaretShift(Document d) {
        // incidentally, for all 3 cases:
        return 2 + super.getSubstituteText().length();
    }

    @NbBundle.Messages({
        "# {0} - event type name",
        "FMT_eventType=<i>{0}</i>"
    })
    @Override
    protected String getRightHtmlText() {
        if (eventType == null) {
            return null;
        }
        return FMT_eventType(eventType);
    }

    @Override
    protected ImageIcon getIcon() {
        if (ICON == null) {
            ICON = ImageUtilities.loadImageIcon(ICON_RESOURCE, false);
        }
        return ICON;
    }
    
    public String toString() {
        return "event[" + getSubstituteText() + "]";
    }
}
