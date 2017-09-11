/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
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
