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
package org.openide.io;

import java.awt.Color;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import org.netbeans.api.io.Hyperlink;
import org.netbeans.api.io.OutputColor;
import org.netbeans.api.io.ShowOperation;
import org.netbeans.spi.io.InputOutputProvider;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;
import org.openide.windows.IOColorLines;
import org.openide.windows.IOColorPrint;
import org.openide.windows.IOContainer;
import org.openide.windows.IOFolding;
import org.openide.windows.IOPosition;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputListener;
import org.openide.windows.OutputWriter;

/**
 *
 * @author jhavlin
 */
public class BridgingIOProvider<IO, S extends PrintWriter, P, F>
        extends IOProvider {

    private static final Logger LOG
            = Logger.getLogger(BridgingIOProvider.class.getName());
    private static final Set<ShowOperation> DEFAULT_SHOW_OPERATIONS
            = EnumSet.of(ShowOperation.OPEN, ShowOperation.MAKE_VISIBLE);

    private final InputOutputProvider<IO, S, P, F> providerDelegate;
    private Lookup lookup = null;

    public static <A, B extends PrintWriter, C, D> IOProvider create(
            InputOutputProvider<A, B, C, D> delegate) {
        return new BridgingIOProvider<A, B, C, D>(delegate);
    }

    private BridgingIOProvider(
            InputOutputProvider<IO, S, P, F> delegate) {
        this.providerDelegate = delegate;
    }

    @Override
    public InputOutput getIO(String name, boolean newIO) {
        return new BridgingInputOutput(
                providerDelegate.getIO(name, newIO, Lookup.EMPTY));
    }

    @Override
    public InputOutput getIO(String name, Action[] actions) {
        return new BridgingInputOutput(
                providerDelegate.getIO(name, true,
                        Lookups.fixed((Object[]) actions)));
    }

    @Override
    public InputOutput getIO(String name, Action[] actions,
            IOContainer ioContainer) {
        return getIO(name, true, actions, ioContainer);
    }

    @Override
    public InputOutput getIO(String name, boolean newIO, Action[] actions,
            IOContainer ioContainer) {
        Object[] lookupContent;
        if (ioContainer != null) {
            lookupContent = Arrays.copyOf(actions, actions.length + 1, Object[].class);
            lookupContent[actions.length] = ioContainer;
        } else {
            lookupContent = actions;
        }
        return new BridgingInputOutput(
                providerDelegate.getIO(name, newIO,
                        Lookups.fixed(lookupContent)));
    }

    @NbBundle.Messages({"LBL_STDOUT=Standard output"})
    @Override
    public OutputWriter getStdOut() {
        IO io = providerDelegate.getIO(Bundle.LBL_STDOUT(), false, Lookup.EMPTY);
        S out = providerDelegate.getOut(io);
        return new BridgingOutputWriter(io, out);
    }

    private Hyperlink listenerToHyperlink(
            final OutputListener l,
            final boolean important) {
        return l == null
                ? null
                : Hyperlink.from(new Runnable() {

                    @Override
                    public void run() {
                        try {
                            // Event is null, it can cause problems.
                            l.outputLineAction(null);
                        } catch (Exception e) {
                            LOG.log(Level.INFO, "Cannot process output" //NOI18N
                                    + " listener action", e);           //NOI18N
                        }
                    }
                }, important);
    }

    /**
     * Bridge from old SPI to new API for I/O instance.
     */
    private class BridgingInputOutput implements InputOutput, Lookup.Provider {

        private final IO ioDelegate;

        public BridgingInputOutput(IO delegate) {
            this.ioDelegate = delegate;
        }

        @Override
        public OutputWriter getOut() {
            return new BridgingOutputWriter(ioDelegate,
                    providerDelegate.getOut(ioDelegate));
        }

        @Override
        public Reader getIn() {
            return providerDelegate.getIn(ioDelegate);
        }

        @Override
        public OutputWriter getErr() {
            return new BridgingOutputWriter(ioDelegate,
                    providerDelegate.getErr(ioDelegate));
        }

        @Override
        public void closeInputOutput() {
            providerDelegate.closeIO(ioDelegate);
        }

        @Override
        public boolean isClosed() {
            return providerDelegate.isIOClosed(ioDelegate);
        }

        @Override
        public void setOutputVisible(boolean value) {
        }

        @Override
        public void setErrVisible(boolean value) {
        }

        @Override
        public void setInputVisible(boolean value) {
        }

        @Override
        public void select() {
            providerDelegate.showIO(ioDelegate, DEFAULT_SHOW_OPERATIONS);
        }

        @Override
        public boolean isErrSeparated() {
            return false;
        }

        @Override
        public void setErrSeparated(boolean value) {
        }

        @Override
        public boolean isFocusTaken() {
            return false;
        }

        @Override
        public void setFocusTaken(boolean value) {
        }

        @Override
        @SuppressWarnings("deprecation")
        public Reader flushReader() {
            return getIn();
        }

        @Override
        public synchronized Lookup getLookup() {
            if (lookup == null) {
                Lookup origLookup = providerDelegate.getIOLookup(ioDelegate);
                Lookup extLookup = Lookups.fixed(
                        new BridgingIOColorLines(ioDelegate),
                        new BridgingIOColorPrint(ioDelegate),
                        new BridgingIOPosition(ioDelegate),
                        new BridgingIOFolding(ioDelegate)
                );
                if (origLookup == Lookup.EMPTY) {
                    lookup = extLookup;
                } else {
                    lookup = new ProxyLookup(origLookup, extLookup);
                }
            }
            return lookup;
        }
    }

    /**
     * Bridge from old SPI to new API for a stream instance.
     */
    private class BridgingOutputWriter extends OutputWriter {

        PrintWriter printWriterToReplace = null;
        private final S writerDelegate;
        private final IO ioDelegate;

        public BridgingOutputWriter(IO ioDelegate, S delegate) {
            super(delegate);
            this.writerDelegate = delegate;
            this.ioDelegate = ioDelegate;
        }

        @Override
        public void println(String s, final OutputListener l)
                throws IOException {

            println(s, l, false);
        }

        @Override
        public void println(String s, final OutputListener l, boolean important)
                throws IOException {

            Hyperlink h = listenerToHyperlink(l, important);
            providerDelegate.print(ioDelegate, writerDelegate, s,
                    h, null, true);
        }

        @Override
        public void reset() throws IOException {
            providerDelegate.resetIO(ioDelegate);
        }
    }

    private class BridgingIOColorLines extends IOColorLines {

        private final IO ioDelegate;

        public BridgingIOColorLines(IO ioDelegate) {
            this.ioDelegate = ioDelegate;
        }

        @Override
        protected void println(CharSequence text, OutputListener listener,
                boolean important, Color color) throws IOException {
            Hyperlink h = listenerToHyperlink(listener, important);
            providerDelegate.print(ioDelegate,
                    providerDelegate.getOut(ioDelegate), text.toString(),
                    h, null, true);
        }
    }

    private class BridgingIOColorPrint extends IOColorPrint {

        private final IO ioDelegate;

        public BridgingIOColorPrint(IO ioDelegate) {
            this.ioDelegate = ioDelegate;
        }

        @Override
        protected void print(CharSequence text, OutputListener listener,
                boolean important, Color color) throws IOException {

            Hyperlink h = listenerToHyperlink(listener, important);
            final OutputColor outColor = color == null ? null : OutputColor.rgb(color.getRGB());
            providerDelegate.print(ioDelegate,
                    providerDelegate.getOut(ioDelegate), text.toString(),
                    h, outColor, false);
        }
    }

    private class BridgingIOPosition extends IOPosition {

        private final IO ioDelegate;

        public BridgingIOPosition(IO ioDelegate) {
            this.ioDelegate = ioDelegate;
        }

        @Override
        protected Position currentPosition() {

            final S writer = providerDelegate.getOut(ioDelegate);
            final P pos = providerDelegate.getCurrentPosition(ioDelegate,
                    writer);

            return new Position() {

                @Override
                public void scrollTo() {
                    providerDelegate.scrollTo(ioDelegate, writer, pos);
                }
            };
        }
    }

    private class BridgingIOFolding extends IOFolding {

        private final IO ioDelegate;

        public BridgingIOFolding(IO ioDelegate) {
            this.ioDelegate = ioDelegate;
        }

        @Override
        protected FoldHandleDefinition startFold(boolean expanded) {
            S writer = providerDelegate.getOut(ioDelegate);
            F fold = providerDelegate.startFold(ioDelegate, writer, expanded);
            return new BridgingFoldHandleDefinition(writer, fold);
        }

        private class BridgingFoldHandleDefinition extends FoldHandleDefinition {

            private final S writer;
            private final F fold;

            public BridgingFoldHandleDefinition(S writer, F fold) {
                this.writer = writer;
                this.fold = fold;
            }

            @Override
            public void finish() {
                providerDelegate.endFold(ioDelegate, writer, fold);
            }

            @Override
            public FoldHandleDefinition startFold(boolean expanded) {
                F f = providerDelegate.startFold(ioDelegate, writer, expanded);
                return new BridgingFoldHandleDefinition(writer, f);
            }

            @Override
            public void setExpanded(boolean expanded) {
                providerDelegate.setFoldExpanded(ioDelegate, writer, fold,
                        expanded);
            }
        }
    }
}
