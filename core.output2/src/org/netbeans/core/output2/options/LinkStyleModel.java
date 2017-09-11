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
package org.netbeans.core.output2.options;

import javax.swing.ComboBoxModel;
import javax.swing.event.ListDataListener;
import org.netbeans.core.output2.options.OutputOptions.LinkStyle;
import org.openide.util.NbBundle;

/**
 * List mode for Link Style combo box.
 *
 * @author jhavlin
 */
@NbBundle.Messages({
    "LBL_Underline=Underline", //NOI18N
    "LBL_None=None" //NOI18N
})
public class LinkStyleModel implements ComboBoxModel {

    private class LinkStyleItem {

        private LinkStyle linkStyle;
        private String displayName;

        public LinkStyleItem(LinkStyle linkStyle, String displayName) {
            this.linkStyle = linkStyle;
            this.displayName = displayName;
        }

        public LinkStyle getLinkStyle() {
            return linkStyle;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }

    /* Instances of combo box items. */
    private LinkStyleItem none = new LinkStyleItem(LinkStyle.NONE,
            Bundle.LBL_None());
    private LinkStyleItem underline = new LinkStyleItem(LinkStyle.UNDERLINE,
            Bundle.LBL_Underline());
    /**
     * The currently selected item.
     */
    private LinkStyleItem selectedItem = underline;

    @Override
    public void setSelectedItem(Object anItem) {
        if (anItem instanceof LinkStyleItem) {
            selectedItem = (LinkStyleItem) anItem;
        } else {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public Object getSelectedItem() {
        return selectedItem;
    }

    @Override
    public int getSize() {
        return 2;
    }

    @Override
    public Object getElementAt(int index) {
        switch (index) {
            case 0:
                return underline;
            case 1:
                return none;
            default:
                throw new IndexOutOfBoundsException();
        }
    }

    @Override
    public void addListDataListener(ListDataListener l) {
        // Nothing, model is constant.
    }

    @Override
    public void removeListDataListener(ListDataListener l) {
        // Nothing, model is constant.
    }

    LinkStyle getLinkStyle() {
        return selectedItem.getLinkStyle();
    }

    void setLinkStyle(LinkStyle style) {
        if (style != selectedItem.getLinkStyle()) {
            switch (style) {
                case NONE:
                    selectedItem = none;
                    break;
                case UNDERLINE:
                    selectedItem = underline;
                    break;
                default:
                    throw new IllegalArgumentException();
            }
        }
    }

    private LinkStyleItem linkStyleItemFor(LinkStyle linkStyle) {
        switch (linkStyle) {
            case NONE:
                return none;
            case UNDERLINE:
                return underline;
            default:
                throw new IllegalArgumentException();
        }
    }

    public Object itemFor(LinkStyle linkStyle) {
        return linkStyleItemFor(linkStyle);
    }
}
