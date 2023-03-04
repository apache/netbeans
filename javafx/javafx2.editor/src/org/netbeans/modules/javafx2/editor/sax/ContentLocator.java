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
