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


/** Description of annotation. The class is extended by modules
 * which creates annotations and defines annotation type and tooltip.
 * The annotation can be attached to Annotatable object. Editors which
 * displays Annotations listen on PropertyChangeListner for changes of
 * annotation type or tooltip text. The tooltip text can be evaluated
 * asynchronously. It means that editors after the getShortDescription call
 * must listen on PropertyChangeListner while the tooltip is visible.
 * If the tooltip text property is changed, the tooltip value must be updated.
 * <p>
 * See more - <a href="doc-files/api.html#auto-ann">description of the way to declare an Annotation</a>.
 *
 * @author David Konecny, Jaroslav Tulach
 * @since 1.20
 */
public abstract class Annotation extends Object {
    /** Property name of the tip text */
    public static final String PROP_SHORT_DESCRIPTION = "shortDescription"; // NOI18N

    /** Property name of the annotation type */
    public static final String PROP_ANNOTATION_TYPE = "annotationType"; // NOI18N

    /** Virtual property which does not have getter/setter. When change on this
     * property is fired, listeners (= editors) must ensure that this annotation is not covered
     * by some other annotation which is on the same line - annotation must be moved in front
     * of others. See also moveToFront() method.
     * @since 1.27 */
    public static final String PROP_MOVE_TO_FRONT = "moveToFront"; // NOI18N

    /** Support for property change listeners*/
    private java.beans.PropertyChangeSupport support;

    /** Holding the reference to Annotatable object to
     * which is this Annotation attached*/
    private Annotatable attached;

    /** Whether the annotation was added into document or not.
     * Annotation is added to document only in case it is opened.
     * WARNING: It is highly probable that this implementation
     * will change and maybe will be completely removed
     * in next version. It is used only during the attaching
     * and detaching of annotations to document. If the code
     * for loading/closing of document and add/remove of
     * annotations would be synchronized, this variable would not be
     * necessary. However, some refactoring on side of DocumentLine
     * and CloneableEditorSupport will be necessary to achieve this.
     */
    private boolean inDocument = false;

    public Annotation() {
        support = new java.beans.PropertyChangeSupport(this);
    }

    /** Returns name of the file which describes the annotation type.
     * The file must be defined in module installation layer in the
     * directory "Editors/AnnotationTypes"
     * @return  name of the anotation type*/
    public abstract String getAnnotationType();

    /**
     * Gets the tool tip text for this annotation.
     * @return tool tip for this annotation, or null for no tool tip
     */
    public abstract String getShortDescription();

    /** Attach annotation to Annotatable object.
     * @param anno annotatable class to which this annotation will be attached */
    public final void attach(Annotatable anno) {
        if (attached != null) {
            detach();
        }

        attached = anno;

        attached.addAnnotation(this);
        notifyAttached(attached);
    }

    /** Notifies the annotation that it was attached
     * to the annotatable.
     * @param toAnno annotatable to which the annotation
     *  was attached.
     * @since 1.38
     */
    protected void notifyAttached(Annotatable toAnno) {
    }

    /** Detach annotation.*/
    public final void detach() {
        if (attached != null) {
            attached.removeAnnotation(this);

            Annotatable old = attached;
            attached = null;
            notifyDetached(old);
        }
    }

    /** Notifies the annotation that it was detached
     * from the annotatable.
     * @param fromAnno annotatable from which the annotation
     *  was detached.
     * @since 1.38
     */
    protected void notifyDetached(Annotatable fromAnno) {
    }

    /** Gets annotatable object to which this annotation is attached.
     * @return null if annotation is not attached or reference to annotatable object.
     * @since 1.27 */
    final public Annotatable getAttachedAnnotatable() {
        return attached;
    }

    /** Add listeners on changes of annotation properties
     * @param l  change listener*/
    final public void addPropertyChangeListener(java.beans.PropertyChangeListener l) {
        support.addPropertyChangeListener(l);
    }

    /** Remove listeners on changes of annotation properties
     * @param l  change listener*/
    final public void removePropertyChangeListener(java.beans.PropertyChangeListener l) {
        support.removePropertyChangeListener(l);
    }

    /** Fire property change to registered listeners. */
    final protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
        support.firePropertyChange(propertyName, oldValue, newValue);
    }

    /** Helper method for moving annotation which is covered by other annotations
     * on the same line in front of others. The method fires change on PROP_MOVE_TO_FRONT
     * property on which editors must listen and do the fronting of the annotation. Whether the annotation
     * is visible in editor or not is not guaranteed by this method - use Line.show instead.
     * @since 1.27 */
    final public void moveToFront() {
        support.firePropertyChange(PROP_MOVE_TO_FRONT, null, null);
    }

    /** Getter for the inDocument property
     * @return is in document or not */
    final boolean isInDocument() {
        return inDocument;
    }

    /** Setter for the inDocument property
     * @param b is in document or not */
    final void setInDocument(boolean b) {
        inDocument = b;
    }
}
