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
package org.netbeans.modules.openide.loaders.data;

import org.openide.filesystems.MIMEResolver;

/**
 *
 * @author Eric Barboni <skygo@netbeans.org>
 */
public class DoFPMIMEType {

    @MIMEResolver.ExtensionRegistration(displayName = "tt1", extension = "tt1", mimeType = "text/test1",position=1000)
    public void tt1() {
    }

    @MIMEResolver.ExtensionRegistration(displayName = "tt3", extension = "tt3", mimeType = "text/test3",position=1002)
    public void tt3() {
    }

    @MIMEResolver.ExtensionRegistration(displayName = "ttm1", extension = "ttm1", mimeType = "text/testm1",position=1003)
    public void ttm1() {
    }

    @MIMEResolver.ExtensionRegistration(displayName = "ttm2", extension = "ttm2", mimeType = "text/testm2",position=1004)
    public void ttm2() {
    }
}
