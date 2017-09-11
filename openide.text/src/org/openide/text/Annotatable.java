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
    final public void addPropertyChangeListener(java.beans.PropertyChangeListener l) {
        propertyChangeSupport.addPropertyChangeListener(l);
    }

    /** Remove listeners on changes of annotatable properties
     * @param l change listener*/
    final public void removePropertyChangeListener(java.beans.PropertyChangeListener l) {
        propertyChangeSupport.removePropertyChangeListener(l);
    }

    /** Fire property change to registered listeners. */
    final protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
        propertyChangeSupport.firePropertyChange(propertyName, oldValue, newValue);
    }

    /** Whether this Annotatable object was removed or not.
     * @return whether the Annotatable object was removed or not
     */
    final public boolean isDeleted() {
        return deleted;
    }

    /** Get content of the annotatable. The listeners can listen
     * on changes of PROP_TEXT property to learn that content of Annotatable
     * is changing.
     * @return text representing the content of annotatable. The return value can be null,
     * what means that document is closed.
     * @since 1.35
     */
    abstract public String getText();

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
    final public int getAnnotationCount() {
        return attachedAnnotations.size();
    }
}
