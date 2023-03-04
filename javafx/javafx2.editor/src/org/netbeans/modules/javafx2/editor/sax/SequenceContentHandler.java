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

import org.xml.sax.ContentHandler;

/**
 * Extended ContentHandler interface, which allows to emit 
 * contents as CharSequences rather than char[]. This behaviour allows
 * to reuse content, not make copies. If a ContentHandler instance provided to
 * the {@link XmlLexerParser} implements this extended contract, the methods
 * {@link #characterSequence} will be called rather than the original
 * {@link #characters}
 * 
 * @author sdedic
 */
public interface SequenceContentHandler extends ContentHandler {
    /**
     * Character content was found
     * @param seq 
     */
    public void characterSequence(CharSequence seq);
    
    public void ignorableWhitespaceSequence(CharSequence seq);
}
