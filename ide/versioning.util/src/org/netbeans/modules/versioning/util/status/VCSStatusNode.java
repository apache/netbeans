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

package org.netbeans.modules.versioning.util.status;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorSupport;
import java.io.File;
import java.text.DateFormat;
import java.util.Date;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import org.netbeans.modules.versioning.util.common.VCSFileInformation;
import org.netbeans.modules.versioning.util.common.VCSFileNode;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.PropertySupport.ReadOnly;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author ondra
 */
public abstract class VCSStatusNode<T extends VCSFileNode> extends AbstractNode {
    protected final T node;
    protected final NameProperty nameProperty;
    protected final PathProperty pathProperty;
    protected final StatusProperty statusProperty;

    protected VCSStatusNode (T node) {
        this(node, Lookups.fixed(node.getLookupObjects()));
    }

    protected VCSStatusNode (T node, Lookup lkp) {
        super(Children.LEAF, lkp);
        this.node = node;
        nameProperty = new NameProperty(this);
        pathProperty = new PathProperty(this);
        statusProperty = new StatusProperty(this);
    }

    public Action getNodeAction () {
        return getPreferredAction();
    }

    @Override
    public String getHtmlDisplayName () {
        return node.getInformation().annotateNameHtml(nameProperty.getValue());
    }
    
    public Color getAnnotatedFontColor () {
        return node.getInformation().getAnnotatedColor();
    }

    @Override
    public String getName() {
        return node.getName();
    }

    public File getFile() {
        return node.getFile();
    }

    public final T getFileNode () {
        return node;
    }

    public abstract void refresh ();

    protected abstract static class NodeProperty<T> extends ReadOnly<T> {
        protected NodeProperty (String name, Class<T> type, String displayName, String description) {
            super(name, type, displayName, description);
        }

        @Override
        public String toString() {
            return getValue().toString();
        }

        @Override
        public abstract T getValue ();
        
        @Override
        public PropertyEditor getPropertyEditor() {
            try {
                return new NodePropertyEditor(getValue());
            } catch (Exception e) {
                return super.getPropertyEditor();
            }
        }
    }

    public static class PathProperty extends NodeProperty<String> {
        private String shortPath;
        public static final String NAME = "path"; //NOI18N
        public static final String DISPLAY_NAME = NbBundle.getMessage(VCSStatusNode.class, "LBL_Path.DisplayName"); //NOI18N
        public static final String DESCRIPTION = NbBundle.getMessage(VCSStatusNode.class, "LBL_Path.Description"); //NOI18N

        public PathProperty (VCSStatusNode statusNode) {
            super(NAME, String.class, DISPLAY_NAME, DESCRIPTION); // NOI18N
            shortPath = statusNode.getFileNode().getRelativePath();
            setValue("sortkey", shortPath + "\t" + statusNode.getName()); // NOI18N
        }

        @Override
        public String getValue() {
            return shortPath;
        }
    }

    public static class NameProperty extends NodeProperty<String> {
        public static final String NAME = "name"; //NOI18N
        public static final String DISPLAY_NAME = NbBundle.getMessage(VCSStatusNode.class, "LBL_File.DisplayName"); //NOI18N
        public static final String DESCRIPTION = NbBundle.getMessage(VCSStatusNode.class, "LBL_File.Description"); //NOI18N
        private final VCSFileNode fileNode;

        public NameProperty (VCSStatusNode statusNode) {
            super(NAME, String.class, DISPLAY_NAME, DESCRIPTION); // NOI18N
            setValue("sortkey", statusNode.getFileNode().getName()); // NOI18N
            this.fileNode = statusNode.getFileNode();
        }

        @Override
        public String getValue () {
            return fileNode.getName();
        }
    }

    private static final String [] zeros = new String [] { "", "00", "0", "" }; // NOI18N
    public static class StatusProperty extends NodeProperty<String> {
        public static final String NAME = "status"; //NOI18N
        public static final String DISPLAY_NAME = NbBundle.getMessage(VCSStatusNode.class, "LBL_Status.DisplayName"); //NOI18N
        public static final String DESCRIPTION = NbBundle.getMessage(VCSStatusNode.class, "LBL_Status.Description"); //NOI18N
        private final VCSFileNode fileNode;

        public StatusProperty (VCSStatusNode statusNode) {
            super(NAME, String.class, DISPLAY_NAME, DESCRIPTION);
            String sortable = Integer.toString(statusNode.getFileNode().getInformation().getComparableStatus());
            setValue("sortkey", zeros[sortable.length()] + sortable + "\t" + statusNode.getFileNode().getName()); // NOI18N
            this.fileNode = statusNode.node;
        }

        @Override
        public String getValue () {
            VCSFileInformation finfo =  fileNode.getInformation();
            return finfo.getStatusText();
        }
    }
    
    private static class NodePropertyEditor extends PropertyEditorSupport {

        private static final JLabel renderer = new JLabel();

        static {
            renderer.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 2));
        }

        public NodePropertyEditor (Object value) {
            setValue(value);
        }

        @Override
        public void paintValue (Graphics gfx, Rectangle box) {
            renderer.setForeground(gfx.getColor());
            Object val = getValue();
            if (val instanceof Date) {
                val = DateFormat.getDateTimeInstance().format((Date) val);
            }
            renderer.setText(val.toString());
            renderer.setBounds(box);
            renderer.paint(gfx);
        }

        @Override
        public boolean isPaintable() {
            return true;
        }
    }
}
