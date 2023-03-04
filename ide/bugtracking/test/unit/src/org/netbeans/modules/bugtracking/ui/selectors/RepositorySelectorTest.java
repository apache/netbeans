/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
