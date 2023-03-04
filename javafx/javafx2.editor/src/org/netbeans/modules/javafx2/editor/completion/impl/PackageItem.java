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
package org.netbeans.modules.javafx2.editor.completion.impl;

import javax.swing.ImageIcon;
import org.openide.util.ImageUtilities;

/**
 *
 * @author sdedic
 */
final class PackageItem extends AbstractCompletionItem {
    private static final String PACKAGE = "org/netbeans/modules/javafx2/editor/resources/package.gif"; // NOI18N
    private static final String PACKAGE_COLOR = "<font color=#005600>"; //NOI18N
    private static ImageIcon icon;
    
    private String  fqn;
    
    private static String simpleName(String fullName) {
        int dot = fullName.lastIndexOf('.');
        if (dot > 0 && dot < fullName.length() - 1) {
            return fullName.substring(dot + 1);
        } else {
            return fullName;
        }
        
    }
    
    PackageItem(CompletionContext ctx, String fullName) {
        super(ctx, simpleName(fullName));
        this.fqn = fullName;
    }

    @Override
    protected String getSubstituteText() {
        if (Character.isJavaIdentifierPart(fqn.charAt(fqn.length() - 1))) {
            return fqn + "."; // NOI18N
        } else {
            return fqn; // NO18N
        }
    }

    protected ImageIcon getIcon() {
        if (icon ==  null) {
            icon = ImageUtilities.loadImageIcon(PACKAGE, false);
        }
        return icon;
    }
}
