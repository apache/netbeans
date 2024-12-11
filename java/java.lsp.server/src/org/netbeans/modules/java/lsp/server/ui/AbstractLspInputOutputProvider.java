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
package org.netbeans.modules.java.lsp.server.ui;

import org.netbeans.modules.java.lsp.server.protocol.OutputMessage;
import java.io.CharArrayReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.Set;
import org.netbeans.api.io.Hyperlink;
import org.netbeans.api.io.OutputColor;
import org.netbeans.api.io.ShowOperation;
import org.netbeans.modules.java.lsp.server.LspServerUtils;
import org.netbeans.modules.java.lsp.server.protocol.NbCodeLanguageClient;
import org.netbeans.modules.java.lsp.server.ui.AbstractLspInputOutputProvider.LspIO;
import org.netbeans.spi.io.InputOutputProvider;
import org.openide.util.Lookup;

public abstract class AbstractLspInputOutputProvider implements InputOutputProvider<LspIO, PrintWriter, Void, Void> {
    protected AbstractLspInputOutputProvider() {
        if (!"org.netbeans.modules.nbcode.integration.LspInputOutputProvider".equals(getClass().getName())) { // NOI18N
            throw new IllegalStateException();
        }
    }

    @Override
    public final String getId() {
        return "lspio";
    }

    @Override
    public final LspIO getIO(String name, boolean newIO, Lookup lookup) {
        IOContext ioCtx = IOContext.find();
        return new LspIO(name, ioCtx, lookup);
    }

    @Override
    public final Reader getIn(LspIO io) {
        return io.in;
    }

    @Override
    public final PrintWriter getOut(LspIO io) {
        return new PrintWriter(io.out);
    }

    @Override
    public final PrintWriter getErr(LspIO io) {
        return new PrintWriter(io.err);
    }

    @Override
    public final void print(LspIO io, PrintWriter writer, String text, Hyperlink link, OutputColor color, boolean printLineEnd) {
        if (printLineEnd) {
            writer.println(text);
        } else {
            writer.print(text);
        }
    }

    @Override
    public final Lookup getIOLookup(LspIO io) {
        return io.lookup;
    }

    @Override
    public final void resetIO(LspIO io) {
        NbCodeLanguageClient client = LspServerUtils.findLspClient(Lookup.getDefault());
        if (client != null) {
            client.resetOutput(io.name);
        }
    }

    @Override
    public final void showIO(LspIO io, Set<ShowOperation> operations) {
        NbCodeLanguageClient client = LspServerUtils.findLspClient(Lookup.getDefault());
        if (client != null) {
            client.showOutput(io.name);
        }
    }

    @Override
    public final void closeIO(LspIO io) {
        NbCodeLanguageClient client = LspServerUtils.findLspClient(Lookup.getDefault());
        if (client != null) {
            client.closeOutput(io.name);
        }
    }

    @Override
    public final boolean isIOClosed(LspIO io) {
        return io.isClosed();
    }

    @Override
    public final Void getCurrentPosition(LspIO io, PrintWriter writer) {
        return null;
    }

    @Override
    public final void scrollTo(LspIO io, PrintWriter writer, Void position) {
    }

    @Override
    public final Void startFold(LspIO io, PrintWriter writer, boolean expanded) {
        return null;
    }

    @Override
    public final void endFold(LspIO io, PrintWriter writer, Void fold) {
    }

    @Override
    public final void setFoldExpanded(LspIO io, PrintWriter writer, Void fold, boolean expanded) {
    }

    @Override
    public final String getIODescription(LspIO io) {
        return "";
    }

    @Override
    public final void setIODescription(LspIO io, String description) {
    }

    public static final class LspIO {
        private final String name;
        private final IOContext ctx;
        private final NbCodeLanguageClient client;
        final Lookup lookup;
        final Reader in;
        final LspWriter out;
        final LspWriter err;

        LspIO(String name, IOContext ioCtx, Lookup lookup) {
            this.name = name;
            this.ctx = ioCtx;
            this.lookup = lookup;
            this.out = new LspWriter(true);
            this.err = new LspWriter(false);
            Reader in;
            try {
                InputStream is = ioCtx.getStdIn();
                in = new InputStreamReader(is, "UTF-8") {
                    @Override
                    public void close() throws IOException {
                        // the underlying StreamDecoder would just block on synchronized read(); close the underlying stream.
                        is.close();
                        super.close();
                    }
                };
            } catch (IOException ex) {
                try {
                    err.write(ex.getLocalizedMessage());
                } catch (IOException ioex) {}
                in = new CharArrayReader(new char[0]) {
                    @Override
                    public void close() {
                    }
                };
            }
            this.in = in;
            client = LspServerUtils.findLspClient(Lookup.getDefault());
            if (client != null) {
                client.resetOutput(name);
            }
        }

        boolean isClosed() {
            return out.closed && err.closed;
        }
        
        private final class LspWriter extends Writer {
            private final boolean stdIO;
            volatile boolean closed;

            LspWriter(boolean stdIO) {
                this.stdIO = stdIO;
            }

            @Override
            public void write(char[] cbuf, int off, int len) throws IOException {
                String chunk = new String(cbuf, off, len);
                if (len > 0) {
                    client.writeOutput(new OutputMessage(name, chunk, stdIO));
                }
            }

            @Override
            public void flush() throws IOException {
            }

            @Override
            public void close() throws IOException {
                closed = true;
            }
        }
    }
    
}
