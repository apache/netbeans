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
package org.netbeans.modules.java.editor.base.semantic;

import java.text.MessageFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.StyledDocument;
import org.netbeans.api.lexer.Token;
import org.openide.text.NbDocument;

/**
 *
 * @author Jan Lahoda
 */
public final class HighlightImpl {
    
    private Document doc;
    private int start;
    private int end;
    private Collection<ColoringAttributes> colorings;
    private String textPrepend;
    
    public HighlightImpl(Document doc, Token token, Collection<ColoringAttributes> colorings) {
        this.doc       = doc;
        this.start     = token.offset(null);
        this.end       = token.offset(null) + token.text().length();
        this.colorings = colorings;
    }
    
    public HighlightImpl(Document doc, int start, int end, Collection<ColoringAttributes> colorings) {
        this.doc = doc;
        this.start = start;
        this.end = end;
        this.colorings = colorings;
    }
    
    public HighlightImpl(Document doc, int start, int end, String textPrepend) {
        this.doc = doc;
        this.start = start;
        this.end = end;
        this.colorings = ColoringAttributes.empty();
        this.textPrepend = textPrepend;
    }

    public int getStart() {
        return start;
    }
    
    public int getEnd() {
        return end;
    }
    
    public String getHighlightTestData() {
        int lineStart = NbDocument.findLineNumber((StyledDocument) doc, start);
        int columnStart = NbDocument.findLineColumn((StyledDocument) doc, start);
        int lineEnd = NbDocument.findLineNumber((StyledDocument) doc, end);
        int columnEnd = NbDocument.findLineColumn((StyledDocument) doc, end);
        
        return coloringsToString() + ", " + lineStart + ":" + columnStart + "-" + lineEnd + ":" + columnEnd;
    }
    
    private String coloringsToString() {
        StringBuffer result = new StringBuffer();
        boolean first = true;
        
        result.append("[");
        
        for (ColoringAttributes attribute : coloringsAttributesOrder) {
            if (colorings.contains(attribute)) {
                if (!first) {
                    result.append(", ");
                }
                
                first = false;
                result.append(attribute.name());
            }
        }

        if (textPrepend != null) {
            if (!first) {
                result.append(", ");
            }

            result.append(textPrepend);
        }

        result.append("]");
        
        return result.toString();
    }
    
    Collection<ColoringAttributes> coloringsAttributesOrder = Arrays.asList(new ColoringAttributes[] {
        ColoringAttributes.STATIC,
        ColoringAttributes.ABSTRACT,
        
        ColoringAttributes.PUBLIC,
        ColoringAttributes.PROTECTED,
        ColoringAttributes.PACKAGE_PRIVATE,
        ColoringAttributes.PRIVATE,
        
        ColoringAttributes.DEPRECATED,
        
        ColoringAttributes.FIELD,
        ColoringAttributes.LOCAL_VARIABLE,
        ColoringAttributes.PARAMETER,
        ColoringAttributes.METHOD,
        ColoringAttributes.CONSTRUCTOR,
        ColoringAttributes.CLASS,
        ColoringAttributes.INTERFACE,
        ColoringAttributes.ANNOTATION_TYPE,
        ColoringAttributes.ENUM,
        
        ColoringAttributes.UNUSED,
        
        ColoringAttributes.TYPE_PARAMETER_DECLARATION,
        ColoringAttributes.TYPE_PARAMETER_USE,
        
        ColoringAttributes.UNDEFINED,
        
        ColoringAttributes.DECLARATION,
        
        ColoringAttributes.MARK_OCCURRENCES,

        ColoringAttributes.KEYWORD,
        
        ColoringAttributes.UNINDENTED_TEXT_BLOCK,
    });
 
    public static HighlightImpl parse(StyledDocument doc, String line) throws ParseException, BadLocationException {
        MessageFormat f = new MessageFormat("[{0}], {1,number,integer}:{2,number,integer}-{3,number,integer}:{4,number,integer}");
        Object[] args = f.parse(line);
        
        String attributesString = (String) args[0];
        int    lineStart  = ((Long) args[1]).intValue();
        int    columnStart  = ((Long) args[2]).intValue();
        int    lineEnd  = ((Long) args[3]).intValue();
        int    columnEnd  = ((Long) args[4]).intValue();
        
        String[] attrElements = attributesString.split(",");
        List<ColoringAttributes> attributes = new ArrayList<ColoringAttributes>();
        
        for (String a : attrElements) {
            a = a.trim();
            
            attributes.add(ColoringAttributes.valueOf(a));
        }
        
        if (attributes.contains(null))
            throw new NullPointerException();
        
        int offsetStart = NbDocument.findLineOffset(doc, lineStart) + columnStart;
        int offsetEnd = NbDocument.findLineOffset(doc, lineEnd) + columnEnd;
        
        return new HighlightImpl(doc, offsetStart, offsetEnd, attributes);
    }
    
}
