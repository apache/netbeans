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
