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
