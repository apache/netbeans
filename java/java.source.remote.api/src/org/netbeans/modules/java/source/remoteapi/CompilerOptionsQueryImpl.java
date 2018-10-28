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
package org.netbeans.modules.java.source.remoteapi;

import java.util.List;
import javax.swing.event.ChangeListener;
import org.netbeans.spi.java.queries.CompilerOptionsQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author lahvac
 */
@ServiceProvider(service=CompilerOptionsQueryImplementation.class, position=95)
public class CompilerOptionsQueryImpl implements CompilerOptionsQueryImplementation {

    public static final String KEY_COMPILER_OPTIONS = "compiler-options";

    @Override
    public Result getOptions(FileObject javaFile) {
        List<String> options = (List<String>) javaFile.getAttribute(KEY_COMPILER_OPTIONS);
        if (options == null) {
            return null;
        }
        return new Result() {
            @Override
            public List<? extends String> getArguments() {
                return options;
            }
            @Override
            public void addChangeListener(ChangeListener listener) {
            }

            @Override
            public void removeChangeListener(ChangeListener listener) {
            }
        };
    }
    
}
