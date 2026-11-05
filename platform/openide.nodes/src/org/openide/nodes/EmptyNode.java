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
package org.openide.nodes;

import java.awt.Image;
import java.awt.datatransfer.Transferable;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import org.openide.util.HelpCtx;
import org.openide.util.datatransfer.NewType;
import org.openide.util.datatransfer.PasteType;

final class EmptyNode extends Node {

    private static final PropertySet[] NO_PROPERTY_SETS = new PropertySet[0];

    EmptyNode() {
        super(Children.LEAF, null);

        // Setting the name to non-null value for the node
        // to return "reasonable" name and displayName
        // not using this.setName since that throws
        super.setName(""); // NOI18N
    }

    @Override
    public Node cloneNode() {
        return new FilterNode(this);
    }

    @Override
    public void setName(String s) {
        throw new UnsupportedOperationException("Setting name on Node.EMPTY is not allowed.");
    }

    @Override
    public void setDisplayName(String s) {
        throw new UnsupportedOperationException("Setting name on Node.EMPTY is not allowed.");
    }

    @Override
    public void setShortDescription(String s) {
        throw new UnsupportedOperationException("Setting name on Node.EMPTY is not allowed.");
    }

    @Override
    public Image getIcon(int type) {
        return findIcon(type, NodeIcons.ICON_BASE);
    }

    @Override
    public Image getOpenedIcon(int type) {
        return findIcon(type, NodeIcons.OPENED_ICON_BASE);
    }

    private Image findIcon(int type, int ib) {
        return NodeIcons.findIcon(NodeIcons.DEFAULT_ICON_BASE, type, ib, NodeIcons.DEFAULT_ICON_EXTENSION);
    }

    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    public boolean canRename() {
        return false;
    }

    @Override
    public boolean canDestroy() {
        return false;
    }

    @Override
    public PropertySet[] getPropertySets() {
        return NO_PROPERTY_SETS;
    }

    @Override
    public Transferable clipboardCopy() throws IOException {
        return NodeTransfer.transferable(this, NodeTransfer.CLIPBOARD_COPY);
    }

    @Override
    public Transferable clipboardCut() throws IOException {
        return NodeTransfer.transferable(this, NodeTransfer.CLIPBOARD_CUT);
    }

    @Override
    public Transferable drag() throws IOException {
        return clipboardCopy();
    }

    @Override
    public boolean canCopy() {
        return true;
    }

    @Override
    public boolean canCut() {
        return false;
    }

    private void createPasteTypes(Transferable t, List<PasteType> s) {
        NodeTransfer.Paste p = NodeTransfer.findPaste(t);
        if (p != null) {
            // adds all its types into the set
            s.addAll(Arrays.asList(p.types(this)));
        }
    }

    @Override
    public PasteType[] getPasteTypes(Transferable t) {
        List<PasteType> s = new LinkedList<>();
        createPasteTypes(t, s);
        return s.toArray(NodeIcons.NO_PASTE_TYPES);
    }

    @Override
    public PasteType getDropType(Transferable t, int action, int index) {
        List<PasteType> s = new LinkedList<>();
        createPasteTypes(t, s);
        return s.isEmpty() ? null : s.get(0);
    }

    @Override
    public NewType[] getNewTypes() {
        return NodeIcons.NO_NEW_TYPES;
    }

    @Override
    public boolean hasCustomizer() {
        return false;
    }

    @Override
    public java.awt.Component getCustomizer() {
        return null;
    }

    @Override
    public Handle getHandle() {
        return DefaultHandle.createHandle(this);
    }

}
