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

package org.netbeans.modules.debugger.jpda.truffle.mime;

import org.openide.filesystems.MIMEResolver;

/**
 * Inform the IDE of MIME types of files interpreted by Truffle/GraalVM. This is
 * necessary for the files to be recognized for breakpoints submission, etc.
 * based on the MIME type.
 */
public class LanguageResolvers {
    
    @MIMEResolver.ExtensionRegistration(displayName = "Simple Language", extension = { "sl", "SL" }, mimeType = "application/x-r", position = 2140000100)
    public void SimpleLanguage() {}
    
    @MIMEResolver.ExtensionRegistration(displayName = "R", extension = { "r", "R" }, mimeType = "application/x-r", position = 2140000200)
    public void R() {}

    @MIMEResolver.ExtensionRegistration(displayName = "Ruby", extension = { "rb", "RB", "Rb" }, mimeType = "application/x-ruby", position = 2140000300)
    public void Ruby() {}

    @MIMEResolver.ExtensionRegistration(displayName = "Python", extension = { "py", "PY", "Py" }, mimeType = "text/x-python", position = 2140000400)
    public void Python() {}

}
