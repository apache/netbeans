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
package org.netbeans.modules.versioning.ui.history;

/**
 *
 * @author Tomas Stupka
 */
public abstract class TableEntry implements Comparable<TableEntry> {
    public abstract String getDisplayValue();
    public abstract String getTooltip();
    public Integer order() {
        return 1;
    }
    @Override
    public int compareTo(TableEntry e) {
        String d1 = getDisplayValue();
        String d2 = e != null ? e.getDisplayValue() : "";
        if(d1 == null) d1 = "";
        if(d2 == null) d2 = "";
        return d1.compareTo(d2);                 
    }

    @Override
    public String toString() {
        return getDisplayValue();
    }
    
}
