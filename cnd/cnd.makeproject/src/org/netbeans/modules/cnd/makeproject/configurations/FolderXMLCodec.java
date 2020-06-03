/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
