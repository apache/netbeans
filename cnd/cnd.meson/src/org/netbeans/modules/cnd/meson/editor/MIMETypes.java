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

package org.netbeans.modules.cnd.meson.editor;

import org.openide.filesystems.MIMEResolver;
import org.openide.util.NbBundle;

@NbBundle.Messages({
    "LBL_MESON_BUILD_MIME_RESOLVER=Meson MIME Resolver"
})
@MIMEResolver.Registration(
    displayName = "#LBL_MESON_BUILD_MIME_RESOLVER",
    position = 139, // anything > 140 causes meson_options.txt to not be recognized
    resource = "../resources/mime_resolver.xml"
)
public class MIMETypes {
    public static final String MESON_BUILD = "text/x-meson-build"; //NOI18N
    public static final String MESON_OPTIONS = "text/x-meson-options"; //NOI18N
}