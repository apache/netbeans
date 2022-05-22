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
package org.netbeans.modules.java.openjdk.jtreg;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.swing.event.ChangeListener;
import org.netbeans.spi.java.queries.CompilerOptionsQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service=CompilerOptionsQueryImplementation.class, position=9999)
public class CompilerOptionsQueryImpl implements CompilerOptionsQueryImplementation {

    @Override
    public Result getOptions(FileObject file) {
        TestRootDescription rootDesc = TestRootDescription.findRootDescriptionFor(file);

        if (rootDesc == null) {
            return null;
        }

        //enable preview in tests:
        return ENABLE_PREVIEW;
    }

    private static final Result ENABLE_PREVIEW = new EnablePreviewResult();

    private static final class EnablePreviewResult extends Result {

        private static final List<String> ENABLE_PREVIEW_ARGS =
                Collections.unmodifiableList(Arrays.asList("--enable-preview"));

        @Override
        public List<? extends String> getArguments() {
            return ENABLE_PREVIEW_ARGS;
        }

        @Override
        public void addChangeListener(ChangeListener listener) {}

        @Override
        public void removeChangeListener(ChangeListener listener) {}

    }
}
