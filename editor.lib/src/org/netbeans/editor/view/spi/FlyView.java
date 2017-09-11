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

package org.netbeans.editor.view.spi;

import javax.swing.text.View;

/**
 * Interface marking the flyweight views.
 * <br>
 * Flyweight views
 * are immutable view instances that can be shared in arbitrary number of
 * occurrences.
 * <br>
 * They are typically only leaf views usually built on top of flyweight
 * elements.
 *
 * <p>
 * A view can be rendered differently in various rendering contexts.
 * Rendering contexts can affect measurements done by the view.
 * Therefore there are methods that can replicate the view
 * into a new instance in a particular context.
 *
 * <p>
 * Flyweight views cannot hold a reference to parent
 * and their <code>getParent()</code> returns null.
 * <br>
 * A call to <code>setParent()</code> would throw an exception.
 * <br>
 * Their <code>getElement()</code> returns <code>null</code>.
 * <br>
 * <code>getStartOffset()</code> always returns 0.
 * <br>
 * <code>getEndOffset()</code> returns length of the text
 * that they represent.
 * <br>
 * <code>getContainer()</code> always returns null.
 * 
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public interface FlyView {

    /**
     * Create an instance of the view dependent on the context
     * given by the parent view. If this view instance
     * satisfies the conditions imposed by parent's context
     * (e.g. this view's measurements match those with
     * the given parent's container font rendering context)
     * then this instance can be returned instead of creating a new one.
     *
     * <b>Note:</b> The parent view should only be used to perform
     * the necessary initialization of the new instance (or verification 
     * that existing instance can be created) but it should never
     * be held by the flyweight view permanently.
     *
     * @param parent instance of view that will act as parent for the view
     *  in the given context. The possibly created instance of the view
     *  can use the parent but it must not hold the reference to it permanently.
     * @return a this view instance if measurements of this view satisfy
     *  the context of the parent or a new view instance otherwise.
     */
    public FlyView flyInstance(View parent);
    
    /**
     * Create a regular instance that will act as a normal view.
     * This can be used in certain contexts where a regular view
     * would be needed typically for a short term use.
     * <br>
     * Caller ensures that the text represented by the given offset range
     * matches the text returned by {@link #getText()}.
     * <br>
     * Caller is also responsible to remove this view in case the text
     * in the particular area changes.
     */
    public View regularInstance(View parent, int startOffset, int endOffset);

    /**
     * Get the text represented by this view.
     * @return non-null instance of a character sequence.
     *  In case the view does not represent any text an empty sequence
     *  must be returned.
     */
    public CharSequence getText();

    
    /**
     * Interface that views capable of maintaining flyweight views
     * as their children must implement.
     */
    public interface Parent {
        
        /**
         * Get start offset of the child with the given index.
         * <br>
         * The child can be either flyweight or regular view.
         *
         * @param childViewIndex &gt;=0 index of the child.
         * @return start offset of the requested child.
         */
        public int getStartOffset(int childViewIndex);
        
        /**
         * Get end offset of the child with the given index.
         * <br>
         * The child can be either flyweight or regular view.
         *
         * @param childViewIndex &gt;=0 index of the child.
         * @return start offset of the requested child.
         */
        public int getEndOffset(int childViewIndex);
        
    }
}
