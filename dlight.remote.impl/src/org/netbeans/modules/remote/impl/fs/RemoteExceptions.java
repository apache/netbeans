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
package org.netbeans.modules.remote.impl.fs;

import java.io.FileNotFoundException;
import org.openide.util.Exceptions;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.ConnectException;

/**
 * Inspired by org.netbeans.modules.masterfs.filebasedfs.utils.FSException
 */
public final class RemoteExceptions {

    private RemoteExceptions() {
    }

    /**
     * Creates the localized IOException
     * @param localizedMessage to take localization string from
     */
    public static IOException createIOException(final String localizedMessage)  {
        final IOException fsExc = new IOException(localizedMessage);
        return Exceptions.attachLocalizedMessage(fsExc, localizedMessage);
    }

    /**
     * Creates the localized IOException
     * @param localizedMessage to take localization string from
     */
    public static IOException createIOException(final String localizedMessage, final Exception cause)  {
        final IOException fsExc = new IOException(localizedMessage, cause);
        return Exceptions.attachLocalizedMessage(fsExc, localizedMessage);
    }

    /**
     * Creates the localized IOException
     * @param localizedMessage to take localization string from
     */
    public static InterruptedIOException createInterruptedIOException(final String localizedMessage, final Exception cause)  {
        final InterruptedIOException fsExc = new InterruptedIOException(localizedMessage);
        fsExc.initCause(cause);
        return Exceptions.attachLocalizedMessage(fsExc, localizedMessage);
    }

    /**
     * Creates the localized IOException
     * @param localizedMessage to take localization string from
     */
    public static FileNotFoundException createFileNotFoundException(final String localizedMessage)  {
        final FileNotFoundException fsExc = new FileNotFoundException(localizedMessage);
        return Exceptions.attachLocalizedMessage(fsExc, localizedMessage);
    }

    /**
     * Creates the localized IOException
     * @param localizedMessage to take localization string from
     */
    public static FileNotFoundException createFileNotFoundException(final String localizedMessage, final Exception cause)  {
        final FileNotFoundException fsExc = new FileNotFoundException(localizedMessage);
        fsExc.initCause(cause);
        return Exceptions.attachLocalizedMessage(fsExc, localizedMessage);
    }

    /**
     * Creates the localized IOException
     * @param localizedMessage to take localization string from
     */
    public static ConnectException createConnectException(final String localizedMessage)  {
        final ConnectException fsExc = new ConnectException(localizedMessage);
        return Exceptions.attachLocalizedMessage(fsExc, localizedMessage);
    }

    public static <T extends Throwable> T annotateException(T t) {
        return Exceptions.attachLocalizedMessage(t, t.getLocalizedMessage());
    }
}
