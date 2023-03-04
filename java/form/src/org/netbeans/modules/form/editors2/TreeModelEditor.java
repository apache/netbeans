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
package org.netbeans.modules.form.editors2;

import java.awt.Component;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.beans.PropertyEditorSupport;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import org.netbeans.modules.form.FormCodeAwareEditor;
import org.netbeans.modules.form.FormModel;
import org.netbeans.modules.form.FormProperty;
import org.netbeans.modules.form.NamedPropertyEditor;
import org.netbeans.modules.form.RADComponent;
import org.netbeans.modules.form.RADProperty;
import org.netbeans.modules.form.codestructure.CodeStructure;
import org.netbeans.modules.form.codestructure.CodeVariable;
import org.openide.explorer.propertysheet.editors.XMLPropertyEditor;
import org.openide.util.NbBundle;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Simple property editor for <code>TreeModel</code>.
 *
 * @author Jan Stola
 */
public class TreeModelEditor extends PropertyEditorSupport
        implements XMLPropertyEditor, NamedPropertyEditor, FormCodeAwareEditor {
    /** Prefix of variables used by the property editor. */
    private static final String NODE_VARIABLE_NAME = "treeNode"; // NOI18N
    /** Model of the edited form. */
    private FormModel formModel;
    /** Property being edited. */
    private FormProperty property;

    /**
     * Returns source code for the current value.
     * 
     * @return source code for the current value.
     */
    @Override
    public String getSourceCode() {
        DefaultTreeModel model = (DefaultTreeModel)getValue();
        
        // Nodes
        DefaultMutableTreeNode root = (DefaultMutableTreeNode)model.getRoot();
        StringBuilder buf = new StringBuilder();
        String rootVarName = generateCode(root, null, 0, buf);

        // The model itself
        RADProperty prop = (RADProperty)this.property;
        RADComponent comp = prop.getRADComponent();
        CodeVariable var = comp.getCodeExpression().getVariable();
        String varName = (var == null) ? null : var.getName();
        String setter = prop.getPropertyDescriptor().getWriteMethod().getName();
        if (varName != null) {
            setter = varName + '.' + setter;
        }
        buf.append(setter).append("(new "); // NOI18N
        buf.append(DefaultTreeModel.class.getName()).append('(').append(rootVarName).append("));\n"); // NOI18N

        return buf.toString();
    }

    /**
     * Generates code for the given node (and its subnodes). The code is generated into the passed buffer.
     * 
     * @param node node for which the code should be generated.
     * @param parentVarName name of the variable used for the parent of the node (is <code>null</code> for the root).
     * @param varNameSuffix preferred integer suffix of the variable name (minus one).
     * @param buf buffer used to generate the code into.
     * @return variable name used for the given node.
     */
    private String generateCode(DefaultMutableTreeNode node, String parentVarName, int varNameSuffix, StringBuilder buf) {
        CodeStructure codeStructure = formModel.getCodeStructure();
        String varName;
        CodeVariable variable;
        do {
            varNameSuffix++;
            varName = NODE_VARIABLE_NAME + varNameSuffix;
            variable = codeStructure.getVariable(varName);
        } while ((variable != null) && !DefaultMutableTreeNode.class.equals(variable.getDeclaredType()));
        if (variable == null) {
            String name = codeStructure.getExternalVariableName(DefaultMutableTreeNode.class, varName, true);
            assert varName.equals(name);
            buf.append(DefaultMutableTreeNode.class.getName()).append(' ');
        }
        buf.append(varName).append(" = new "); // NOI18N
        buf.append(DefaultMutableTreeNode.class.getName()).append('(').append('"');
        String name = node.getUserObject().toString();
        name = name.replace("\"", "\\\""); // NOI18N
        buf.append(name).append("\");\n"); // NOI18N
        for (int i=0; i<node.getChildCount(); i++) {
            DefaultMutableTreeNode child = (DefaultMutableTreeNode)node.getChildAt(i);
            generateCode(child, varName, varNameSuffix, buf);
        }
        if (parentVarName != null) {
            buf.append(parentVarName).append(".add(").append(varName).append(");\n"); // NOI18N
        }
        return varName;
    }

    /**
     * Sets context of the property editor.
     * 
     * @param formModel model of the edited form.
     * @param property property being edited.
     */
    @Override
    public void setContext(FormModel formModel, FormProperty property) {
        this.formModel = formModel;
        this.property = property;
    }

    /**
     * Determines whether custom property editor is supported.
     * 
     * @return <code>true</code>.
     */
    @Override
    public boolean supportsCustomEditor() {
        return true;
    }

    /**
     * Returns custom property editor.
     * 
     * @return custom property editor.
     */
    @Override
    public Component getCustomEditor() {
        return new TreeModelCustomizer(this);
    }

    @Override
    public String getAsText() {
        return null;
    }

    @Override
    public boolean isPaintable() {
        return true;
    }

    @Override
    public void paintValue(Graphics g, Rectangle rectangle) {
        String msg = NbBundle.getMessage(TreeModelEditor.class, "MSG_TreeModel"); // NOI18N
        FontMetrics fm = g.getFontMetrics();
        g.drawString(msg, rectangle.x, rectangle.y + (rectangle.height - fm.getHeight())/2 + fm.getAscent());
    }

    /**
     * Returns human-readable name of this property editor.
     * 
     * @return human-readable name of this property editor.
     */
    @Override
    public String getDisplayName() {
        return NbBundle.getBundle(TreeModelEditor.class).getString("CTL_TreeModelEditor_DisplayName"); // NOI18N
    }

    /**
     * Creates the tree model according to the passed textual representation.
     * 
     * @param txt textual representation of the model.
     * @return tree model that corresponds to the passed textual representation.
     */
    TreeModel createTreeModel(String txt) {
        StringTokenizer st = new StringTokenizer(txt, "\n"); // NOI18N
        List<Integer> indents = new ArrayList<Integer>();
        List<String> names = new ArrayList<String>();
        while (st.hasMoreTokens()) {
            String line = st.nextToken();
            String name = line.trim();
            if (name.length() == 0) {
                continue;
            }
            int indent = 0;
            while (line.charAt(indent) == ' ') {
                indent++;
            }
            indents.add(indent);
            names.add(name);
        }
        DefaultMutableTreeNode root = null;
        List<DefaultMutableTreeNode> nodes = new ArrayList<DefaultMutableTreeNode>(names.size());
        for (int i=0; i<names.size(); i++) {
            int indent = indents.get(i);
            String name = names.get(i);
            DefaultMutableTreeNode node = new DefaultMutableTreeNode(name);
            nodes.add(node);
            if (i == 0) {
                continue;
            }
            int j;
            for (j=i-1; j>=0; j--) {
                int ind = indents.get(j);
                if (ind < indent) {
                    break;
                }
            }
            DefaultMutableTreeNode parent;
            if (j >= 0) {
                parent = nodes.get(j);
            } else {
                if (root == null) {
                    root = new DefaultMutableTreeNode("root"); // NOI18N
                    root.add(nodes.get(0));
                }
                parent = root;
            }
            parent.add(node);
        }
        if (nodes.isEmpty()) {
            root = new DefaultMutableTreeNode("root"); // NOI18N
        }
        if (root == null) {
            // User's root
            root = nodes.get(0);
        }
        root.setUserObject(new CodeUserObject(root.getUserObject().toString(), txt));
        DefaultTreeModel model = new DefaultTreeModel(root);
        return model;
    }

    /**
     * Returns textual representation of the tree model.
     * 
     * @return textual representation of the tree model.
     */
    String getCodeValue() {
        Object value = getValue();
        if (value instanceof DefaultTreeModel) {
            DefaultTreeModel model = (DefaultTreeModel)value;
            Object root = model.getRoot();
            if (root instanceof DefaultMutableTreeNode) {
                DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode)root;
                Object userObject = rootNode.getUserObject();
                if (userObject instanceof CodeUserObject) {
                    CodeUserObject code = (CodeUserObject)userObject;
                    return code.code;
                }
            }
        }
        return null;
    }

    /**
     * Raises version of the form file to the NB release where this
     * property editor was added.
     */
    @Override
    public void updateFormVersionLevel() {
        formModel.raiseVersionLevel(FormModel.FormVersion.NB65, FormModel.FormVersion.NB65);
    }

    /** XML element used to store the tree model. */
    private static final String XML_TREE_MODEL = "TreeModel"; // NOI18N
    /**
     * XML attribute of TreeModel element where the textual
     * representation of the model is stored.
     */
    private static final String ATTR_CODE = "code"; // NOI18N

    /**
     * Reads the tree model from XML.
     * 
     * @param element element that holds information about the model.
     */
    @Override
    public void readFromXML(Node element) {
        Node attr = element.getAttributes().getNamedItem(ATTR_CODE);
        String txt = attr.getNodeValue();
        setValue(createTreeModel(txt));
    }

    /**
     * Stores the tree model into XML.
     * 
     * @param doc document where the XML representation should be stored.
     * @return XML representation of the model.
     */
    @Override
    public Node storeToXML(Document doc) {
        String code = getCodeValue();
        if (code != null) {
            Element el = doc.createElement(XML_TREE_MODEL);
            el.setAttribute(ATTR_CODE, code);            
            return el;
        }
        return null;
    }

    /**
     * User-code object used in the root of the tree model. It holds
     * the textual representation of the whole tree e.g. it is
     * a hack to avoid necessity to create FormDesignValue.
     */
    static class CodeUserObject implements Serializable {
        /**
         * Creates new <code>CodeUserObject</code>.
         * 
         * @param name original user object.
         * @param code additional information stored in this user object.
         */
        CodeUserObject(String name, String code) {
           this.name = name;
           this.code = code;
        }
        /** Original user object e.g. name/text of the node. */
        String name;
        /** Additional information stored in this user object. */
        String code;
        /**
         * Returns original user object.
         * 
         * @return original user object.
         */
        @Override
        public String toString() {
            return name;
        }
    }

}
