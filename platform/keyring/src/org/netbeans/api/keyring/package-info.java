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

/**
 * API for secure storage of user secrets such as passwords.
 * <p>Rather than every module in a NetBeans-based application that needs to store
 * passwords for repeated access having to write them to some file on disk using
 * Base64 encoding or similar, this API permits passwords to be stored securely.</p>
 * <div class="nonnormative">
 * <p>There are several platform-specific implementations of password storage
 * currently available in a separate module {@code org.netbeans.modules.keyring.impl},
 * which offer the best combination of security and convenience:</p>
 * <ol>
 * <li>Login-based encryption on Windows. (NetBeans physically stores the encrypted passwords.)</li>
 * <li>Mac OS X Keychain, using the default login keychain.</li>
 * <li>GNOME Keyring, using the default keyring (often unlocked by login).</li>
 * <li>KDE KWallet.</li>
 * </ol>
 * <p>If none of these can be loaded, a fallback implementation is used which encrypts
 * stored passwords using a single master password, as in e.g. Firefox. The user must
 * pick a master password, then enter it once per session if the keyring is accessed.
 * Java's {@code PBEWithSHA1AndDESede} algorithm (SHA-1 / 3-DES) is used to encrypt
 * passwords. It creates a random salt for the user using {@link java.security.SecureRandom}.
 * In addition to the passwords you ask to save, a sample string is saved to
 * verify that an entered master password is correct: the sample must be
 * decryptable and the decrypted value must begin with a magic sequence (the
 * remainder having been generated randomly, again with {@code UUID}).
 * The files in the user directory relating to this fallback keyring are marked
 * {@code go-w} on Unix systems, to discourage brute-force cracking attempts on
 * multiuser machines.</p>
 * <p>If even master password encryption is unavailable, due to missing security
 * providers, or a headless AWT which makes dialogs impossible, or simply because
 * the implementation module is not available, then a trivial
 * implementation is used which just keeps passwords in memory for the duration
 * of the JVM session.</p>
 * </div>
 * <p>Since Java lacks any API for secure non-pageable memory, please consider the
 * following recommendations when working with passwords in memory:</p>
 * <ol>
 * <li>Avoid retaining passwords in instance fields reachable from GC roots other
 * than the active stack frame if possible: get the password from {@link org.netbeans.api.keyring.Keyring#read},
 * pass it on, and do not retain it.</li>
 * <li>Use {@code char[]} in preference to {@code String} where possible.
 * This API works with {@code char[]} only.</li>
 * <li>Zero out a {@code char[]} password if you know you are done with it.
 * See {@link org.netbeans.api.keyring.Keyring#read} and {@link org.netbeans.api.keyring.Keyring#save}
 * for the behavior of this API.</li>
 * </ol>
 */
package org.netbeans.api.keyring;
