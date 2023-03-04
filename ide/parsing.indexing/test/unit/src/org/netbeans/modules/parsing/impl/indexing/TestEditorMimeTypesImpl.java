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
package org.netbeans.modules.parsing.impl.indexing;

import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.modules.parsing.spi.indexing.support.EmbeddedPathRecognizer;
import org.netbeans.spi.editor.document.EditorMimeTypesImplementation;

/**
 *
 * @author Tomas Zezula
 */
public class TestEditorMimeTypesImpl implements EditorMimeTypesImplementation {

    private static final Set<String> TEST_MIME_TYPES =
            new HashSet<>(Arrays.asList(new String[]{
                "text/x-java",
                "text/x-foo",
                "text/x-top",
                "text/x-inner",
                "text/foo",
                "text/emb",
                "text/plain",
                EmbeddedPathRecognizer.EMB_MIME,
                FooPathRecognizer.FOO_MIME
    }));

    @Override
    public Set<String> getSupportedMimeTypes() {
        return Collections.unmodifiableSet(TEST_MIME_TYPES);
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
    }
}
