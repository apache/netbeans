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

package org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.entity;

import java.io.IOException;
import javax.swing.Action;
import org.openide.cookies.OpenCookie;
import org.openide.loaders.DataObject;
import org.openide.util.actions.SystemAction;
import org.netbeans.modules.j2ee.dd.api.ejb.Entity;
import org.netbeans.modules.j2ee.ejbcore.api.methodcontroller.EntityMethodController;
import org.openide.actions.OpenAction;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.AbstractNode;
import org.openide.util.Exceptions;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 *
 * @author Martin Adamek
 */
public class CMPFieldsNode extends AbstractNode implements OpenCookie {
    private final EntityMethodController controller;
    private final FileObject ddFile;
    private Entity entity;

    public CMPFieldsNode(EntityMethodController controller, Entity model, FileObject ddFile) throws IOException {
        this(new InstanceContent(), controller, model, ddFile);
    }
    
    private CMPFieldsNode(InstanceContent content, EntityMethodController controller, Entity model, FileObject ddFile) throws IOException {
        super(new CMFieldChildren(controller, model, ddFile), new AbstractLookup(content));
        entity = model;
        this.controller = controller;
        this.ddFile = ddFile;
        content.add(this);
        //TODO: RETOUCHE
//        JavaClass jc = controller.getBeanClass();
//        if (jc != null)
//            content.add(jc);
    }
    
    public Action[] getActions(boolean context) {
        return new SystemAction[] {};
    }
    
    public Action getPreferredAction() {
        return SystemAction.get(OpenAction.class);
    }
    
    public void open() {
        try {
            DataObject ddFileDO = DataObject.find(ddFile);
            OpenCookie cookie = ddFileDO.getLookup().lookup(OpenCookie.class);
            if (cookie != null) {
                cookie.open();
            }
        } catch (DataObjectNotFoundException donf) {
            Exceptions.printStackTrace(donf);
        }
    }
}
