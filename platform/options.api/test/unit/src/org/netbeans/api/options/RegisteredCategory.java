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

package org.netbeans.api.options;

import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.HashSet;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import junit.framework.TestCase;
import org.netbeans.spi.options.OptionsCategory;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.HelpCtx;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;


/**
 */
public final class RegisteredCategory extends OptionsCategory {
    private static Icon icon;
    private static PropertyChangeListener propertyChangeListener;
    private Collection<String> calls = new HashSet<String>();

    public void setInvalid() {
        propertyChangeListener.propertyChange(new PropertyChangeEvent(this, OptionsPanelController.PROP_VALID, null, null));
    }
    
    public void helpChanged() {
        propertyChangeListener.propertyChange(new PropertyChangeEvent(this, OptionsPanelController.PROP_HELP_CTX, null, null));
    }
    

    @Override
    public Icon getIcon() {
        if (icon == null) {
            Image image = ImageUtilities.loadImage("org/netbeans/modules/options/resources/advanced.png");
            icon = new ImageIcon(image);
        }
        return icon;
    }

    public String getCategoryName() {
        return "CTL_General_Options";
    }

    public String getTitle() {
        return "CTL_General_Options_Title";
    }

    public String getDescription() {
        return "CTL_General_Options_Description";
    }
    
    public void assertThreadingForAllCallsWereTested() {
        TestCase.assertTrue(calls.contains("update()"));
        TestCase.assertTrue(calls.contains("cancel()"));        
        TestCase.assertTrue(calls.contains("isValid()"));
        TestCase.assertTrue(calls.contains("getLookup()"));
        TestCase.assertTrue(calls.contains("getComponent()"));        
        TestCase.assertTrue(calls.contains("applyChanges()"));
        
    }

    public static String subcategoryID;

    public OptionsPanelController create() {
        return new OptionsPanelController() {

            public void update() {
                TestCase.assertTrue(SwingUtilities.isEventDispatchThread());
                calls.add("update()");
            }

            public void applyChanges() {
                TestCase.assertTrue(!SwingUtilities.isEventDispatchThread());
                TestCase.assertTrue(calls.contains("update()"));
                calls.add("applyChanges()");
            }

            public void cancel() {
                TestCase.assertTrue(SwingUtilities.isEventDispatchThread());
                TestCase.assertTrue(calls.contains("update()"));
                calls.add("cancel()");
            }

            public boolean isValid() {
                TestCase.assertTrue(SwingUtilities.isEventDispatchThread());
                calls.add("isValid()");
                return true;
            }

            public boolean isChanged() {
                return false;
            }

            public HelpCtx getHelpCtx() {
                return null;
            }
            
            @Override
            public Lookup getLookup() {
                TestCase.assertFalse(SwingUtilities.isEventDispatchThread());
                calls.add("getLookup()");                
                return super.getLookup();
            }
            

            public JComponent getComponent(Lookup masterLookup) {
                TestCase.assertTrue(SwingUtilities.isEventDispatchThread());
                calls.add("getComponent()");
                return new JLabel();
            }
            
            @Override
            public void setCurrentSubcategory(String id) {
                subcategoryID = id;
            }

            public void addPropertyChangeListener(PropertyChangeListener l) {
                propertyChangeListener = l;
            }

            public void removePropertyChangeListener(PropertyChangeListener l) {
                propertyChangeListener = null;
            }
        };        
    }
}
