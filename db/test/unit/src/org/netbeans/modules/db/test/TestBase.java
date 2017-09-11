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

package org.netbeans.modules.db.test;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import javax.swing.SwingUtilities;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;

/**
 * Common ancestor for all test classes.
 *
 * This currently does nothing but keeping it here in case we do want to
 * add common functionality.
 *
 * @author Andrei Badea
 */
public abstract class TestBase extends NbTestCase {
    public TestBase(String name) {
        super(name);
    }

    /**
     * Force flush of config filesystem and EDT.
     *
     * Make sure outstanding writes to the config filesystem and outstanding
     * events on the EDT are flushed
     */
    protected void forceFlush() {
        if (SwingUtilities.isEventDispatchThread()) {
            throw new IllegalStateException(
                    "forceFlush might only be called off the EDT!");
        }
        try {
            FileUtil.getConfigRoot().getFileSystem()
                    .runAtomicAction(new FileSystem.AtomicAction() {
                        @Override
                        public void run() throws IOException {
                            // NOOP - force a wait
                        }
                    });
            Thread.sleep(1 * 1000);
            SwingUtilities.invokeAndWait(new Runnable() {
                @Override
                public void run() {
                    // NOOP - force a wait
                }
            });
        } catch (IOException | InterruptedException | InvocationTargetException ex) {
            throw new RuntimeException(ex);
        }
    }
}
