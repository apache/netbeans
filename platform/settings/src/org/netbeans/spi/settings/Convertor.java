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

package org.netbeans.spi.settings;

import org.openide.util.Lookup;

/** Convertor allows to read/write objects in own format and notify about
 * object changes.
 *
 * @author  Jan Pokorsky
 */
public abstract class Convertor {

    /** Subclasses can implement own storing format.
     * @param w stream into which inst is written
     * @param inst the setting object to be written
     * @exception java.io.IOException if the object cannot be written
     */
    public abstract void write (java.io.Writer w, Object inst) throws java.io.IOException;

    /** Subclasses have to be able to read format implemented by {@link #write}.
     * @param r stream containing stored object
     * @return the read setting object
     * @exception java.io.IOException if the object cannot be read
     * @exception ClassNotFoundException if the object class cannot be resolved
     */
    public abstract Object read (java.io.Reader r) throws java.io.IOException, ClassNotFoundException;
    
    /** register {@link Saver saver}; convertor can provide own policy notifing
     * the saver about changes of setting object. (e.g. register property
     * change listener)
     * @param inst setting object
     * @param s saver implementation
     */
    public abstract void registerSaver (Object inst, Saver s);
    
    /** unregister {@link Saver saver}
     * @param inst setting object
     * @param s saver implementation
     * @see #registerSaver
     */
    public abstract void unregisterSaver (Object inst, Saver s);
    
    /** get a context associated with the reader <code>r</code>. It can contain
     * various info like a file location of the read object etc.
     * @param r stream containing stored object
     * @return a context associated with the reader
     * @since 1.2
     */
    protected static org.openide.util.Lookup findContext(java.io.Reader r) {
        if (r instanceof Lookup.Provider) {
            return ((Lookup.Provider) r).getLookup();
        } else {
            return Lookup.EMPTY;
        }
    }
    
    /** get a context associated with the writer <code>w</code>. It can contain
     * various info like a file location of the written object etc.
     * @param w stream into which inst is written
     * @return a context associated with the reader
     * @since 1.2
     */
    protected static org.openide.util.Lookup findContext(java.io.Writer w) {
        if (w instanceof Lookup.Provider) {
            return ((Lookup.Provider) w).getLookup();
        } else {
            return Lookup.EMPTY;
        }
    }
    
}
