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
