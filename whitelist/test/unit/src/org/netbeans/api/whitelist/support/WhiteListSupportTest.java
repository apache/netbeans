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
package org.netbeans.api.whitelist.support;

import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.concurrent.Callable;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.whitelist.WhiteListQuery;
import org.netbeans.api.whitelist.WhiteListQuery.Operation;
import org.netbeans.api.whitelist.WhiteListQuery.Result;
import org.netbeans.api.whitelist.WhiteListQuery.WhiteList;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.source.parsing.JavacParser;
import org.netbeans.modules.java.source.parsing.JavacParserFactory;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.Task;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.spi.whitelist.WhiteListQueryImplementation;
import org.netbeans.spi.whitelist.WhiteListQueryImplementation.WhiteListImplementation;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Tomas Zezula
 */
public class WhiteListSupportTest extends NbTestCase {

    public WhiteListSupportTest(final String name) {
        super(name);
    }

    public void testGetWhiteListViolationsCancelable() throws Exception {
        MockServices.setServices(AllAllowedWLQuery.class);
        final File wd = getWorkDir();
        final File root = new File (wd,"src");   //NOI18N
        root.mkdirs();
        final FileObject file = createFile (
                FileUtil.toFileObject(root),
                "org.me.Test",  //NOI18N
                "public class Test { public void a(){} public void b(){}}");    //NOI18N
        final WhiteList wl = WhiteListQuery.getWhiteList(file);
        assertNotNull(wl);
        final Source src = Source.create(file);
        final Snapshot snapshot = src.createSnapshot();
        final JavacParser jp = new JavacParserFactory().createPrivateParser(snapshot);
        final Task task = new UserTask() {
            @Override
            public void run(ResultIterator resultIterator) throws Exception {
            }
        };
        jp.parse(snapshot, task, null);
        Parser.Result result = jp.getResult(task);
        final CompilationController cc = CompilationController.get(result);
        cc.toPhase(Phase.PARSED);
        final Cancel cancel = new Cancel();
        //Not canceled - should return Map
        assertNotNull(WhiteListSupport.getWhiteListViolations(
                cc.getCompilationUnit(),
                wl,
                cc.getTrees(),
                cancel));
        //Canceled before run - should return null
        cancel.cancel = Boolean.TRUE;
        assertNull(WhiteListSupport.getWhiteListViolations(
                cc.getCompilationUnit(),
                wl,
                cc.getTrees(),
                cancel));
        //Canceled buring tree scan - should return null
        cancel.cancel = Boolean.FALSE;
        final Logger log = Logger.getLogger(WhiteListSupport.class.getName());
        log.setLevel(Level.FINEST);
        log.addHandler(new Handler() {
            @Override
            public void publish(LogRecord record) {
                if ("Visiting {0}".equals(record.getMessage())) {   //NOI18N
                    final Object[] vals = record.getParameters();
                    assert vals.length == 1;
                    assert vals[0] instanceof Tree;
                    if (((Tree)vals[0]).getKind() == Tree.Kind.METHOD) {
                        if ("a".contentEquals(((MethodTree)vals[0]).getName())) {
                            //Cancel during scan
                            cancel.cancel = Boolean.TRUE;
                        }
                    }
                }
            }
            @Override
            public void flush() {
            }
            @Override
            public void close() throws SecurityException {
            }
        });
        assertNull(WhiteListSupport.getWhiteListViolations(
                cc.getCompilationUnit(),
                wl,
                cc.getTrees(),
                cancel));
    }


    private static FileObject createFile (
            final FileObject root,
            final String fqn,
            final String content) throws IOException {
        final FileObject file = FileUtil.createData(
                root,
                String.format("%s.java", fqn.replace('.', '/'))); //NOI18N
        final FileLock lck = file.lock();
        try {
            final PrintWriter out = new PrintWriter(new OutputStreamWriter(file.getOutputStream(lck)));
            try {
                out.print(content);
            } finally {
                out.close();
            }
        } finally {
            lck.releaseLock();
        }
        return file;
    }

    private static class Cancel implements Callable<Boolean> {
        private volatile Boolean cancel = Boolean.FALSE;
        @Override
        public Boolean call() throws Exception {
            return cancel;
        }
    }

    private static class AllAllowedWL implements WhiteListImplementation {
        @Override
        public Result check(ElementHandle<?> element, Operation operation) {
            return new Result();
        }

        @Override
        public void addChangeListener(ChangeListener listener) {
        }

        @Override
        public void removeChangeListener(ChangeListener listener) {
        }
    }

    public static class AllAllowedWLQuery implements WhiteListQueryImplementation {
        @Override
        public WhiteListImplementation getWhiteList(FileObject file) {
            return new AllAllowedWL();
        }
    }
}
