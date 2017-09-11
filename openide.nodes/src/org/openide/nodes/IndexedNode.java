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
