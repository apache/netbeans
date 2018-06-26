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

package org.netbeans.modules.j2ee.ddloaders.multiview;

import org.netbeans.modules.j2ee.dd.api.common.EnvEntry;
import org.netbeans.modules.j2ee.dd.api.ejb.Ejb;

/**
 * @author pfiala
 */
public class EjbHelper {
    private EjbJarMultiViewDataObject dataObject;
    private Ejb ejb;

    public EjbHelper(EjbJarMultiViewDataObject dataObject, Ejb ejb) {
        this.dataObject = dataObject;
        this.ejb = ejb;
    }

    public EnvEntryHelper getEnvEntryHelper(int rowIndex) {
        return new EnvEntryHelper(ejb.getEnvEntry(rowIndex));
    }

    public int getEnvEntryCount() {
        return ejb.getEnvEntry().length;
    }

    public EnvEntryHelper newEnvEntry() {
        EnvEntry entry = ejb.newEnvEntry();
        ejb.addEnvEntry(entry);
        modelUpdatedFromUI();
        return new EnvEntryHelper(entry);
    }

    public void removeEnvEntry(int row) {
        ejb.removeEnvEntry(ejb.getEnvEntry(row));
        modelUpdatedFromUI();
    }

    private void modelUpdatedFromUI() {
        dataObject.modelUpdatedFromUI();
    }

    public class EnvEntryHelper {
        private EnvEntry envEntry;

        public EnvEntryHelper(EnvEntry envEntry) {
            this.envEntry = envEntry;
            modelUpdatedFromUI();
        }

        public void setEnvEntryName(String value) {
            envEntry.setEnvEntryName(value);
            modelUpdatedFromUI();
        }

        public void setEnvEntryType(String value) {
            envEntry.setEnvEntryType(value);
            modelUpdatedFromUI();
        }

        public void setEnvEntryValue(String value) {
            envEntry.setEnvEntryValue(value);
            modelUpdatedFromUI();
        }

        public void setDescription(String description) {
            envEntry.setDescription(description);
            modelUpdatedFromUI();
        }

        public String getEnvEntryName() {
            return envEntry.getEnvEntryName();
        }

        public String getEnvEntryType() {
            return envEntry.getEnvEntryType();
        }

        public String getEnvEntryValue() {
            return envEntry.getEnvEntryValue();
        }

        public String getDefaultDescription() {
            return envEntry.getDefaultDescription();
        }
    }
}
