/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.parsing.spi.indexing;

import java.net.URL;

/**
 *
 * @author vita
 */
public abstract class BinaryIndexerFactory {

    /**
     * Creates  new {@link BinaryIndexer}.
     * @return an indexer
     */
    public abstract BinaryIndexer createIndexer();

    /**
     * Called by indexing infrastructure to notify indexer that roots were deregistered,
     * for example the project owning these roots was closed. The indexer may free memory caches
     * for given roots or do any other clean up.
     *
     * @param removedRoots the iterable of removed roots
     * @since 1.19
     */
    public abstract void rootsRemoved (Iterable<? extends URL> removedRoots);

    /**
     * Return the name of this indexer. This name should be unique because GSF
     * will use this name to produce a separate data directory for each indexer
     * where it has its own storage.
     *
     * @return The indexer name. This does not need to be localized since it is
     * never shown to the user, but should contain filesystem safe characters.
     */
    public abstract String getIndexerName ();
    
    /**
     * Return the version stamp of the schema that is currently being stored
     * by this indexer. Along with the index name this string will be used to
     * create a unique data directory for the database.
     *
     * Whenever you incompatibly change what is stored by the indexer,
     * update the version stamp.
     *
     * @return The version stamp of the current index.
     */
    public abstract int getIndexVersion ();

    /**
     * Notifies the indexer that a binary root is going to be scanned.
     *
     * @param context The indexed binary root.
     *
     * @return <code>false</code> means that the whole root should be rescanned
     *   (eg. no up to date check is done, etc)
     * @since 1.29
     */
    public boolean scanStarted (final Context context) {
        return true;
    }

    /**
     * Notifies the indexer that scanning of a binary root just finished.
     *
     * @param context The indexed binary root.
     *
     * @since 1.29
     */
    public void scanFinished (final Context context) {

    }
}
