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
package org.netbeans.api.java.queries;

import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.spi.java.queries.SourceJavadocAttacherImplementation;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.Parameters;

/**
 * A support for attaching source roots and javadoc roots to binary roots.
 * @author Tomas Zezula
 * @since 1.35
 */
public final class SourceJavadocAttacher {

    private static final Logger LOG = Logger.getLogger(SourceJavadocAttacher.class.getName());

    private SourceJavadocAttacher() {}

    /**
     * Attaches a source root provided by the SPI {@link SourceJavadocAttacherImplementation}
     * to given binary root.
     * @param root the binary root to attach sources to
     * @param listener notified about result when attaching is done
     */
    public static void attachSources(
            @NonNull final URL root,
            @NullAllowed final AttachmentListener listener) {
            attach(root, listener, 0);
    }

    /**
     * Attaches a javadoc root provided by the SPI {@link SourceJavadocAttacherImplementation}
     * to given binary root.
     * @param root the binary root to attach javadoc to
     * @param listener notified about result when attaching is done
     */
    public static void attachJavadoc(
            @NonNull final URL root,
            @NullAllowed final AttachmentListener listener) {
            attach(root, listener, 1);
    }

    /**
     * Listener notified by {@link SourceJavadocAttacher} about a result
     * of attaching source (javadoc) to binary root.
     */
    public interface AttachmentListener {
        /**
         * Invoked when the source (javadoc) was successfully attached to
         * binary root.
         */
        void attachmentSucceeded();
        /**
         * Invoked when the source (javadoc) was not attached to
         * binary root. The attaching either failed or was canceled by user.
         */
        void attachmentFailed();
    }

    private static void attach(
            final URL root,
            @NullAllowed AttachmentListener listener,
            final int mode) {
        Parameters.notNull("root", root);   //NOI18N
        if (listener == null) {
            listener = new AttachmentListener() {
                @Override public void attachmentSucceeded() {}
                @Override public void attachmentFailed() {}
            };
        }
        try {
            for (SourceJavadocAttacherImplementation attacher : Lookup.getDefault().lookupAll(SourceJavadocAttacherImplementation.class)) {
                final boolean handles  = mode == 0 ?
                    attacher.attachSources(root, listener) :
                    attacher.attachJavadoc(root, listener);
                if (handles) {
                    LOG.log(Level.FINE,
                            "Attaching of {0} to root: {1} handled by: {2}",    //NOI18N
                            new Object[]{
                                (mode == 0) ? "sources" : "javadoc",
                                root,
                                attacher.getClass().getName()
                            });
                    return;
                }
            }
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
        }
        if (listener != null) {
            listener.attachmentFailed();
        }
        LOG.log(
            Level.FINE,
            "No provider handled attaching of {0} to root: {1}",    //NOI18N
            new Object[]{
                (mode == 0) ? "sources" : "javadoc",
                root
            });
    }
}
