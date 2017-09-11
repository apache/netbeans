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

package org.netbeans.insane.scanner;

import java.lang.reflect.Field;

/**
 * A visitor interface that is called by the engine during the heap scan.
 *
 * @author  Nenik
 */
public interface Visitor {
    /**
     * A new type was found.
     * It is guaranteed to be reported before first instance of given class.
     * It is also guaranteed that all superclasses and interfaces will be
     * reported before a subclass.
     *
     * @param cls the new type found.
     */
    public void visitClass(Class<?> cls);

    /**
     * A new object instance was found.
     * It is guaranteed to be reported before first reference sourced from
     * or targetted to this instance.
     * It is also guaranteed that the instance's class will be reported
     * before the instance.
     *
     * @param map The {@link ObjectMap} containing this object.
     * @param object the reported instance.
     */
    public void visitObject(ObjectMap map, Object object);
    
    /**
     * A reference from object <code>from</code> to object <code>to</code>
     * was found as the contents of the field <code>ref</code>.
     *
     * It is guaranteed that both <code>from</code> and <code>to</code> objects
     * will be reported before the reference.
     *
     * @param map The {@link ObjectMap} containing the objects.
     * @param from The object from which the reference sources.
     * @param to The object to which the reference points.
     * @param ref The representation of the reference. Describes the class
     * the referring field is declared in, and how it is named.
     */
    public void visitObjectReference(ObjectMap map, Object from, Object to, Field ref);
    
    /**
     * A new reference to target object was found. The object <code>to</code>
     * is referenced by <code>index</code>-th slot of the array <code>from</code>
     *
     * It is guaranteed that both <code>from</code> and <code>to</code> objects
     * will be reported before the reference.
     *
     * @param map The {@link ObjectMap} containing the objects.
     * @param from The object from which the reference sources.
     * @param to The object to which the reference points.
     * @param index The array index of the <code>to<code> reference in
     * <code>from</code> array.
     */
    public void visitArrayReference(ObjectMap map, Object from, Object to, int index);
    
    /**
     * A new reference static reference to target object was found.
     * 
     * It is guaranteed that the <code>to</code> object will be reported before
     * the reference.
     *
     * @param map The {@link ObjectMap} containing the object.
     * @param to The object to which the reference points.
     * @param ref The representation of the reference. Describes the class
     * the referring field is declared in, and how it is named.
     */
    public void visitStaticReference(ObjectMap map, Object to, Field ref);
}
