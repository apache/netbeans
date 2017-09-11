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
package org.openide.text;

import javax.swing.event.DocumentListener;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.*;


// Document implementation

/** Document that delegates all functionality to given document.
* Useful if a subclass wants to modify a behaviour of a document.
* <p>{@link StyledDocument} methods are just defaulted to the plainest behavior.
*
* @author Jaroslav Tulach
*/
public class FilterDocument extends Object implements StyledDocument {
    /** default leaf element */
    private static Element leaf;

    /** the original document to delegate to */
    protected Document original;

    /** Create new document instance.
     * @param original delegated-to document
      */
    public FilterDocument(Document original) {
        this.original = original;
    }

    /* Length of document.
    * @return number of characters >= 0
    */
    public int getLength() {
        return original.getLength();
    }

    /* Add listener for changes in document */
    public void addDocumentListener(DocumentListener l) {
        original.addDocumentListener(l);
    }

    /* Remove listener for chagnes in document */
    public void removeDocumentListener(DocumentListener l) {
        original.removeDocumentListener(l);
    }

    /* Add listener for undoable edit actions over document */
    public void addUndoableEditListener(UndoableEditListener listener) {
        original.addUndoableEditListener(listener);
    }

    /* Remove listener for undoable edit actions */
    public void removeUndoableEditListener(UndoableEditListener listener) {
        original.removeUndoableEditListener(listener);
    }

    /* Gets document property by key */
    public Object getProperty(Object key) {
        if (key == DocumentFilter.class && original instanceof AbstractDocument) {
            return ((AbstractDocument)original).getDocumentFilter();
        }
        return original.getProperty(key);
    }

    /* Puts new property of document */
    public void putProperty(Object key, Object value) {
        if (key == DocumentFilter.class && original instanceof AbstractDocument) {
            ((AbstractDocument)original).setDocumentFilter((DocumentFilter)value);
        } else {
            original.putProperty(key, value);
        }
    }

    /* Removes portion of a document */
    public void remove(int offset, int len) throws BadLocationException {
        original.remove(offset, len);
    }

    /* Inserts string into document */
    public void insertString(int offset, String str, AttributeSet a)
    throws BadLocationException {
        original.insertString(offset, str, a);
    }

    /* Get text at given offset in document as string */
    public String getText(int offset, int len) throws BadLocationException {
        return original.getText(offset, len);
    }

    /* Get text at given offset in document as segment */
    public void getText(int offset, int len, Segment txt)
    throws BadLocationException {
        original.getText(offset, len, txt);
    }

    /* Get the start non-movable position in the document */
    public Position getStartPosition() {
        return original.getStartPosition();
    }

    /* Get end document position */
    public Position getEndPosition() {
        return original.getEndPosition();
    }

    /* Create position in document */
    public Position createPosition(int offset) throws BadLocationException {
        return original.createPosition(offset);
    }

    /* Return array of root elements - usually only one */
    public Element[] getRootElements() {
        return original.getRootElements();
    }

    /* Return default root element */
    public Element getDefaultRootElement() {
        return original.getDefaultRootElement();
    }

    /* Rendering on document as reader. Renderer must not make
    * any mutations.
    */
    public void render(Runnable r) {
        original.render(r);
    }

    //
    // StyledDocument methods
    //

    /*
    * Adds a new style into the logical style hierarchy.  Style attributes
    * resolve from bottom up so an attribute specified in a child
    * will override an attribute specified in the parent.
    *
    * @param nm   the name of the style (must be unique within the
    *   collection of named styles).  The name may be null if the style
    *   is unnamed, but the caller is responsible
    *   for managing the reference returned as an unnamed style can't
    *   be fetched by name.  An unnamed style may be useful for things
    *   like character attribute overrides such as found in a style
    *   run.
    * @param parent the parent style.  This may be null if unspecified
    *   attributes need not be resolved in some other style.
    * @return the style
    */
    public Style addStyle(String nm, Style parent) {
        return null;
    }

    /*
    * Removes a named style previously added to the document.
    *
    * @param nm  the name of the style to remove
    */
    public void removeStyle(String nm) {
    }

    /*
    * Fetches a named style previously added.
    *
    * @param nm  the name of the style
    * @return the style
    */
    public Style getStyle(String nm) {
        return null;
    }

    /*
    * Changes the content element attributes used for the given range of
    * existing content in the document.  All of the attributes
    * defined in the given Attributes argument are applied to the
    * given range.  This method can be used to completely remove
    * all content level attributes for the given range by
    * giving an Attributes argument that has no attributes defined
    * and setting replace to true.
    *
    * @param offset the start of the change >= 0
    * @param length the length of the change >= 0
    * @param s    the non-null attributes to change to.  Any attributes
    *  defined will be applied to the text for the given range.
    * @param replace indicates whether or not the previous
    *  attributes should be cleared before the new attributes
    *  as set.  If true, the operation will replace the
    *  previous attributes entirely.  If false, the new
    *  attributes will be merged with the previous attributes.
    */
    public void setCharacterAttributes(int offset, int length, AttributeSet s, boolean replace) {
    }

    /*
    * Sets paragraph attributes.
    *
    * @param offset the start of the change >= 0
    * @param length the length of the change >= 0
    * @param s    the non-null attributes to change to.  Any attributes
    *  defined will be applied to the text for the given range.
    * @param replace indicates whether or not the previous
    *  attributes should be cleared before the new attributes
    *  are set.  If true, the operation will replace the
    *  previous attributes entirely.  If false, the new
    *  attributes will be merged with the previous attributes.
    */
    public void setParagraphAttributes(int offset, int length, AttributeSet s, boolean replace) {
    }

    /*
    * Sets the logical style to use for the paragraph at the
    * given position.  If attributes aren't explicitly set
    * for character and paragraph attributes they will resolve
    * through the logical style assigned to the paragraph, which
    * in turn may resolve through some hierarchy completely
    * independent of the element hierarchy in the document.
    *
    * @param pos the starting position >= 0
    * @param s the style to set
    */
    public void setLogicalStyle(int pos, Style s) {
    }

    /*
    * Gets a logical style for a given position in a paragraph.
    *
    * @param p the position >= 0
    * @return the style
    */
    public Style getLogicalStyle(int p) {
        return null;
    }

    /*
    * Gets the element that represents the paragraph that
    * encloses the given offset within the document.
    *
    * @param pos the offset >= 0
    * @return the element
    */
    public Element getParagraphElement(int pos) {
        Element e = getDefaultRootElement();
        if (e != null && !e.isLeaf()) {
            int index = e.getElementIndex(pos);
            e = e.getElement(index);
        } else {
            e = getLeafElement();
    }
        return e;
    }

    /*
    * Gets the element that represents the character that
    * is at the given offset within the document.
    *
    * @param pos the offset >= 0
    * @return the element
    */
    public Element getCharacterElement(int pos) {
        return getLeafElement();
    }

    /*
    * Takes a set of attributes and turn it into a foreground color
    * specification.  This might be used to specify things
    * like brighter, more hue, etc.
    *
    * @param attr the set of attributes
    * @return the color
    */
    public java.awt.Color getForeground(AttributeSet attr) {
        return java.awt.Color.black;
    }

    /*
    * Takes a set of attributes and turn it into a background color
    * specification.  This might be used to specify things
    * like brighter, more hue, etc.
    *
    * @param attr the set of attributes
    * @return the color
    */
    public java.awt.Color getBackground(AttributeSet attr) {
        return java.awt.Color.white;
    }

    /*
    * Takes a set of attributes and turn it into a font
    * specification.  This can be used to turn things like
    * family, style, size, etc into a font that is available
    * on the system the document is currently being used on.
    *
    * @param attr the set of attributes
    * @return the font
    */
    public java.awt.Font getFont(AttributeSet attr) {
        return null;
    }

    /** Lazy initialization of leaf element.
    */
    private static Element getLeafElement() {
        if (leaf != null) {
            return leaf;
        }

        AbstractDocument doc = new javax.swing.text.html.HTMLDocument();

        return leaf = doc.new LeafElement(null, null, 0, 0);
    }
}
