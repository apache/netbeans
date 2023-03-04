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


package org.netbeans.modules.properties;


import java.beans.*;
import java.io.*;
import javax.swing.text.BadLocationException;

import org.openide.nodes.Node;
import org.openide.ErrorManager;
import org.openide.text.PositionBounds;


/**
 * Base class for representations of elements in properties files.
 *
 * @author Petr Jiricka
 * @author Petr Kuzel - moved to nonescaped strings level
 * //!!! why is it serializable?
 */
public abstract class Element implements Serializable {

    /** Property change support */
    private transient PropertyChangeSupport support = new PropertyChangeSupport(this);

    /** Position of the begin and the end of the element. Could
     * be null indicating the element is not part of properties structure yet. */
    protected PositionBounds bounds;

    
    /** Create a new element. */
    protected Element(PositionBounds bounds) {
        this.bounds = bounds;
    }

    
    /** Getter for bounds property. */
    public PositionBounds getBounds() {
        return bounds;
    }

    /**
     * Updates the element fields. This method is called after reparsing.
     * @param elem the element to merge with
     */
    void update(Element elem) {
        this.bounds = elem.bounds;
    }

    /** Fires property change event.
     * @param name property name
     * @param o old value
     * @param n new value
     */
    protected final void firePropertyChange(String name, Object o, Object n) {
        support.firePropertyChange (name, o, n);
    }

    /** Adds property listener */
    public void addPropertyChangeListener (PropertyChangeListener l) {
        support.addPropertyChangeListener (l);
    }

    /** Removes property listener */
    public void removePropertyChangeListener (PropertyChangeListener l) {
        support.removePropertyChangeListener (l);
    }

    /** Prints this element (and all its subelements) by calling <code>bounds.setText(...)</code>
     * If <code>bounds</code> is null does nothing. 
     * @see #bounds */
    public final void print() {
        if (bounds == null) {
            return;
        }
        try {
            bounds.setText(getDocumentString());
        } catch (BadLocationException e) {
            ErrorManager.getDefault().notify(e);
        } catch (IOException e) {
            ErrorManager.getDefault().notify(e);
        }
    }

    /**
     * Get a string representation of the element for printing into Document.
     * It currently means that it's properly escaped.
     * @return the string in its Document form
     */
    public abstract String getDocumentString();

    /**
     * Get debug string of the element.
     * @return the string
     */
    public String toString() {
        if (bounds == null) {
            return "(no bounds)";
        }
        return new StringBuffer(16)
                .append('(')
                .append(bounds.getBegin().getOffset())
                .append(", ")                                           //NOI18N
                .append(bounds.getEnd().getOffset())
                .append(')')
                .toString();
    }

    
    /** General class for basic elements, which contain value directly. */
    public abstract static class Basic extends Element {

        private static final String hexaDigitChars
                                    = "0123456789abcdefABCDEF";         //NOI18N

        protected static void appendIsoControlChar(final StringBuilder buf,
                                                   final char c) {
            switch (c) {
            case '\t':
                buf.append('\\').append('t');
                break;
            case '\n':
                buf.append('\\').append('n');
                break;
            case '\f':
                buf.append('\\').append('f');
                break;
            case '\r':
                buf.append('\\').append('r');
                break;
            default:
                buf.append('\\').append('u');
                for (int shift = 12; shift >= 0; shift -= 4) {
                    buf.append(hexaDigitChars.charAt(
                                    ((c >> shift) & 0xf)));
                }
            }
        }

        /** Parsed value of the element */
        protected String value;

        /** Create a new basic element. */
        protected Basic(PositionBounds bounds, String value) {
            super(bounds);
            this.value = value;
        }

        /**
         * Updates the element fields. This method is called after reparsing.
         * @param elem elemnet to merge with
         */
        void update(Element elem) {
            super.update(elem);
            this.value = ((Basic)elem).value;
        }

        /** Get a string representation of the element.
         * @return the string + bounds
         */
        public String toString() {
            return value + "   " + super.toString(); // NOI18N
        }

        /**
         * Get a value of the element.
         * @return the Java string (no escaping)
         */
        public String getValue() {
            return value;
        }

        /**
         * Sets the value. Does not check if the value has changed.
         * The value is immediately propadated in text Document possibly
         * triggering DocumentEvents.
         * @param value Java string (no escaping)
         */
        public void setValue(String value) {
            this.value = value;
            this.print();
        }

        @Override
        public boolean equals(Object anObject) {
            if(this == anObject) {
                return true;
            }
            if(anObject instanceof Basic) {
                Basic b = (Basic)anObject;
                if(value == null) {
                    return b.value == null;
                }
                return value.equals(b.value);
            }
            return false;
        }

    } // End of nested class Basic.


    /** Class representing key element in properties file. */
    public static class KeyElem extends Basic {

        /** Generated serial version UID. */
        static final long serialVersionUID =6828294289485744331L;
        
        
        /** Create a new key element. */
        protected KeyElem(PositionBounds bounds, String value) {
            super(bounds, value);
        }

        
        /** Get a string representation of the key for printing. Treats the '=' sign as a part of the key
        * @return the string
        */
        public String getDocumentString() {
            return escapeSpecialChars(value) + "=";                     //NOI18N
        }

        /**
         * 
         * @author  Marian Petras
         */
        private static final String escapeSpecialChars(final String text) {
            StringBuilder buf = new StringBuilder(text.length() + 16);

            final int length = text.length();
            for (int i = 0; i < length; i++) {
                char c = text.charAt(i);
                if (c < 0x20) {
                    Basic.appendIsoControlChar(buf, c);
                } else {
                    switch (c) {
                    case '#':
                    case '!':
                        if (i == 0) {
                            buf.append('\\');
                        }
                        break;
                    case ' ':
                    case '=':
                    case ':':
                    case '\\':
                        buf.append('\\');
                        break;
                    }
                    buf.append(c);
                }
            }
            return buf.toString();
        }

        @Override
        public boolean equals(Object anObject) {
            return anObject instanceof KeyElem && super.equals(anObject);
        }

    } // End of nested class KeyElem.
    

    /** Class representing value element in properties files. */
    public static class ValueElem extends Basic {

        /** Generated serial version UID. */
        static final long serialVersionUID =4662649023463958853L;
        
        /** Create a new value element. */
        protected ValueElem(PositionBounds bounds, String value) {
            super(bounds, value);
        }

        /** Get a string representation of the value for printing. Appends end of the line after the value.
        * @return the string
        */
        public String getDocumentString() {
            // escape outerspaces and continious line marks
            return escapeSpecialChars(value) + "\n";                    //NOI18N
        }

        /**
         * 
         * @author  Marian Petras
         */
        private static final String escapeSpecialChars(final String text) {
            StringBuilder buf = new StringBuilder(text.length() + 16);

            boolean isInitialWhitespace = true;
            final int length = text.length();
            for (int i = 0; i < length; i++) {
                char c = text.charAt(i);

                boolean escape = false;
                if (c == '\\') {
                    isInitialWhitespace = false;
                    escape = true;
                } else if (isInitialWhitespace) {
                    if (c == ' ') {
                        escape = true;
                    } else {
                        isInitialWhitespace =    (c == '\t') || (c == '\r')
                                              || (c == '\n') || (c == '\f');
                    }
                }

                if (c < 0x20) {
                    Basic.appendIsoControlChar(buf, c);
                } else {
                    if (escape) {
                        buf.append('\\');
                    }
                    buf.append(c);
                }
            }
            return buf.toString();
        }

    } // End of nested class ValueElem.

    /**
     * Class representing comment element in properties files. <code>null</code> values of the
     * string are legal and indicate that the comment is empty. It should contain
     * pure comment string without comment markers.
     */
    public static class CommentElem extends Basic {

        /** Genererated serial version UID. */
        static final long serialVersionUID =2418308580934815756L;
        
        
        /**
         * Create a new comment element.
         * @param value Comment without its markers (leading '#' or '!'). Markers
         *        are automatically prepended while writing it down to Document.
         */
        protected CommentElem(PositionBounds bounds, String value) {
            super(bounds, value);
        }

        
        /** Get a string representation of the comment for printing. Makes sure every non-empty line starts with a # and
        * that the last line is terminated with an end of line marker.
        * @return the string
        */
        public String getDocumentString() {
            if (value == null || value.length() == 0)
                return ""; // NOI18N
            else {
                // insert #s at the beginning of the lines which contain non-blank characters
                // holds the last position where we might have to insert a # if this line contains non-blanks
                StringBuffer sb = new StringBuffer(value);
                // append the \n if missing
                if (sb.charAt(sb.length() - 1) != '\n') {
                    sb.append('\n');
                }
                int lineStart = 0;
                boolean hasCommentChar = false;
                for (int i=0; i<sb.length(); i++) {
                    char aChar = sb.charAt(i);
                    // new line
                    if (aChar == '\n') {
                        String line = sb.substring(lineStart, i);
                        String convertedLine = escapeSpecialChars(line);
                        sb.replace(lineStart, i, convertedLine);

                        // shift the index:
                        i += convertedLine.length() - line.length();

                        // the next line starts after \n:
                        lineStart = i + 1;

                        hasCommentChar = false;
                    } else if (!hasCommentChar
                          && UtilConvert.whiteSpaceChars.indexOf(aChar) == -1) {
                        // nonempty symbol
                        if ((aChar == '#') || (aChar == '!')) {
                            lineStart = i + 1;
                        } else {
                            // insert a #
                            sb.insert(lineStart, '#');
                            i++;
                            lineStart = i;
                        }
                        hasCommentChar = true;
                    }
                }
                return sb.toString();
            }
        }

        /**
         *
         * @author  Marian Petras
         */
        private static final String escapeSpecialChars(final String text) {
            StringBuilder buf = new StringBuilder(text.length() + 16);

            final int length = text.length();
            for (int i = 0; i < length; i++) {
                char c = text.charAt(i);
                assert (c != '\n');

                if ((c < 0x20) && (c != '\t')) { // keep tabs un-escaped in comments
                    Basic.appendIsoControlChar(buf, c);
                    continue;
                } else if (c == '\\') {
                    buf.append('\\');
                }
                buf.append(c);
            }
            return buf.toString();
        }

        @Override
        public boolean equals(Object anObject) {
            return anObject instanceof CommentElem && super.equals(anObject);
        }

    } // End of nested CommentElem.


    /** 
     * Class representing element in  properties file. Each element contains comment (preceding the property),
     * key and value subelement.
     */
    public static class ItemElem extends Element implements Node.Cookie {

        /** Key element.  */
        private KeyElem     key;
        
        /** Value element. */        
        private ValueElem   value;
        
        /** Comment element. */
        private CommentElem comment;
        
        /** Parent of this element - active element has a non-null parent. */
        private PropertiesStructure parent;

        /** Name of the Key property */
        public static final String PROP_ITEM_KEY     = "key"; // NOI18N
        /** Name of the Value property */
        public static final String PROP_ITEM_VALUE   = "value"; // NOI18N
        /** Name of the Comment property */
        public static final String PROP_ITEM_COMMENT = "comment"; // NOI18N

        /** Generated serial version UID. */
        static final long serialVersionUID =1078147817847520586L;

        
        /** Create a new basic element. <code>key</code> and <code>value</code> may be null. */
        protected ItemElem(PositionBounds bounds, KeyElem key, ValueElem value, CommentElem comment) {
            super(bounds);
            this.key     = key;
            this.value   = value;
            this.comment = comment;
        }

        
        /** Sets the parent of this element. */
        void setParent(PropertiesStructure ps) {
            parent = ps;
        }

        /** Gets parent.
         * @exception IllegalStateException if the parent is <code>null</code>. */
        private PropertiesStructure getParent() {
            if(parent == null) {
                throw new IllegalStateException("Resource Bundle: Parent is missing"); // NOI18N
            }

            return parent;
        }

        /** Get a value string of the element.
         * @return the string
         */
        public String toString() {
            return comment.toString() + "\n" + // NOI18N
                ((key   == null) ? "" : key.toString()) + "\n" + // NOI18N
                ((value == null) ? "" : value.toString()) + "\n"; // NOI18N
        }

        /** Returns the key element for this item. */
        public KeyElem getKeyElem() {
            return key;
        }

        /** Returns the value element for this item. */
        public ValueElem getValueElem() {
            return value;
        }

        /** Returns the comment element for this item. */
        public CommentElem getCommentElem() {
            return comment;
        }

        void update(Element elem) {
            super.update(elem);
            if (this.key == null)
                this.key     = ((ItemElem)elem).key;
            else
                this.key.update(((ItemElem)elem).key);

            if (this.value == null)
                this.value   = ((ItemElem)elem).value;
            else
                this.value.update(((ItemElem)elem).value);

            this.comment.update(((ItemElem)elem).comment);
        }

        public String getDocumentString() {
            return comment.getDocumentString() +
                ((key   == null) ? "" : key.getDocumentString()) + // NOI18N
                ((value == null) ? "" : value.getDocumentString()); // NOI18N
        }

        /** Get a key by which to identify this record
         * @return nonescaped key
         */
        public String getKey() {
            return (key == null) ? null : key.getValue();
        }

        /** Set the key for this item
        *  @param newKey nonescaped key
        */                        
        public void setKey(String newKey) {
            String oldKey = key.getValue();
            if (!oldKey.equals(newKey)) {
                key.setValue(newKey);
                getParent().itemKeyChanged(oldKey, this);
                this.firePropertyChange(PROP_ITEM_KEY, oldKey, newKey);
            }
        }

        /** Get the value of this item */
        public String getValue() {
            return (value == null) ? null : value.getValue();
        }

        /** Set the value of this item
         *  @param newValue the new value
         */                        
        public void setValue(String newValue) {
            String oldValue = value.getValue();
            if (!oldValue.equals(newValue)) {
                
                if(oldValue.equals("")) // NOI18N
                    // Reprint key for the case it's alone yet and doesn't have seprator after (= : or whitespace).
                    key.print();
                
                value.setValue(newValue);
                getParent().itemChanged(this);
                this.firePropertyChange(PROP_ITEM_VALUE, oldValue, newValue);
            }
        }

        /** Get the comment for this item */
        public String getComment() {
            return (comment == null) ? null : comment.getValue();
        }

        /** Set the comment for this item
         *  @param newComment the new comment (escaped value)
         *  //??? why is required escaped value? I'd expect escapng to be applied during
         *  writing value down to stream no earlier
         */                        
        public void setComment(String newComment) {
            String oldComment = comment.getValue();
            if ((oldComment == null && newComment != null) || (oldComment != null && !oldComment.equals(newComment))) {
                comment.setValue(newComment);
                getParent().itemChanged(this);
                this.firePropertyChange(PROP_ITEM_COMMENT, oldComment, newComment);
            }
        }

        /** Checks for equality of two ItemElem-s */
        public boolean equals(Object item) {
            if (!(item instanceof ItemElem))
                return false;
            ItemElem ie = (ItemElem)item;
            return isKeyEqual(ie) && isValueEqual(ie) && isCommentEqual(ie);
        }

        private boolean isKeyEqual(ItemElem ie) {
            if(key==null) {
                return ie.key==null;
            }
            return key.equals(ie.key);
        }

        private boolean isValueEqual(ItemElem ie) {
            if(value==null) {
                return ie.value==null;
            }
            return value.equals(ie.value);
        }

        private boolean isCommentEqual(ItemElem ie) {
            if(comment==null) {
                return ie.comment==null;
            }
            return comment.equals(ie.comment);
        }

    } // End of nested class ItemElem.
}
