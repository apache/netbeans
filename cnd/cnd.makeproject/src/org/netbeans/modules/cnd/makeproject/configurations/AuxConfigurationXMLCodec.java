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

import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.cnd.api.picklist.PicklistElement;
import org.netbeans.modules.cnd.api.toolchain.PlatformTypes;
import org.netbeans.modules.cnd.api.xml.VersionException;
import org.netbeans.modules.cnd.api.xml.XMLDecoder;
import org.netbeans.modules.cnd.api.xml.XMLEncoderStream;
import org.netbeans.modules.cnd.makeproject.api.configurations.CompileConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.Configuration;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationAuxObject;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptor;
import org.netbeans.modules.cnd.makeproject.api.configurations.Configurations;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import static org.netbeans.modules.cnd.makeproject.configurations.CommonConfigurationXMLCodec.DEVELOPMENT_SERVER_ELEMENT;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.xml.sax.Attributes;

class AuxConfigurationXMLCodec extends CommonConfigurationXMLCodec {

    private final String tag;
    private final ConfigurationDescriptor configurationDescriptor;
    private Configuration currentConf;
    private List<XMLDecoder> decoders = new ArrayList<>();
    private int descriptorVersion = -1;

    public AuxConfigurationXMLCodec(String tag, ConfigurationDescriptor configurationDescriptor) {
        super(configurationDescriptor, false);
        this.tag = tag;
        this.configurationDescriptor = configurationDescriptor;
    }

    // interface XMLDecoder
    @Override
    public String tag() {
        return tag;
    }

    // interface XMLDecoder
    @Override
    public void start(Attributes atts) throws VersionException {
        String versionString = atts.getValue("version");        // NOI18N
        if (versionString != null) {
            descriptorVersion = Integer.parseInt(versionString);
        }
    }

    // interface XMLDecoder
    @Override
    public void end() {
    }

    // interface XMLDecoder
    @Override
    public void startElement(String element, Attributes atts) {
        if (element.equals(CONF_ELEMENT)) {
            String currentConfName = atts.getValue(NAME_ATTR);
            Configurations confs = configurationDescriptor.getConfs();
            currentConf = confs.getConf(currentConfName);
            if (currentConf == null) {
                // it is valid situation when configuration was removed from public project properties
                return;
            }

            // switch out old decoders
            for (int dx = 0; dx < decoders.size(); dx++) {
                XMLDecoder decoder = decoders.get(dx);
                deregisterXMLDecoder(decoder);
            }

            // switch in new decoders
            ConfigurationAuxObject[] profileAuxObjects =
                    currentConf.getAuxObjects();
            decoders = new ArrayList<>();
            for (int i = 0; i < profileAuxObjects.length; i++) {
                if (!profileAuxObjects[i].shared()) {
                    XMLDecoder newDecoder = profileAuxObjects[i].getXMLDecoder();
                    registerXMLDecoder(newDecoder);
                    decoders.add(newDecoder);
                }
            }
        }
    }

    // interface XMLDecoder
    @Override
    public void endElement(String element, String currentText) {
        if (element.equals(DEFAULT_CONF_ELEMENT)) {
            configurationDescriptor.getConfs().setActive(Integer.parseInt(currentText));
        } else if (element.equals(DEVELOPMENT_SERVER_ELEMENT)) {
            if (currentConf instanceof MakeConfiguration) {
                ExecutionEnvironment env = ExecutionEnvironmentFactory.fromUniqueID(currentText);
                env = CppUtils.convertAfterReading(env, (MakeConfiguration) currentConf);
                ((MakeConfiguration) currentConf).getDevelopmentHost().setHost(env);
            }
        } else if (element.equals(PLATFORM_ELEMENT)) {
            if (currentConf instanceof MakeConfiguration) {
                int set = Integer.parseInt(currentText);
                if (descriptorVersion <= 37 && set == 4) {
                    set = PlatformTypes.PLATFORM_GENERIC;
                }
                ((MakeConfiguration) currentConf).getDevelopmentHost().setBuildPlatform(set);
            }
        } else if (element.equals(COMPILE_DIR_ELEMENT)) {
            if (currentConf instanceof MakeConfiguration) {
                ((MakeConfiguration) currentConf).getCompileConfiguration().getCompileCommandWorkingDir().setValue(currentText);
                ((MakeConfiguration) currentConf).getCompileConfiguration().getCompileCommandWorkingDir().getPicklist().addElement(currentText);
            }
        } else if (element.equals(COMPILE_DIR_PICKLIST_ITEM_ELEMENT)) {
            if (currentConf instanceof MakeConfiguration) {
                ((MakeConfiguration) currentConf).getCompileConfiguration().getCompileCommandWorkingDir().getPicklist().addElement(currentText);
            }
        } else if (element.equals(COMPILE_COMMAND_ELEMENT)) {
            if (currentConf instanceof MakeConfiguration) {
                ((MakeConfiguration) currentConf).getCompileConfiguration().getCompileCommand().setValue(currentText);
                ((MakeConfiguration) currentConf).getCompileConfiguration().getCompileCommand().getPicklist().addElement(currentText);
            }
        } else if (element.equals(COMPILE_COMMAND_PICKLIST_ITEM_ELEMENT)) {
            if (currentConf instanceof MakeConfiguration) {
                ((MakeConfiguration) currentConf).getCompileConfiguration().getCompileCommand().getPicklist().addElement(currentText);
            }
	}
    }

    @Override
    protected void writeToolsSetBlock(XMLEncoderStream xes, MakeConfiguration makeConfiguration) {
        xes.elementOpen(TOOLS_SET_ELEMENT);
        String hostKey = makeConfiguration.getDevelopmentHost().getHostKey();
        ExecutionEnvironment env = ExecutionEnvironmentFactory.fromUniqueID(hostKey);
        env = CppUtils.convertBeforeWriting(env, makeConfiguration);
        hostKey = ExecutionEnvironmentFactory.toUniqueID(env);
        xes.element(DEVELOPMENT_SERVER_ELEMENT, hostKey);
        xes.element(PLATFORM_ELEMENT, "" + makeConfiguration.getDevelopmentHost().getBuildPlatform()); // NOI18N
        xes.elementClose(TOOLS_SET_ELEMENT);
    }

    @Override
    protected void writeCompileConfBlock(XMLEncoderStream xes, MakeConfiguration makeConfiguration) {
        if (makeConfiguration.isMakefileConfiguration()) {
            CompileConfiguration compileConfiguration = makeConfiguration.getCompileConfiguration();
            xes.elementOpen(COMPILE_ID);

            xes.elementOpen(COMPILE_DIR_PICKLIST_ELEMENT);
            PicklistElement[] elements = compileConfiguration.getCompileCommandWorkingDir().getPicklist().getElements();
            for (int i = (elements.length-1); i >= 0; i--) {
                 xes.element(COMPILE_DIR_PICKLIST_ITEM_ELEMENT, elements[i].displayName());
            }
            xes.elementClose(COMPILE_DIR_PICKLIST_ELEMENT);
            xes.element(COMPILE_DIR_ELEMENT, compileConfiguration.getCompileCommandWorkingDir().getValue());

            xes.elementOpen(COMPILE_COMMAND_PICKLIST_ELEMENT);
            elements = compileConfiguration.getCompileCommand().getPicklist().getElements();
            for (int i = (elements.length-1); i >= 0; i--) {
                 xes.element(COMPILE_COMMAND_PICKLIST_ITEM_ELEMENT, elements[i].displayName());
            }
            xes.elementClose(COMPILE_COMMAND_PICKLIST_ELEMENT);
            xes.element(COMPILE_COMMAND_ELEMENT, compileConfiguration.getCompileCommand().getValue());

            xes.elementClose(COMPILE_ID);
        }
    }

}
