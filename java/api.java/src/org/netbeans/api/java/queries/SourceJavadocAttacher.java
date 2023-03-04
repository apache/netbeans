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
package org.netbeans.api.java.queries;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.spi.java.queries.SourceJavadocAttacherImplementation;
import org.openide.util.Lookup;
import org.openide.util.Parameters;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

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
            attach(root, listener, 0, null);
    }

    /**
     * Attaches a source root provided by the SPI {@link SourceJavadocAttacherImplementation}
     * to given binary root. If `context is given, it is used for 2 purposes:
     * <ul>
     * <li>locate additional attacher plugins ({@link SourceJavadocAttacherImplementation}s)
     * <li>provide information for {@link SourceJavadocAttacherImplementation}s
     * </ul>
     * Typically a Project lookup can be passed in.
     * <p>
     * If a {@link SourceJavadocAttacherImplementation} fails, the next one willing to handle 
     * the URL is tried. Final result is reported through the {@link AttachmentListener}.
     * 
     * @param root the binary root to attach sources to
     * @param listener notified about result when attaching is done
     * @param context additional context for the operation: may contain project, or additional info
     * consumed by SPI implementors.
     * @since 1.78
     */
    public static void attachSources(
            @NonNull final URL root,
            @NonNull final Lookup context,
            @NullAllowed final AttachmentListener listener) {
            attach(root, listener, 0, context);
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
            attach(root, listener, 1, null);
    }

    /**
     * Attaches a javadoc root provided by the SPI {@link SourceJavadocAttacherImplementation}
     * to given binary root. If `context is given, it is used for 2 purposes:
     * <ul>
     * <li>locate additional attacher plugins ({@link SourceJavadocAttacherImplementation}s)
     * <li>provide information for {@link SourceJavadocAttacherImplementation}s
     * </ul>
     * Typically a Project lookup can be passed in.
     * <p>
     * If a {@link SourceJavadocAttacherImplementation} fails, the next one willing to handle 
     * the URL is tried. Final result is reported through the {@link AttachmentListener}.
     * @param root the binary root to attach sources to
     * @param listener notified about result when attaching is done
     * @param context additional context for the operation: may contain project, or additional info
     * consumed by SPI implementors.
     * @since 1.78
     */
    public static void attachJavadoc(
            @NonNull final URL root,
            @NonNull final Lookup context,
            @NullAllowed final AttachmentListener listener) {
            attach(root, listener, 1, context);
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
    
    /**
     * Calls individual {@link SourceJavadocAttacherImplementation}s. Calls the next implementation
     * in case the predecessor reports that it handles the root, but fails.
     */
    private static class AttacherExecution implements AttachmentListener, Runnable {
        private final Lookup lkp;
        private final int mode;
        private final URL root;
        private final AttachmentListener delegate;
        private final List<? extends SourceJavadocAttacherImplementation> attachers;
        private int index;
        private int attempts;

        public AttacherExecution(int mode, URL root, Lookup lkp, AttachmentListener delegate) {
            this.mode = mode;
            this.root = root;
            this.delegate = delegate;
            this.lkp = lkp;
            this.attachers = new ArrayList<>(lkp.lookupAll(SourceJavadocAttacherImplementation.class));
        }

        @Override
        public void attachmentSucceeded() {
            if (delegate != null) {
                delegate.attachmentSucceeded();
            }
        }

        @Override
        public void attachmentFailed() {
            if (isEmpty()) {
                if (delegate != null) {
                    int a;
                    synchronized (this) {
                        a = attempts;
                    }
                    LOG.log(
                        Level.FINE,
                        "No provider from {2} invoked ({3} total) succeeded attaching of {0} to root: {1}",    //NOI18N
                        new Object[]{
                            (mode == 0) ? "sources" : "javadoc",
                            root,
                            a, attachers.size()
                        });
                    delegate.attachmentFailed();
                }
                return;
            }
            run();
        }
        
        synchronized boolean isEmpty() {
            return index >= attachers.size();
        }
        
        private boolean currentHandles;

        @Override
        public void run() {
            for (int pos = index; pos < attachers.size(); ) {
                SourceJavadocAttacherImplementation attacher = attachers.get(pos++);
                synchronized (this) {
                    index = pos;
                    // count well, even if the attacher calls succeeded / failed sycnhronously.
                    attempts++;
                    Lookups.executeWith(lkp, () -> {
                        try {
                            currentHandles  = mode == 0 ?
                                attacher.attachSources(root, this) :
                                attacher.attachJavadoc(root, this);
                        } catch (IOException ioe) {
                            LOG.log(Level.WARNING, "Attacher {1} failed for {0}", new Object[] { root, attacher.getClass().getName()} );
                            LOG.log(Level.WARNING, "Thrown exception: ", ioe);
                        }
                    });
                }
                if (currentHandles) {
                    LOG.log(Level.FINE,
                            "Attaching of {0} to root: {1} handled by: {2}",    //NOI18N
                            new Object[]{
                                (mode == 0) ? "sources" : "javadoc",
                                root,
                                attacher.getClass().getName()
                            });
                    LOG.log(Level.FINE, "Remaingin attachers: {0}", attachers.subList(pos, attachers.size()));
                    return;
                } else {
                    // compensate the count, in case attacher rejects.
                    attempts--;
                }
            }
        }
    }
    
    private static void attach(
            final URL root,
            @NullAllowed AttachmentListener aListener,
            final int mode, final Lookup context) {
        Parameters.notNull("root", root);   //NOI18N
        AttachmentListener listener = aListener != null ? aListener : new AttachmentListener() {
                @Override public void attachmentSucceeded() {}
                @Override public void attachmentFailed() {}
        };
        Lookup attacherLookup = context == null ?
                Lookup.getDefault() : new ProxyLookup(context, Lookup.getDefault());
        new AttacherExecution(mode, root, attacherLookup, listener).run();
    }
}
