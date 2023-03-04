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

package org.netbeans.modules.xml.jaxb.spi;

import java.math.BigDecimal;
import org.openide.WizardDescriptor;

/**
 *
 * @author gpatil
 */
public interface JAXBWizModuleConstants {
    public static final String LAST_BROWSED_SCHEMA_DIR = "last.browsed.schema.dir" ;  //NOI18N
    public static final String LAST_BROWSED_BINDING_DIR = "last.browsed.binding.dir" ; //NOI18N
    public static final String LAST_BROWSED_CATALOG_DIR = "last.browsed.catalog.dir" ; //NOI18N
    
    public static final String WIZ_ERROR_MSG = WizardDescriptor.PROP_ERROR_MESSAGE ; //NOI18N
    public static final String WIZ_STYLE_AUTO = WizardDescriptor.PROP_AUTO_WIZARD_STYLE; //NOI18N
    public static final String WIZ_CONTENT_DISPLAYED = WizardDescriptor.PROP_CONTENT_DISPLAYED ; //NOI18N
    public static final String WIZ_CONTENT_NUMBERED = WizardDescriptor.PROP_CONTENT_NUMBERED; //I18N
    
    public static final String JAXB_SCHEMA_TYPE_XML_SCHEMA = "-xmlschema" ; //NOI18N
    public static final String JAXB_SCHEMA_TYPE_RELAX_NG = "-relaxng" ; //NOI18N
    public static final String JAXB_SCHEMA_TYPE_RELAX_NG_COMPACT = "-relaxng-compact" ; //NOI18N
    public static final String JAXB_SCHEMA_TYPE_DTD = "-dtd" ; //NOI18N
    public static final String JAXB_SCHEMA_TYPE_WSDL = "-wsdl" ; //NOI18N    
    
    public static final String JAXB_OPTION_NV = "-nv" ; //NOI18N    
    public static final String JAXB_OPTION_READ_ONLY = "-readOnly" ; //NOI18N    
    public static final String JAXB_OPTION_NPA = "-npa" ; //NOI18N    
    public static final String JAXB_OPTION_VERBOSE = "-verbose" ; //NOI18N    
    public static final String JAXB_OPTION_QUIET = "-quiet" ; //NOI18N        
    public static final String JAXB_OPTION_EXTENSION = "-extension" ; //NOI18N            

    public static final String SRC_LOC_TYPE_URL = "url" ; //NOI18N
    public static final String SRC_LOC_TYPE_FS = "fileSystem" ; //NOI18N
    public static final String SCHEMA_NAME = "schema.name"; //NOI18N
    public static final String PROJECT_NAME = "project.name"; //NOI18N
    public static final String PROJECT_DIR = "project.dir"; //NOI18N
    public static final String XSD_FILE_LIST = "xsd.file.list"; //NOI18N
    public static final String SOURCE_LOCATION_TYPE = "xsd.locatiom.type"; //NOI18N
    public static final String PACKAGE_NAME = "xsd.package.name"; //NOI18N
    public static final String SCHEMA_TYPE = "jaxb.schema.type"; //NOI18N
    public static final String XJC_OPTIONS = "jaxb.xjc.options" ; //NOI18N
    public static final String JAXB_BINDING_FILES = "jaxb.binding.files" ; //NOI18N
    public static final String CATALOG_FILE = "jaxb.catalog.file" ; //NOI18N
    public static final String EXISTING_SCHEMA_NAMES = "jaxb.cfg.existingSchemaNames" ; //NOI18N
    
    public static final String ORIG_LOCATION = "origLocation" ; // No I18N
    public static final String ORIG_LOCATION_TYPE = "orginLocationType"; //NOI18N
    public static final String LOC_SCHEMA_ROOT = "localSchemaRoot"; //NOI18N

    public static final String JAXB_COMPILE_TARGET_DEPENDS = "-pre-pre-compile" ;//NOI18N
    public static final String JAXB_COMPILE_TARGET = "jaxb-code-generation"; //NOI18N
    public static final String JAXB_CLEAN_COMPILE_TARGET = "jaxb-clean-code-generation"; //NOI18N
    public static final BigDecimal LATEST_CFG_VERSION = new BigDecimal("1.0"); //NOI18n

}

