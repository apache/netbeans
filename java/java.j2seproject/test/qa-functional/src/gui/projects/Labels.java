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

package gui.projects;

import org.netbeans.jellytools.Bundle;

/**
 *
 * @author Tomas Musil
 */
public class Labels {

        public static String standard = Bundle.getString("org.netbeans.modules.java.j2seproject.ui.wizards.Bundle", "Templates/Project/Standard");
        public static String javaApp = Bundle.getString("org.netbeans.modules.java.j2seproject.ui.wizards.Bundle", "template_app");
        public static String libMgrDlgLbl = Bundle.getString("org.netbeans.api.project.libraries.Bundle", "TXT_LibrariesManager");
        public static String tools = Bundle.getString("org.openide.actions.Bundle", "CTL_Tools");
        public static String libraries = Bundle.getStringTrimmed("org.netbeans.modules.project.libraries.ui.Bundle", "CTL_LibrariesManager");
        public static String addNewLibraryBtnLbl = Bundle.getStringTrimmed("org.netbeans.modules.project.libraries.ui.Bundle", "CTL_NewLibrary");
        public static String newLibraryDlgLbl = Bundle.getString("org.netbeans.modules.project.libraries.ui.Bundle", "CTL_CreateLibrary");
        public static String addToCpBtnLbl = Bundle.getString("org.netbeans.modules.java.j2seplatform.libraries.Bundle", "CTL_AddClassPath");
        public static String addSrcBtnLbl = Bundle.getString("org.netbeans.modules.java.j2seplatform.libraries.Bundle", "CTL_AddSources");
        public static String librariesNode = Bundle.getString("org.netbeans.modules.java.j2seproject.ui.Bundle", "CTL_LibrariesNode");
        public static String copyToShLibRadioLbl = Bundle.getStringTrimmed("org.netbeans.modules.project.ant.Bundle", "FileChooserAccessory.rbCopy.text");
        public static String addLibraryActionLbl = Bundle.getString("org.netbeans.modules.java.j2seproject.ui.Bundle", "LBL_AddLibrary_Action");
        public static String addLibraryDlg = Bundle.getString("org.netbeans.api.project.libraries.Bundle", "LibraryChooserGUI.add.title");
        public static String addLibraryBtn = Bundle.getString("org.netbeans.api.project.libraries.Bundle", "LibraryChooserGUI.add.button");
        public static String globalLibraries = Bundle.getString("org.netbeans.modules.project.libraries.ui.Bundle", "LABEL_Global_Libraries");


        //no instances needed
        private Labels(){}

}
