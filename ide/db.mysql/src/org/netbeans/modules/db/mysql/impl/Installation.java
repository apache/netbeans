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

package org.netbeans.modules.db.mysql.impl;

/**
 * This interface defines an abstraction of an installation of MySQL, which
 * gives you information such as the path/arguments to the start command,
 * stop command, admin command.
 * 
 * Valid installations are loaded through the layer file using the folder
 * Databases/MySQL/Installations
 * 
 * @author David Van Couvering
 */
public interface Installation {
     public enum Command { START, STOP, ADMIN };


    /**
     * @return true if this installation is part of a stack installation
     * like XAMPP or MAMP.  Stack-based installations take preference because 
     * they usually have an admin tool (myphpadmin) and usually don't install 
     * MySQL as a service but are instead manually started and stopped.
     * 
     * Also, standalone installs often come as part of the OS distribution,
     * where a stack based install is explicitly installed by the user, and
     * thus is probably their preference.
     */
    public boolean isStackInstall();
    
    /**
     * Returns true if this installation is valid for the current OS
     */
    public boolean isInstalled();

    /**
     * @return the command to administer this installation.  This is normally
     * phpMyAdmin; rarely does an installation come with the MySQL admin tool.
     * <p>
     * The first element is the path/URL to the command.  
     * The second element is the arguments to the command
     */
    public String[] getAdminCommand();
    
    /**
     * @return the command to stop the server.  The first element is the path
     * to the command. The second element is the arguments to the command
     */
    public String[] getStartCommand();

    /**
     * @return the command to start the server.  The first element is the path
     * to the command. The second element is the arguments to the command
     */
    public String[] getStopCommand();
    
    /**
     * @return the default port number for the server
     */
    public String getDefaultPort();
}

