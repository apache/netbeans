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

import org.netbeans.modules.cnd.api.toolchain.PredefinedToolKind;
import org.netbeans.modules.cnd.api.xml.AttrValuePair;
import org.netbeans.modules.cnd.api.xml.VersionException;
import org.netbeans.modules.cnd.api.xml.XMLDecoder;
import org.netbeans.modules.cnd.api.xml.XMLEncoder;
import org.netbeans.modules.cnd.api.xml.XMLEncoderStream;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationAuxObjectWithDictionary.Dictionaries;
import org.netbeans.modules.cnd.makeproject.api.configurations.ItemConfiguration;
import org.xml.sax.Attributes;

public class ItemXMLCodec extends XMLDecoder implements XMLEncoder {

    public final static String ITEM_ELEMENT = "item"; // NOI18N
    public final static String PATH_ATTR = "path"; // NOI18N
    public final static String EXCLUDED_ATTR = "ex"; // NOI18N
    public final static String TOOL_ATTR = "tool"; // NOI18N
    public final static String FLAVOR_ATTR = "flavor"; // NOI18N
    public final static String FLAVOR2_ATTR = "flavor2"; //SINCE V82 // NOI18N
    public final static String EXCLUDED_ELEMENT = "excluded"; // FIXUP: < 7 // NOI18N
    public final static String TOOL_ELEMENT = "tool"; // FIXUP: < 7 // NOI18N
    public final static String ITEM_EXCLUDED_ELEMENT = "itemExcluded"; // NOI18N
    public final static String ITEM_TOOL_ELEMENT = "itemTool"; // NOI18N
    public final static String DEBUGGING_ELEMENT = "justfordebugging"; // NOI18N
    public final static String TRUE_VALUE = "true"; // NOI18N
    public final static String FALSE_VALUE = "false"; // NOI18N
    private final ItemConfiguration item;
    private final Dictionaries dictionaries;

    public ItemXMLCodec(ItemConfiguration item) {
        this.item = item;
        this.dictionaries = null;
    }

    public ItemXMLCodec(ItemConfiguration item, Dictionaries dictionaries) {
        this.item = item;
        this.dictionaries = dictionaries;
    }

    // interface XMLDecoder
    @Override
    public String tag() {
        return item.getId();
    }

    // interface XMLDecoder
    @Override
    public void start(Attributes atts) throws VersionException {
        String what = "item"; // NOI18N
        int maxVersion = 1;
        checkVersion(atts, what, maxVersion);
    }

    // interface XMLDecoder
    @Override
    public void end() {
        item.clearChanged();
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
        if (item.isDefaultConfiguration()) {
            return;
        }
        PredefinedToolKind tool = item.getTool();

        xes.elementOpen(ITEM_ELEMENT, new AttrValuePair[]{
            new AttrValuePair(PATH_ATTR, item.getItem().getPath()),
            new AttrValuePair(EXCLUDED_ATTR, "" + item.getExcluded().getValue()),
            new AttrValuePair(TOOL_ATTR, "" + tool.ordinal()),
            new AttrValuePair(FLAVOR2_ATTR, "" + item.getLanguageFlavor().toExternal()),
        });
//        if (item.getExcluded().getModified()) {
//            xes.element(ITEM_EXCLUDED_ELEMENT, "" + item.getExcluded().getValue()); // NOI18N
//        }
//        xes.element(ITEM_TOOL_ELEMENT, "" + item.getTool()); // NOI18N
        if (tool == PredefinedToolKind.CCompiler) {
            CommonConfigurationXMLCodec.writeCCompilerConfiguration(xes, item.getCCompilerConfiguration(), CommonConfigurationXMLCodec.ITEM_LEVEL, dictionaries);
            if(item.isProCFile()) {
                CommonConfigurationXMLCodec.writeCustomToolConfiguration(xes, item.getCustomToolConfiguration());
            }
        } else if (tool == PredefinedToolKind.CCCompiler) {
            CommonConfigurationXMLCodec.writeCCCompilerConfiguration(xes, item.getCCCompilerConfiguration(), CommonConfigurationXMLCodec.ITEM_LEVEL, dictionaries);
            if(item.isProCFile()) {
                CommonConfigurationXMLCodec.writeCustomToolConfiguration(xes, item.getCustomToolConfiguration());
            }
        } else if (tool == PredefinedToolKind.FortranCompiler) {
            CommonConfigurationXMLCodec.writeFortranCompilerConfiguration(xes, item.getFortranCompilerConfiguration());
        } else if (tool == PredefinedToolKind.CustomTool) {
            CommonConfigurationXMLCodec.writeCustomToolConfiguration(xes, item.getCustomToolConfiguration());
        }
        xes.elementClose(ITEM_ELEMENT);
    }
}
