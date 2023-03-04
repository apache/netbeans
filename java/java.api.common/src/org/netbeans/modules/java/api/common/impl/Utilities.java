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
package org.netbeans.modules.java.api.common.impl;

import java.net.URL;
import org.netbeans.api.annotations.common.NonNull;

/**
 *
 * @author Tomas Zezula
 */
public class Utilities {
    private Utilities() {
        throw new IllegalStateException("No instance allowed.");    //NOI18N
    }

    public static boolean isParentOf(
            @NonNull final URL folder,
            @NonNull final URL file) {
        String sfld = folder.toExternalForm();
        if (sfld.charAt(sfld.length()-1) != '/') {  //NOI18N
            sfld = sfld + '/';                      //NOI18N
        }
        final String sfil = file.toExternalForm();
        return sfil.startsWith(sfld);
    }
}
