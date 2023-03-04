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
package org.netbeans.modules.csl.api;

import java.net.URL;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.text.Document;

import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.csl.spi.ParserResult;
import org.openide.filesystems.FileObject;


/**
 *
 * @author Tor Norbye
 */
public interface DeclarationFinder {
    /**
     * Find the declaration for the program element that is under the caretOffset
     * Return a Set of regions that should be renamed if the element under the caret offset is
     * renamed.
     *
     * Return {@link DeclarationLocation#NONE} if the declaration can not be found, otherwise return
     *   a valid DeclarationLocation.
     */
    @NonNull
    DeclarationLocation findDeclaration(@NonNull ParserResult info, int caretOffset);

    /**
     * Check the caret offset in the document and determine if it is over a span
     * of text that should be hyperlinkable ("Go To Declaration" - in other words,
     * locate the reference and return it. When the user drags the mouse with a modifier
     * key held this will be hyperlinked, and so on.
     * <p>
     * Remember that when looking up tokens in the token hierarchy, you will get the token
     * to the right of the caret offset, so check for these conditions
     * {@code (sequence.move(offset); sequence.offset() == offset)} and check both
     * sides such that placing the caret between two tokens will match either side.
     *
     * @return {@link OffsetRange#NONE} if the caret is not over a valid reference span,
     *   otherwise return the character range for the given hyperlink tokens
     */
    @NonNull
    public OffsetRange getReferenceSpan(@NonNull Document doc, int caretOffset);

    
    /**
     * Holder object for return values from the DeclarationFinder#findDeclaration method.
     * The constant {@link #NONE} object should be returned when finding a declaration failed.
     */
    public final class DeclarationLocation {
        /** DeclarationLocation representing no match or failure to find declaration */
        public static final DeclarationLocation NONE = new DeclarationLocation(null, -1);
        private final FileObject fileObject;
        private final int offset;
        private final URL url;
        private List<AlternativeLocation> alternatives;
        /** Associated element, if any */
        private ElementHandle element;
        private String invalidMessage;

        public DeclarationLocation(@NonNull final FileObject fileObject, final int offset) {
            this.fileObject = fileObject;
            this.offset = offset;
            this.url = null;
        }

        public DeclarationLocation(@NonNull final FileObject fileObject, final int offset, @NonNull ElementHandle element) {
            this(fileObject, offset);
            this.element = element;
        }

        public DeclarationLocation(@NonNull final URL url) {
            this.url = url;
            this.fileObject = null;
            this.offset = -1;
        }

        /** Mark this declaration location as an "invalid" location (for example,
         * in Ruby or JavaScript, a builtin function that has no corresponding source.
         * @param invalidMessage A message which will be displayed to the user on attempts to
         * go to this declaration location.
         */
        public void setInvalidMessage(String invalidMessage) {
            this.invalidMessage = invalidMessage;
        }
        
        public void addAlternative(@NonNull AlternativeLocation location) {
            if (alternatives == null) {
                alternatives = new ArrayList<AlternativeLocation>();
            }
            
            alternatives.add(location);
        }
        
        @NonNull
        public List<AlternativeLocation> getAlternativeLocations() {
            if (alternatives != null) {
                return alternatives;
            } else {
                return Collections.emptyList();
            }
        }
        
        @CheckForNull
        public URL getUrl() {
            return url;
        }

        @CheckForNull
        public FileObject getFileObject() {
            return fileObject;
        }

        public int getOffset() {
            return offset;
        }
        
        @CheckForNull
        public ElementHandle getElement() {
            return element;
        }

        @CheckForNull
        public String getInvalidMessage() {
            return invalidMessage;
        }
        
        @Override
        public String toString() {
            if (this == NONE) {
                return "NONE";
            }

            if (url != null) {
                return url.toExternalForm();
            }

            return fileObject.getNameExt() + ":" + offset;
        }
    }
    
    public interface AlternativeLocation extends Comparable<AlternativeLocation> {
        ElementHandle getElement();
        String getDisplayHtml(HtmlFormatter formatter);
        DeclarationLocation getLocation();
    }
}
