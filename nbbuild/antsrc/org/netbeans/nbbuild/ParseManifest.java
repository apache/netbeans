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

package org.netbeans.nbbuild;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.jar.Manifest;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

/**
 * Task which parses given manifest file, searches for given attribute
 * and stores it in property.
 * 
 * @author Richard Michalsky
 */
public class ParseManifest extends Task {

    /**
     * Path to manifest.
     */
    private File manifest;
    public void setManifest(File manifest) {
        this.manifest = manifest;
    }
    /**
     * Task sets attribute name to given property.
     * If attribute is not found, property remains unset.
     */
    private String property;
    public void setProperty(String property) {
        this.property = property;
    }

    private String attribute;
    /**
     * Name of attribute to be read from given manifest.
     * @param attribute
     */
    public void setAttribute(String attribute) {
        this.attribute = attribute;
    }


    @Override
    public void execute() throws BuildException {
        if (manifest == null) {
            throw new BuildException("Must specify parameter 'manifest'.");
        }
        if (property == null) {
            throw new BuildException("Must specify parameter 'property'.");
        }
        if (attribute == null) {
            throw new BuildException("Must specify parameter 'attribute'.");
        }
        try {
            BufferedInputStream is = new BufferedInputStream(new FileInputStream(manifest));
            try {
                Manifest mf = new Manifest(is);
                String attr = mf.getMainAttributes().getValue(attribute);
                if (attr == null)
                    return;
                getProject().setProperty(property, attr);
            } finally {
                is.close();
            }
        } catch (Exception x) {
            throw new BuildException("Reading manifest " + manifest + ": " + x, x, getLocation());
        }
    }

}
