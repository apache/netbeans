/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.source.usages;

import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.java.source.indexing.TransactionContext;
import org.netbeans.modules.parsing.api.indexing.IndexingManager;

/**
 * Commits Class Indexes during scanFinished.
 *
 * @author sdedic
 */
//@NotThreadSafe
public final class PersistentIndexTransaction extends TransactionContext.Service {

    private static final Logger LOG = Logger.getLogger(PersistentIndexTransaction.class.getName());

    private final URL root;

    private ClassIndexImpl.Writer indexWriter;
    private boolean closedTx;
    private boolean brokenIndex;

    private PersistentIndexTransaction(@NonNull final URL root) {
        this.root = root;
    }

    @NonNull
    public static PersistentIndexTransaction create(@NonNull final URL root) {
        return new PersistentIndexTransaction(root);
    }

    @Override
    protected void commit() throws IOException {
        closeTx();
        if (indexWriter != null) {
            if (!brokenIndex) {
                try {
                    indexWriter.commit();
                } catch (Throwable t) {
                    if (t instanceof ThreadDeath) {
                        throw (ThreadDeath) t;
                    } else {
                        LOG.log(
                            Level.WARNING,
                            "Broken index for root: {0} reason: {1}, recovering.",  //NOI18N
                            new Object[] {
                                root,
                                t.getMessage()
                            });
                        brokenIndex = true;
                    }
                }
            } else {
                rollBackImpl();
            }
            if (brokenIndex) {
                handleBrokenRoot();
            }
        }
    }

    @Override
    protected void rollBack() throws IOException {
        closeTx();
        if (indexWriter != null) {            
            rollBackImpl();
            if (brokenIndex) {
                handleBrokenRoot();
            }
        }
    }
    
    public void setIndexWriter(@NonNull ClassIndexImpl.Writer writer) {
        assert this.indexWriter == null;
        assert writer != null;
        this.indexWriter = writer;
    }

    public void setBroken() {
        brokenIndex = true;
    }
    
    @CheckForNull
    public ClassIndexImpl.Writer getIndexWriter() {
        return this.indexWriter;
    }

    private void closeTx() {
        if (closedTx) {
            throw new IllegalStateException("Already commited or rolled back transaction.");    //NOI18N
        }
        closedTx = true;
    }

    private void handleBrokenRoot() throws IOException {
        indexWriter.clear();
        IndexingManager.getDefault().refreshIndex(root, null, true, false);
    }

    private void rollBackImpl() {
        try {
            indexWriter.rollback();
        } catch (Throwable t) {
            if (t instanceof ThreadDeath) {
                throw (ThreadDeath) t;
            } else {
                LOG.log(
                    Level.WARNING,
                    "Broken index for root: {0} reason: {1}, recovering.",  //NOI18N
                    new Object[] {
                        root,
                        t.getMessage()
                    });
                brokenIndex = true;
            }
        }
    }
}
