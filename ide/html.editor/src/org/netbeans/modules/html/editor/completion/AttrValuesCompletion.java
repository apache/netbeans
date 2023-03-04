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
package org.netbeans.modules.html.editor.completion;

import java.awt.Color;
import org.netbeans.modules.html.editor.api.completion.HtmlCompletionItem;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.swing.ImageIcon;
import org.netbeans.modules.web.common.api.ValueCompletion;
import org.netbeans.modules.web.common.ui.api.FileReferenceCompletion;
import org.openide.filesystems.FileObject;

/**
 *
 * @author marekfukala
 */
public abstract class AttrValuesCompletion {

    private static final Map<String, Map<String, ValueCompletion<HtmlCompletionItem>>> SUPPORTS =
            new HashMap<>();
    private static final Map<String, ValueCompletion<HtmlCompletionItem>> ALL_TAG_SUPPORTS =
            new HashMap<>();
    public static final ValueCompletion<HtmlCompletionItem> FILE_NAME_SUPPORT = new FilenameSupport();
    private static final ValueCompletion<HtmlCompletionItem> LINK_TYPE_SUPPORT =
            new ValuesSetSupport(new String[]{"text/css", "text/javascript"});
    private static final ValueCompletion<HtmlCompletionItem> SCRIPT_TYPE_SUPPORT = 
            new ValuesSetSupport(new String[]{"text/javascript", "module"}); // NOI18N
    private static final ValueCompletion<HtmlCompletionItem> STYLE_TYPE_SUPPORT = 
            new ValuesSetSupport(new String[]{"text/css"});
    private static final ValueCompletion<HtmlCompletionItem> TRUE_FALSE_SUPPORT =
            new ValuesSetSupport(new String[]{"true", "false"});
    private static final ValueCompletion<HtmlCompletionItem> SCOPE_SUPPORT =
            new ValuesSetSupport(new String[]{"row", "col", "rowgroup", "colgroup"});
    private static final ValueCompletion<HtmlCompletionItem> SHAPE_SUPPORT =
            new ValuesSetSupport(new String[]{"circle", "default", "poly", "rect"});
    private static final ValueCompletion<HtmlCompletionItem> ON_OFF_SUPPORT =
            new ValuesSetSupport(new String[]{"on", "off"});
    private static final ValueCompletion<HtmlCompletionItem> FORM_ENCTYPE_SUPPORT =
            new ValuesSetSupport(new String[]{"application/x-www-form-urlencoded", "multipart/form-data", "text/plain"});
    private static final ValueCompletion<HtmlCompletionItem> FORM_METHOD_SUPPORT =
            new ValuesSetSupport(new String[]{"get", "post", "dialog"});
    private static final ValueCompletion<HtmlCompletionItem> PRELOAD_SUPPORT =
            new ValuesSetSupport(new String[]{"none", "metadata", "auto"});
    private static final ValueCompletion<HtmlCompletionItem> BUTTON_TYPE_SUPPORT =
            new ValuesSetSupport(new String[]{"submit", "reset", "button"});
    private static final ValueCompletion<HtmlCompletionItem> COMMAND_TYPE_SUPPORT =
            new ValuesSetSupport(new String[]{"command", "checkbox", "radio"});
    private static final ValueCompletion<HtmlCompletionItem> MENU_TYPE_SUPPORT =
            new ValuesSetSupport(new String[]{"context", "toolbar"});
    private static final ValueCompletion<HtmlCompletionItem> WRAP_SUPPORT =
            new ValuesSetSupport(new String[]{"soft", "hard"});
    private static final ValueCompletion<HtmlCompletionItem> TARGET_SUPPORT =
            new ValuesSetSupport(new String[]{"_self", "_blank", "_parent", "_top"});
    private static final ValueCompletion<HtmlCompletionItem> INPUT_TYPE_SUPPORT =
            new ValuesSetSupport(new String[]{"hidden",
                "text","search","tel","url","email","password","datetime","date",
                "month","week","time","datetime-local","number","range","color",
                "checkbox","radio","file","submit","image","reset","button"});

    private static final ValueCompletion<HtmlCompletionItem> LINK_TYPES_SUPPORT =
            new ValuesSetSupport(new String[]{
                "alternate", "stylesheet", "start", "next", "prev", "contents",
                "index", "glossary", "copyright", "chapter", "section", "subsection",
                "appendix", "help","bookmark"});
    
    private static final ValueCompletion<HtmlCompletionItem> DIR_SUPPORT =
            new ValuesSetSupport(new String[]{"ltr", "rtl"});
    
    static {
        //TODO uff, such long list ... redo it so it resolves according to the DTD attribute automatically
        //mixed with html5 content...
        putSupport(null, "contenteditable", TRUE_FALSE_SUPPORT); //NOI18N
        putSupport(null, "dir", DIR_SUPPORT);
        putSupport(null, "itemprop", FILE_NAME_SUPPORT); //NOI18N
        putSupport(null, "itemtype", FILE_NAME_SUPPORT); //NOI18N
        putSupport(null, "rel", LINK_TYPES_SUPPORT);
        putSupport(null, "spellcheck", TRUE_FALSE_SUPPORT); //NOI18N

        putSupport("a", "href", FILE_NAME_SUPPORT); //NOI18N
        putSupport("a", "ping", FILE_NAME_SUPPORT); //NOI18N
        putSupport("a", "target", TARGET_SUPPORT); //NOI18N

        putSupport("applet", "codebase", FILE_NAME_SUPPORT); //NOI18N

        putSupport("area", "href", FILE_NAME_SUPPORT); //NOI18N
        putSupport("area", "ping", FILE_NAME_SUPPORT); //NOI18N
        putSupport("area", "shape", SHAPE_SUPPORT); //NOI18N

        putSupport("audio", "preload", PRELOAD_SUPPORT); //NOI18N
        putSupport("audio", "src", FILE_NAME_SUPPORT); //NOI18N

        putSupport("base", "href", FILE_NAME_SUPPORT); //NOI18N

        putSupport("blackquote", "cite", FILE_NAME_SUPPORT); //NOI18N

        putSupport("body", "background", FILE_NAME_SUPPORT); //NOI18N

        putSupport("button", "formaction", FILE_NAME_SUPPORT); //NOI18N
        putSupport("button", "formenctype", FORM_ENCTYPE_SUPPORT); //NOI18N
        putSupport("button", "formmethod", FORM_METHOD_SUPPORT); //NOI18N
        putSupport("button", "formtarget", TARGET_SUPPORT); //NOI18N
        putSupport("button", "type", BUTTON_TYPE_SUPPORT); //NOI18N

        putSupport("command", "icon", FILE_NAME_SUPPORT); //NOI18N
        putSupport("command", "type", COMMAND_TYPE_SUPPORT); //NOI18N

        putSupport("del", "cite", FILE_NAME_SUPPORT); //NOI18N

        putSupport("embed", "src", FILE_NAME_SUPPORT); //NOI18N

        putSupport("form", "action", FILE_NAME_SUPPORT); //NOI18N
        putSupport("form", "autocomplete", ON_OFF_SUPPORT); //NOI18N
        putSupport("form", "enctype", FORM_ENCTYPE_SUPPORT); //NOI18N
        putSupport("form", "method", FORM_METHOD_SUPPORT); //NOI18N
        putSupport("form", "target", TARGET_SUPPORT); //NOI18N

        putSupport("frame", "src", FILE_NAME_SUPPORT); //NOI18N

        putSupport("html", "manifest", FILE_NAME_SUPPORT); //NOI18N

        putSupport("iframe", "src", FILE_NAME_SUPPORT); //NOI18N

        putSupport("img", "longdesc", FILE_NAME_SUPPORT); //NOI18N
        putSupport("img", "src", FILE_NAME_SUPPORT); //NOI18N
        putSupport("img", "usemap", FILE_NAME_SUPPORT); //NOI18N

        putSupport("input", "autocomplete", ON_OFF_SUPPORT); //NOI18N
        putSupport("input", "formaction", FILE_NAME_SUPPORT); //NOI18N
        putSupport("input", "formenctype", FORM_ENCTYPE_SUPPORT); //NOI18N
        putSupport("input", "formmethod", FORM_METHOD_SUPPORT); //NOI18N
        putSupport("input", "formtarget", TARGET_SUPPORT); //NOI18N
        putSupport("input", "src", FILE_NAME_SUPPORT); //NOI18N
        putSupport("input", "type", INPUT_TYPE_SUPPORT); //NOI18N
        putSupport("input", "usemap", FILE_NAME_SUPPORT); //NOI18N

        putSupport("ins", "cite", FILE_NAME_SUPPORT); //NOI18N

        putSupport("link", "href", FILE_NAME_SUPPORT); //NOI18N
        putSupport("link", "type", LINK_TYPE_SUPPORT); //NOI18N

        putSupport("menu", "type", MENU_TYPE_SUPPORT); //NOI18N

        putSupport("object", "classid", FILE_NAME_SUPPORT); //NOI18N
        putSupport("object", "codebase", FILE_NAME_SUPPORT); //NOI18N
        putSupport("object", "data", FILE_NAME_SUPPORT); //NOI18N
        putSupport("object", "usemap", FILE_NAME_SUPPORT); //NOI18N

        putSupport("q", "cite", FILE_NAME_SUPPORT); //NOI18N

        putSupport("script", "src", FILE_NAME_SUPPORT); //NOI18N
        putSupport("script", "type", SCRIPT_TYPE_SUPPORT); //NOI18N

        putSupport("source", "src", FILE_NAME_SUPPORT); //NOI18N

        putSupport("style", "type", STYLE_TYPE_SUPPORT); //NOI18N

        putSupport("th", "scope", SCOPE_SUPPORT); //NOI18N

        putSupport("textarea", "wrap", WRAP_SUPPORT); //NOI18N

        putSupport("track", "src", FILE_NAME_SUPPORT); //NOI18N

        putSupport("video", "poster", FILE_NAME_SUPPORT); //NOI18N
        putSupport("video", "preload", PRELOAD_SUPPORT); //NOI18N
        putSupport("video", "src", FILE_NAME_SUPPORT); //NOI18N        
    }

    private static void putSupport(String tag, String attr, ValueCompletion<HtmlCompletionItem> support) {
        if (tag == null) {
            ALL_TAG_SUPPORTS.put(attr, support);
        } else {
            Map<String, ValueCompletion<HtmlCompletionItem>> map = SUPPORTS.get(tag);
            if (map == null) {
                map = new HashMap<>();
                SUPPORTS.put(tag, map);
            }
            map.put(attr, support);
        }
    }

    public static Map<String, ValueCompletion<HtmlCompletionItem>> getSupportsForTag(String tag) {
        return SUPPORTS.get(tag.toLowerCase(Locale.ENGLISH));
    }

    public static ValueCompletion<HtmlCompletionItem> getSupport(String tag, String attr) {
        Map<String, ValueCompletion<HtmlCompletionItem>> map = getSupportsForTag(tag);
        if (map == null) {
            return ALL_TAG_SUPPORTS.get(attr);
        } else {
            ValueCompletion completion = map.get(attr.toLowerCase(Locale.ENGLISH));
            return completion == null ? ALL_TAG_SUPPORTS.get(attr) : completion;
        }
    }

    public static class ValuesSetSupport implements ValueCompletion {

        private String[] values;

        public ValuesSetSupport(String[] values) {
            this.values = values;
        }

        public String[] getTags() {
            return values;
        }
        
        @Override
        public List<HtmlCompletionItem> getItems(FileObject file, int offset, String valuePart) {
            //linear search, too little items, no problem
            List<HtmlCompletionItem> items = new ArrayList<>();
            for (int i = 0; i < values.length; i++) {
                if (values[i].startsWith(valuePart)) {
                    items.add(HtmlCompletionItem.createAttributeValue(values[i], offset));
                }
            }
            return items;
        }
    }

    public static class FilenameSupport extends FileReferenceCompletion<HtmlCompletionItem> {

        @Override
        public HtmlCompletionItem createFileItem(FileObject file, int anchor) {
            return HtmlCompletionItem.createFileCompletionItem(file, anchor);
        }

        @Override
        public HtmlCompletionItem createGoUpItem(int anchor, Color color, ImageIcon icon) {
            return HtmlCompletionItem.createGoUpFileCompletionItem(anchor, color, icon); // NOI18N
        }
    }
}
