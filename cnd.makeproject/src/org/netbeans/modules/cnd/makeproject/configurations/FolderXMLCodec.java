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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.cnd.makeproject.configurations;

import org.netbeans.modules.cnd.api.xml.AttrValuePair;
import org.netbeans.modules.cnd.api.xml.VersionException;
import org.netbeans.modules.cnd.api.xml.XMLDecoder;
import org.netbeans.modules.cnd.api.xml.XMLEncoder;
import org.netbeans.modules.cnd.api.xml.XMLEncoderStream;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationAuxObjectWithDictionary.Dictionaries;
import org.netbeans.modules.cnd.makeproject.api.configurations.FolderConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.LinkerConfiguration;
import org.xml.sax.Attributes;

public class FolderXMLCodec extends XMLDecoder implements XMLEncoder {

    public final static String FOLDER_ELEMENT = "folder"; // NOI18N
    public final static String PATH_ATTR = "path"; // NOI18N
    private final FolderConfiguration folder;
    private final Dictionaries dictionaries;


    public FolderXMLCodec(FolderConfiguration folder) {
	this.folder = folder;
        this.dictionaries = null;
    }
    
    public FolderXMLCodec(FolderConfiguration folder, Dictionaries dictionaries) {
        this.folder = folder;
        this.dictionaries = dictionaries;
    }


    // interface XMLDecoder
    @Override
    public String tag() {
	return folder.getId();
    }

    // interface XMLDecoder
    @Override
    public void start(Attributes atts) throws VersionException {
        String what = "folder"; // NOI18N
        int maxVersion = 1;
        checkVersion(atts, what, maxVersion);
    }

    // interface XMLDecoder
    @Override
    public void end() {
        folder.clearChanged();
    }

    // interface XMLDecoder
    @Override
    public void startElement(String element, Attributes atts) {
    }

    // interface XMLDecoder
    @Override
    public void endElement(String element, String currentText) {
    }

    // interface XMLEncoder
    @Override
    public void encode(XMLEncoderStream xes) {
        boolean cCompilerConfigurationModified = folder.getCCompilerConfiguration().getModified();
        boolean ccCompilerConfigurationModified = folder.getCCCompilerConfiguration().getModified();
        final LinkerConfiguration linkerConfiguration = folder.getLinkerConfiguration();
        boolean linkerConfigurationModified = linkerConfiguration != null ? linkerConfiguration.getModified() : false;
        if (cCompilerConfigurationModified || ccCompilerConfigurationModified || linkerConfigurationModified) {
            xes.elementOpen(FOLDER_ELEMENT, new AttrValuePair[]{new AttrValuePair(PATH_ATTR, folder.getFolder().getPath())});
            if (cCompilerConfigurationModified) {
                CommonConfigurationXMLCodec.writeCCompilerConfiguration(xes, folder.getCCompilerConfiguration(), CommonConfigurationXMLCodec.FOLDER_LEVEL, dictionaries);
            }
            if (ccCompilerConfigurationModified) {
                CommonConfigurationXMLCodec.writeCCCompilerConfiguration(xes, folder.getCCCompilerConfiguration(), CommonConfigurationXMLCodec.FOLDER_LEVEL, dictionaries);
            }
            if (linkerConfigurationModified) {
                CommonConfigurationXMLCodec.writeLinkerConfiguration(xes, folder.getLinkerConfiguration());
            }
            xes.elementClose(FOLDER_ELEMENT);
        }
    } 
}
