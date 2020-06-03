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
package org.netbeans.modules.cnd.toolchain.ui.impl;

import org.netbeans.modules.cnd.spi.toolchain.ErrorParserProvider;
import org.netbeans.modules.cnd.spi.toolchain.OutputListenerExt;
import org.netbeans.modules.cnd.spi.toolchain.OutputListenerProvider;
import org.openide.filesystems.FileObject;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.IOPosition;

/**
 *
 */
@ServiceProvider (service = OutputListenerProvider.class, position = 100)
public class OutputListenerProviderImpl extends OutputListenerProvider{

    @Override
    public OutputListenerExt get(ErrorParserProvider.OutputListenerRegistry registry, FileObject file, int line, boolean isError, String description, IOPosition.Position ioPos) {
        return new OutputListenerImpl(registry, file, line, isError, description, ioPos);
    }

    @Override
    public void attach(ErrorParserProvider.OutputListenerRegistry registry) {
        OutputListenerImpl.attach(registry);
    }
    
}
