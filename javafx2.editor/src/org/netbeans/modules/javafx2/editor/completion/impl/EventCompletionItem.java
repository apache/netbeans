/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
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
