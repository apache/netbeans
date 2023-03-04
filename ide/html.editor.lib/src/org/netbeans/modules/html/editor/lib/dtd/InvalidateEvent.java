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

package org.netbeans.modules.html.editor.lib.dtd;

import java.util.Set;
import java.util.Iterator;

/** The event fired to all registered interfaces when some DTD is invalidated.
 *
 * @author  Petr Nejedly
 * @version 1.0
 */
public class InvalidateEvent {

    private Set identifiers;

    /** Create new InvalidateEvent for given Set of instances of String
     * representing public identifiers of DTDs to invalidate */
    public InvalidateEvent( Set identifiers ) {
        this.identifiers = identifiers;
    }

    /** Get the iterator of instances of String representing
     * public identifiers of the invalidated DTDs.
     * Usable for classes holding more DTDs. */
    public Iterator getIdentifierIterator() {
        return identifiers.iterator();
    }

    /** Test if given public identifier is invalidated by this event.
     * Usable for classes holding only one DTD. */
    public boolean isInvalidatedIdentifier( String identifier ) {
        return identifiers.contains( identifier );
    }
}
