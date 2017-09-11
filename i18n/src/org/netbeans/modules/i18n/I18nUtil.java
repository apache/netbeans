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


package org.netbeans.modules.i18n;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import org.netbeans.api.queries.VisibilityQuery;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.util.NbBundle;

/**
 * Utilities class for I18N module.
 *
 * @author  Peter Zavadsky
 */
public final class I18nUtil {

    /** Help ID for i18n module in general. */
    public static final String HELP_ID_I18N = "internation.internation"; // NOI18N
    /** Help ID for I18N dialog. */
    public static final String HELP_ID_AUTOINSERT = "internation.autoinsert"; // NOI18N
    /** Help ID for Insert I18N dialog. */
    public static final String HELP_ID_MANINSERT = "internation.maninsert"; // NOI18N
    /** Help ID for I18N form property editor. You can see it in Component inspector. */
    public static final String HELP_ID_FORMED = "internation.formed"; // NOI18N
    /** Help ID for I18N test wizard. */
    public static final String HELP_ID_TESTING = "internation.testing"; // NOI18N
    /** Help ID for I18N wizard. */
    public static final String HELP_ID_WIZARD = "internation.wizard"; // NOI18N
    /** Help ID for I18N options. */
    public static final String HELP_ID_CUSTOM = "internation.custom"; // NOI18N
    /** Help ID for parameters dialog. */
    public static final String HELP_ID_ADDPARAMS = "internation.addparams"; // NOI18N
    /** Help ID for replacing format. */
    public static final String HELP_ID_REPLFORMAT = "internation.replformat"; // NOI18N
    /** Help ID for Locale execution. */
    public static final String HELP_ID_RUNLOCALE = "internation.runlocale"; // NOI18N
    
    /** Help ID for property editor */
    public static final String PE_REPLACE_CODE_HELP_ID = "i18n.pe.replacestring"; // NOI18N
    /** Help ID for property editor */
    public static final String PE_I18N_REGEXP_HELP_ID = "i18n.pe.i18nregexp";   // NOI18N
    /** Help ID for property editor */
    public static final String PE_BUNDLE_CODE_HELP_ID = "i18n.pe.bundlestring"; // NOI18N
    /** Help ID for property editor */
    public static final String PE_TEST_REGEXP_HELP_ID = "i18n.pe.testregexp";   // NOI18N
    /** Help ID for javaI18nString. It is a universal one for all subclasses. */
    public static final String PE_I18N_STRING_HELP_ID = "i18n.pe.i18nString";   // NOI18N

    private static final String DEFAULT_STANDARD_REPLACE_FORMAT = "java.util.ResourceBundle.getBundle(\"{bundleNameSlashes}\").getString(\"{key}\")"; // NOI18N
    private static final String DEFAULT_NETBEANS_REPLACE_FORMAT = "org.openide.util.NbBundle.getMessage({sourceFileName}.class, \"{key}\")"; // NOI18N
   
    /** Items for init format customizer. */
    private static List<String> initFormatItems;

    /** Help description for init format customizer. */
    private static List<String> initHelpItems;

    /** Items for replace format customizer. */
    private static List<String> replaceFormatItems;

    /** Help description for replace format customizer. */
    private static List<String> replaceHelpItems;

    /** Items for regular expression customizer. */
    private static List<String> regExpItems;

    /** Help description for regular expression customizer. */
    private static List<String> regExpHelpItems;
    
    /** Items for i18n regular expression customizer. */
    private static List<String> i18nRegExpItems;

    /** Gets <code>initFormatItems</code>. */
    public static List<String> getInitFormatItems() { 
        if(initFormatItems == null) {
            initFormatItems = new ArrayList<String>(2);
            initFormatItems.add("java.util.ResourceBundle.getBundle(\"{bundleNameSlashes}\")"); // NOI18N
            initFormatItems.add("org.openide.util.NbBundle.getBundle({sourceFileName}.class)"); // NOI18N
        }
              
        return initFormatItems;
    }

    /** Gets <code>InitHelpFormats</code>. */
    public static List<String> getInitHelpItems() {
        if(initHelpItems == null) {
            ResourceBundle bundle = getBundle();
            initHelpItems = new ArrayList<String>(3);
            initHelpItems.add(bundle.getString("TXT_PackageNameSlashes")); // NOI18N
            initHelpItems.add(bundle.getString("TXT_PackageNameDots")); // NOI18N
            initHelpItems.add(bundle.getString("TXT_SourceDataObjectName")); // NOI18N
        }
         
        return initHelpItems;
    }

    /** Gets <code>replaceFormatItems</code>. */
    public static List<String> getReplaceFormatItems() {
        if(replaceFormatItems == null) {
            replaceFormatItems = new ArrayList<String>(7);
            replaceFormatItems.add("{identifier}.getString(\"{key}\")"); // NOI18N
            replaceFormatItems.add("Utilities.getString(\"{key}\")"); // NOI18N
            replaceFormatItems.add(DEFAULT_STANDARD_REPLACE_FORMAT);
            replaceFormatItems.add("org.openide.util.NbBundle.getBundle({sourceFileName}.class).getString(\"{key}\")"); // NOI18N
            replaceFormatItems.add("java.text.MessageFormat.format(java.util.ResourceBundle.getBundle(\"{bundleNameSlashes}\").getString(\"{key}\"), {arguments})"); // NOI18N
            replaceFormatItems.add(DEFAULT_NETBEANS_REPLACE_FORMAT);
            replaceFormatItems.add("org.openide.util.NbBundle.getMessage({sourceFileName}.class, \"{key}\", {arguments})"); // NOI18N
        }
            
        return replaceFormatItems;
    }

    /** Gets default replace format - based on whether the project type is
     * a NB module project or not. (Module projects use NbBundle preferentially.)
     */
    public static String getDefaultReplaceFormat(boolean nbProject) {
        return nbProject ? DEFAULT_NETBEANS_REPLACE_FORMAT : DEFAULT_STANDARD_REPLACE_FORMAT;
    }

    /** Gets <code>replaceHeplItems</code>.*/
    public static List<String> getReplaceHelpItems() {
        if(replaceHelpItems == null) {
            ResourceBundle bundle = getBundle();
            replaceHelpItems = new ArrayList<String>(6);
            replaceHelpItems.add(bundle.getString("TXT_FieldIdentifier")); // NOI18N
            replaceHelpItems.add(bundle.getString("TXT_KeyHelp")); // NOI18N
            replaceHelpItems.add(bundle.getString("TXT_PackageNameSlashes")); // NOI18N
            replaceHelpItems.add(bundle.getString("TXT_PackageNameDots")); // NOI18N
            replaceHelpItems.add(bundle.getString("TXT_SourceDataObjectName")); // NOI18N
            replaceHelpItems.add(bundle.getString("TXT_Arguments")); // NOI18N
        }
            
        return replaceHelpItems;
    }

    /** Gets <code>regExpItems</code>. 
     * [:alnum:]=any character of [:digit:] or [:alpha:] class
     * [:alpha:]=any letter.
     * [:blank:]=space or tab.
     * [:cntrl:]=any character with octal codes 000 through 037, or DEL (octal code 177).
     * [:digit:]=any digit.
     * [:graph:]=any character that is not a [:alnum:] or [:punct:] class.
     * [:lower:]=any lower case letter.
     * [:print:]=any character from the [:space:] class, and any character that is not in the [:graph:] class.
     * [:punct:]=any one of ! \" # $ % & ' ( ) * + , - . / : ; < = > ? @ [ \\ ] ^ _ ` { | } ~
     * [:space:]=any character of CR FF HT NL VT SPACE.
     * [:upper:]=any upper case letter.
     * [:xdigit:]=any hexa digit character.
     */
    public static List<String> getRegExpItems() {
        if(regExpItems == null) {
            regExpItems = new ArrayList<String>(4);
            //XXX Only first array item taken into account when checking, rest are not used
            regExpItems.add("(getString|getBundle)[:space:]*\\([:space:]*{hardString}|//[:space:]*NOI18|getMessage[:space:]*\\(([:alnum:]|[:punct:]|[:space:])*,[:space:]*{hardString}"); // NOI18N
            regExpItems.add("(getString|getBundle)[:space:]*\\([:space:]*{hardString}"); // NOI18N
            regExpItems.add("//[:space:]*NOI18N"); // NOI18N
            regExpItems.add("(getString|getBundle)[:space:]*\\([:space:]*|getMessage[:space:]*\\(([:alnum:]|[:punct:]|[:space:])*,[:space:]*{hardString}|//[:space:]*NOI18N"); // NOI18N
        }
            
        return regExpItems;
    }
    
    /** Gets <code>i18nRegExpItems</code>. */
    public static List<String> getI18nRegExpItems() {
        if(i18nRegExpItems == null) {
            i18nRegExpItems = new ArrayList<String>(2);
            i18nRegExpItems.add("getString[:space:]*\\([:space:]*{hardString}"); // NOI18N
            i18nRegExpItems.add("(getString[:space:]*\\([:space:]*|getMessage[:space:]*\\(([:alnum:]|[:punct:]|[:space:])*,[:space:]*){hardString}"); // NOI18N
        }
            
        return i18nRegExpItems;
    }

    /** 
     * Indicates if folder or its subfolders contains data object
     * that is supported by any internationalization factory. 
     */
    public static boolean containsAcceptedDataObject(DataFolder folder) {
        DataObject[] children = folder.getChildren();
        DataObject[] folders = new DataObject[children.length];
        int i, foldersCount = 0;

        for (DataObject child : children) {
            if (child instanceof DataFolder) {  
                folders[foldersCount++] = child;
            } else if (FactoryRegistry.hasFactory(child.getClass())) {
                return true;
            }
        }
        for (i = 0; i < foldersCount; i++) {
            if (containsAcceptedDataObject((DataFolder) children[i])) {
                return true;
            }
        }
        return false;
    }
    
    /** 
     * Recursivelly get all accepted data objects starting from given folder. 
     */
    public static List<DataObject> getAcceptedDataObjects(DataObject.Container folder) {
        List<DataObject> accepted = new ArrayList<DataObject>();
        
        final VisibilityQuery visQuery = VisibilityQuery.getDefault();

        DataObject[] children = folder.getChildren();

        for (DataObject child : children) {
            if (!visQuery.isVisible(child.getPrimaryFile())) {
                continue;
            }
            if(child instanceof DataObject.Container) {
                accepted.addAll(getAcceptedDataObjects((DataObject.Container)child));
            } else {
                if(FactoryRegistry.hasFactory(child.getClass()))
                    accepted.add(child);
            }
        }

        return accepted;
    }
    
    /** Gets resource bundle for i18n module. */
    public static ResourceBundle getBundle() {
        return NbBundle.getBundle(I18nUtil.class);
    }
    
    /** Gets i18n options. */
    public static I18nOptions getOptions() {
        return I18nOptions.getDefault();
    }
    

}
