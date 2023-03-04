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
package org.netbeans.modules.junit;

import java.util.Comparator;
import org.netbeans.api.project.libraries.Library;

/**
 * Comparator of JUnit libraries - compares versions of JUnit libraries.
 *
 * @author  Marian Petras
 */
final class JUnitLibraryComparator implements Comparator<Library> {

    public int compare(Library l1, Library l2) {
        String name1 = l1.getName().toLowerCase();
        String name2 = l2.getName().toLowerCase();

        if (name1.equals(name2)) {
            return 0;
        } else if (name1.equals("junit")) {                             //NOI18N
            return -1;
        } else if (name2.equals("junit")) {                             //NOI18N
            return 1;
        }

        final String[] parts1 = name1.substring(5).split("_|\\W");      //NOI18N
        final String[] parts2 = name2.substring(5).split("_|\\W");      //NOI18N
        final int min = Math.min(parts1.length, parts2.length);
        for (int i = 0; i < min; i++) {
            int partCmp = parts1[i].compareTo(parts2[i]);
            if (partCmp != 0) {
                return partCmp;
            }
        }
        return parts2.length - parts1.length;
    }

}
