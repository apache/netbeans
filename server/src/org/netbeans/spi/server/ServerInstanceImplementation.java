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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.spi.server;

import javax.swing.JComponent;
import org.openide.nodes.Node;

/**
 * The representation of the single server instance. Interface describes
 * the instance and provides needed operations.
 *
 * @author Petr Hejl
 * @see ServerInstanceProvider
 */
public interface ServerInstanceImplementation {

    /**
     * Returns the display name of the instance.
     *
     * @return the display name of the instance
     */
    String getDisplayName();

    /**
     * Returns the display name of the server type to which this instance belongs.
     *
     * @return the display name of the server type to which this instance belongs
     */
    String getServerDisplayName();

    /**
     * Returns the node representing the runtime instance. The node should
     * display instance status and provide actions to manage the server.
     *
     * @return the node representing the instance, may return <code>null</code>
     */
    Node getFullNode();

    /**
     * Returns the node representing the instance while configuring it.
     * The node should not display any status, actions or children.
     *
     * @return the node representing the instance, may return <code>null</code>
     */
    Node getBasicNode();

    /**
     * Returns the component allowing the customization of the instance. May
     * return <code>null</code>.
     * <p>
     * Always called from Event Dispatch Thread.
     *
     * @return the component allowing the customization of the instance,
     *             may return <code>null</code>
     */
    JComponent getCustomizer();

    /**
     * Removes the instance. No {@link ServerInstanceProvider} should return
     * this instance once it is removed.
     */
    void remove();

    /**
     * Returns <code>true</code> if the instance can be removed by
     * {@link #remove()}. Otherwise returns <code>false</code>.
     *
     * @return <code>true</code> if the instance can be removed
     */
    boolean isRemovable();

}
