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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.spi.autoupdate;

import java.io.File;
import java.io.IOException;

/** Class that is supposed to be implemented by application
 * providers that can control launcher in order to modify
 * the list of provided clusters.
 *
 * @since 1.2
 * @author  Jaroslav Tulach
 */
public abstract class AutoupdateClusterCreator extends Object {
    /** Finds the right cluster directory for given cluster name.
     * This method can return null if no such cluster name is known or 
     * understandable, otherwise it returns a file object representing
     * <b>not existing</b> directory that will be created later
     * to host hold the content of the cluster.
     * 
     * @param clusterName the name of the cluster the autoupdate client is searching for
     * @return null or File object of the cluster to be created
     */
    protected abstract File findCluster(String clusterName);
    
    /** Changes the launcher to know about the new cluster and 
     * use it next time the system starts.
     * 
     * @param clusterName the name of the cluster
     * @param cluster file previously returned by findCluster
     * @return the list of current cluster directories, including the newly added one
     * @exception IOException if the registration fails
     */
    protected abstract File[] registerCluster(String clusterName, File cluster) throws IOException;
}
