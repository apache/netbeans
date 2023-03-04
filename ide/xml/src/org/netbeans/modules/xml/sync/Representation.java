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
package org.netbeans.modules.xml.sync;

import org.openide.nodes.*;

/**
 * Same data may have multiple representations. A representation can
 * be described using this interface.
 *
 * @author  Petr Kuzel
 * @version
 */
public interface Representation {

    /**
     * @return select button diplay name used during notifying concurent modification
     * conflict.
     */
    public String getDisplayName();

    /**
     * Is this representation modified since last update?
     * Warning isModified() does not equals representationChanged().
     */
    public boolean isModified();

    /**
     * Determine whether given representation is valid. E.g. tree
     * represnattion is valid just if parsed successfully.
     */
    public boolean isValid();
    
    /**
     * Update the representation without marking it as modified.
     */
    public void update(Object change);

    /**
     * Return prefered update class or null if does not matter.
     * //??? Could return <codE>Class[]</code> in future.
     */
    public Class getUpdateClass();

    /**
     * Return modification passed as update parameter to all slave representations.
     * @param type if null return arbitrary representation
     * @return Change or null if change of given type can not be returned
     */
    public Object getChange(Class type);

    /**
     * Does this representation wraps given model?
     */
    public boolean represents(Class type);

    /**
     * Returnrepresentation level: 0 = file, 1 = byte buffer [text],
     * 2 = structural model, 3 = semantics model ... A higher level
     * representaion requires that lower level representation is
     * loaded too.
     *
     * @return
     */
    public int level();
}
