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
package org.openide.util.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.nio.channels.ClosedByInterruptException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import org.openide.util.RequestProcessor;

/**
 * Non-interruptible file streams. The methods in this class should be used instead of
 * {@link Files#newInputStream(Path,OpenOption...)} or
 * {@link Files#newOutputStream(Path,OpenOption...)} when the call site does not have a reasonable
 * way to handle a {@link ClosedByInterruptException}, or when the expected length or frequency of
 * individual I/O operations do not justify the extra handling logic. The file streams returned by
 * methods in this class are guaranteed never to throw {@link ClosedByInterruptException}. Instead,
 * they behave like the old {@link FileInputStream} and {@link FileOutputStream} classes, which do
 * not abort when the current thread is interrupted.
 *
 * <p>Background: In past Java versions, heavy use of {@link FileInputStream} and
 * {@link FileOutputStream} have been known to cause long garbage collection pauses (see
 * <a href="https://bugs.openjdk.java.net/browse/JDK-8080225">JDK-8080225</a> and
 * <a href="https://issues.apache.org/jira/browse/HDFS-8562">this</a> Hadoop bug). The alternative,
 * of using NIO streams via {@link Files#newInputStream(Path,OpenOption...)} and
 * {@link Files#newInputStream(Path,OpenOption...)}, avoids these GC problems, but introduces new
 * backwards-incompatible behavior where individual stream operations may throw
 * {@link ClosedByInterruptException} if the current thread is interrupted. This class serves as a
 * single point to document and work around this problem.
 *
 * <p>Quite often, especially in library code that previously used FileInputStream and
 * FileOutputStream, callers of file stream methods would have no reasonable way to handle a
 * ClosedByInterruptException other than to retry the operation and set the current thread's
 * interrupt bit, since its available options may be restricted by its own API contract and past
 * behavior. For instance, allowing the {@code ClosedByInterruptException} to propagate up the stack
 * as an {@link IOException} may be interpreted by the caller's own clients as an error rather than
 * a normal thread interruption event, leading to the undesired display of user-level error
 * messages. See for example <a href="https://github.com/apache/incubator-netbeans/pull/854">NETBEANS-1197</a>.
 * The other alternative, of returning an incorrect or "empty" result, could cause problems as well,
 * for instance if the incorrect results ends up in a cache somewhere higher up the call stack, in
 * code that is beyond the control of the file stream client's developer. Other problematic cases
 * include existing APIs that pass the InputStream or OutputStream on to existing, external client
 * code. Such existing client code cannot be expected to suddenly handle a
 * {@link ClosedByInterruptException} if it was previously never thrown.
 *
 * <p>Note that library code should always be prepared to handle thread interrupts gracefully, as
 * clients of said library code may well decide to call the library from an interruptible thread.
 * Thread interrupts arise, for instance, when code that is already running in an interrupt-enabled
 * {@link RequestProcessor} is stopped via {@link RequestProcessor#stop()}.
 *
 * <p>See also the discussion on issues
 * <a href="https://github.com/apache/incubator-netbeans/pull/854">NETBEANS-1197</a> and
 * <a href="https://issues.apache.org/jira/browse/NETBEANS-583">NETBEANS-583</a>.
 */
public final class FilesNI {
    /**
     * Marker for currently disabled NIO-based code, kept around for illustration purposes. If we ever
     * are to use the NIO streams here, we would need to wrap them in custom InputStream/OutputStream
     * implementations that delegate to them, restarting operations with a regular
     * FileInputStream/FileOutputStream in the rare case that a ClosedByInterruptException is
     * encountered.
     *
     * <p>Since JDK-8080225 is fixed in JDK 10, it seems that the GC-related problems with
     * InputStream/OutputStream will soon be going away, and so it should be fine to just always use
     * the latter instead of the NIO streams here.
     */
    private static final boolean USE_NIO = false;

    private FilesNI() {
    }

    /**
     * @see FileInputStream#FileInputStream(File)
     */
    public static InputStream newInputStream(File file) throws IOException {
        return USE_NIO
            ? Files.newInputStream(file.toPath())
            : new FileInputStream(file);
    }

    /**
     * @see FileOutputStream#FileOutputStream(File)
     */
    public static OutputStream newOutputStream(File file) throws IOException {
        return newOutputStream(file, false);
    }

    /**
     * @see FileOutputStream#FileOutputStream(File,boolean)
     */
    public static OutputStream newOutputStream(File file, boolean append) throws IOException {
        if (USE_NIO) {
            // StandardOpenOption.CREATE is the standard behavior of "new FileOutputStream(File)".
            return Files.newOutputStream(file.toPath(), append
                ? new OpenOption[] { StandardOpenOption.CREATE }
                : new OpenOption[] { StandardOpenOption.CREATE, StandardOpenOption.APPEND });
        } else {
            return new FileOutputStream(file, append);
        }
    }

    /**
     * @see Files#newBufferedReader(Path)
     */
    public static BufferedReader newBufferedReader(File file) throws IOException {
        return newBufferedReader(file, StandardCharsets.UTF_8);
    }

    /**
     * @see Files#newBufferedReader(Path, Charset)
     */
    public static BufferedReader newBufferedReader(File file, Charset cs)
        throws IOException
    {
        CharsetDecoder decoder = cs.newDecoder();
        Reader reader = new InputStreamReader(newInputStream(file), decoder);
        return new BufferedReader(reader);
    }
}
