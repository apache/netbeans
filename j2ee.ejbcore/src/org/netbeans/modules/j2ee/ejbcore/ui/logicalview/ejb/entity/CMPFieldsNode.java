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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
