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
package org.netbeans.modules.web.jsf.editor.facelets;

import java.util.Comparator;

/**
 *
 * @author Benjamin Asbach
 */
public class JsfNamespaceComparator implements Comparator<String> {

    private static final JsfNamespaceComparator INSTANCE = new JsfNamespaceComparator();

    private JsfNamespaceComparator() {
    }

    public static JsfNamespaceComparator getInstance() {
        return INSTANCE;
    }

    @Override
    public int compare(String namespace1, String namespace2) {
        int prefixRating = rate(namespace1).compareTo(rate(namespace2));
        if (prefixRating != 0) {
            return prefixRating;
        }

        return namespace1.compareTo(namespace2);
    }

    private Integer rate(String namespace) {
        if (namespace.startsWith("jakarta")) {
            return 1;
        } else if (namespace.startsWith("http://xmlns.jcp.org")) {
            return 2;
        } else if (namespace.startsWith("http://java.sun.com")) {
            return 3;
        } else {
            return 4;
        }
    }
}
