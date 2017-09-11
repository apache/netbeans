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

package org.netbeans.insane.model;

import java.util.*;

/**
 * A simplified model of the Java heap. It represents relations between
 * Java objects, set of known root references and a set of types. It does not
 * represent values of the primitive object fields.
 *
 * @author Nenik
 */
public interface HeapModel {

    /**
     * Provides access to all known instances on the heap.
     *
     * @return Iterator of all Items
     */
    public Iterator<Item> getAllItems();

    /**
     * Provides access to all known instances of given type.
     *
     * @return Collection of Items of given type
     */    
    public Collection<Item> getObjectsOfType(String type);
    
    /**
     * Provides a collection of known root (static) references
     *
     * @return Collection of Strings representing the names
     *   of static fields
     */
    public Collection<String> getRoots();
    
    /* Access an object referenced from given field
     *
     * @param staticRefName the name of the static field as returned
     *   from getRoots() method.
     * @return Item representing the object at given static reference
     */
    public Item getObjectAt(String staticRefName);
    
    /* Access an object with a known ID
     *
     * @param id the id of the object
     * @return Item with given id, if such Item exists.
     * @throws IllegalArgumentException if no Item whith given id exists.
     */
    Item getItem(int id);    
}
