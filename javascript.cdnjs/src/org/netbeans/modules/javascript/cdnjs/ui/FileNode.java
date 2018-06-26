/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javascript.cdnjs.ui;

import java.util.Collection;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.NbBundle;

/**
 * Node that represents one library file.
 *
 * @author Jan Stola
 */
public class FileNode extends AbstractNode {
    /** "Install" property of the node. */
    private final InstallProperty installProperty = new InstallProperty();

    /**
     * Creates a new {@code FileNode}.
     * 
     * @param fileName name of a file.
     * @param install default value of the "install" property.
     */
    public FileNode(String fileName, boolean install) {
        super(Children.LEAF);
        setName(fileName);
        installProperty.setValue(install);
    }

    @Override
    protected Sheet createSheet() {
        Sheet sheet = super.createSheet();
        Sheet.Set set = Sheet.createPropertiesSet();
        set.put(installProperty);
        sheet.put(set);
        return sheet;
    }

    /**
     * Collects the names of the files the user is not interested in.
     * 
     * @param refusedFiles collection that should be populated by the refused files.
     */
    void collectRefusedFiles(Collection<String> refusedFiles) {
        if (!installProperty.getValue()) {
            refusedFiles.add(getName());
        }
    }

    /**
     * Property that determines whether the file should be installed or not.
     */
    static final class InstallProperty extends PropertySupport.ReadWrite<Boolean> {
        /** Name of the property. */
        static final String NAME = "instal"; // NOI18N
        /** Value of the property. */
        private boolean value;

        /**
         * Creates a new {@code InstallProperty}.
         */
        @NbBundle.Messages({"FileNode.installProperty.displayName=Install"})
        InstallProperty() {
            super(NAME, Boolean.class, Bundle.FileNode_installProperty_displayName(), null);
        }

        @Override
        public Boolean getValue() {
            return value;
        }

        @Override
        public void setValue(Boolean value) {
            this.value = value;
        }
        
    }
    
}
