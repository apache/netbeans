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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.openide.actions;

import java.awt.event.ActionEvent;
import javax.swing.JComponent;
import org.openide.awt.DynamicMenuContent;
import javax.swing.Action;
import javax.swing.AbstractAction;
import javax.swing.JMenuItem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.FileObject;
import org.netbeans.junit.NbTestCase;
import org.openide.util.Lookup;
import static org.junit.Assert.*;
import org.openide.util.Exceptions;
import org.openide.util.actions.Presenter;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class ToolsActionSlowTest extends NbTestCase {

    public ToolsActionSlowTest(String n) {
        super(n);
    }

    @Override
    protected int timeOut() {
        return 15000;
    }

    @Override
    protected void setUp() throws Exception {
        FileObject fo = FileUtil.createFolder(FileUtil.getConfigRoot(), "UI/ToolActions");
        fo.delete();
    }

    
    public void testInlineIsNotBlocked() throws Exception {
        ToolsAction ta = ToolsAction.get(ToolsAction.class);

        Lookup lkp = Lookups.singleton(this);
        FileObject fo = FileUtil.createFolder(FileUtil.getConfigRoot(), "UI/ToolActions");
        assertNotNull("ToolActions folder found", fo);

        fo.createFolder("Cat3").createData(BlockingAction.class.getName().replace('.', '-') + ".instance").setAttribute("position", 100);

        Action a = ta.createContextAwareInstance(lkp);
        assertTrue("It is menu presenter", a instanceof Presenter.Menu);
        Presenter.Menu mp = (Presenter.Menu)a;
        JMenuItem item = mp.getMenuPresenter();

        assertTrue("Item is enabled", item.isEnabled());
        DynamicMenuContent dmc = (DynamicMenuContent)item;
        final JComponent[] arr = dmc.getMenuPresenters();
        assertEquals("One presenter to delegte to", 1, arr.length);
        assertFalse("Disabled", arr[0].isEnabled());
    }

    public static final class BlockingAction extends AbstractAction {
        private static boolean proceed;
        
        public BlockingAction() {
            synchronized (BlockingAction.class) {
                while (!proceed) {
                    try {
                        BlockingAction.class.wait();
                    } catch (InterruptedException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }
        }
        
        @Override
        public void actionPerformed(ActionEvent e) {
        }
        
    }
}
