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

