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
package org.netbeans.modules.css.editor;

import java.util.regex.Pattern;
import org.netbeans.lib.editor.util.CharSequenceUtilities;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.css.editor.module.CssModuleSupport;
import org.netbeans.modules.css.editor.module.spi.Browser;
import org.netbeans.modules.css.lib.api.Node;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.web.common.api.Constants;
import org.netbeans.modules.web.common.api.LexerUtils;
import org.openide.filesystems.FileObject;

/**
 * 
 * @author mfukala@netbeans.org
 */
public final class Css3Utils {
    
    public static char FILE_SEPARATOR = '/'; //NOI18N
    
    public static final Pattern URI_PATTERN = Pattern.compile("url\\(\\s*(.*)\\s*\\)"); //NOI18N
    
    public static OffsetRange getOffsetRange(Node node) {
        int from = node.from();
        int to = node.to();
        if(from < 0 || to < 0) {
            return OffsetRange.NONE;
        }
        if(from > to) {
            return OffsetRange.NONE;
        }
        return new OffsetRange(from, to);
    }
    
    public static OffsetRange getDocumentOffsetRange(Node node, Snapshot snapshot) {
        int from = node.from();
        int to = node.to();
        if(from < 0 || to < 0) {
            return OffsetRange.NONE;
        }
        if(from > to) {
            return OffsetRange.NONE;
        }
        int origFrom = snapshot.getOriginalOffset(from);
        int origTo = snapshot.getOriginalOffset(to);
        if(origFrom < 0 || origTo < 0) {
            return OffsetRange.NONE;
        }
        if(origFrom > origTo) {
            return OffsetRange.NONE;
        }
        
        return new OffsetRange(origFrom, origTo);
    }
    
    public static boolean isValidOffsetRange(OffsetRange range) {
        return range.getStart() != -1 && range.getEnd() != -1;
    }
    
    public static OffsetRange getValidOrNONEOffsetRange(OffsetRange range) {
        return isValidOffsetRange(range) ? range : OffsetRange.NONE;
    }

    public static boolean containsGeneratedCode(CharSequence text) {
        return CharSequenceUtilities.indexOf(text, Constants.LANGUAGE_SNIPPET_SEPARATOR) != -1;
    }
    
    public static boolean isVendorSpecificProperty(CharSequence propertyName) {
        return CharSequenceUtilities.startsWith(propertyName, "_") || CharSequenceUtilities.startsWith(propertyName, "-"); //NOI18N
    }
    
    public static boolean isVendorSpecificPropertyValue(FileObject file, CharSequence value) {
        if(value == null) {
            throw new NullPointerException();
        }
        if(value.length() == 0) {
            return false;
        }
        for(Browser b : CssModuleSupport.getBrowsers(file)) {
            if(LexerUtils.startsWith(value, b.getVendorSpecificPropertyPrefix(), true, false)) {
                return true;
            }
        }
        
        return false;
    }
    
}
