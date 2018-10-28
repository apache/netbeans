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

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import javax.swing.event.ChangeListener;
import javax.tools.ToolProvider;
import org.netbeans.spi.java.queries.CompilerOptionsQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author lahvac
 */
@ServiceProvider(service=CompilerOptionsQueryImplementation.class)
public class CompilerOptionsQueryImpl implements CompilerOptionsQueryImplementation {

    @Override
    public Result getOptions(FileObject file) {
        if (!file.isData()) {
            return null;
        }

        FileObject search = file.getParent();

        while (search != null) {
            FileObject testRoot = search.getFileObject("TEST.ROOT");
            
            if (testRoot != null) {
                try {
                    String text = Utilities.fileContent(file);
                    Pattern compile = Pattern.compile("@compile.*--enable-preview.*\n");

                    if (compile.matcher(text).find()) {
                        return new Result() {
                            @Override
                            public List<? extends String> getArguments() {
                                return Arrays.asList("--enable-preview");
                            }

                            @Override
                            public void addChangeListener(ChangeListener listener) {}

                            @Override
                            public void removeChangeListener(ChangeListener listener) {}
                        };
                    }
                } catch (IOException ex) {
                    return null;
                }
            }

            search = search.getParent();
        }
        
        return null;
    }
    
}
