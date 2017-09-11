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

package org.netbeans.modules.db.api.sql.execute;

import java.util.List;

/**
 * Provides information about the execution of one or more SQL statements.
 *
 * Note this initial implementation is quite simple, just doing as much
 * as I need.  It can be extended to provide more information.
 *
 * @author David Van Couvering
 */
public interface SQLExecutionInfo {
    /**
     * Determine if any statements had exceptions
     *
     * @return true if any statement executed had exceptions
     */
    public boolean hasExceptions();

    /**
     * Get all exceptions for all statements.  This is useful for quickly
     * reporting all exceptions.  Use getStatementInfos() for more complete
     * information about each statement
     *
     * @return the list of all exceptions encountered when executing the SQL, or
     * an empty list if none were encountered
     */
    public List<? extends Throwable> getExceptions();

    /**
     * Get the list of information about the statements that were executed
     *
     * @return list of information about statements executed
     */
    public List<StatementExecutionInfo> getStatementInfos();
}
