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
package org.netbeans.modules.web.jsf;

import java.awt.Component;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import org.netbeans.modules.web.jsf.palette.items.CancellableDialog;
import org.openide.cookies.EditCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public class JsfTemplateUtils {

    /** Base path within the layer.xml for definition of templates. */
    public static final String BASE_TPL_PATH = "/Templates/JSF/JSF_From_Entity_Wizard"; //NOI18N

    /** Path of the Standard JavaServer Faces templates. */
    public static final String STANDARD_TPL = "StandardJSF"; //NOI18N

    private static final String LOCALIZING_BUNDLE = "SystemFileSystem.localizingBundle"; //NOI18N
    private static final Comparator TEMPLATE_COMPARATOR = new TemplateComparator();

    public static String getLocalizedName(FileObject fo) {
        String name = fo.getNameExt();
        String bundleName = (String) fo.getAttribute(LOCALIZING_BUNDLE);
        if (bundleName != null) {
            try {
                bundleName = org.openide.util.Utilities.translate(bundleName);
                ResourceBundle b = NbBundle.getBundle(bundleName);
                String localizedName = b.getString(fo.getPath());
                if (localizedName != null) {
                    name = localizedName;
                }
            } catch (MissingResourceException ex) {
            // ignore
            }
        }

        return name;
    }

    public static List<Template> getTemplates(TemplateType tt) {
        List<Template> result = new ArrayList<>();
        FileObject templateRoot = FileUtil.getConfigRoot().getFileObject(tt.getValue());
        assert templateRoot != null; // at least the base templates should be registered
        Enumeration<? extends FileObject> children = templateRoot.getChildren(false);
        while (children.hasMoreElements()) {
            FileObject folder = children.nextElement();
            Object position = folder.getAttribute("position");
            if (!(position instanceof Integer)) {
                result.add(new Template(folder.getName(), getLocalizedName(folder)));
            } else {
                result.add(new Template(folder.getName(), getLocalizedName(folder), (Integer) position));
            }
        }
        result.sort(TEMPLATE_COMPARATOR);
        return result;
    }

    public static String getTemplatePath(TemplateType tt, String templatesStyle, String template) {
        return tt.getValue() + "/" + templatesStyle + "/" + template; //NOI18N
    }

    public static class TemplateComparator implements Comparator<Template> {
        @Override
        public int compare(Template o1, Template o2) {
            return o1.getPosition() - o2.getPosition();
        }
    }

    public static enum TemplateType {
        SNIPPETS("/Templates/JSF/JSF_From_Entity_Snippets"),
        PAGES("/Templates/JSF/JSF_From_Entity_Wizard");

        private final String value;

        private TemplateType(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

    }

    public static class Template {

        private final String name;
        private final String displayName;
        private final int position;

        public Template(String name, String displayName) {
            this(name, displayName, Integer.MAX_VALUE);
        }

        public Template(String name, String displayName, int position) {
            this.name = name;
            this.displayName = displayName;
            this.position = position;
        }

        public String getName() {
            return name;
        }

        public String getDisplayName() {
            return displayName;
        }

        public int getPosition() {
            return position;
        }

    }

    public static class TemplateCellRenderer implements ListCellRenderer {

        protected DefaultListCellRenderer defaultRenderer = new DefaultListCellRenderer();

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JLabel renderer = (JLabel) defaultRenderer.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof JsfTemplateUtils.Template) {
                renderer.setText(((JsfTemplateUtils.Template) value).getDisplayName());
            }
            return renderer;
        }
    }

    public static class OpenTemplateAction extends AbstractAction {
        private static final long serialVersionUID = 1L;

        private String[] templateFileName;
        private CancellableDialog panel;

        public OpenTemplateAction(CancellableDialog panel, String actionName, String ... templateFileName) {
            this.templateFileName = templateFileName;
            this.panel = panel;
            this.putValue(Action.NAME, actionName);
        }

        @Override
        public void actionPerformed(ActionEvent arg0) {
            for (String template : templateFileName) {
                openSingle(template);
            }
        }

        private void openSingle(String template) {
            FileObject tableTemplate = FileUtil.getConfigRoot().getFileObject(template);
            try {
                final DataObject dob = DataObject.find(tableTemplate);
                EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        dob.getLookup().lookup(EditCookie.class).edit();
                    }
                });
                panel.cancel();
            } catch (DataObjectNotFoundException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

}
