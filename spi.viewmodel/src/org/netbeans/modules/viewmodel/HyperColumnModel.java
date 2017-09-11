/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.viewmodel;

import java.util.HashSet;
import java.util.Set;
import org.netbeans.spi.viewmodel.ColumnModel;

/**
 * Represents a binding of specific column model of inner view
 * to the main colum model of the enclosing view.
 *
 * @author Martin Entlicher
 */
final class HyperColumnModel extends ColumnModel {

    private ColumnModel main;
    private ColumnModel specific;
    private final Set<String> ids;

    public HyperColumnModel(ColumnModel main, ColumnModel specific) {
        this.main = main;
        this.specific = specific;
        ids = createAllIDs(main, specific);
    }
    
    private static Set<String> createAllIDs(ColumnModel... cms) {
        Set<String> allIds = new HashSet<String>();
        for (ColumnModel cm : cms) {
            if (cm instanceof HyperColumnModel) {
                allIds.addAll(((HyperColumnModel) cm).getAllIDs());
            } else {
                allIds.add(cm.getID());
            }
        }
        return allIds;
    }

    public ColumnModel getMain() {
        return main;
    }

    public ColumnModel getSpecific() {
        return specific;
    }

    @Override
    public String getID() {
        return main.getID();
    }

    @Override
    public String getDisplayName() {
        return main.getDisplayName();
    }

    @Override
    public String getShortDescription() {
        return main.getShortDescription();
    }

    @Override
    public Class getType() {
        return specific.getType();
    }
    
    Set<String> getAllIDs() {
        return ids;
    }

}
