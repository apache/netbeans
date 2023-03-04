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
package org.netbeans.modules.web.jsf.editor.completion;

import java.awt.Color;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.UIManager;
import javax.swing.text.JTextComponent;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.html.editor.api.completion.HtmlCompletionItem;
import org.netbeans.modules.html.editor.lib.api.DefaultHelpItem;
import org.netbeans.modules.html.editor.lib.api.HelpItem;
import org.netbeans.modules.web.jsf.editor.facelets.AbstractFaceletsLibrary;
import org.netbeans.modules.web.jsfapi.api.LibraryComponent;
import org.netbeans.modules.web.jsfapi.api.Library;
import org.netbeans.modules.web.jsfapi.spi.LibraryUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.NbBundle;

/**
 *
 * @author marekfukala
 */
public class JsfCompletionItem {

    //html items priority varies from 10 to 20
    private static final int JSF_DEFAULT_SORT_PRIORITY = 5;

    /** Replacements for tags and attributes to color properly documentation window colors (#232983). */
    private static final Map<String, String> COLORING_REPLACEMENTS = new HashMap<String, String>() {{
        put("<p>", "<p" + getColorStyleAttribute() + ">");      //NOI18N
        put("<div>", "<div" + getColorStyleAttribute() + ">");  //NOI18N
        put("<ol>", "<ol" + getColorStyleAttribute() + ">");    //NOI18N
        put("class=[^>]*", getColorStyleAttribute());           //NOI18N
    }};
    private static final Pattern REPLACEMENT_PATTERN = getPattern();

    //----------- Factory methods --------------
    public static JsfTag createTag(int substitutionOffset, LibraryComponent component, String declaredPrefix, boolean autoimport, boolean isJsf22Plus) {
        return new JsfTag(substitutionOffset, component, declaredPrefix, autoimport, isJsf22Plus);
    }

    public static JsfTagAttribute createAttribute(String name, int substitutionOffset, Library library, org.netbeans.modules.web.jsfapi.api.Tag tag, org.netbeans.modules.web.jsfapi.api.Attribute attr) {
        return new JsfTagAttribute(name, substitutionOffset, library, tag, attr);
    }


    private static String convertTextColors(String content) {
        // Hack to recolor HTML defined by JSF library in the metadata.
        //   This recoloring is tuned for Mojarra implementation.
        Matcher matcher = REPLACEMENT_PATTERN.matcher(content);
        StringBuffer result = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(result, getFromReplacementMap(matcher.group()));
        }
        matcher.appendTail(result);
        return result.toString();
    }

    private static String getFromReplacementMap(String string) {
        String result = COLORING_REPLACEMENTS.get(string);
        return result != null ? result : getColorStyleAttribute();
    }

    private static Pattern getPattern() {
        StringBuilder patternString = new StringBuilder("");
        String delimiter = ""; //NOI18N
        for (String toReplace : COLORING_REPLACEMENTS.keySet()) {
            patternString.append(delimiter).append(toReplace);
            delimiter = "|"; //NOI18N
        }
        return Pattern.compile(patternString.toString());
    }

    private static String getColorStyleAttribute() {
        Color textColor = UIManager.getColor("Tree.textForeground"); //NOI18N
        return " style=\"color: #" + Integer.toHexString(textColor.getRGB()).substring(2, 8) + ";\""; //NOI18N
    }

    public static class JsfTag extends HtmlCompletionItem.Tag {

        private static final String BOLD_OPEN_TAG = "<b>"; //NOI18N
        private static final String BOLD_END_TAG = "</b>"; //NOI18N
        
        private static final String AND_HTML_ENTITY = "&amp;"; //NOI18N
        private static final String AND_HTML = "&"; //NOI18N
        
        private LibraryComponent component;
        private boolean autoimport; //autoimport (declare) the tag namespace if set to true
        private boolean isJsf22Plus;

        public JsfTag(int substitutionOffset, LibraryComponent component, String declaredPrefix, boolean autoimport, boolean isJsf22Plus) {
            super(generateItemText(component, declaredPrefix), substitutionOffset, null, true);
            this.component = component;
            this.autoimport = autoimport;
            this.isJsf22Plus = isJsf22Plus;
        }

        private static String generateItemText(LibraryComponent component, String declaredPrefix) {
            String libraryPrefix = component.getLibrary().getDefaultPrefix();
            return (declaredPrefix != null ? declaredPrefix : libraryPrefix) + ":" + component.getName(); //NOI18N
        }

        @Override
        protected String getRightHtmlText() {
            return component.getLibrary().getDisplayName();
        }

        @Override
        public void defaultAction(JTextComponent component) {
            super.defaultAction(component);
            if (autoimport) {
                autoimportLibrary(component);
            }
        }

        private void autoimportLibrary(JTextComponent component) {
            final BaseDocument doc = (BaseDocument) component.getDocument();
            Library lib = JsfTag.this.component.getLibrary();
            LibraryUtils.importLibrary(doc, lib, null, isJsf22Plus);
        }

        //use bold font
        @Override
        protected String getLeftHtmlText() {
            StringBuilder buff = new StringBuilder();
            buff.append(BOLD_OPEN_TAG);
            buff.append(super.getLeftHtmlText());
            buff.append(BOLD_END_TAG);
            return buff.toString();
        }

        @Override
        public int getSortPriority() {
            return JSF_DEFAULT_SORT_PRIORITY; //jsf tags are more important than html content
        }

        private String getHelpContent() {
            StringBuilder sb = new StringBuilder();
            sb.append(getLibraryHelpHeader(component.getLibrary()));
            sb.append("<h1>"); //NOI18N
            sb.append(component.getName());
            sb.append("</h1>"); //NOI18N

            if(Boolean.getBoolean("show-facelets-libraries-locations")) {
                if (component.getLibrary() instanceof AbstractFaceletsLibrary) {
                AbstractFaceletsLibrary lib = (AbstractFaceletsLibrary) component.getLibrary();
                URL url = lib.getLibraryDescriptorSource();
                    if(url != null) {
                        FileObject fo = URLMapper.findFileObject(url);
                        if(fo != null) {
                            sb.append("<div style=\"font-size: smaller; color: gray;\">");
                            sb.append("Source: ");
                            sb.append(FileUtil.getFileDisplayName(fo));
                            sb.append("</div>");
                        }
                    }
                }
            }

            org.netbeans.modules.web.jsfapi.api.Tag tag = component.getTag();
            if (tag != null) {
                sb.append("<div>");
                //there is TLD available
                String descr = tag.getDescription();
                if (descr == null) {
                    sb.append(NbBundle.getMessage(this.getClass(), "MSG_NO_TLD_ITEM_DESCR")); //NOI18N
                } else {
                    sb.append(descr);
                }
                sb.append("</div>");
            } else {
                //extract some simple info from the component
                sb.append("<table border=\"1\">"); //NOI18N
                for (String[] descr : component.getDescription()) {
                    sb.append("<tr>"); //NOI18N
                    sb.append("<td>"); //NOI18N
                    sb.append("<div style=\"font-weight: bold\">"); //NOI18N
                    sb.append(descr[0]);
                    sb.append("</div>"); //NOI18N
                    sb.append("</td>"); //NOI18N
                    sb.append("<td>"); //NOI18N
                    sb.append(descr[1]);
                    sb.append("</td>"); //NOI18N
                    sb.append("</tr>"); //NOI18N
                }
                sb.append("</table>"); //NOI18N
            }
            
            // Bug 208982 - Problem was found in JSF API source metadata file ui.taglib.xml. 
            // This fix is temporary and must be removed after JSF will solve bug http://java.net/jira/browse/JAVASERVERFACES_SPEC_PUBLIC-1106
            if (sb.indexOf(AND_HTML_ENTITY) >= 0) {
                Pattern pattern = Pattern.compile(AND_HTML_ENTITY);
                Matcher matcher = pattern.matcher(sb);
                
                while (matcher.find()) {
                    sb.replace(matcher.start(), matcher.end(), AND_HTML);
                    matcher.reset();
                }
            } 
            // Bug 208982 <--
            return convertTextColors(sb.toString());
        }

        @Override
        public boolean hasHelp() {
            return true;
        }

        @Override
        public HelpItem getHelpItem() {
            return new DefaultHelpItem(null, JsfDocumentation.getDefault(), null, getHelpContent());
        }


    }

    public static class JsfTagAttribute extends HtmlCompletionItem.Attribute {

        private Library library;
        private org.netbeans.modules.web.jsfapi.api.Tag tag;
        private org.netbeans.modules.web.jsfapi.api.Attribute attr;

        public JsfTagAttribute(String value, int offset, Library library, org.netbeans.modules.web.jsfapi.api.Tag tag, org.netbeans.modules.web.jsfapi.api.Attribute attr) {
            super(value, offset, attr.isRequired(), "");
            assert value != null;
            this.library = library;
            this.tag = tag;
            this.attr = attr;
        }

        private String getHelpContent() {
            StringBuilder sb = new StringBuilder();
            sb.append(getLibraryHelpHeader(library));
            sb.append("<div><b>"); //NOI18N
            sb.append(NbBundle.getMessage(JsfCompletionItem.class, "MSG_Tag"));
            sb.append(":</b> "); //NOI18N
            sb.append(tag.getName());
            sb.append("</div>"); //NOI18N
            sb.append("<h1>"); //NOI18N
            sb.append(attr.getName());
            sb.append("</h1>"); //NOI18N
            if (attr.getType() != null) {
                sb.append("<div><b>"); //NOI18N
                sb.append(NbBundle.getMessage(JsfCompletionItem.class, "MSG_Type"));
                sb.append(":</b> "); //NOI18N
                sb.append(attr.getType());
                sb.append("</div>"); //NOI18N
            }
            if (attr.getMethodSignature() != null) {
                sb.append("<div><b>"); //NOI18N
                sb.append(NbBundle.getMessage(JsfCompletionItem.class, "MSG_MethodSignature"));
                sb.append(":</b> "); //NOI18N
                sb.append(attr.getMethodSignature());
                sb.append("</div>"); //NOI18N
            }
            if (attr.getDefaultValue() != null) {
                sb.append("<div><b>"); //NOI18N
                sb.append(NbBundle.getMessage(JsfCompletionItem.class, "MSG_DefaultValue"));
                sb.append(":</b> "); //NOI18N
                sb.append(attr.getDefaultValue());
                sb.append("</div>"); //NOI18N
            }
            if(attr.isRequired()) {
                sb.append("<p>");
                sb.append(NbBundle.getMessage(JsfCompletionItem.class, "MSG_RequiredAttribute"));
                sb.append("</p>");
            }
            sb.append("<p>");
            if(attr.getDescription() != null) {
                sb.append(attr.getDescription());
            } else {
                sb.append(NbBundle.getMessage(JsfCompletionItem.class, "MSG_NoAttributeDescription"));
            }
            sb.append("</p>");

            return convertTextColors(sb.toString());
        }

        @Override
        public boolean hasHelp() {
            // show the type info anyway
            return true;
        }

         @Override
        public HelpItem getHelpItem() {
            return new DefaultHelpItem(null, JsfDocumentation.getDefault(), null, getHelpContent());
        }

    }

    private static String getLibraryHelpHeader(Library library) {
        StringBuilder sb = new StringBuilder();
        sb.append("<div><b>"); //NOI18N
        sb.append(NbBundle.getMessage(JsfCompletionItem.class, "MSG_Library"));
        sb.append(":</b> "); //NOI18N
        sb.append(library.getNamespace());
        if (library.getLegacyNamespace() != null) {
            sb.append(", ").append(library.getLegacyNamespace()); //NOI18N
        }
        if (library.getDisplayName() != null && !library.getDisplayName().equals(library.getNamespace())) {
            sb.append(" ("); //NOI18N
            sb.append(library.getDisplayName());
            sb.append(")"); //NOI18N
        }
        sb.append("</div>"); //NOI18N
        return sb.toString();

    }
}
