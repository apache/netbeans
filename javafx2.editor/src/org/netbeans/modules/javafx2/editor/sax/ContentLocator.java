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
package org.netbeans.modules.javafx2.editor.sax;

import org.netbeans.modules.javafx2.editor.ErrorMark;
import java.util.Collection;
import java.util.List;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.xml.lexer.XMLTokenId;

/**
 *
 * @author sdedic
 */
public interface ContentLocator {
    public static final int NOPOS = -1;
    public static final int APPROX = -2;
    
    /**
     * Pseudo-attribute name, to get offset of the target in processing instruction
     */
    public static final String ATTRIBUTE_TARGET = "*target";
    
    /**
     * Pseudo-attribute name, to get offset of the 'data' part in processing instruction
     */
    public static final String ATTRIBUTE_DATA = "*data";
    
    /**
     * @return start of the element's text
     */
    public int getElementOffset();
    
    public int getEndOffset();
    
    public static final int OFFSET_START = 0;
    public static final int OFFSET_END = 1;
    public static final int OFFSET_VALUE_START = 2;
    public static final int OFFSET_VALUE_END = 3;

    /**
     * 
     * @param attribute
     * @return 
     */
    public int[] getAttributeOffsets(String attribute);

    /**
     * @return true, if the element contains some errors
     */
    public Collection<ErrorMark>    getErrors();
    
    /**
     * Return lexer tokens whcich correspond to the currently reported
     * element.
     * @return 
     */
    public List<Token<XMLTokenId>>  getMatchingTokens();
    
    /**
     * Provides access to the underlying TokenSequence.
     * @return 
     */
    public TokenSequence<XMLTokenId>   getTokenSequence();
    
    /**
     * This interface should be implemented on the SAX ContentHandler, if it wants
     * to receive extended content location info.
     */
    public interface Receiver {
        public void setContentLocator(ContentLocator l);
    }
}
