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

package org.netbeans.modules.beans;

import org.openide.nodes.*;
import static org.netbeans.modules.beans.BeanUtils.*;


/** Node representing a indexed property.
* @see IdxPropertyPattern
* @author Petr Hrebejk
*/
public final class IdxPropertyPatternNode extends PropertyPatternNode  {

    /** Create a new pattern node.
    * @param pattern field element to represent
    * @param writeable <code>true</code> to be writable
    */
    public IdxPropertyPatternNode( IdxPropertyPattern pattern, boolean writeable) {
        super(pattern, writeable);
    }

    /** Gets the localized string name of property pattern type i.e.
     * "Indexed Property", "Property".
     */
    @Override
    String getTypeForHint() {
        return getString( "HINT_IndexedProperty" );
    }

    /** Overrides the default implementation of clone node
    */
    @Override
    public Node cloneNode() {
        return new IdxPropertyPatternNode((IdxPropertyPattern)pattern, writeable);
    }
}

