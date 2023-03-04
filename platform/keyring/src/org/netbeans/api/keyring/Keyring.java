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

package org.netbeans.api.keyring;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressUtils;
import org.netbeans.spi.keyring.KeyringProvider;
import org.openide.util.Cancellable;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Parameters;
import org.openide.util.RequestProcessor;

/**
 * Client class for working with stored keys (such as passwords).
 * <p>The key identifier should be unique for the whole application,
 * so qualify it with any prefixes as needed.
 * <p> <i>Since 1.10</i> it is allowed to call methods of this class from even
 * dispatch thread.
 */
public class Keyring {

    // throughput 1 is intentional
    private static final RequestProcessor KEYRING_ACCESS = new RequestProcessor(Keyring.class);

    private static final long SAFE_DELAY = 70;

    private Keyring() {}

    private static final Logger LOG = Logger.getLogger("org.netbeans.modules.keyring");

    private static KeyringProvider PROVIDER;
    private static synchronized KeyringProvider provider() {
        if (PROVIDER == null) {
            for (KeyringProvider p : Lookup.getDefault().lookupAll(KeyringProvider.class)) {
                if (p.enabled()) {
                    PROVIDER = p;
                    break;
                }
            }
            if (PROVIDER == null) {
                PROVIDER = new DummyKeyringProvider();
            }
            LOG.log(Level.FINE, "Using provider: {0}", PROVIDER);
        }
        return PROVIDER;
    }

    private static synchronized char[] readImpl(String key) {
        LOG.log(Level.FINEST, "reading: {0}", key);
        return provider().read(key);
    }

    /**
     * Reads a key from the ring.
     * <p>
     * This method can be called from any thread.
     * All the changes done by previous calls to {@link #delete(java.lang.String)}
     * or {@link #save(java.lang.String, char[], java.lang.String)} methods
     * are guaranteed to be visible by subsequent calls to this method.
     *
     * @param key the identifier of the key
     * @return its value if found (you may null out its elements), else null if not present
     */
    @NbBundle.Messages("MSG_KeyringAccess=Requesting keyring access")
    @CheckForNull
    public static char[] read(@NonNull final String key) {
        Parameters.notNull("key", key);

        try {
            final Future<char[]> result = KEYRING_ACCESS.submit(new Callable<char[]>() {
                @Override
                public char[] call() throws Exception {
                    return Keyring.readImpl(key);
                }
            });

            if (SwingUtilities.isEventDispatchThread()) {
                if (!result.isDone()) {
                    try {
                        // lets wait in awt to avoid flashing dialogs
                        return result.get(SAFE_DELAY, TimeUnit.MILLISECONDS);
                    } catch (TimeoutException ex) {
                        // show progress dialog
                        return ProgressUtils.showProgressDialogAndRun(
                                new ProgressRunnable<char[]>(result), Bundle.MSG_KeyringAccess(), false);
                    }
                }
            }
            return result.get();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        } catch (ExecutionException ex) {
            LOG.log(Level.INFO, null, ex);
        }
        return null;
    }

    private static synchronized void saveImpl(String key, char[] password, String description) {
        LOG.log(Level.FINEST, "saving: {0}", key);
        provider().save(key, password, description);
        Arrays.fill(password, (char) 0);
    }

    /**
     * Saves a key to the ring.
     * If it could not be saved, does nothing.
     * If the key already existed, overwrites the password.
     * <p>
     * This method can be called from any thread.
     * The changes done by multiple calls to {@link #delete(java.lang.String)}
     * or {@link #save(java.lang.String, char[], java.lang.String)} methods
     * are guaranteed to be processed in order in which they were called.
     *
     * @param key a key identifier
     * @param password the password or other sensitive information associated with the key
     *                 (its contents will be nulled out by end of call)
     * @param description a user-visible description of the key (may be null)
     */
    public static void save(@NonNull final String key, @NonNull final char[] password,
            @NullAllowed final String description) {

        Parameters.notNull("key", key);
        Parameters.notNull("password", password);

        KEYRING_ACCESS.post(new Runnable() {

            @Override
            public void run() {
                Keyring.saveImpl(key, password, description);
            }
        });
    }

    private static synchronized void deleteImpl(String key) {
        LOG.log(Level.FINEST, "deleting: {0}", key);
        provider().delete(key);
    }

    /**
     * Deletes a key from the ring.
     * If the key was not in the ring to begin with, does nothing.
     * <p>
     * This method can be called from any thread.
     * The changes done by multiple calls to {@link #delete(java.lang.String)}
     * or {@link #save(java.lang.String, char[], java.lang.String)} methods
     * are guaranteed to be processed in order in which they were called.
     *
     * @param key a key identifier
     */
    public static void delete(@NonNull final String key) {
        Parameters.notNull("key", key);

        KEYRING_ACCESS.post(new Runnable() {

            @Override
            public void run() {
                Keyring.deleteImpl(key);
            }
        });
    }

    private static class DummyKeyringProvider implements KeyringProvider {
        public @Override boolean enabled() {
            return true;
        }
        // prefer byte[] to make passwords less readable in heap dumps:
        private final Map<String,byte[]> passwords = new HashMap<String,byte[]>();
        public @Override char[] read(String key) {
            byte[] pwd = passwords.get(key);
            return pwd != null ? bytes2Chars(pwd) : null;
        }
        public @Override void save(String key, char[] password, String description) {
            passwords.put(key, chars2Bytes(password));
        }
        public @Override void delete(String key) {
            passwords.remove(key);
        }
    }

    // XXX copied from org.netbeans.modules.keyring.impl.Utils:
    private static byte[] chars2Bytes(char[] chars) {
        byte[] bytes = new byte[chars.length * 2];
        for (int i = 0; i < chars.length; i++) {
            bytes[i * 2] = (byte) (chars[i] / 256);
            bytes[i * 2 + 1] = (byte) (chars[i] % 256);
        }
        return bytes;
    }
    private static char[] bytes2Chars(byte[] bytes) {
        char[] result = new char[bytes.length / 2];
        for (int i = 0; i < result.length; i++) {
            result[i] = (char) (((int) bytes[i * 2]) * 256 + (int) bytes[i * 2 + 1]);
        }
        return result;
    }

    private static class ProgressRunnable<T> implements org.netbeans.api.progress.ProgressRunnable<T>, Cancellable {

        private final Future<? extends T> task;

        public ProgressRunnable(Future<? extends T> task) {
            this.task = task;
        }

        @Override
        public T run(ProgressHandle handle) {
            try {
                return task.get();
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            } catch (ExecutionException ex) {
                LOG.log(Level.INFO, null, ex);
            }
            return null;
        }

        @Override
        public boolean cancel() {
            return task.cancel(true);
        }
    }
}
