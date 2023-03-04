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

package org.netbeans.modules.db.metadata.model.test.api;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.db.metadata.model.api.MetadataElement;

/**
 *
 * @author Andrei Badea
 */
public class MetadataTestBase extends NbTestCase {

    public MetadataTestBase(String name) {
        super(name);
    }

    public static <T extends MetadataElement> void assertNames(Set<String> names, Collection<T> elements) {
        Set<String> computedNames = new HashSet<String>();
        for (MetadataElement element : elements) {
            computedNames.add(element.getName());
        }
        assertEquals(names, computedNames);
    }

    public static <T extends MetadataElement> void assertNames(List<String> names, Collection<T> elements) {
        assertEquals(names.size(), elements.size());
        int i = 0;
        for (MetadataElement element : elements) {
            assertEquals(names.get(i++), element.getName());
        }
    }
}
