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
package org.openide.text;

import java.beans.PropertyChangeSupport;
import java.util.LinkedList;
import java.util.List;
import java.util.Collections;

/** Classes which are capable of holding annotations must
 * extend this abstract class. The examples of these classes are
 * Line or Line.Part. It allows to add/remove
 * Annotation(s) to this class. There is also support for
 * listening on changes of the properties like deleted or
 * count of attached annotations.
 *
 * @author David Konecny, Jaroslav Tulach
 * @since 1.20
 */
public abstract class Annotatable extends Object {
    /** Property name of the count of annotations  */
    public static final String PROP_ANNOTATION_COUNT = "annotationCount"; // NOI18N

    /** Property name for the deleted attribute */
    public static final String PROP_DELETED = "deleted"; // NOI18N

    /** Property name for the content of the annotatable
     * @since 1.35
     */
    public static final String PROP_TEXT = "text"; // NOI18N

    /** Support for property change listeners*/
    private final PropertyChangeSupport propertyChangeSupport;

    /** List of all annotations attached to this annotatable object */
    private final List<Annotation> attachedAnnotations;

    /** Whether the Annotatable object was deleted during
     * the editting of document or not. */
    private boolean deleted;

    public Annotatable() {
        deleted = false;
        propertyChangeSupport = new PropertyChangeSupport(this);
        attachedAnnotations = Collections.synchronizedList(new LinkedList<Annotation>());
    }

    /** Add annotation to this Annotatable class
     * @param anno annotation which will be attached to this class */
    protected void addAnnotation(Annotation anno) {
        int count;
        synchronized (attachedAnnotations) {
            attachedAnnotations.add(anno);
            count = attachedAnnotations.size();
        }
        propertyChangeSupport.firePropertyChange(PROP_ANNOTATION_COUNT, count - 1, count);
    }

    /** Remove annotation to this Annotatable class
     * @param anno annotation which will be detached from this class  */
    protected void removeAnnotation(Annotation anno) {
        int count;
        synchronized (attachedAnnotations) {
            attachedAnnotations.remove(anno);
            count = attachedAnnotations.size();
        }
        // XXX: If the annotation was not present, the PCE will be inaccurate
        // but it was so (and worse) from the invention of this API
        propertyChangeSupport.firePropertyChange(PROP_ANNOTATION_COUNT, count + 1, count);
    }

    /** Gets the list of all annotations attached to this annotatable object
     * @since 1.27 */
    List<? extends Annotation> getAnnotations() {
        return attachedAnnotations;
    }

    /** Add listeners on changes of annotatable properties
     * @param l change listener*/
    public final void addPropertyChangeListener(java.beans.PropertyChangeListener l) {
        propertyChangeSupport.addPropertyChangeListener(l);
    }

    /** Remove listeners on changes of annotatable properties
     * @param l change listener*/
    public final void removePropertyChangeListener(java.beans.PropertyChangeListener l) {
        propertyChangeSupport.removePropertyChangeListener(l);
    }

    /** Fire property change to registered listeners. */
    protected final void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
        propertyChangeSupport.firePropertyChange(propertyName, oldValue, newValue);
    }

    /** Whether this Annotatable object was removed or not.
     * @return whether the Annotatable object was removed or not
     */
    public final boolean isDeleted() {
        return deleted;
    }

    /** Get content of the annotatable. The listeners can listen
     * on changes of PROP_TEXT property to learn that content of Annotatable
     * is changing.
     * @return text representing the content of annotatable. The return value can be null,
     * what means that document is closed.
     * @since 1.35
     */
    public abstract String getText();

    /** Setter for property deleted.
     * @param deleted New value of property deleted.
     */
    void setDeleted(boolean deleted) {
        if (this.deleted != deleted) {
            this.deleted = deleted;
            propertyChangeSupport.firePropertyChange(PROP_DELETED, !deleted, deleted);
        }
    }

    /** The count of already attached annotations. Modules can use
     * this property to learn whether to this instance are
     * already attached some annotations or not.
     * @return count of attached annotations
     */
    public final int getAnnotationCount() {
        return attachedAnnotations.size();
    }
}
