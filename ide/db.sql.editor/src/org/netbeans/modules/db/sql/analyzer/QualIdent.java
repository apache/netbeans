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

package org.netbeans.modules.db.sql.analyzer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author Andrei Badea
 */
public class QualIdent implements Comparable<QualIdent> {

    private final List<String> parts;

    public QualIdent(String... parts) {
        this.parts = Arrays.asList(parts);
    }

    public QualIdent(List<String> parts) {
        this.parts = new ArrayList<String>(parts);
    }

    public QualIdent(QualIdent prefix, String name) {
        parts = new ArrayList<String>(prefix.parts.size() + 1);
        parts.addAll(prefix.parts);
        parts.add(name);
    }

    public QualIdent(String prefix, QualIdent name) {
        parts = new ArrayList<String>(name.parts.size() + 1);
        parts.add(prefix);
        parts.addAll(name.parts);
    }

    private QualIdent(List<String> parts, int start, int end) {
        this.parts = parts.subList(start, end);
    }

    public String getFirstQualifier() {
        if (parts.size() == 0) {
            throw new IllegalArgumentException("The identifier is empty.");
        }
        return parts.get(0);
    }

    public String getSecondQualifier() {
        if (parts.size() < 2) {
            throw new IllegalArgumentException("The identifier is empty or simple.");
        }
        return parts.get(1);
    }

    public String getSimpleName() {
        if (parts.size() == 0) {
            throw new IllegalArgumentException("The identifier is empty.");
        }
        return parts.get(parts.size() - 1);
    }

    public QualIdent getPrefix() {
        if (parts.size() == 0) {
            throw new IllegalArgumentException("The identifier is empty");
        }
        return new QualIdent(parts, 0, parts.size() - 1);
    }

    public boolean isEmpty() {
        return size() == 0;
    }

    public boolean isSimple() {
        return size() == 1;
    }

    public boolean isSingleQualified() {
        return size() == 2;
    }

    public int size() {
        return parts.size();
    }

    public boolean isPrefixedBy(QualIdent prefix) {
        if (this.size() < prefix.size()) {
            return false;
        }
        for (int i = 0; i < prefix.size(); i++) {
            if (!this.parts.get(i).equals(prefix.parts.get(i))) {
                return false;
            }
        }
        return true;
    }

    public int compareTo(QualIdent that) {
        for (int i = 0; ; i++) {
            if (i < this.parts.size()) {
                if (i < that.parts.size()) {
                    int compare = this.parts.get(i).compareToIgnoreCase(that.parts.get(i));
                    if (compare != 0) {
                        return compare;
                    }
                } else {
                    return 1;
                }
            } else {
                if (i < that.parts.size()) {
                    return -1;
                } else {
                    return 0;
                }
            }
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof QualIdent)) {
            return false;
        }
        QualIdent that = (QualIdent) obj;
        return that.parts.equals(this.parts);
    }

    @Override
    public int hashCode() {
        return parts.hashCode();
    }

    @Override
    public String toString() {
        if (parts.size() == 0) {
            return "<empty>"; // NOI18N
        }
        StringBuilder result = new StringBuilder(parts.size() * 10);
        Iterator<String> i = parts.iterator();
        while (i.hasNext()) {
            result.append(i.next());
            if (i.hasNext()) {
                result.append('.');
            }
        }
        return result.toString();
    }
}
