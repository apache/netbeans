/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.cnd.api.xml;

import java.util.ArrayList;
import java.util.List;


/**
 * Utility class for constructing {@link AttrValuePair}s to be passed to
 * {@link XMLEncoderStream}.
 * <p>
 * There is no need to escape attribute values.
 * <p>
 * <pre>
 XMLEncoderStream xes;
 ...
 AttrValuePairs pairs = new AttrValuePairs();
 pairs.add("firstName", person.getFirstName());
 pairs.add("lastName", person.getLastName());
 xes.elementOpen("person", pairs.toArray());
 * </pre>
 */
public final class AttrValuePairs {

    private final List<AttrValuePair> vector = new ArrayList<AttrValuePair>();

    public void add(String name, String value) {
        AttrValuePair attr;
        // we used to escape the values here, but moved to AttrValuePair
        // constructor
        attr = new AttrValuePair(name, value);
        vector.add(attr);
    }

    public AttrValuePair[] toArray() {
        return vector.toArray(new AttrValuePair[vector.size()]);
    }
}
