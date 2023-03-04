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

package org.netbeans.modules.bugtracking.ui.selectors;

import java.util.Comparator;
import org.netbeans.modules.bugtracking.DelegatingConnector;

/**
 *
 * @author Tomas Stupka
 */
public class ConnectorComparator implements Comparator<DelegatingConnector>{

    @Override
    public int compare(DelegatingConnector c1, DelegatingConnector c2) {
        if(c1 == null && c2 == null) {
            return 0;
        }
        if(c2 == null) {
            return 1;
        }
        if(c1 == null) {
            return -1;
        }
        return c1.getDisplayName().compareTo(c2.getDisplayName());
    }

}
