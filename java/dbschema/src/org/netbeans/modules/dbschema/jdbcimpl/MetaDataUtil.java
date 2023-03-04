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

package org.netbeans.modules.dbschema.jdbcimpl;

import java.util.LinkedList;
import java.util.List;

public class MetaDataUtil {

    public static boolean areViewsSupported(String productName) {
        List<String> list = new LinkedList<>();

        list.add("PointBase"); // NOI18N
        list.add("MySQL"); // NOI18N
        list.add("HypersonicSQL"); // NOI18N

        if (list.contains(productName.trim()))
            return false;
        else
            return true;
    }
}
