/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.api.makefile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.text.Document;
import org.netbeans.modules.cnd.makefile.MakefileApiAccessor;
import org.netbeans.modules.cnd.makefile.parser.MakefileParseResult;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser;
import org.openide.filesystems.FileObject;

/**
 */
public class MakefileSupport {

    private MakefileSupport() {
    }

    static {
        MakefileApiAccessor.setInstance(new MakefileApiAccessorImpl());
    }

    /**
     * Parses given makefile and returns a list of its top-level elements.
     *
     * <p>Don't call from EDT.
     *
     * @param fileObject  valid <code>text/x-make</code> file
     * @return list of makefile's top-level elements
     * @throws NullPointerException if <code>fileObject</code> is <code>null</code>
     * @throws IllegalArgumentException if <code>fileObject</code> is not
     *      a valid <code>text/x-make</code> file
     * @throws ParseException if any other error happens during parsing
     */
    public static List<MakefileElement> parseFile(FileObject fileObject) throws ParseException {
        Source source = Source.create(fileObject);
        if (source == null) {
            new IllegalArgumentException("Valid file expected, got " + fileObject).printStackTrace(System.err); // NOI18N
            return Collections.emptyList();
        } else {
            return parse(source);
        }
    }

    /**
     * Parses given makefile and returns a list of its top-level elements.
     *
     * <p>Don't call from EDT.
     *
     * @param doc  <code>text/x-make</code> document
     * @return list of makefile's top-level elements
     * @throws NullPointerException if <code>doc</code> is <code>null</code>
     * @throws IllegalArgumentException if <code>doc</code>'s MIME type
     *      is not <code>text/x-make</code>
     * @throws ParseException if any other error happens during parsing
     */
    public static List<MakefileElement> parseDocument(Document doc) throws ParseException {
        Source source = Source.create(doc);
        if (source == null) {
            throw new IllegalArgumentException("Valid document expected, got " + doc); // NOI18N
        } else {
            return parse(source);
        }
    }

    private static List<MakefileElement> parse(Source source) throws ParseException {
        if (!MIMENames.MAKEFILE_MIME_TYPE.equals(source.getMimeType())) {
            throw new IllegalArgumentException("Makefile source expected, got " + source); // NOI18N
        }
        List<MakefileElement> result = new ArrayList<MakefileElement>(1);
        ParseTask task = new ParseTask(result);
        ParserManager.parse(Collections.singleton(source), task);
        return result;
    }

    private static final class ParseTask extends UserTask {

        private final List<MakefileElement> elements;

        public ParseTask(List<MakefileElement> elements) {
            this.elements = elements;
        }

        @Override
        public void run(ResultIterator resultIterator) throws Exception {
            Parser.Result result = resultIterator.getParserResult();
            if (result instanceof MakefileParseResult) {
                MakefileParseResult makefileResult = (MakefileParseResult) result;
                elements.addAll(makefileResult.getElements());
            }
        }
    }
}
