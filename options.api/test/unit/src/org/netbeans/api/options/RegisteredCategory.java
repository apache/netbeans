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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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
