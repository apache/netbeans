/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
public class LinkStyleModel implements ComboBoxModel<Object> {

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
