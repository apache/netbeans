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
