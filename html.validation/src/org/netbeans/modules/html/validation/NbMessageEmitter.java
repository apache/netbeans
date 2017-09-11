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

package org.netbeans.modules.html.validation;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import nu.validator.htmlparser.sax.HtmlSerializer;
import nu.validator.messages.MessageEmitter;
import nu.validator.messages.MessageTextHandler;
import nu.validator.messages.ResultHandler;
import nu.validator.messages.TextMessageTextHandler;
import nu.validator.messages.types.MessageType;
import nu.validator.source.SourceHandler;
import org.netbeans.modules.html.editor.lib.api.ProblemDescription;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;

import org.xml.sax.SAXException;

/**
 * This is netbeans implementation of MessageEmitter
 */
public class NbMessageEmitter extends MessageEmitter {

    private static final char[] COLON_SPACE = { ':', ' ' };

    private static final char[] PERIOD = { '.' };

    private static final char[] ON_LINE = "On line ".toCharArray();

    private static final char[] AT_LINE = "At line ".toCharArray();

    private static final char[] FROM_LINE = "From line ".toCharArray();

    private static final char[] TO_LINE = "; to line ".toCharArray();

    private static final char[] COLUMN = ", column ".toCharArray();

    private static final char[] IN_RESOURCE = " in resource ".toCharArray();

    private TextMessageTextHandler messageTextHandler;

    private String systemId;

    private int oneBasedFirstLine;

    private int oneBasedFirstColumn;

    private int oneBasedLastLine;

    private int oneBasedLastColumn;

    private boolean textEmitted;

    private HtmlSerializer contentHandler;

    private ProblemsHandler problemsHandler;

    private int problemType;

    private LinesMapper linesMapper;
    
    private Writer writer;

    private boolean asciiQuotes;

    private ContentHandler elaborationContentHandler;

//    private static Writer newOutputStreamWriter(OutputStream out) {
//        CharsetEncoder enc = Charset.forName("UTF-8").newEncoder();
//        enc.onMalformedInput(CodingErrorAction.REPLACE);
//        enc.onUnmappableCharacter(CodingErrorAction.REPLACE);
//        return new OutputStreamWriter(out, enc);
//    }

    public NbMessageEmitter(ProblemsHandler problemsHandler, LinesMapper linesMapper, boolean asciiQuotes) {
        this.problemsHandler = problemsHandler;
        this.linesMapper = linesMapper;
        this.asciiQuotes = asciiQuotes;
    }

    private void emitErrorLevel(char[] level) throws IOException {
        writer.write(level, 0, level.length);
    }

    private void maybeEmitLocation() throws IOException {
        if (oneBasedLastLine == -1 && systemId == null) {
            return;
        }
        if (oneBasedLastLine == -1) {
            emitSystemId();
        } else if (oneBasedLastColumn == -1) {
            emitLineLocation();
        } else if (oneBasedFirstLine == -1
                || (oneBasedFirstLine == oneBasedLastLine && oneBasedFirstColumn == oneBasedLastColumn)) {
            emitSingleLocation();
        } else {
            emitRangeLocation();
        }
        writer.write('\n');
    }

    /**
     * @throws SAXException
     */
    private void maybeEmitInResource() throws IOException {
        if (systemId != null) {
            this.writer.write(IN_RESOURCE);
            emitSystemId();
        }
    }

    /**
     * @throws SAXException
     */
    private void emitSystemId() throws IOException {
        this.writer.write(systemId);
    }

    private void emitRangeLocation() throws IOException {
        this.writer.write(FROM_LINE);
        this.writer.write(Integer.toString(oneBasedFirstLine));
        this.writer.write(COLUMN);
        this.writer.write(Integer.toString(oneBasedFirstColumn));
        this.writer.write(TO_LINE);
        this.writer.write(Integer.toString(oneBasedLastLine));
        this.writer.write(COLUMN);
        this.writer.write(Integer.toString(oneBasedLastColumn));
        maybeEmitInResource();
    }

    private void emitSingleLocation() throws IOException {
        this.writer.write(AT_LINE);
        this.writer.write(Integer.toString(oneBasedLastLine));
        this.writer.write(COLUMN);
        this.writer.write(Integer.toString(oneBasedLastColumn));
        maybeEmitInResource();
    }

    private void emitLineLocation() throws IOException {
        this.writer.write(ON_LINE);
        this.writer.write(Integer.toString(oneBasedLastLine));
        maybeEmitInResource();
    }

    @Override
    public void startMessage(MessageType type, String systemId,
            int oneBasedFirstLine, int oneBasedFirstColumn,
            int oneBasedLastLine, int oneBasedLastColumn, boolean exact)
            throws SAXException {

        this.writer = new StringWriter();
        this.elaborationContentHandler = new SimplifiedMessagesContentHandler(writer);
        this.messageTextHandler = new TextMessageTextHandler(writer, asciiQuotes);
        this.contentHandler = new HtmlSerializer(writer);


        this.systemId = systemId;
        this.oneBasedFirstLine = oneBasedFirstLine;
        this.oneBasedFirstColumn = oneBasedFirstColumn;
        this.oneBasedLastLine = oneBasedLastLine;
        this.oneBasedLastColumn = oneBasedLastColumn;
        try {
            emitErrorLevel(type.getPresentationName());
        } catch (IOException e) {
            throw new SAXException(e.getMessage(), e);
        }
        this.textEmitted = false;

        problemType = messageTypeToProblemType(type);
    }

    private static int messageTypeToProblemType(MessageType type) {
        if(type == MessageType.INFO) {
            return ProblemDescription.INFORMATION;
        } else if(type == MessageType.WARNING) {
            return ProblemDescription.WARNING;
        } else if(type == MessageType.FATAL) {
            return ProblemDescription.FATAL;
        } else if(type == MessageType.INTERNAL) {
            return ProblemDescription.INTERNAL_ERROR;
        } else {
            return ProblemDescription.ERROR;
        }
    }

    /**
     * @see nu.validator.messages.MessageEmitter#startMessages(java.lang.String)
     */
    @Override
    public void startMessages(String documentUri, boolean willShowSource) throws SAXException {
        problemsHandler.startProblems();
    }

    /**
     * @see nu.validator.messages.MessageEmitter#endMessages()
     */
    @Override
    public void endMessages() throws SAXException {
        problemsHandler.endProblems();
    }

    /**
     * @see nu.validator.messages.MessageEmitter#endText()
     */
    @Override
    public void endText() throws SAXException {
        try {
            this.writer.write('\n');
            this.textEmitted = true;
        } catch (IOException e) {
            throw new SAXException(e.getMessage(), e);
        }

    }


    /**
     * @see nu.validator.messages.MessageEmitter#startText()
     */
    @Override
    public MessageTextHandler startText() throws SAXException {
        try {
            this.writer.write(COLON_SPACE);
            return messageTextHandler;
        } catch (IOException e) {
            throw new SAXException(e.getMessage(), e);
        }
    }

    @Override
    public void endMessage() throws SAXException {
        try {
            if (!textEmitted) {
                writer.write(PERIOD);
                writer.write('\n');
            }
            maybeEmitLocation();
            writer.write('\n');
        } catch (IOException e) {
            throw new SAXException(e.getMessage(), e);
        }

        int from, to;
        if(oneBasedFirstLine == -1) {
            //no position at all - use first line
            from = 0;
            to = linesMapper.getLinesCount() > 0 ? linesMapper.getLine(0).getEnd() : 0;

        } else {
            int linefrom = -1, lineto = -1, columnfrom = -1, columnto = -1;
            if (oneBasedFirstLine != -1) {
                linefrom = oneBasedFirstLine;
                columnfrom = oneBasedFirstColumn == -1 ? 0 : oneBasedFirstColumn;
            }
            if (oneBasedLastLine != -1) {
                lineto = oneBasedLastLine;
                columnto = oneBasedLastColumn == -1 ? 0 : oneBasedLastColumn;
            } else {
                lineto = linefrom;
                columnto = 1;
            }

            from = linesMapper.getSourceOffsetForLocation(linefrom - 1, columnfrom - 1);
            to = linesMapper.getSourceOffsetForLocation(lineto - 1, columnto);
        }

        problemsHandler.addProblem(ProblemDescription.create("nu.validator.issue",
                writer.toString(),
                problemType,
                from,
                to));


    }

    /**
     * @see nu.validator.messages.MessageEmitter#startResult()
     */
    @Override
    public ResultHandler startResult() throws SAXException {
        return null;
    }

    @Override
    public ContentHandler startElaboration() throws SAXException {
        return elaborationContentHandler;
    }

    @Override
    public SourceHandler startFullSource(int lineOffset) throws SAXException {
        return super.startFullSource(lineOffset);
    }

    @Override
    public SourceHandler startSource() throws SAXException {
        return super.startSource();
    }

    private static class SimplifiedMessagesContentHandler implements ContentHandler {

        private static boolean inDD, inDL, inDT, inCODE;
        private Writer out;

        private static final String DL = "dl";
        private static final String DT = "dt";
        private static final String DD = "dd";
        private static final String CODE = "code";

        public SimplifiedMessagesContentHandler(Writer out) {
            this.out = out;
        }

        private void write(String s) throws SAXException {
            try {
                out.write(s);
            } catch (IOException ex) {
                throw new SAXException(ex);
            }
        }

        public void setDocumentLocator(Locator locator) {
        }

        public void startDocument() throws SAXException {
            inDD = false;
            inDT = false;
            inDL = false;
            inCODE = false;

            write("\n");
        }

        public void endDocument() throws SAXException {
        }

        public void startPrefixMapping(String prefix, String uri) throws SAXException {
        }

        public void endPrefixMapping(String prefix) throws SAXException {
        }

        public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
            if(DL.equals(localName)) {
                inDL = true;
            } else if (DT.equals(localName)) {
                inDT = true;
            } else if(DD.equals(localName)) {
                inDD = true;
            } else if(CODE.equals(localName)) {
                inCODE = true;
                write("'");
            }
        }

        public void endElement(String uri, String localName, String qName) throws SAXException {
            if(DL.equals(localName)) {
                inDL = false;
            } else if (DT.equals(localName)) {
                inDT = false;
                write("\n");
            } else if(DD.equals(localName)) {
                inDD = false;
                write("\n");
            } else if(CODE.equals(localName)) {
                inCODE = false;
                write("'");
            }
        }

        public void characters(char[] ch, int start, int length) throws SAXException {
            if(inDL) {
                if(inDD || inDT || inCODE) {
                    write(new String(ch, start, length));
                }
            }
        }

        public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
        }

        public void processingInstruction(String target, String data) throws SAXException {
        }

        public void skippedEntity(String name) throws SAXException {
        }

    }


}
