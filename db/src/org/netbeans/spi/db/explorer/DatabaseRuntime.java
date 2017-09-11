/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.spi.db.explorer;

/**
 * Represents an instance of a database server, typically a local installation which can be
 * started when a connection to this server is being made, or stopped upon IDE shutdown.
 *
 * <p>Implementations of this class should be put in the Databases/Runtimes folder
 * in the default filesystem.</p>
 *
 * @author Nam Nguyen, Andrei Badea
 */
public interface DatabaseRuntime {

    /**
     * Returns the JDBC driver class which is used to make connections to the
     * represented database server instance.
     *
     * <p>When a connection is being made, only the database runtimes which have
     * the same JDBC driver as the driver used by this connection are considered 
     * for further usage (e.g., starting the database server instance).</p>
     * 
     * @return the fully-qualified class name of the driver used to make
     * connections to the represented database server instance.
     */
    public String getJDBCDriverClass();
    
    /**
     * Returns whether this runtime accepts this database URL (the database URL
     * would cause a connection to be made to the database server instance 
     * represented by this runtime).
     *
     * @param url the database URL
     * 
     * @return true if the runtime accepts this database URL; false otherwise.
     */
    boolean acceptsDatabaseURL(String url);
    
    /**
     * Returns the state (running/not running) of the represented database server
     * instance.
     *
     * @return true if the database server instance is running; false otherwise.
     */
    boolean isRunning();
    
    /**
     * Returns whether the database server instance can be started by a call to the 
     * {@link #start} method.
     *
     * @return true if the database server instance can be started; false
     * otherwise.
     */
    public boolean canStart();

    /**
     * Starts the database server instance represented by this runtime.
     */
    void start();
    
    /**
     * Stops the database server instance represented by this runtime.
     */
    void stop();
}
