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

package org.openide.loaders;

import java.io.Serializable;

/** Filter that accepts everything.
* @author Jaroslav Tulach
*/
class DataFilterAll extends Object implements DataFilter, Serializable {
    static final long serialVersionUID =-760448687111430451L;
    public boolean acceptDataObject (DataObject obj) {
        return true;
    }

    /** Gets a resolvable. */
    public Object writeReplace() {
        return new Replace();
    }

    static class Replace implements Serializable {
        static final long serialVersionUID =3204495526835476127L;
        public Object readResolve() {
            return DataFilter.ALL;
        }
    }
}
