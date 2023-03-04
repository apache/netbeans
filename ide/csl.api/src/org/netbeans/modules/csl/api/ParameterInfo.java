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
package org.netbeans.modules.csl.api;

import java.util.List;
import org.netbeans.api.annotations.common.NonNull;


/**
 *
 * @author Tor Norbye
 */
public class ParameterInfo {
    public static final ParameterInfo NONE = new ParameterInfo(null, -1, -1);
    private List<String> names;
    private int index;
    private int anchorOffset;

    public ParameterInfo(@NonNull List<String> names, int index, int anchorOffset) {
        this.names = names;
        this.index = index;
        this.anchorOffset = anchorOffset;
    }

    /** one list for each parameter; list contains of elements to be shown (e.g. type then name,
     * or just name, or just type, etc.)
     */
    @NonNull
    public List<String> getNames() {
        return names;
    }

    public int getCurrentIndex() {
        return index;
    }

    public int getAnchorOffset() {
        return anchorOffset;
    }
}
