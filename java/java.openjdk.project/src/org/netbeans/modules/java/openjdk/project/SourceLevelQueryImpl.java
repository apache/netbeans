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
package org.netbeans.modules.java.openjdk.project;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
    private static final Pattern JDK_PATTERN = Pattern.compile("jdk([0-9]+)");

    private final String sourceLevel;

    public SourceLevelQueryImpl(FileObject jdkRoot) {
        FileObject sourceVersion = BuildUtils.getFileObject(jdkRoot, "src/java.compiler/share/classes/javax/lang/model/SourceVersion.java");
        int sl = DEFAULT_SOURCE_LEVEL;

        if (sourceVersion == null) {
            sourceVersion = BuildUtils.getFileObject(jdkRoot, "langtools/src/java.compiler/share/classes/javax/lang/model/SourceVersion.java");
        }
        if (sourceVersion != null) {
            try {
                TokenHierarchy<String> th =
                        TokenHierarchy.create(sourceVersion.asText(), JavaTokenId.language());
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
