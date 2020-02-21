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

package org.netbeans.modules.cnd.spi.remote.setup;

import java.io.IOException;
import java.net.ConnectException;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;

/**
 * Allows to redefine places where local->remote and remote->local mirrors are located
 */
public interface MirrorPathProvider {

    /**
     * Gets remote mirror absolute path.
     * Remote mirror is a place to which project files are copied
     * in the case non-sharing synchronization.
     * @param executionEnvironment execution environment
     * @return remote mirror absolute path
     * or null in the case this provider can not provide it for the given environment
     */
    String getRemoteMirror(ExecutionEnvironment executionEnvironment) throws ConnectException, IOException, ConnectionManager.CancellationException;

    /**
     * Gets local mirror absolute path.
     * Local mirror is used for copying remote files cache on the local machine
     * (for now, remote headers are processed this way)
     * @param executionEnvironment execution environment
     * @return local mirror absolute path
     * or null in the case this provider can not provide it for the given environment
     *
     * The method is not used in 6.8; it is reserved for post 6.8 use
     */
    String getLocalMirror(ExecutionEnvironment executionEnvironment);
}
