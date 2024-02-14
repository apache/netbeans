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


package org.netbeans.modules.i18n;


import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.queries.SourceForBinaryQuery;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.HelpCtx;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.NbPreferences;

/**
 * Options for i18n module.
 *
 * @author  Peter Zavadsky
 */
public class I18nOptions {
    private static final I18nOptions INSTANCE = new I18nOptions();

    /** Property name for init java code.
     * Format for code which initializes generated resource bundle field in java source. */
    public static final String PROP_INIT_JAVA_CODE = "initJavaCode"; // NOI18N
    
    /** Property name for replacing java code.
     * Format for actual i18n-ized code which replaces found non-i18n-ized hardcoded string. */
    public static final String PROP_REPLACE_JAVA_CODE = "replaceJavaCode"; // NOI18N
    
    /** Property name for regular expression for finding non-i18n strings.
     * Regular expression format which is used for deciding whether found hardcoded string is non-i18n-ized. 
     * If line with found hardcoded string doesn't satisfy the expression it's non-i18n-ized. */
    public static final String PROP_REGULAR_EXPRESSION = "regularExpression"; // NOI18N

    /** Property name for regular expression for finding i18n strings.
     * Regular expression format which is used for deciding whether found hardcoded string is i18n-ized. 
     * If line with found hardcoded string satisfies the expression it's i18n-ized. */
    public static final String PROP_I18N_REGULAR_EXPRESSION = "i18nRegularExpression"; // NOI18N

    /** Property name for replace rseource value. 
     * Indicates wheter values in resources for existing keys has to be replacesed or kept the old ones. */
    public static final String PROP_REPLACE_RESOURCE_VALUE = "replaceResourceValue"; // NOI18N
    
    /** Property name for last used resource data object.
     * Hidden property which serializes last resource data object used by i18n module. */
    public static final String PROP_LAST_RESOURCE2 = "lastResource2"; // NOI18N

    private static final Logger LOG =
            Logger.getLogger("org.netbeans.modules.i18n.I18nOptions"); // NOI18N

    
    private I18nOptions() {}

    public static I18nOptions getDefault() {
        return INSTANCE;
    }

    private  static Preferences getPreferences() {
        return NbPreferences.forModule(I18nOptions.class);
    }    
    
    /** Getter for init java code property. */
    public String getInitJavaCode() {
        return getPreferences().get(PROP_INIT_JAVA_CODE,
                                    I18nUtil.getInitFormatItems().get(0));
    }

    /** Setter for init java code property. */
    public void setInitJavaCode(String initJavaCode) {
        getPreferences().put(PROP_INIT_JAVA_CODE,
                             initJavaCode);
    }    
    
    /** Getter for replace java code property. */
    public String getReplaceJavaCode() {
        // Lazy init.
        return getPreferences().get(PROP_REPLACE_JAVA_CODE,
                                    I18nUtil.getDefaultReplaceFormat(false));
    }

    /** Setter for replace java code property. */
    public void setReplaceJavaCode(String replaceJavaCode) {
        getPreferences().put(PROP_REPLACE_JAVA_CODE, replaceJavaCode);
    }    

    /** Getter for regular expression property. 
     * @see #PROP_REGULAR_EXPRESSION */
    public String getRegularExpression() {
        return getPreferences().get(PROP_REGULAR_EXPRESSION,
                                    I18nUtil.getRegExpItems().get(0));        
    }

    /** Setter for regular expression property. 
     * @see #PROP_REGULAR_EXPRESSION */
    public void setRegularExpression(String regExp) {
        getPreferences().put(PROP_REGULAR_EXPRESSION, regExp);
    }    
    
    /** Getter for i18n regular expression property. 
     * @see #PROP_I18N_REGULAR_EXPRESSION */
    public String getI18nRegularExpression() {
        return getPreferences().get(PROP_I18N_REGULAR_EXPRESSION,
                                    I18nUtil.getI18nRegExpItems().get(0));        
    }

    /** Setter for i18n regular expression property. 
     * @see #PROP_I18N_REGULAR_EXPRESSION */
    public void setI18nRegularExpression(String regExp) {
        getPreferences().put(PROP_I18N_REGULAR_EXPRESSION, regExp);
    }    

    /** Getter for replace resource value property. */
    public boolean isReplaceResourceValue() {
        return getPreferences().getBoolean(PROP_REPLACE_RESOURCE_VALUE,
                                           false);        
    }

    /** Setter for replacve resource value property. */
    public void setReplaceResourceValue(boolean replaceResourceValue) {
        getPreferences().putBoolean(PROP_REPLACE_RESOURCE_VALUE,
                                    replaceResourceValue);
    }

    /** Getter for last resource property. */
    @Deprecated
    public DataObject getLastResource2() {
        return getLastResource2(null);
    }

    /** Getter for last resource property. */
    public DataObject getLastResource2(DataObject srcDataObject) {
        String path = getPreferences().get(PROP_LAST_RESOURCE2,null);
        if(path == null) {
            return null;
        }
        FileObject f = (srcDataObject != null) ? findFileObject(srcDataObject, path) : findFileObject(path);
        if ((f != null) && !f.isFolder() && f.isValid()) {
            try {
                return DataObject.find(f);
            } catch (DataObjectNotFoundException e) {
                /* The file was probably deleted or moved. */
                getPreferences().remove(PROP_LAST_RESOURCE2);
            }
        }
        return null;
    }
    
    /** Setter for last resource property. */
    public void setLastResource2(DataObject lastResource) {
        // Make sure it is sane.        
        if(lastResource == null)
            return;
        
        getPreferences().put(PROP_LAST_RESOURCE2,
                             lastResource.getPrimaryFile().getPath());
    }
    
    /** Get context help for this system option.
    * @return context help
    */
    public HelpCtx getHelpCtx () {
        return new HelpCtx (I18nOptions.class);
    }
    
    private static FileSystem[] getFileSystems() {
        List<FileSystem> retval = new ArrayList<FileSystem>();
        for (File file : File.listRoots()) {
            FileObject fo = FileUtil.toFileObject(file);
            if (fo != null) {
                try {
                    retval.add(fo.getFileSystem());
                } catch (FileStateInvalidException ex) {
                        LOG.log(Level.INFO, null, ex);
                }
            }
        }        
        return retval.toArray(new FileSystem[0]);
    }
    
    @Deprecated
    private static FileObject findFileObject(String path) {
        for (FileSystem fileSystem : getFileSystems()) {
            FileObject retval = fileSystem.findResource(path);
            if (retval != null) {
                return retval;
            }
        }
        return null;
    }

    private static FileObject findFileObject(DataObject srcDataObject, String path) {
        FileObject pfo = srcDataObject.getPrimaryFile();
        ClassPath cp = ClassPath.getClassPath(pfo, ClassPath.EXECUTE);

        // #167334
        if(cp == null) {
            LOG.info("Unable to find FileObject due to ClassPath is null");
            return null;
        }

        for(FileObject fo : getRoots(cp)) {
            try {
                FileSystem fs = fo.getFileSystem();
                if (fs != null) {
                    FileObject retval = fs.findResource(path);
                    if (retval != null) {
                        return retval;
                    }
                }
            } catch (FileStateInvalidException ex) {
                LOG.log(Level.INFO, null, ex);
            }
        }
        
        return null;
    }


    private static List<FileObject> getRoots(ClassPath cp) {
        assert cp != null;
        ArrayList<FileObject> l = new ArrayList<FileObject>(cp.entries().size());
        for (ClassPath.Entry e : cp.entries()) {

            // try to map it to sources
            URL url = e.getURL();
            SourceForBinaryQuery.Result r= SourceForBinaryQuery.findSourceRoots(url);
            FileObject [] fos = r.getRoots();
            if (fos.length > 0) {
                for (FileObject fo : fos) {
                    l.add(fo);
                }
            } else {
                if (e.getRoot()!=null) 
                    l.add(e.getRoot()); // add the class-path location
                                        // directly
            }
        }

        return l;
    }    
}
