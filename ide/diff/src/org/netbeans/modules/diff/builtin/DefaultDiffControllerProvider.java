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
package org.netbeans.modules.diff.builtin;

import org.netbeans.spi.diff.DiffControllerProvider;
import org.netbeans.spi.diff.DiffControllerImpl;
import org.netbeans.api.diff.StreamSource;
import org.netbeans.modules.diff.builtin.visualizer.editable.EditableDiffView;

import java.io.IOException;

/**
 * Default implementation of DiffControllerProvider.
 * 
 * @author Maros Sandor
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.spi.diff.DiffControllerProvider.class)
public class DefaultDiffControllerProvider extends DiffControllerProvider {

    public DiffControllerImpl createDiffController(StreamSource base, StreamSource modified) throws IOException {
        return new EditableDiffView(base, modified);
    }

    @Override
    public DiffControllerImpl createEnhancedDiffController(StreamSource base, StreamSource modified) throws IOException {
        if (Boolean.getBoolean("netbeans.diff.default.compact")) {
            return createDiffController(base, modified);
        }
        return new EditableDiffView(base, modified, true);
    }
}
