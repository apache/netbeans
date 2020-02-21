/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.cnd.classview.model;

import java.awt.Image;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;

import  org.netbeans.modules.cnd.api.model.*;
import static org.netbeans.modules.cnd.classview.model.BaseNode.RP;
import org.netbeans.modules.cnd.modelutil.CsmImageLoader;
import org.openide.nodes.Children;

/**
 */
public class EnumNode extends ClassifierNode {
    private CharSequence name;
    private CharSequence qname;
    private static Image enumImage = null;

    public EnumNode(CsmEnum enumeration,Children.Array key) {
        super(enumeration,key);
        init(enumeration);
    }
    
    private void init(CsmEnum enumeration){
        final CharSequence old = name;
        name = enumeration.getName();
        final CharSequence oldQ = qname;
        qname = enumeration.getQualifiedName();
        if( enumImage == null ) {
            enumImage = CsmImageLoader.getImage(enumeration);
        }
        final Runnable runnable = new Runnable() {
            
            @Override
            public void run() {
                if ((old == null) || !old.equals(name)) {
                    fireNameChange(old == null ? null : old.toString(),
                            name == null ? null : name.toString());
                    fireDisplayNameChange(old == null ? null : old.toString(),
                            name == null ? null : name.toString());
                }
                if ((oldQ == null) || !oldQ.equals(qname)) {
                    fireShortDescriptionChange(oldQ == null ? null : oldQ.toString(),
                            qname == null ? null : qname.toString());
                }
            }
        };
        if (SwingUtilities.isEventDispatchThread()) {
            runnable.run();
        } else {
            SwingUtilities.invokeLater(runnable);
        }
    }

    @Override
    public String getName() {
        return name.toString();
    }

    @Override
    public String getDisplayName() {
        return name.toString();
    }

    @Override
    public String getShortDescription() {
        return qname.toString();
    }

    @Override
    public Image getIcon(int param) {
        return enumImage;
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        Object o = e.getSource();
        if (o instanceof CsmEnum){
            CsmEnum cls = (CsmEnum)o;
            setObject(cls);
            init(cls);
        } else if (o != null) {
            System.err.println("Expected CsmEnum. Actually event contains "+o.toString());
        }
    }
}
