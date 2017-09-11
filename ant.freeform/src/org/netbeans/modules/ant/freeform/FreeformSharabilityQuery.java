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

package org.netbeans.modules.ant.freeform;

import java.io.File;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.queries.SharabilityQuery;
import org.netbeans.api.queries.SharabilityQuery.Sharability;
import org.netbeans.spi.project.support.ant.AntProjectEvent;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.AntProjectListener;
import org.netbeans.modules.ant.freeform.spi.support.Util;
import org.netbeans.spi.queries.SharabilityQueryImplementation2;
import org.openide.filesystems.FileUtil;
import org.openide.util.Utilities;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 *
 * @author Jan Lahoda
 * @author Tomas Zezula
 */
public class FreeformSharabilityQuery implements SharabilityQueryImplementation2, AntProjectListener {

    private String nbproject;
    private String nbprojectPrivate;
    private volatile  Set<File> exported;
    private final FreeformProject project;

    /** Creates a new instance of FreeformSharabilityQuery */
    public FreeformSharabilityQuery(final FreeformProject project) {
        assert project != null;
        this.project = project;
        final AntProjectHelper helper = project.helper();
	nbproject = helper.resolveFile("nbproject").getAbsolutePath();
	nbprojectPrivate = helper.resolveFile("nbproject/private").getAbsolutePath();        
        helper.addAntProjectListener(this);
    }
    
    @Override public Sharability getSharability(URI uri) {
        File file = Utilities.toFile(uri);
	String absolutePath = file.getAbsolutePath();
	
	if (absolutePath.equals(nbproject)) {
	    return SharabilityQuery.Sharability.MIXED;
	}
	
	if (absolutePath.startsWith(nbproject)) {
	    return absolutePath.startsWith(nbprojectPrivate) ? SharabilityQuery.Sharability.NOT_SHARABLE : SharabilityQuery.Sharability.SHARABLE;
	}

        if (isExported(file)) {
            return SharabilityQuery.Sharability.NOT_SHARABLE;
        }
	
	return SharabilityQuery.Sharability.UNKNOWN;
    }

    public void configurationXmlChanged(AntProjectEvent ev) {
        exported = null;
    }

    public void propertiesChanged(AntProjectEvent ev) {
    }

    private boolean isExported(final File file) {
        Set<File> _exported = this.exported;
        if (_exported == null) {
            final Set<File> _exportedFinal = _exported = new HashSet<File>();
            ProjectManager.mutex().readAccess(new Runnable() {
                public void run () {
                    final Element root = project.getPrimaryConfigurationData();
                    final NodeList exports = root.getElementsByTagNameNS(FreeformProjectType.NS_GENERAL, "export"); //NOI18N
                    for (int i=0; i< exports.getLength(); i++) {
                        final Element export = (Element) exports.item(i);
                        final Element location = XMLUtil.findElement(export, "location", FreeformProjectType.NS_GENERAL);   //NOI18N
                        if (location != null) {
                            final String path = XMLUtil.findText(location);
                            if (path != null) {
                                final File exportedFile = Util.resolveFile(project.evaluator(), FileUtil.toFile(project.getProjectDirectory()), path);
                                if (exportedFile != null) {
                                    _exportedFinal.add(exportedFile);
                                }
                            }
                        }
                    }
                }
            });
            synchronized (this) {
                if (exported == null) {
                    exported = _exported;
                }
            }
        }
        return _exported.contains(file);
    }
    
}
