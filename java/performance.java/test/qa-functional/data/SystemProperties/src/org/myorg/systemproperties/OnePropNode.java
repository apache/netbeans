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
