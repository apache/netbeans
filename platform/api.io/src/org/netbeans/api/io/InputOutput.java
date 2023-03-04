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
package org.netbeans.api.io;

import java.io.PrintWriter;
import java.io.Reader;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.spi.io.InputOutputProvider;
import org.openide.util.Lookup;

/**
 * An I/O connection to one tab on the Output Window. To acquire an instance to
 * write to, call, e.g.,
 * <code>IOProvider.getDefault().getInputOutput("someName", false)</code>. To
 * get actual streams to write to, call <code>getOut()</code> or <code>
 * getErr()</code> on the returned instance.
 * <p>
 * Generally it is preferable not to hold a reference to an instance of
 * {@link org.netbeans.api.io.InputOutput}, but rather to fetch it by name from
 * {@link org.netbeans.api.io.IOProvider} as needed.
 * </p>
 *
 * <p>
 * Methods of this class can be called in any thread.
 * </p>
 *
 * @see OutputWriter
 * @author Ian Formanek, Jaroslav Tulach, Petr Hamernik, Ales Novak, Jan
 * Jancura, Jaroslav Havlin
 */
public abstract class InputOutput implements Lookup.Provider {

    private static final Set<ShowOperation> DEFAULT_SHOW_OPERATIONS
            = EnumSet.of(ShowOperation.OPEN, ShowOperation.MAKE_VISIBLE);

    private InputOutput() {
    }

    /**
     * Get a named instance of InputOutput from the default {@link IOProvider},
     * which represents an output tab in the output window. Streams for
     * reading/writing can be accessed via getters on the returned instance.
     *
     * <p>
     * This is a shorthand for {@code IOProvider.getDefault().getIO(...)}.
     * </p>
     *
     * @param name A localised display name for the tab.
     * @param newIO If <tt>true</tt>, a new <code>InputOutput</code> is
     * returned, else an existing <code>InputOutput</code> of the same name may
     * be returned.
     * @return An <code>InputOutput</code> instance for accessing the new tab.
     * @see IOProvider
     */
    @NonNull
    public static InputOutput get(@NonNull String name, boolean newIO) {
        return IOProvider.getDefault().getIO(name, newIO);
    }

     /**
     * Get a named instance of InputOutput from the default {@link IOProvider},
     * which represents an output tab in the output window. Streams for
     * reading/writing can be accessed via getters on the returned instance.
     *
     * <p>
     * This is a shorthand for {@code IOProvider.getDefault().getIO(...)}.
     * </p>
     *
     * @param name A localised display name for the tab.
     * @param newIO If <tt>true</tt>, a new <code>InputOutput</code> is
     * returned, else an existing <code>InputOutput</code> of the same name may
     * be returned.
     * @param lookup Lookup which may contain additional information for various
     * implementations of output window.
     * @return An <code>InputOutput</code> instance for accessing the new tab.
     * @see IOProvider
     */
    @NonNull
    public static InputOutput get(@NonNull String name, boolean newIO,
            @NonNull Lookup lookup) {
        return IOProvider.getDefault().getIO(name, newIO, lookup);
    }

    /**
     * Get a reader to read from the tab.
     *
     * @return The reader.
     */
    @NonNull
    public abstract Reader getIn();

    /**
     * Acquire an output writer to write to the tab.
     *
     * @return The writer.
     */
    @NonNull
    public abstract OutputWriter getOut();

    /**
     * Get an output writer to write to the tab in error mode. This might show
     * up in a different color than the regular output, e.g., or appear in a
     * separate pane.
     *
     * @return The writer.
     */
    @NonNull
    public abstract OutputWriter getErr();

    /**
     * Clear the output pane.
     */
    public abstract void reset();

    /**
     * Get lookup which may contain extensions provided by implementation of
     * output window.
     *
     * @return The lookup.
     */
    @NonNull
    @Override
    public abstract Lookup getLookup();

    /**
     * Closes this tab. The effect of calling any method on an instance of
     * InputOutput after calling <code>close()</code> on it is
     * undefined.
     */
    public abstract void close();

    /**
     * Test whether this tab has been closed, either by a call to
     * {@link #close()} or by the user closing the tab in the UI.
     *
     * @return Value <code>true</code> if it is closed.
     */
    public abstract boolean isClosed();

    /**
     * Get description of this I/O instance.
     *
     * @return The description, or null if not set.
     */
    @CheckForNull
    public abstract String getDescription();

    /**
     * Set description of this I/O instance. It can be used e.g. as tooltip for
     * output tab in the GUI.
     *
     * @param description The description, can be null.
     */
    public abstract void setDescription(@NullAllowed String description);

    static <IO, OW extends PrintWriter, P, F> InputOutput create(
            InputOutputProvider<IO, OW, P, F> provider, IO io) {

        return new Impl<IO, OW, P, F>(provider, io);
    }

    /**
     * Show this I/O if possible (e.g. in tabbed pane).
     * <p>
     * Calling this method is the same as calling:
     * </p>
     * <pre>
     * show(EnumSet.of(ShowOperation.OPEN, ShowOperation.MAKE_VISIBLE));
     * </pre>
     *
     * @see #show(java.util.Set)
     */
    public final void show() {
        show(DEFAULT_SHOW_OPERATIONS);
    }

    /**
     * Show this I/O if possible (e.g. in tabbed pane).
     *
     * @param operations Set of operations that should be invoked to show the
     * output. If the set is empty or null, pane for this I/O will be only
     * selected, but its component will not be opened or made visible. So it
     * will stay closed or hidden if it is not opened or not visible.
     *
     * @see ShowOperation
     */
    public abstract void show(@NullAllowed Set<ShowOperation> operations);

    private static class Impl<IO, OW extends PrintWriter, P, F>
            extends InputOutput {

        private final Map<OW, OutputWriter> cache
                = Collections.synchronizedMap(
                        new WeakHashMap<OW, OutputWriter>());

        private final InputOutputProvider<IO, OW, P, F> provider;
        private final IO ioObject;

        public Impl(InputOutputProvider<IO, OW, P, F> provider, IO ioObject) {

            this.provider = provider;
            this.ioObject = ioObject;
        }

        @Override
        public Reader getIn() {
            return provider.getIn(ioObject);
        }

        @Override
        public OutputWriter getOut() {
            return createOrGetCachedWrapper(provider.getOut(ioObject));
        }

        @Override
        public OutputWriter getErr() {
            return createOrGetCachedWrapper(provider.getErr(ioObject));
        }

        @Override
        @NonNull
        public Lookup getLookup() {
            return provider.getIOLookup(ioObject);
        }

        @Override
        public void reset() {
            provider.resetIO(ioObject);
        }

        private OutputWriter createOrGetCachedWrapper(OW pw) {
            OutputWriter ow = cache.get(pw);
            if (ow == null) {
                ow = OutputWriter.create(provider, ioObject, pw);
                cache.put(pw, ow);
            }
            return ow;
        }

        @Override
        public void close() {
            provider.closeIO(ioObject);
        }

        @Override
        public boolean isClosed() {
            return provider.isIOClosed(ioObject);
        }

        @Override
        public String getDescription() {
            return provider.getIODescription(ioObject);
        }

        @Override
        public void setDescription(String description) {
            provider.setIODescription(ioObject, description);
        }

        @Override
        public void show(Set<ShowOperation> operations) {
            provider.showIO(ioObject,
                    operations != null
                            ? operations
                            : Collections.<ShowOperation>emptySet());
        }
    }
}
