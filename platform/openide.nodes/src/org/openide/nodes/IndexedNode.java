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
package org.openide.nodes;

import javax.swing.JPanel;
import org.openide.util.Lookup;


/** An implementation of a node that has children and
* supports reordering by providing Index implementor.
* Index implementor and children can be the same instance,
* allowing us to use either Index.ArrayChildren or Index.MapChildren
*
* @author Jaroslav Tulach, Dafe Simonek
*/
public class IndexedNode extends AbstractNode {
    /** Index implementation */
    private Index indexImpl;

    /** Create an indexed node. Uses {@link Index.ArrayChildren} to both
    * hold the children, and as an implementation of {@link Index}.
    */
    public IndexedNode() {
        super(new Index.ArrayChildren());
        indexImpl = (Index) getChildren();
    }

    /** Allows subclasses to provide their own children and
    * index handling.
    * @param children the children implementation
    * @param indexImpl the index implementation
    */
    protected IndexedNode(Children children, Index indexImpl) {
        super(children);
        this.indexImpl = indexImpl;
    }

    /** Allows subclasses to provide their own children and
    * index handling as well as {@link Lookup}.
    * @param children the children implementation
    * @param indexImpl the index implementation
    * @param lookup lookup the node shall use
    * @since 7.16
    */
    protected IndexedNode(Children children, Index indexImpl, Lookup lookup) {
        super(children, lookup);
        this.indexImpl = indexImpl;
    }

    /*
    * @return false to signal that the customizer should not be used.
    *  Subclasses can override this method to enable customize action
    *  and use customizer provided by this class.
    */
    public boolean hasCustomizer() {
        return false;
    }

    /* Returns the customizer component.
    * @return the component
    */
    public java.awt.Component getCustomizer() {
        java.awt.Container c = new JPanel();
        @SuppressWarnings("deprecation")
        IndexedCustomizer customizer = new IndexedCustomizer(c, false);
        customizer.setObject(indexImpl);

        return c;
    }

    /** Get a cookie.
    * @param clazz representation class
    * @return the index implementation or children if these match the cookie class,
    * else using the superclass cookie lookup
    */
    public <T extends Node.Cookie> T getCookie(Class<T> clazz) {
        if (clazz.isInstance(indexImpl)) {
            // ok, Index implementor is enough
            return clazz.cast(indexImpl);
        }

        Children ch = getChildren();

        if (clazz.isInstance(ch)) {
            // ok, children are enough
            return clazz.cast(ch);
        }

        return super.getCookie(clazz);
    }
}
