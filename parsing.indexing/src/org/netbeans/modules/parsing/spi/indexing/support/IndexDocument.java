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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.parsing.spi.indexing.support;

import org.openide.util.Parameters;

/**
 * An IndexDocument lets you store a series of [key,value] pairs in the
 * persistent store.
 *
 * @author Tor Norbye
 * @author Tomas Zezula
 */
//@ThreadSafe
public final class IndexDocument {

    final org.netbeans.modules.parsing.lucene.support.IndexDocument spi;

    IndexDocument (final org.netbeans.modules.parsing.lucene.support.IndexDocument spi) {
        Parameters.notNull("spi", spi);
        this.spi = spi;
    }

    /**
     * Add a [key,value] pair to this document. Note that the document really
     * contains a multi-map, so it is okay and normal to call addPair multiple
     * times with the same key. This just adds the value to the set of values
     * associated with the key.
     *
     * @param key The key that you will later search by. Note that you are NOT
     *   allowed to use the keys <code>filename</code> or <code>timestamp</code>
     *   since these are reserved (and in fact used) by GSF.
     * @param value The value that will be retrieved for this key
     * @param searchable A boolean which if set to true will store the pair with
     *   an indexed/searchable field key, otherwise with an unindexed field (that cannot be
     *   searched).  You <b>must</b> be consistent in how keys are identified
     *   as searchable; the same key must always be referenced with the same
     *   value for searchable when pairs are added (per document).
     */
    public void addPair( /*@NonNull*/ String key, /*@NonNull*/ String value, boolean searchable, boolean stored) {
        Parameters.notNull("key", key); //NOI18N
        Parameters.notEmpty("key", key);    //NOI18N
        Parameters.notNull("value", value); //NOI18N
        this.spi.addPair(key, value, searchable, stored);
    }    
}
