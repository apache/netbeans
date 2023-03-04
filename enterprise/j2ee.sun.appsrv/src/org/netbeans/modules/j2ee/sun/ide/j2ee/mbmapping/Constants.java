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
