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

package org.netbeans.editor;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.awt.Image;
import javax.swing.Action;

/** Description of the annotation. The annotations is defined by
 * AnnotationType, length, offset and description.
 *
 * @author David Konecny
 * @since 07/2001
 */
public abstract class AnnotationDesc extends Object implements Comparable<AnnotationDesc> {

    /** Property name of the tip text */
    public static final String PROP_SHORT_DESCRIPTION = "shortDescription"; // NOI18N

    /** Property name of the annotation type */
    public static final String PROP_ANNOTATION_TYPE = "annotationType"; // NOI18N

    /** Virtual property for fronting of annotation */
    public static final String PROP_MOVE_TO_FRONT = "moveToFront"; // NOI18N

    /** Support for property change listeners*/
    private PropertyChangeSupport support;

    /** This is sequential number which is used for cycling 
     * through the anotations. Each added annotion gets number
     * and this number if necessary is used for cycling through the 
     * annotations - cycle causes that annotation with higher number
     * is shown. If no higher number exist the cycling starts from the
     * begining. */
    private int order;

    /** Unique counter used for order variable.*/
    private static int counter = 0;

    /** Length of the annotated text. If -1 than whole line is annotated */
    private int length;

    /** Private member used by Annotations class. After the annotation is
     * attached to document, the Mark which is crated (or shared with some
     * other annotation) is stored here. Only for internal purpose of Annoations 
     * class */
    private Mark mark;

    /** AnnotationType attached to this annotation. Annotation has a few 
     * pass through methods to AnnotationType for simple access to the 
     * annotation type information. */
    private AnnotationType type = null;

    public AnnotationDesc(int offset, int length) {
        counter++;
        this.order = counter;
        this.length = length;
        support = new PropertyChangeSupport(this);
    }

    /** Gets annotation coloring. This is pass through method to annotation type */
    public Coloring getColoring() {
        if (type == null) updateAnnotationType();
        return (type != null) ? type.getColoring() : new Coloring(null, Coloring.FONT_MODE_DEFAULT, null, null, null, null, null);
    }

    /** Gets glyph image. This is pass through method to annotation type */
    public Image getGlyph() {
        if (type == null) updateAnnotationType();
        return (type != null) ? type.getGlyphImage() : null;
    }

    /**
     * Gets annotation priority. This is pass through method to annotation type
     * @return priority, defaults to zero.
     */
    private int getPriority() {
        if (type == null) {
            updateAnnotationType();
        }
        return type.getPriority();
    }

    /** Checks whether the annotation type has its own glyph icon */
    public boolean isDefaultGlyph() {
        if (type == null) updateAnnotationType();
        return (type != null) ? type.isDefaultGlyph() : false;
    }

    /** Is annotation type visible. This is pass through method to annotation type */
    public boolean isVisible() {
        if (type == null) updateAnnotationType();
        return (type != null) ? type.isVisible() : false;
    }

    /** Internal order of the annotations. Used for correct cycling. */
    public int getOrderNumber() {
        return order;
    }

    /** Returns list of actions associated to this annotation type. */
    public Action[] getActions() {
        if (type == null) updateAnnotationType();
        return (type != null) ? type.getActions() : new Action[0];
    }

    /** Whether this annotation annotates whole line or just part of the text*/
    public boolean isWholeLine() {
        return length == -1;
    }

    /** Get length of the annotation*/
    public int getLength() {
        return length;
    }

    /** Set Mark which represent this annotation in document */
    void setMark(Mark mark) {
        this.mark = mark;
    }

    /** Get Mark which represent this annotation in document */
    Mark getMark() {
        return mark;
    }

    /** Getter for annotation type object */
    public AnnotationType getAnnotationTypeInstance() {
        return type;
    }
    
    /** Getter for annotation type name */
    public abstract String getAnnotationType();

    /** Getter for localized tooltip text for this annotation */
    public abstract String getShortDescription();

    /** Getter for offset of this annotation */
    public abstract int getOffset();

    /** Getter for line number of this annotation */
    public abstract int getLine();

    
    /** Method for fetching AnnotationType which
     * correspond to the name of the annotation type stored
     * in annotation. */
    public void updateAnnotationType() {
        type = AnnotationTypes.getTypes().getType(getAnnotationType());
    }

    /** Add listeners on changes of annotation properties
     * @param l  change listener*/
    public final void addPropertyChangeListener(PropertyChangeListener l) {
        support.addPropertyChangeListener(l);
    }

    /** Remove listeners on changes of annotation properties
     * @param l  change listener*/
    public final void removePropertyChangeListener(PropertyChangeListener l) {
        support.removePropertyChangeListener(l);
    }

    /** Fire property change to registered listeners. */
    protected final void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
        support.firePropertyChange(propertyName, oldValue, newValue);
    }

    public int compareTo(AnnotationDesc o) {
        int p1 = this.getPriority();
        int p2 = o.getPriority();
        return (p1 > p2 ? -1 : (p1 == p2 ? 0 : 1));
    }

    @Override
    public String toString() {
        return "Annotation: type='" + getAnnotationType() + "', line=" + getLine() + // NOI18N
            ", offset=" + getOffset() + ", length=" + length + // NOI18N
            ", coloring=" + getColoring(); // NOI18N
    }
    
}
