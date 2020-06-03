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
package org.netbeans.modules.cnd.search.ui;

import java.lang.reflect.InvocationTargetException;
import org.netbeans.modules.cnd.search.SearchResult;
import org.openide.nodes.Node;
import org.openide.nodes.Node.Property;
import org.openide.nodes.PropertySupport;
import org.openide.util.NbBundle;

/**
 *
 */
public final class SearchResultPropertySet extends Node.PropertySet {

    private static final String[] namesAndDisplayNames = new String[]{
        NbBundle.getMessage(SearchResultPropertySet.class, "SearchResultPropertySet.column.path.name"), // NOI18N
        NbBundle.getMessage(SearchResultPropertySet.class, "SearchResultPropertySet.column.path.desc"), // NOI18N
        NbBundle.getMessage(SearchResultPropertySet.class, "SearchResultPropertySet.column.size.name"), // NOI18N
        NbBundle.getMessage(SearchResultPropertySet.class, "SearchResultPropertySet.column.size.desc"), // NOI18N
    };
    private final Property[] properties;
    private final SearchResult result;

    public SearchResultPropertySet(SearchResult result) {
        this.result = result;
        properties = new Property[]{
            new PathProperty(), new SizeProperty()
        };
    }

    public static String[] getNamesAndDisplayNames() {
        return namesAndDisplayNames;
    }

    @Override
    public Property<?>[] getProperties() {
        return properties;
    }

    private class PathProperty extends PropertySupport.ReadOnly<String> {

        public PathProperty() {
            super(namesAndDisplayNames[0], String.class, namesAndDisplayNames[1], namesAndDisplayNames[1]);
        }

        @Override
        public String getValue() throws IllegalAccessException, InvocationTargetException {
            return result.data.getPath();
        }
    }

    private class SizeProperty extends PropertySupport.ReadOnly<Integer> {

        public SizeProperty() {
            super(namesAndDisplayNames[2], Integer.class, namesAndDisplayNames[3], namesAndDisplayNames[3]);
        }

        @Override
        public Integer getValue() throws IllegalAccessException, InvocationTargetException {
            return result.data.getSize();
        }
    }
}
