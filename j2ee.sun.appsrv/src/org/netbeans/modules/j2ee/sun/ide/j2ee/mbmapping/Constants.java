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
/*
 * Constants.java
 *
 * Created on January 16, 2004, 4:16 PM
 */

package org.netbeans.modules.j2ee.sun.ide.j2ee.mbmapping;

/**
 *
 * @author  nityad
 */
public interface Constants {

    static final String MAP_J2EEAPP_STANDALONE = "com.sun.appserv:type=applications,category=config"; //NOI18N

    //Server MBean
    static final String OBJ_J2EE = "com.sun.appserv:j2eeType=J2EEServer,name=server,category=runtime"; //NOI18N
    static final String[] JSR_SERVER_INFO = {"debugPort", "nodes", "serverVersion", "restartRequired", "serverVendor" }; //NOI18N
    static final String[] ADDITIONAL_SERVER_INFO = {"port", "domain" }; //NOI18N
    
    static final String MAP_RESOURCES = "com.sun.appserv:type=resources,category=config";//NOI18N               
    
    static final String MAP_JVMOptions = "com.sun.appserv:type=java-config,config=server-config,category=config"; //NOI18N
    static final String DEBUG_OPTIONS = "debug-options"; //NOI18N
    static final String JAVA_HOME = "java-home"; //NOI18N 
    static final String DEBUG_OPTIONS_ADDRESS = "address="; //NOI18N 
    static final String JPDA_PORT = "jpda_port_number"; //NOI18N 
    static final String SHARED_MEM = "shared_memory"; //NOI18N 
    static final String ISMEM = "transport=dt_shmem"; //NOI18N
    static final String ISSOCKET = "transport=dt_socket"; //NOI18N
    static final String DEF_DEUG_OPTIONS = "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=1044"; //NOI18N                         
    static final String DEF_DEUG_OPTIONS_81 = "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=9009"; //NOI18N                         
    static final String DEF_DEUG_OPTIONS_SOCKET = "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=11000"; //NOI18N                         
    static final String DEF_DEUG_OPTIONS_SHMEM = "-agentlib:jdwp=transport=dt_shmem,server=y,suspend=n,address="; //NOI18N                         
    
    //Config Mbean Queries
    static final String[] CONFIG_MODULE = {"web-module", "j2ee-application", "ejb-module", "connector-module", "appclient-module"}; //NOI18N
    
}
