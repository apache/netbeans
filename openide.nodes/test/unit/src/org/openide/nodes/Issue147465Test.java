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

package org.openide.nodes;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.netbeans.junit.Log;
import org.netbeans.junit.NbTestCase;
import org.openide.util.Exceptions;

/**
 *
 * @author Holy
 */
public class Issue147465Test extends NbTestCase {
    Logger logger = Logger.getLogger(Issue147465Test.class.getName());

    public Issue147465Test(java.lang.String testName) {
        super(testName);
    }

    @Override
    protected Level logLevel() {
        return Level.FINE;
    }

    @Override
    protected int timeOut() {
        return 30000;
    }
    
    public void testDeadlock() throws InterruptedException, InvocationTargetException {
        class Keys extends Children.Keys {

            public Keys(String... args) {
                super(true);
                if (args != null && args.length > 0) {
                    setKeys(args);
                }
            }

            public void keys(String... args) {
                super.setKeys(args);
            }

            public void keys(Collection args) {
                super.setKeys(args);
            }

            protected Node[] createNodes(Object key) {
                AbstractNode an = new AbstractNode(Children.LEAF);
                an.setName(key.toString());
                return new Node[]{an};
            }
        }
        
        class GetFromSnapshot implements Runnable {
            List<Node> snapshot;

            public GetFromSnapshot(List<Node> snapshot) {
                this.snapshot = snapshot;
            }

            public void run() {
                logger.info("getting from snapshot");
                Node n = snapshot.get(0);
                logger.info("obtained from snapshot");
            }
            
        }
        
        Keys ch = new Keys("a", "b", "c");
        Node root = new FilterNode(new AbstractNode(ch));
        Node[] nodes = root.getChildren().getNodes();
        Reference<Object> ref = new WeakReference<Object>(nodes[0]);
        
        Log.controlFlow(Logger.getLogger("org.openide.nodes"), Logger.getLogger("1.2.3.4.5"), 
              "THREAD: AWT-EventQueue-0 MSG: getting from snapshot"
            + "THREAD: Active Reference Queue Daemon MSG: register node"
            + "THREAD: AWT-EventQueue-0 MSG: obtained from snapshot"
            , 5000);
        
        GetFromSnapshot g = new GetFromSnapshot(root.getChildren().snapshot());
        SwingUtilities.invokeLater(g);
        Thread.sleep(1000);
        nodes = null;
        assertGC("should be gced", ref);
        SwingUtilities.invokeAndWait(new Runnable() {

            public void run() {
            }
        });
    }
}
