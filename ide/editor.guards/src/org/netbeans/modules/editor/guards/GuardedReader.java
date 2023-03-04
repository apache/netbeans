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

package org.netbeans.modules.editor.guards;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import org.netbeans.spi.editor.guards.support.AbstractGuardedSectionsProvider;

/** This stream is able to filter special guarded comments.
 * Holding this information is optional and depends on the construction
 * of this stream - the reason of this feature is that
 * GuardedReader is used also for parser (and it doesn't require
 * the storing the guarded block information - just filter the comments).
 */
final class GuardedReader extends Reader {

    
    /** Encapsulated reader */
    Reader reader;
    
    private NewLineInputStream newLineStream;

    /** Character buffer */
    char[] charBuff;
    int howmany;

    /** The flag determining if this stream should store the guarded
     * block information (list of the SectionDesc).
     */
    boolean justFilter;

    /** The position at the current line. */
    int position;

//    /** The list of the SectionsDesc. */
//    LinkedList<GuardedSection> list;

//    /** The count of types new line delimiters used in the file */
//    final int[] newLineTypes;

    
    private final GuardedSectionsImpl callback;

    private final AbstractGuardedSectionsProvider gr;

    private boolean isClosed = false;

    private AbstractGuardedSectionsProvider.Result result;

    /** Creates new stream.
     * @param is encapsulated input stream.
     * @param justFilter The flag determining if this stream should
     *        store the guarded block information. True means just filter,
     *        false means store the information.
     */
//    GuardedReader(InputStream is, boolean justFilter) throws UnsupportedEncodingException {
//        this(is, justFilter, null);
//    }
//
    GuardedReader(AbstractGuardedSectionsProvider gr, InputStream is, boolean justFilter, Charset encoding, GuardedSectionsImpl guards) {
        newLineStream = new NewLineInputStream(is);
        if (encoding == null)
            reader = new InputStreamReader(newLineStream);
        else
            reader = new InputStreamReader(newLineStream, encoding);
        this.justFilter = justFilter;
//        list = new LinkedList<SectionDescriptor>();
//        newLineTypes = new int[] { 0, 0, 0 };
        this.callback = guards;
        this.gr = gr;
    }

    /** Read the array of chars */
    public int read(char[] cbuf, int off, int len) throws IOException {

        if (charBuff == null) {
            char[] readBuff = readCharBuff();
            this.result = this.gr.readSections(readBuff);
            charBuff = this.result.getContent();
            howmany = charBuff.length;
        }

        if (howmany <= 0) {
            return -1;
        } else {
            int min = Math.min(len, howmany);
            System.arraycopy(charBuff, position, cbuf, off, min);
            howmany -= min;
            position += min;
            return min;
        }
    }

    /** Reads readBuff */
    final char[] readCharBuff() throws IOException {

        char[] readBuff;
        char[] tmp = new char[2048];
        int read;
        ArrayList<char[]> buffs = new ArrayList<char[]>(20);

        for (; ; ) {
            read = readFully(tmp);
            buffs.add(tmp);
            if (read < 2048) {
                break;
            } else {
                tmp = new char[2048];
            }
        }

        int listsize = buffs.size() - 1;
        int size = listsize * 2048 + read;
        readBuff = new char[size];
        charBuff = new char[size];
        int copy = 0;

        for (int i = 0; i < listsize; i++) {
            char[] tmp2 = (char[]) buffs.get(i);
            System.arraycopy(tmp2, 0, readBuff, copy, 2048);
            copy += 2048;
        }
        System.arraycopy(tmp, 0, readBuff, copy, read);
        return readBuff;
    }

    /** reads fully given buffer */
    final int readFully(final char[] buff) throws IOException {
        int read = 0;
        int sum = 0;

        do {
            read = reader.read(buff, sum, buff.length - sum);
            sum += read;
        } while ((sum < buff.length) && (read > 0));

        return sum + 1;
    }

    /** Close underlying writer. */
    public void close() throws IOException {
        if (!isClosed) {
            isClosed = true;
            reader.close();
            if (this.result != null) {
                callback.fillSections(gr, this.result.getGuardedSections(), newLineStream.getNewLineType());
            }
        }
    }
    
    private final class NewLineInputStream extends InputStream {

        private final InputStream stream;
        
        private int b;
        
        private int lookahead;
        private boolean isLookahead = false;
    
        /** The count of types new line delimiters used in the file */
        final int[] newLineTypes;
        
        public NewLineInputStream(InputStream source) {
            this.stream = source;
            this.newLineTypes = new int[] { 0, 0, 0 };
        }
        
        /** @return most frequent type of new line delimiter */
        public NewLine getNewLineType() {
            // special case: an empty file (all newline types equal)
            if (newLineTypes[NewLine.N.ordinal()] == newLineTypes[NewLine.R.ordinal()] &&
                    newLineTypes[NewLine.R.ordinal()] == newLineTypes[NewLine.RN.ordinal()]) {
                
                String s = System.getProperty("line.separator"); // NOI18N
                return NewLine.resolve(s);
            }
            if (newLineTypes[NewLine.N.ordinal()] > newLineTypes[NewLine.R.ordinal()]) {
                return (newLineTypes[NewLine.N.ordinal()] > newLineTypes[NewLine.RN.ordinal()]) ? NewLine.N : NewLine.RN;
            } else {
                return (newLineTypes[NewLine.R.ordinal()] > newLineTypes[NewLine.RN.ordinal()]) ? NewLine.R : NewLine.RN;
            }
        }
        
        public int read() throws IOException {
            
            b = isLookahead? lookahead: this.stream.read();
            isLookahead = false;
            
            switch (b) {
                case (int) '\n':
                    newLineTypes[NewLine.N.ordinal()/*NEW_LINE_N*/]++;
                    return b;
                case (int) '\r':
                    lookahead = this.stream.read();
                    if (lookahead != (int) '\n') {
                        newLineTypes[NewLine.R.ordinal()/*NEW_LINE_R*/]++;
                        isLookahead = true;
                    }  else {
                        newLineTypes[NewLine.RN.ordinal()/*NEW_LINE_RN*/]++;
                    }
                    return (int) '\n';
                default:
                    return b;
            }
            
        }

        public void close() throws IOException {
            super.close();
            this.stream.close();
        }
        
    }

}
