/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.csl.editor.semantic;


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
import org.netbeans.modules.csl.api.ColoringAttributes;
import org.openide.text.NbDocument;

/**
 * This file is originally from Retouche, the Java Support 
 * infrastructure in NetBeans. I have modified the file as little
 * as possible to make merging Retouche fixes back as simple as
 * possible. 
 *
 *
 * @author Jan Lahoda
 */
public final class HighlightImpl {
    
    private Document doc;
    private int start;
    private int end;
    private Collection<ColoringAttributes> colorings;
    
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
        
        result.append("]");
        
        return result.toString();
    }
    
    Collection<ColoringAttributes> coloringsAttributesOrder = Arrays.asList(new ColoringAttributes[] {
        ColoringAttributes.ABSTRACT,
        ColoringAttributes.ANNOTATION_TYPE,
        ColoringAttributes.CLASS,
        ColoringAttributes.CONSTRUCTOR,
        ColoringAttributes.CUSTOM1,
        ColoringAttributes.CUSTOM2,
        ColoringAttributes.CUSTOM3,
        ColoringAttributes.DECLARATION,
        ColoringAttributes.DEPRECATED,
        ColoringAttributes.ENUM,
        ColoringAttributes.FIELD,
        ColoringAttributes.GLOBAL,
        ColoringAttributes.INTERFACE,
        ColoringAttributes.LOCAL_VARIABLE,
        ColoringAttributes.MARK_OCCURRENCES,
        ColoringAttributes.METHOD,
        ColoringAttributes.PACKAGE_PRIVATE,
        ColoringAttributes.PARAMETER,
        ColoringAttributes.PRIVATE,
        ColoringAttributes.PROTECTED,
        ColoringAttributes.PUBLIC,
        ColoringAttributes.REGEXP,
        ColoringAttributes.STATIC,
        ColoringAttributes.TYPE_PARAMETER_DECLARATION,
        ColoringAttributes.TYPE_PARAMETER_USE,
        ColoringAttributes.UNDEFINED,
        ColoringAttributes.UNUSED,
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
