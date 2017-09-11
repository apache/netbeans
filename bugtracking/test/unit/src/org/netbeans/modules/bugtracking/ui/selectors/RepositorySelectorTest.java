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

package org.netbeans.modules.bugtracking.ui.selectors;

import java.awt.Image;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.util.logging.Level;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.event.ChangeListener;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.bugtracking.TestKit;
import org.netbeans.modules.bugtracking.TestRepository;
import org.netbeans.modules.bugtracking.RepositoryImpl;
import org.netbeans.modules.bugtracking.spi.*;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;

/**
 *
 * @author tomas
 */
public class RepositorySelectorTest extends NbTestCase {

    public RepositorySelectorTest(String arg0) {
        super(arg0);
    }

    @Override
    protected Level logLevel() {
        return Level.ALL;
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    public void testRepositorySelectorBuilder() throws MalformedURLException, IOException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        String errorMsg = "tpyo";
        MyRepository repo = new MyRepository();

        SelectorPanel sp = new SelectorPanel();
        createEditDescriptor(sp, TestKit.getRepository(repo), errorMsg);

        String text = getErrroLabelText(sp);
        assertEquals(errorMsg, text);
    }

    private void createEditDescriptor(SelectorPanel sp, RepositoryImpl repository, String error) throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        Method m = sp.getClass().getDeclaredMethod("createEditDescriptor", RepositoryImpl.class, String.class);
        m.setAccessible(true);
        m.invoke(sp, repository, error);
    }


    private String getErrroLabelText(SelectorPanel sp) throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        RepositorySelectorBuilder builder = (RepositorySelectorBuilder) getField(sp, "builder");
        RepositoryFormPanel form = (RepositoryFormPanel) getField(builder, "repositoryFormsPanel");
        JTextArea label = (JTextArea) getField(form, "errorText");
        return label.getText();
    }

    private Object getField(Object o, String name) throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        Field f = o.getClass().getDeclaredField(name);
        f.setAccessible(true);
        return f.get(o);
    }

    private class MyRepository extends TestRepository {
        private RepositoryInfo info;

        public MyRepository() {
            this.info = new RepositoryInfo("repoid", "connectorid", "http://foo.bar/bogus", "My repository", "My repository", null, null, null, null);
        }

        
        @Override
        public Image getIcon() {
            return null;
        }

        @Override
        public RepositoryInfo getInfo() {
            return info;
        }

        @Override
        public RepositoryController getController() {
            final JPanel panel = new JPanel();
            JLabel label = new JLabel();
            label.setText("<html>" +
                            getInfo().getDisplayName() + "</br>" +
                            getInfo().getUrl() +
                          "</html>");
            panel.add(label);
            return new RepositoryController() {

                @Override
                public JComponent getComponent() {
                    return panel;
                }

                @Override
                public HelpCtx getHelpCtx() {
                    return null;
                }

                @Override
                public boolean isValid() {
                    return false;
                }

                @Override
                public void applyChanges() {

                }

                @Override
                public void populate() {
                    
                }

                @Override
                public String getErrorMessage() {
                    return null;
                }

                @Override
                public void addChangeListener(ChangeListener l) {
                    
                }

                @Override
                public void removeChangeListener(ChangeListener l) {
                    
                }

                @Override
                public void cancelChanges() {
                    
                }
            };
        }

        public Lookup getLookup() {
            return Lookup.EMPTY;
        }
    }

}
