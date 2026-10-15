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
package org.netbeans.modules.csl.editor.semantic;


import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Enumeration;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.AttributeSet;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.settings.AttributesUtilities;
import org.netbeans.api.editor.settings.EditorStyleConstants;
import org.netbeans.api.editor.settings.FontColorSettings;
import org.netbeans.modules.csl.api.ColoringAttributes;
import org.netbeans.spi.editor.highlighting.HighlightAttributeValue;
import static org.netbeans.modules.csl.api.ColoringAttributes.*;
import org.openide.util.NbBundle;

/**
 * This file is originally from Retouche, the Java Support 
 * infrastructure in NetBeans. I have modified the file as little
 * as possible to make merging Retouche fixes back as simple as
 * possible. 
 *
 * The main modification to this file is to change the static methods
 * into instance methods, and change the ColoringManager from a singleton
 * (hardcoded to the Java mimetype) into a per-mimetype ColoringManager
 * stashed in each Language.
 * 
 *
 * @author Jan Lahoda
 */
public final class ColoringManager {
    private final String mimeType;
    private final Map<Set<ColoringAttributes>, String> type2Coloring;
    
    public ColoringManager(String mimeType) {
        this.mimeType = mimeType;
        
        type2Coloring = new LinkedHashMap<>();
        
        put("mark-occurrences", MARK_OCCURRENCES);
        put("mod-abstract", ABSTRACT);
        put("mod-annotation-type", ANNOTATION_TYPE);
        put("mod-class", CLASS);
        put("mod-constructor", CONSTRUCTOR);
        put("mod-custom1", CUSTOM1);
        put("mod-custom2", CUSTOM2);
        put("mod-custom3", CUSTOM3);
        put("mod-declaration", DECLARATION);
        put("mod-deprecated", DEPRECATED);
        put("mod-enum", ENUM);
        put("mod-field", FIELD);
        put("mod-global", GLOBAL);
        put("mod-interface", INTERFACE);
        put("mod-local-variable", LOCAL_VARIABLE);
        put("mod-local-variable-declaration", LOCAL_VARIABLE_DECLARATION);
        put("mod-method", METHOD);
        put("mod-package-private", PACKAGE_PRIVATE);
        put("mod-parameter", PARAMETER);
        put("mod-private", PRIVATE);
        put("mod-protected", PROTECTED);
        put("mod-public", PUBLIC);
        put("mod-regexp", REGEXP);
        put("mod-static", STATIC);
        put("mod-type-parameter-declaration", TYPE_PARAMETER_DECLARATION);
        put("mod-type-parameter-use", TYPE_PARAMETER_USE);
        put("mod-undefined", UNDEFINED);
        put("mod-unused", UNUSED);
        
        
        COLORING_MAP.put(ColoringAttributes.UNUSED_SET, getColoring(ColoringAttributes.UNUSED_SET));
        COLORING_MAP.put(ColoringAttributes.FIELD_SET, getColoring(ColoringAttributes.FIELD_SET));
        COLORING_MAP.put(ColoringAttributes.STATIC_FIELD_SET, getColoring(ColoringAttributes.STATIC_FIELD_SET));
        COLORING_MAP.put(ColoringAttributes.PARAMETER_SET, getColoring(ColoringAttributes.PARAMETER_SET));
        COLORING_MAP.put(ColoringAttributes.CUSTOM1_SET, getColoring(ColoringAttributes.CUSTOM1_SET));
        COLORING_MAP.put(ColoringAttributes.CUSTOM2_SET, getColoring(ColoringAttributes.CUSTOM2_SET));
        COLORING_MAP.put(ColoringAttributes.CONSTRUCTOR_SET, getColoring(ColoringAttributes.CONSTRUCTOR_SET));
        COLORING_MAP.put(ColoringAttributes.METHOD_SET, getColoring(ColoringAttributes.METHOD_SET));
        COLORING_MAP.put(ColoringAttributes.CLASS_SET, getColoring(ColoringAttributes.CLASS_SET));
        COLORING_MAP.put(ColoringAttributes.GLOBAL_SET, getColoring(ColoringAttributes.GLOBAL_SET));
        COLORING_MAP.put(ColoringAttributes.REGEXP_SET, getColoring(ColoringAttributes.REGEXP_SET));
        COLORING_MAP.put(ColoringAttributes.STATIC_SET, getColoring(ColoringAttributes.STATIC_SET));
    }
    
    private void put(String coloring, ColoringAttributes... attributes) {
        Set<ColoringAttributes> attribs = EnumSet.copyOf(Arrays.asList(attributes));
        
        type2Coloring.put(attribs, coloring);
    }
    
    final Map<Set<ColoringAttributes>,Coloring> COLORING_MAP = new IdentityHashMap<>();
    
    public Coloring getColoring(Set<ColoringAttributes> attrs) {
        Coloring c = COLORING_MAP.get(attrs);
        if (c != null) {
            return c;
        }
        
        c = ColoringAttributes.empty();

        for (ColoringAttributes color : attrs) {
            c = ColoringAttributes.add(c, color);
        }
        
        return c;
    }

    @NonNull
    public AttributeSet getColoringImpl(Coloring colorings) {
        FontColorSettings fcs = MimeLookup.getLookup(MimePath.get(mimeType)).lookup(FontColorSettings.class);
        
        if (fcs == null) {
            //in tests (or possibly some other condition - see issue #137797)
            return AttributesUtilities.createImmutable();
        }
        
        assert fcs != null;
        
        List<AttributeSet> attribs = new LinkedList<>();
        
        EnumSet<ColoringAttributes> es = EnumSet.noneOf(ColoringAttributes.class);
        
        es.addAll(colorings);
        
        if (colorings.contains(UNUSED)) {
            attribs.add(AttributesUtilities.createImmutable(EditorStyleConstants.Tooltip, UNUSED_TOOLTIP_RESOLVER));
            attribs.add(AttributesUtilities.createImmutable("unused-browseable", Boolean.TRUE));
        }
        
        for (Entry<Set<ColoringAttributes>, String> attribs2Colorings : type2Coloring.entrySet()) {
            if (es.containsAll(attribs2Colorings.getKey())) {
                String key = attribs2Colorings.getValue();
                
                es.removeAll(attribs2Colorings.getKey());
                
                if (key != null) {
                    AttributeSet colors = fcs.getTokenFontColors(key);
                    
                    if (colors == null) {
                        Logger.getLogger(ColoringManager.class.getName()).log(Level.SEVERE, "no colors for: {0} with mime" + mimeType, key);
                        continue;
                    }
                    
                    attribs.add(adjustAttributes(colors));
                }
            }
        }
        
        Collections.reverse(attribs);
        
        return AttributesUtilities.createComposite(attribs.toArray(AttributeSet[]::new));
    }
    
    private static AttributeSet adjustAttributes(AttributeSet as) {
        Collection<Object> attrs = new LinkedList<>();
        
        for (Enumeration<?> e = as.getAttributeNames(); e.hasMoreElements(); ) {
            Object key = e.nextElement();
            Object value = as.getAttribute(key);
            
            if (value != Boolean.FALSE) {
                attrs.add(key);
                attrs.add(value);
            }
        }
        
        return AttributesUtilities.createImmutable(attrs.toArray());
    }

    private static final HighlightAttributeValue<String> UNUSED_TOOLTIP_RESOLVER =
            (component, document, attributeKey, startOffset, endOffset) -> NbBundle.getMessage(ColoringManager.class, "LBL_UNUSED");

}
