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

package org.netbeans.modules.db.metadata.model.api;

import org.netbeans.modules.db.metadata.model.spi.ForeignKeyColumnImplementation;

/**
 * This class defines a column that is a foreign key, referring to a
 * column in the primary key of another table.
 * 
 * @author David Van Couvering
 */
public class ForeignKeyColumn extends MetadataElement {
    ForeignKeyColumnImplementation impl;

    ForeignKeyColumn(ForeignKeyColumnImplementation impl) {
        this.impl = impl;
    }

    @Override
    public ForeignKey getParent() {
        return impl.getParent();
    }

    @Override
    public String getName() {
        return impl.getName();
    }

    /**
     * Get the column in the source table which is referring to the column
     * in the target table.
     *
     * @return the referring column definition
     */
    public Column getReferringColumn() {
        return impl.getReferringColumn();
    }

    /**
     * Get the primary key column in the target table which is being referred to
     *
     * @return the referred column
     */
    public Column getReferredColumn() {
        return impl.getReferredColumn();
    }

    /**
     * Get the position of this column in the foreign key
     *
     * @return the position of the column in the foreign key
     */
    public int getPosition() {
        return impl.getPosition();
    }
}
