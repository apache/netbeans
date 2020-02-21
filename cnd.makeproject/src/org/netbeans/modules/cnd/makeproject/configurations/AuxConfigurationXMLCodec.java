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
