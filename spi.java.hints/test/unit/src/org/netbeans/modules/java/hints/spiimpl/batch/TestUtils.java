/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
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
