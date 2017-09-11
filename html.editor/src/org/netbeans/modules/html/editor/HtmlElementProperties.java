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
package org.netbeans.modules.html.editor;

import java.beans.PropertyEditor;
import java.beans.PropertyEditorSupport;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.html.api.HtmlDataNode;
import org.netbeans.modules.html.editor.api.gsf.HtmlParserResult;
import org.netbeans.modules.html.editor.completion.AttrValuesCompletion;
import org.netbeans.modules.html.editor.lib.api.elements.Attribute;
import org.netbeans.modules.html.editor.lib.api.elements.ElementType;
import org.netbeans.modules.html.editor.lib.api.elements.Node;
import org.netbeans.modules.html.editor.lib.api.elements.OpenTag;
import org.netbeans.modules.html.editor.lib.api.model.HtmlModel;
import org.netbeans.modules.html.editor.lib.api.model.HtmlModelFactory;
import org.netbeans.modules.html.editor.lib.api.model.HtmlTag;
import org.netbeans.modules.html.editor.lib.api.model.HtmlTagAttribute;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.spi.CursorMovedSchedulerEvent;
import org.netbeans.modules.parsing.spi.SchedulerEvent;
import org.netbeans.modules.web.common.api.ValueCompletion;
import org.openide.cookies.EditorCookie;
import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Node.Property;
import org.openide.nodes.Node.PropertySet;
import org.openide.nodes.PropertySupport;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author marekfukala
 */
@NbBundle.Messages({
    "edit.attribute.tooltip=You can edit the value or delete the attribute by setting an empty value",
    "new.attribute.tooltip=You can add the attribute by setting its value",
    "element.element.attributes.title=Element Attributes"
})
public class HtmlElementProperties {

    private static final Logger LOGGER = Logger.getLogger(HtmlElementProperties.class.getSimpleName());
    private static final Level LEVEL = Level.FINE;

    static void parsed(final HtmlParserResult result, SchedulerEvent event) {
        try {
            FileObject file = result.getSnapshot().getSource().getFileObject();
            if (file == null) {
                LOGGER.log(LEVEL, "null file, exit");
                return;
            }

            if (!file.isValid()) {
                LOGGER.log(LEVEL, "invalid file, exit");
                return;
            }

            final DataObject dobj = DataObject.find(file);
            org.openide.nodes.Node dataObjectNode = dobj.getNodeDelegate();
            if(!(dataObjectNode instanceof HtmlDataNode)) {
                return ;
            }
            final HtmlDataNode htmlNode =  (HtmlDataNode) dataObjectNode;
            
            final int caretOffset;
            if (event == null) {
                LOGGER.log(LEVEL, "run() - NULL SchedulerEvent?!?!?!");
                caretOffset = -1;
            } else {
                if (event instanceof CursorMovedSchedulerEvent) {
                    caretOffset = ((CursorMovedSchedulerEvent) event).getCaretOffset();
                } else {
                    LOGGER.log(LEVEL, "run() - !(event instanceof CursorMovedSchedulerEvent)");
                    caretOffset = -1;
                }
            }

            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    runInEDT(result, htmlNode, dobj, caretOffset);
                }
            });


        } catch (DataObjectNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        }

    }

    private static void runInEDT(HtmlParserResult result, HtmlDataNode htmlNode, DataObject dobj, int caretOffset) {
        EditorCookie ec = dobj.getLookup().lookup(EditorCookie.class);
        if(ec == null) {
            return ;
        }
        
        //dirty workaround
        if (caretOffset == -1) {
            JEditorPane[] panes = ec.getOpenedPanes(); //needs EDT
            if (panes != null && panes.length > 0) {
                JEditorPane pane = panes[0]; //hopefully the active one
                caretOffset = pane.getCaretPosition();
            }
            LOGGER.log(LEVEL, "workarounded caret offset: {0}", caretOffset);
        }

        Node node = result.findBySemanticRange(caretOffset, true);
        if (node != null) {
            if (node.type() == ElementType.OPEN_TAG) { //may be root node!
                OpenTag ot = (OpenTag) node;
                
                JEditorPane[] panes = ec.getOpenedPanes(); //needs EDT
                if (panes != null && panes.length > 0) {
                    //update the property set only for opened documents
                    htmlNode.setPropertySets(new PropertySet[]{new PropertiesPropertySet(result, ot)});
                }
                
            }
        }
    }

    public static class PropertiesPropertySet extends PropertySet {

        private OpenTag openTag;
        private HtmlParserResult res;

        public PropertiesPropertySet(HtmlParserResult res, OpenTag openTag) {
            this.res = res;
            this.openTag = openTag;
            setName(Bundle.element_element_attributes_title());
        }

        @Override
        public Property<String>[] getProperties() {
            Snapshot s = res.getSnapshot();
            Document doc = s.getSource().getDocument(false);
            Collection<Property> props = new ArrayList<>();
            Collection<String> existingAttrNames = new HashSet<>();
            for (Attribute a : openTag.attributes()) {
                props.add(new AttributeProperty(doc, s, openTag, a));
                existingAttrNames.add(a.name().toString().toLowerCase(Locale.ENGLISH));
            }
            HtmlModel model = HtmlModelFactory.getModel(res.getHtmlVersion());
            HtmlTag tagModel = model.getTag(openTag.name().toString());
            if (tagModel != null) {
                List<String> attrNames = new ArrayList<>();
                for (HtmlTagAttribute htmlTagAttr : tagModel.getAttributes()) {
                    String name = htmlTagAttr.getName().toLowerCase();
                    if (!existingAttrNames.contains(name)) {
                        attrNames.add(name);
                    }
                }

                Collections.sort(attrNames);
                for (String attrName : attrNames) {
                    props.add(new NewAttributeProperty(doc, s, attrName, openTag));
                }
            }

            return props.toArray(new Property[]{});
        }
    }

    private static String[] findTags(String tagName, String attrName) {
        ValueCompletion support = AttrValuesCompletion.getSupport(tagName, attrName);
        if (support != null && support instanceof AttrValuesCompletion.ValuesSetSupport) {
            AttrValuesCompletion.ValuesSetSupport fixedValuesSupport = (AttrValuesCompletion.ValuesSetSupport) support;
            return fixedValuesSupport.getTags();
        }
        return null;
    }

    private static class AttributeProperty extends PropertySupport<String> {

        private Attribute attr;
        private Document doc;
        private Snapshot snap;
        private String[] tags;

        public AttributeProperty(Document doc, Snapshot snap, OpenTag openTag, Attribute attr) {
            super(attr.name().toString(), String.class, attr.name().toString(), Bundle.edit_attribute_tooltip(), true, doc != null);
            this.doc = doc;
            this.snap = snap;
            this.attr = attr;

            tags = findTags(openTag.name().toString(), attr.name().toString());
        }

        @Override
        public PropertyEditor getPropertyEditor() {
            return tags == null ? null : new PropertyValuesEditor(tags);
        }

        @Override
        public String getHtmlDisplayName() {
            return new StringBuilder()
                    .append("<b>")
                    .append(attr.name())
                    .append("</b>")
                    .toString();
        }

        @Override
        public String getValue() throws IllegalAccessException, InvocationTargetException {
            return attr.unquotedValue().toString();
        }

        @Override
        public void setValue(final String val) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
            int astFrom, astTo;
            if (val.length() == 0) {
                //remove the whole attribute=value pair
                astFrom = attr.nameOffset() - 1; //there must be a WS before so lets remove it
                astTo = attr.valueOffset() + attr.value().length();
            } else {
                //modify
                astFrom = attr.valueOffset() + (attr.isValueQuoted() ? 1 : 0);
                astTo = astFrom + attr.unquotedValue().length();
            }

            final int docFrom = snap.getOriginalOffset(astFrom);
            final int docTo = snap.getOriginalOffset(astTo);

            if (docFrom != -1 && docTo != -1) {
                ((BaseDocument) doc).runAtomicAsUser(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            doc.remove(docFrom, docTo - docFrom);
                            if (val.length() > 0) {
                                doc.insertString(docFrom, val, null);
                            }
                        } catch (BadLocationException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                });
            }
        }
    }

    private static class NewAttributeProperty extends PropertySupport<String> {

        private static final String EMPTY = "";
        private String attrName;
        private Document doc;
        private Snapshot snap;
        private OpenTag ot;
        private String[] tags;

        public NewAttributeProperty(Document doc, Snapshot snap, String attrName, OpenTag ot) {
            super(attrName, String.class, attrName, Bundle.new_attribute_tooltip(), true, doc != null);
            this.doc = doc;
            this.snap = snap;
            this.ot = ot;
            this.attrName = attrName;

            tags = findTags(ot.name().toString(), attrName);
        }

        @Override
        public PropertyEditor getPropertyEditor() {
            return tags == null ? null : new PropertyValuesEditor(tags);
        }

        @Override
        public String getValue() throws IllegalAccessException, InvocationTargetException {
            return EMPTY;
        }

        @Override
        public void setValue(String val) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
            if (val.length() == 0) {
                return;
            }
            //remove the whole attribute=value pair
            int astFrom = ot.from() + 1 /* "<".length() */ + ot.name().length(); //just after the attribute name

            final int docFrom = snap.getOriginalOffset(astFrom);
            if (docFrom != -1) {
                final StringBuilder insertBuilder = new StringBuilder()
                        .append(' ')
                        .append(attrName)
                        .append('=')
                        .append('"')
                        .append(val)
                        .append('"');

                ((BaseDocument) doc).runAtomicAsUser(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (doc.getText(docFrom, 1).trim().length() == 0) {
                                //there's already a WS after the insertion place
                            } else {
                                //lets add one more WS
                                insertBuilder.append(' ');
                            }
                            doc.insertString(docFrom, insertBuilder.toString(), null);
                        } catch (BadLocationException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                });
            }
        }
    }

    public static class PropertyValuesEditor extends PropertyEditorSupport implements ExPropertyEditor {

        private static String NONE_PROPERTY_NAME = "<none>";
        private String[] tags;

        public PropertyValuesEditor(String[] tags) {
            this.tags = new String[tags.length + 1];
            this.tags[0] = NONE_PROPERTY_NAME;
            System.arraycopy(tags, 0, this.tags, 1, tags.length);
        }

        @Override
        public synchronized String[] getTags() {
            return tags;
        }

        @Override
        public void setAsText(String str) {
            if (str == null) {
                return;
            }

            if (str.isEmpty() || NONE_PROPERTY_NAME.equals(str)) {
                setValue(str); //pass the empty value to the Property
                return;
            }

            setValue(str);
        }

        @Override
        public String getAsText() {
            return getValue().toString();
        }

        @Override
        public void attachEnv(PropertyEnv env) {
            //if there's at least one unit element, then the text field needs to be editable
            env.getFeatureDescriptor().setValue("canEditAsText", Boolean.TRUE);
        }
    }
}
