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

package org.netbeans.modules.debugger.jpda.expr.formatters;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Martin Entlicher
 */
public class FormattersLoopControl {
    
    private final Map<String, VariablesFormatter> usedFormatters;

    public FormattersLoopControl() {
        usedFormatters = new LinkedHashMap<String, VariablesFormatter>();
    }

    public VariablesFormatter[] getFormatters() {
        return Formatters.getDefault().getFormatters();
    }

    public boolean canUse(VariablesFormatter f, String type, String[] formattersInLoopRef) {
        boolean can = usedFormatters.put(type, f) == null;
        if (!can && formattersInLoopRef != null && !String.class.getName().equals(type)) {
            List<String> names = new ArrayList<String>(usedFormatters.size());
            for (Map.Entry<String, VariablesFormatter> vf : usedFormatters.entrySet()) {
                names.add(vf.getValue().getName()+" ("+vf.getKey()+")");
            }
            formattersInLoopRef[0] = names.toString();
        }
        return can;
    }
}
