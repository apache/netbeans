/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http:www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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

package org.myorg.systemproperties;

import java.io.IOException;
import java.util.Properties;
import java.util.ResourceBundle;
import javax.swing.Action;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.actions.DeleteAction;
import org.openide.actions.PropertiesAction;
import org.openide.actions.RenameAction;
import org.openide.actions.ToolsAction;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;

/**
 *
 * @author Administrator
 */
public class OnePropNode extends AbstractNode {
    private static ResourceBundle bundle = NbBundle.getBundle(OnePropNode.class);
    private String key;
    private ChangeListener listener;
    public OnePropNode(String key) {
        super(Children.LEAF);
        this.key = key;
        setIconBase("org/myorg/systemproperties/onePropIcon");
        //setDefaultAction(SystemAction.get(PropertiesAction.class));
        super.setName(key);
        setShortDescription(bundle.getString("HINT_OnePropNode"));
    }
    
    
    public Action[] getActions(boolean context) {
        Action[] result = new Action[] {
            SystemAction.get(DeleteAction.class),
                    SystemAction.get(RenameAction.class),
                    null,
                    SystemAction.get(ToolsAction.class),
                    SystemAction.get(PropertiesAction.class),
        };
        return result;
    }
    
    
    public Node cloneNode() {
        return new OnePropNode(key);
    }
    protected Sheet createSheet() {
        Sheet sheet = super.createSheet();
        Sheet.Set props = sheet.get(Sheet.PROPERTIES);
        if (props == null) {
            props = Sheet.createPropertiesSet();
            sheet.put(props);
        }
        props.put(new PropertySupport.Name(this));
        class ValueProp extends PropertySupport.ReadWrite {
            public ValueProp() {
                super("value", String.class,
                        bundle.getString("PROP_value"), bundle.getString("HINT_value"));
            }
            public Object getValue() {
                return System.getProperty(key);
            }
            public void setValue(Object nue) {
                System.setProperty(key, (String) nue);
                PropertiesNotifier.changed();
            }
            
        }
        
        props.put(new ValueProp());
        PropertiesNotifier.addChangeListener(listener = new
                ChangeListener() {
            public void stateChanged(ChangeEvent ev) {
                firePropertyChange("value", null, null);
            }
        });
        return sheet;
    }
    protected void finalize() throws Throwable {
        super.finalize();
        if (listener != null)
            PropertiesNotifier.removeChangeListener(listener);
    }
    public boolean canRename() {
        return true;
    }
    public void setName(String nue) {
        Properties p = System.getProperties();
        String value = p.getProperty(key);
        p.remove(key);
        if (value != null) p.setProperty(nue, value);
        System.setProperties(p);
        PropertiesNotifier.changed();
    }
    public boolean canDestroy() {
        return true;
    }
    public void destroy() throws IOException {
        Properties p = System.getProperties();
        p.remove(key);
        System.setProperties(p);
        PropertiesNotifier.changed();
    }
}
