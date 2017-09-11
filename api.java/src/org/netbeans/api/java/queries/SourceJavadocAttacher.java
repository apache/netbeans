/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
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
