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
package org.netbeans.modules.java.openjdk.project;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.java.openjdk.common.BuildUtils;

import org.netbeans.spi.java.queries.SourceLevelQueryImplementation;
import org.openide.filesystems.FileObject;

/**
 *
 * @author lahvac
 */
public class SourceLevelQueryImpl implements SourceLevelQueryImplementation  {

    private static final Logger LOG = Logger.getLogger(SourceLevelQueryImpl.class.getName());
    private static final int DEFAULT_SOURCE_LEVEL = 11;
    private static final String[] SOURCE_VERSION_LOCATIONS = new String[] {
        "src/java.compiler/share/classes/javax/lang/model/SourceVersion.java",
        "open/src/java.compiler/share/classes/javax/lang/model/SourceVersion.java",
        "langtools/src/java.compiler/share/classes/javax/lang/model/SourceVersion.java"
    };

    private final String sourceLevel;

    public SourceLevelQueryImpl(FileObject jdkRoot) {
        Optional<FileObject> sourceVersionCandidate =
                Arrays.stream(SOURCE_VERSION_LOCATIONS)
                      .map(location -> BuildUtils.getFileObject(jdkRoot, location))
                      .filter(file -> file != null)
                      .findFirst();

        int sl = DEFAULT_SOURCE_LEVEL;

        if (sourceVersionCandidate.isPresent()) {
            try {
                TokenHierarchy<String> th =
                        TokenHierarchy.create(sourceVersionCandidate.get().asText(), JavaTokenId.language());
                TokenSequence<?> seq = th.tokenSequence();

                while (seq.moveNext()) {
                    if (seq.token().id() == JavaTokenId.IDENTIFIER) {
                        String ident = seq.token().text().toString();

                        if (ident.startsWith("RELEASE_")) {
                            try {
                                sl = Math.max(sl, Integer.parseInt(ident.substring("RELEASE_".length())));
                            } catch (NumberFormatException ex) {
                                //ignore
                            }
                        }
                    }
                }
            } catch (IOException ex) {
                LOG.log(Level.FINE, null, ex);
            }
        }

        this.sourceLevel = "" + sl;
    }


    @Override
    public String getSourceLevel(FileObject javaFile) {
        return sourceLevel;
    }
    
}
