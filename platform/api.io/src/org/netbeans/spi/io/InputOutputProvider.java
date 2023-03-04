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
package org.netbeans.spi.io;

import java.io.PrintWriter;
import java.io.Reader;
import java.util.Set;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.io.Hyperlink;
import org.netbeans.api.io.InputOutput;
import org.netbeans.api.io.OutputColor;
import org.netbeans.api.io.ShowOperation;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * SPI for custom output window implementations.
 * <p>
 * Use {@link ServiceProvider} annotation for registration.
 * </p>
 *
 * <p>
 * Note: Methods of this interface can be called in any thread by the
 * infrastructure, so implementations should ensure proper synchronization.
 * </p>
 *
 * @author jhavlin
 *
 * @param <IO> Type of objects that will represent I/O instances (e.g. tabs in
 * output window).
 * @param <WRITER> Type of writers for standard and error streams.
 * @param <POS> Type of object that describes position of a character in the
 * output window. If the implementation does not support this type of
 * information, use {@link Void} here.
 * @param <FOLD> Type of object that describes a fold in output window. If the
 * implementation does not support this type of information, use {@link Void}
 * here.
 */
public interface InputOutputProvider<IO, WRITER extends PrintWriter, POS, FOLD> {

    /**
     * Get identifier of this provider.
     *
     * @return Name of this provider, never null.
     */
    @NonNull
    String getId();

    /**
     * Get or create an object that encapsulates state of a single I/O instance
     * (e.g. tab in output window).
     *
     * @param name Display name of the output pane.
     * @param newIO True to always create new I/O, false to return already
     * existing instance if available.
     * @param lookup Lookup with additional information.
     *
     * @return A single I/O instance, a newly created or already existing.
     * @see InputOutput
     */
    @NonNull
    IO getIO(@NonNull String name, boolean newIO, @NonNull Lookup lookup);

    /**
     * Get input of the passed I/O.
     *
     * @param io I/O instance.
     *
     * @return {@link Reader} Reader for the input entered in the output pane.
     */
    @NonNull
    Reader getIn(@NonNull IO io);

    /**
     * Get output stream of the passed I/O.
     *
     * @param io I/O instance.
     *
     * @return {@link PrintWriter} for the output stream.
     */
    @NonNull
    WRITER getOut(@NonNull IO io);

    /**
     * Get error stream of the passed I/O.
     *
     * @param io The I/O instance.
     *
     * @return {@link PrintWriter} for the error stream.
     */
    @NonNull
    WRITER getErr(@NonNull IO io);

    /**
     * Print enhanced text. It can represent a clickable hyperlink, and can be
     * assigned some color.
     *
     * <p>
     * If the implementation doesn't support this feature, this method should
     * call something like
     * {@code writer.print(text); if (printLineEnd) {writer.println();}}.
     * </p>
     *
     * @param io The I/O instance.
     * @param writer The Stream to write into.
     * @param text Text to print.
     * @param link Link which should be represented by the text, can be null for
     * standard text.
     * @param color Color of the text, can be null for the default color of the
     * stream.
     * @param printLineEnd True if new-line should be appended after printing
     * {code text}.
     */
    void print(@NonNull IO io, @NonNull WRITER writer, @NullAllowed String text,
            @NullAllowed Hyperlink link, @NullAllowed OutputColor color,
            boolean printLineEnd);

    /**
     * Get lookup of an I/O instance, which can contain various extensions and
     * additional info.
     *
     * @param io The I/O instance.
     *
     * @return The lookup, which can be empty, but never null.
     */
    @NonNull
    Lookup getIOLookup(@NonNull IO io);

    /**
     * Reset the I/O. Clear previously written data and prepare it for new data.
     * <p>
     * If the implementation doesn't support this feature, this method should do
     * nothing.
     * </p>
     *
     * @param io The I/O instance.
     */
    void resetIO(@NonNull IO io);

    /**
     * Show output pane for the passed I/O instance.
     * <p>
     * If the implementation doesn't support this feature, this method should do
     * nothing.
     * </p>
     *
     * @param io The I/O instance.
     * @param operations Operations that should be performed to show the output.
     * If the set is empty, the output pane (e.g. tab) can be selected, but no
     * GUI component should be shown or made visible if it is currently closed
     * or hidden.
     *
     * @see ShowOperation
     */
    void showIO(@NonNull IO io,
            Set<ShowOperation> operations);

    /**
     * Close the I/O, its output pane and release resources.
     *
     * @param io The I/O instance.
     *
     * @see #isIOClosed(java.lang.Object)
     */
    void closeIO(@NonNull IO io);

    /**
     * Check whether the I/O is closed.
     *
     * @param io The I/O instance.
     *
     * @return True if the I/O was closed, false otherwise.
     *
     * @see #closeIO(java.lang.Object)
     */
    boolean isIOClosed(@NonNull IO io);

    /**
     * Get current position.
     *
     * @param io The I/O instance.
     * @param writer Output or error writer. If the streams are merged, the
     * value can be ignored.
     *
     * @return The current position in the output pane. If this feature is not
     * supported, return null.
     */
    @CheckForNull
    POS getCurrentPosition(@NonNull IO io, @NonNull WRITER writer);

    /**
     * Scroll to a position.
     * <p>
     * If this feature is not supported
     * ({@link #getCurrentPosition(Object, PrintWriter)} returns null), this
     * method should do nothing, it will be never called.
     * </p>
     *
     * @param io The I/O instance.
     * @param writer Output or error writer. If the streams are merged, the
     * value can be ignored.
     * @param position The position to scroll to.
     *
     * @see #getCurrentPosition(java.lang.Object, java.io.PrintWriter)
     */
    void scrollTo(@NonNull IO io, @NonNull WRITER writer,
            @NonNull POS position);

    /**
     * Start fold at the current position. If a fold is already open, start a
     * new nested fold (but if a fold with the same start position already
     * exists, no nested fold will be created, and the same start position will
     * be returned).
     *
     * @param io The I/O instance.
     * @param writer Output or error writer. If the streams are merged, the
     * value can be ignored.
     * @param expanded True if the new fold should be expanded by default, false
     * if it should be collapsed.
     *
     * @return Object the fold. If the implementation doesn't support this
     * feature, return null.
     */
    @CheckForNull
    FOLD startFold(@NonNull IO io, @NonNull WRITER writer, boolean expanded);

    /**
     * Finish a fold specified by its start position. If some nested folds
     * exist, they will be finished as well if needed.
     *
     * <p>
     * If this feature is not supported
     * ({@link #startFold(Object, PrintWriter, boolean)} returns null), this
     * method should do nothing, it will be never called.
     * </p>
     *
     * @param io The I/O instance.
     * @param writer Output or error writer. If the streams are merged, the
     * value can be ignored.
     * @param fold The fold to finish.
     */
    void endFold(@NonNull IO io, @NonNull WRITER writer,
            @NonNull FOLD fold);

    /**
     * Expand or collapse a fold.
     *
     * <p>
     * If this feature is not supported
     * ({@link #startFold(Object, PrintWriter, boolean)} returns null), this
     * method should do nothing, it will be never called.
     * </p>
     *
     * @param io The I/O instance.
     * @param writer Output or error writer. If the streams are merged, the
     * value can be ignored.
     * @param fold The fold to finish.
     * @param expanded True to expand the fold, false to collapse the fold.
     */
    void setFoldExpanded(@NonNull IO io, @NonNull WRITER writer,
            FOLD fold, boolean expanded);


    /**
     * Get description of an I/O instance. It can be used e.g. as tooltip text
     * for the output tab.
     *
     * @param io The I/O instance.
     *
     * @return The description, or null if not set.
     */
    @CheckForNull String getIODescription(@NonNull IO io);

    /**
     * Set description of an I/O instance.
     *
     * <p>
     * If this feature is not supported, this method should do nothing.
     * </p>
     *
     * @param io The I/O instance.
     * @param description The description, can be null.
     *
     * @see #getIODescription(java.lang.Object)
     */
    void setIODescription(@NonNull IO io, @NullAllowed String description);
}
