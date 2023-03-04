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
package org.netbeans.modules.java.hints.spiimpl.batch;

import java.util.Collection;
import java.util.Collections;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.modules.java.hints.providers.spi.HintDescription.Worker;
import org.netbeans.modules.java.hints.providers.spi.Trigger.PatternDescription;
import org.netbeans.spi.java.hints.ErrorDescriptionFactory;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.modules.java.hints.providers.spi.HintDescriptionFactory;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.TestUtilities;
import org.netbeans.modules.java.hints.providers.spi.HintDescription;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

import static org.junit.Assert.*;
import org.netbeans.spi.java.hints.JavaFixUtilities;

/**
 *
 * @author lahvac
 */
public class TestUtils {

    public static void writeFiles(FileObject sourceRoot, File... files) throws Exception {
        for (FileObject c : sourceRoot.getChildren()) {
            c.delete();
        }

        for (File f : files) {
            FileObject fo = FileUtil.createData(sourceRoot, f.filename);
            TestUtilities.copyStringToFile(fo, f.content);
        }
    }

    public static void writeFilesAndWaitForScan(FileObject sourceRoot, File... files) throws Exception {
        writeFiles(sourceRoot, files);
        SourceUtils.waitScanFinished();
    }
    
    public static final class File {
        public final String filename;
        public final String content;
        public final boolean index;

        public File(String filename, String content) {
            this(filename, content, true);
        }

        public File(String filename, String content, boolean index) {
            this.filename = filename;
            this.content = content;
            this.index = index;
        }
    }

    public static Iterable<? extends HintDescription> prepareHints(String rule, String... constraints) {
        final String[] split = rule.split("=>");

        Worker w;

        if (split.length == 2) {
            w = new HintDescription.Worker() {
                @Override public Collection<? extends ErrorDescription> createErrors(HintContext ctx) {
                    return Collections.singletonList(ErrorDescriptionFactory.forName(ctx, ctx.getPath(), "", JavaFixUtilities.rewriteFix(ctx, "", ctx.getPath(), split[1])));
                }
            };
        } else {
            w = new HintDescription.Worker() {
                @Override public Collection<? extends ErrorDescription> createErrors(HintContext ctx) {
                    return Collections.singletonList(ErrorDescriptionFactory.forName(ctx, ctx.getPath(), ""));
                }
            };
        }

        assertTrue(constraints.length % 2 == 0);

        Map<String, String> constr = new HashMap<String, String>();

        for (int i = 0; i < constraints.length; i += 2) {
            constr.put(constraints[i], constraints[i + 1]);
        }

        HintDescription hd = HintDescriptionFactory.create()
                                                   .setTrigger(PatternDescription.create(split[0], constr))
                                                   .setWorker(w)
                                                   .produce();

        return Collections.singletonList(hd);
    }
}
