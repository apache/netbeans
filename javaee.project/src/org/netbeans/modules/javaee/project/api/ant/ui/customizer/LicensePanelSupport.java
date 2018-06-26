/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javaee.project.api.ant.ui.customizer;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.ui.CustomizerUtilities;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

// this class was copied into org.netbeans.modules.web.clientproject.ui.customizer.LicensePanelSupport
public final class LicensePanelSupport implements CustomizerUtilities.LicensePanelContentHandler {

    public static final String LICENSE_NAME = "project.license";
    public static final String LICENSE_PATH = "project.licensePath";

    private String licenseName;
    private String licensePath;
    private String licenseContent;

    private PropertyEvaluator evaluator;
    private AntProjectHelper antHelper;

    public LicensePanelSupport(PropertyEvaluator evaluator, AntProjectHelper antHelper,
            String licensePath, String licenseName) {
        this.evaluator = evaluator;
        this.antHelper = antHelper;
        this.licensePath = licensePath;
        this.licenseName = licenseName;
        this.licenseContent = null;
    }

    @Override
    public String getProjectLicenseLocation() {
        return licensePath;
    }

    @Override
    public String getGlobalLicenseName() {
        return licenseName;
    }

    @Override
    public FileObject resolveProjectLocation(@NonNull String path) {
        String evaluated = evaluator.evaluate(path);
        if (evaluated != null) {
            return antHelper.resolveFileObject(evaluated);
        } else {
            return null;
        }
    }

    @Override
    public void setProjectLicenseLocation(@NullAllowed String newLocation) {
        licensePath = newLocation;
    }

    @Override
    public void setGlobalLicenseName(@NullAllowed String newName) {
        licenseName = newName;
    }

    @Override
    public String getDefaultProjectLicenseLocation() {
        return "./nbproject/licenseheader.txt";
    }

    @Override
    public void setProjectLicenseContent(@NullAllowed String text) {
        licenseContent = text;
    }

    public void saveLicenseFile() throws IOException {
        if (licenseContent != null) {
            assert licensePath != null; //path needs to exist once we have content?
            String evaluated = evaluator.evaluate(licensePath);
            assert evaluated != null;
            File file = antHelper.resolveFile(evaluated);
            FileObject fo;
            if (!file.exists()) {
                fo = FileUtil.createData(file);
            } else {
                fo = FileUtil.toFileObject(file);
            }
            OutputStream out = fo.getOutputStream();
            try {
                FileUtil.copy(new ByteArrayInputStream(licenseContent.getBytes()), out);
            } finally {
                out.close();
            }
        }
    }

    public void updateProperties(EditableProperties projectProperties) {
        if (licensePath != null) {
            projectProperties.setProperty(LICENSE_PATH, licensePath);
        } else {
            projectProperties.remove(LICENSE_PATH);
        }
        if (licenseName != null) {
            projectProperties.setProperty(LICENSE_NAME, licenseName);
        } else {
            projectProperties.remove(LICENSE_NAME);
        }
    }
}
