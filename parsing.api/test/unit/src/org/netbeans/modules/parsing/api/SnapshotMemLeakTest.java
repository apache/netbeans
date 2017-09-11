/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */
package org.netbeans.modules.parsing.api;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.mimelookup.test.MockMimeLookup;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.parsing.spi.EmbeddingProvider;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.parsing.spi.ParserFactory;
import org.netbeans.modules.parsing.spi.SchedulerTask;
import org.netbeans.modules.parsing.spi.SourceModificationEvent;
import org.netbeans.modules.parsing.spi.TaskFactory;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Tomas Zezula
 */
public class SnapshotMemLeakTest extends NbTestCase {

    private static final char[] CONTENT = {'0','1','2','3','4','5','6','7','8','9'}; //NOI18N
    private static final int REP = 3;
    private static final String FOO_EXT = "foo";    //NOI18N
    private static final String FOO_MIME = "text/x-foo";    //NOI18N

    private static int embCount;

    public SnapshotMemLeakTest(final String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
        MockMimeLookup.setInstances(MimePath.get(FOO_MIME), new FooParser.Factory(), new FooEmbeddingProvider.Factory());
        FileUtil.setMIMEType(FOO_EXT, FOO_MIME);
    }

    @Override
    protected void tearDown() throws Exception {
        clearWorkDir(); //Delete 30M file
        super.tearDown();
    }

    public void testSnapshotMemLeak() throws Exception {
        final FileObject file = generateFile(1<<20);
        Source src = Source.create(file);
        final Snapshot s = src.createSnapshot();
        ParserManager.parse(Collections.singleton(src), new UserTask() {
            @Override
            public void run(ResultIterator resultIterator) throws Exception {
                int count = 0;
                final StringBuilder patternBuilder = new StringBuilder();
                for (int i = 0; i < REP; i++) {
                    patternBuilder.append(CONTENT);
                }
                final String pattern = patternBuilder.toString();
                for (Embedding e : resultIterator.getEmbeddings()) {
                    assertTrue(pattern.contentEquals(e.getSnapshot().getText()));
                    count++;
                }
                assertEquals(embCount, count);
            }
        });
    }

    public void testCharSequenceView() throws Exception {
        final FileObject file = generateFile(100);
        Source src = Source.create(file);
        final Snapshot snapshot = src.createSnapshot();
        final String str = snapshot.getText().toString();
        final CharSequence seq = snapshot.create(0, str.length(), FOO_MIME).getSnapshot().getText();
        final CharSequence seq_dup = snapshot.create(0, str.length(), FOO_MIME).getSnapshot().getText();

        assertEquals(str.length(), seq.length());
        assertTrue(str.contentEquals(seq));
        assertEquals(str, seq.toString());

        final CharSequence subSeq = seq.subSequence(10, 100);
        final String subStr = str.substring(10,100);
        assertEquals(subStr.length(), subSeq.length());
        assertTrue(subStr.contentEquals(subSeq));
        assertEquals(subStr, subSeq.toString());

        final CharSequence subSubSeq = seq.subSequence(10, 20);
        final String subSubStr = str.substring(10,20);
        assertEquals(subSubStr.length(), subSubSeq.length());
        assertTrue(subSubStr.contentEquals(subSubSeq));
        assertEquals(subSubStr, subSubSeq.toString());

        assertEquals(seq, seq_dup);
        assertFalse(seq.equals(subSeq));
        assertFalse(subSeq.equals(subSubSeq));
    }

    private static final class FooParser extends Parser {

        private Snapshot snapshot;

        @Override
        public void parse(Snapshot snapshot, Task task, SourceModificationEvent event) throws ParseException {
            this.snapshot = snapshot;
        }

        @Override
        public Result getResult(Task task) throws ParseException {
            assert this.snapshot != null;
            return new FooResult(snapshot);
        }

        @Override
        public void addChangeListener(ChangeListener changeListener) {
        }

        @Override
        public void removeChangeListener(ChangeListener changeListener) {
        }

        private static final class Factory extends ParserFactory {
            @Override
            public Parser createParser(Collection<Snapshot> snapshots) {
                return new FooParser();
            }
        }

        private static final class FooResult extends Result {
            FooResult(@NonNull final Snapshot snapshot) {
                super(snapshot);
            }

            @Override
            protected void invalidate() {
            }
        }
    }

    private static final class FooEmbeddingProvider extends EmbeddingProvider {

        @Override
        public List<Embedding> getEmbeddings(Snapshot snapshot) {
            final List<Embedding> result = new ArrayList<>();
            int start = 0, end = 0;
            final CharSequence txt = snapshot.getText();
            while (end < txt.length()) {
                if (end > start && end % (REP * CONTENT.length) == 0) {
                    final Embedding emb = snapshot.create(start, end-start, FOO_MIME);
                    result.add(emb);
                    start = end;
                }
                end++;
            }
            embCount = result.size();
            return result;
        }

        @Override
        public int getPriority() {
            return 1;
        }

        @Override
        public void cancel() {
        }

        private static final class Factory extends TaskFactory {
            @Override
            public Collection<? extends SchedulerTask> create(Snapshot snapshot) {
                return Collections.singleton(new FooEmbeddingProvider());
            }
        }
    }

    private FileObject generateFile(final int rounds) throws IOException {
        final FileObject wd = FileUtil.toFileObject(FileUtil.normalizeFile(getWorkDir()));
        final FileObject file = FileUtil.createData(wd, "test.foo");    //NOI18N
        try (BufferedWriter out = new BufferedWriter(new OutputStreamWriter(file.getOutputStream(),"UTF-8"))) { //NOI18N
            for (int i = 0; i< rounds; i++) {
                out.write(CONTENT);
                out.write(CONTENT);
                out.write(CONTENT);
            }
        }
        return file;
    }
}
