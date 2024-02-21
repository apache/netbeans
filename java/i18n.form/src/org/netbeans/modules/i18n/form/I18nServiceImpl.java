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

package org.netbeans.modules.i18n.form;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyEditor;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.ErrorManager;
import org.openide.DialogDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.loaders.*;
import org.openide.cookies.SaveCookie;
import org.openide.DialogDescriptor;
import org.openide.util.NbBundle;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;

import org.netbeans.modules.properties.*;
import org.netbeans.modules.i18n.*;
import org.netbeans.modules.i18n.java.JavaResourceHolder;

import org.netbeans.modules.form.I18nService;
import org.netbeans.modules.form.I18nValue;
import org.openide.filesystems.FileUtil;

/**
 * Implementation of form module's I18nService - used by form editor to control
 * internationalization of forms while i18n module owns all the technical means
 * (i18n values, property editors, bundle files).
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.form.I18nService.class)
public class I18nServiceImpl implements I18nService {

    // remembered original state for changes made for given source data objects
    // mapping source DO to a map of properties DO to ChangeInfo
    private Map/*<DataObject, Map<DataObject, ChangeInfo>>*/ changesMap = new HashMap();

    private static class ChangeInfo {
        Map/*<String, Object[]>*/ changed = new HashMap(); // for each key holds all data across all locales
        Set/*<String>*/ added = new HashSet(); // holds keys that were not originally present
    }

    /**
     * Creates I18nValue object for given key and value. Should not be added
     * to the bundle file yet. (For that purpose 'update' method is called later.)
     */
    @Override
    public I18nValue create(String key, String value, DataObject srcDataObject) {
        FormI18nString i18nString = new FormI18nString(srcDataObject);
        i18nString.setKey(key);
        i18nString.setValue(value);
        return i18nString;
    }

    /**
     * Creates a copy of I18nValue, including data from all locales corresponding
     * to the actual key. The copied value does not refer to the original
     * properties file - i.e. can be added to another one.
     * @param value I18nValue to be copied
     * @return the copied I18nValue
     */
    @Override
    public I18nValue copy(I18nValue value) {
        FormI18nString i18nString = (FormI18nString) value;
        FormI18nString copy = new FormI18nString(i18nString);
        copy.getSupport().getResourceHolder().setResource(null);
        if (i18nString.allData == null && i18nString.getKey() != null)  {
            JavaResourceHolder jrh = (JavaResourceHolder) i18nString.getSupport().getResourceHolder();
            copy.allData = jrh.getAllData(i18nString.getKey());
        } else {
            copy.allData = i18nString.allData;
        }
        return copy;
    }

    /**
     * Creates a new I18nValue object with a new key. Should do no changes to
     * the bundle file at this moment.
     */
    @Override
    public I18nValue changeKey(I18nValue prev, String newKey) {
        FormI18nString oldI18nString = (FormI18nString) prev;
        FormI18nString changedI18nString;
        if (oldI18nString.getKey() == I18nValue.COMPUTE_AUTO_KEY) {
            // set key which was unset so far
            changedI18nString = oldI18nString;
        }
        else { // create a new value for the new key
            changedI18nString = new FormI18nString(oldI18nString);
            changedI18nString.allData = oldI18nString.allData;
        }
        changedI18nString.setKey(newKey);
        return changedI18nString;
    }

    /**
     * Creates a new I18nValue object with changed value. Should not do any
     * changes to the bundle file.
     */
    @Override
    public I18nValue changeValue(I18nValue prev, String value) {
        FormI18nString i18nString = new FormI18nString((FormI18nString)prev);
        i18nString.setValue(value);
        return i18nString;
    }

    /**
     * Creates a new I18nValue refering to given locale (both for reading and
     * writing from now).
     */
    @Override
    public I18nValue switchLocale(I18nValue value, String localeSuffix) {
        if (value == null || value.getKey() == null)
            return value;

        FormI18nString i18nString;
        if (value instanceof FormI18nInteger) {
            i18nString = new FormI18nInteger((FormI18nInteger)value);
        } else if (value instanceof FormI18nMnemonic) {
            i18nString = new FormI18nMnemonic((FormI18nMnemonic)value);
        } else {
            i18nString = new FormI18nString((FormI18nString)value);
        }
        JavaResourceHolder rh = (JavaResourceHolder) i18nString.getSupport().getResourceHolder();
        rh.setLocalization(localeSuffix);
        i18nString.setValue(rh.getValueForKey(i18nString.getKey()));
        i18nString.setComment(rh.getCommentForKey(i18nString.getKey()));
        return i18nString;
    }

    /**
     * Updates bundle file according to given I18nValue objects - oldValue is
     * removed, newValue added. Update goes into given locale - parent files
     * are updated too if given key is not present in them. New properties file
     * is created if needed.
     */
    @Override
    public void update(I18nValue oldValue, I18nValue newValue,
                       DataObject srcDataObject, String bundleName, String localeSuffix,
                       boolean canRemove)
        throws IOException
    {
        FormI18nString oldI18nString = (FormI18nString) oldValue;
        FormI18nString newI18nString = (FormI18nString) newValue;

        if (oldI18nString != null) {
            ResourceHolder oldRH = oldI18nString.getSupport().getResourceHolder();
            DataObject oldRes = oldRH.getResource();
            DataObject newRes = null;
            if (newI18nString != null) {
                ResourceHolder newRH = newI18nString.getSupport().getResourceHolder();
                newRes = newRH.getResource();
                if (newRes == null) { // use same resource bundle as old value
                    newRH.setResource(oldRes);
                    newRes = oldRes;
                }
            }

            String oldKey = oldI18nString.getKey();
            if (canRemove && oldKey != null) {
                JavaResourceHolder jrh = (JavaResourceHolder) oldRH;
                Object allData = jrh.getAllData(oldKey);
                registerChange(srcDataObject, oldRes, oldKey, allData);

                if (newI18nString == null
                    || newI18nString.getKey() == null
                    || !newI18nString.getKey().equals(oldKey)
                    || newRes != oldRes)
                {   // removing i18n value, changing key, or moving to another properties file
                    oldI18nString.allData = allData;
                    jrh.removeProperty(oldKey);
                    if (newI18nString != null)
                        newI18nString.allData = oldI18nString.allData;
                    }
                else if (localeSuffix != null && !localeSuffix.equals("")) { // NOI18N
                    // remember all locale data (to be able to undo adding new specific value to a locale)
                    oldI18nString.allData = allData;
                }

                if (newI18nString == null
                    && oldRes == getPropertiesDataObject(srcDataObject.getPrimaryFile(), bundleName))
                {   // forget the resource bundle file - may want different next time
                    oldRH.setResource(null);
                }
            }
        }

        if (newI18nString != null && newI18nString.getKey() != null) {
            // valid new value - make sure it is up-to-date in the properties file
            JavaResourceHolder rh = (JavaResourceHolder) newI18nString.getSupport().getResourceHolder();
            String key = newI18nString.getKey();

            if (rh.getResource() == null) { // find or create properties file
                DataObject propertiesDO = getPropertiesDataObject(srcDataObject.getPrimaryFile(), bundleName);
                if (propertiesDO == null) { // create new properties file
                    propertiesDO = createPropertiesDataObject(srcDataObject.getPrimaryFile(), bundleName);
                    if (propertiesDO == null)
                        return;
                } else if (oldI18nString == null && newI18nString.getValue() == null) {
                    // if the value itself is null we actually want to update it from the properties file
                    rh.setResource(propertiesDO);
                    newI18nString.setValue(rh.getValueForKey(key));
                    newI18nString.setComment(rh.getCommentForKey(key));
                    return;
                }
                rh.setResource(propertiesDO);

                // make sure we use free (unique) key
                key = rh.findFreeKey(key);
                newI18nString.setKey(key);
            }

            rh.setLocalization(localeSuffix);
            if (!isValueUpToDate(rh, newI18nString)) {
                if (newI18nString.allData != null) { // restore complete data across all locales
                    if (oldI18nString != null && newI18nString.getValue() != null) {
                        // besides changing place (key/file) there might also be a new value/comment
                        updateAllData(newI18nString, localeSuffix);
                    }
                    rh.setAllData(key, newI18nString.allData);
                    newI18nString.allData = null;
                    if (oldI18nString == null) {
                        // update also the current value - might have come from a different locale
                        newI18nString.setValue(rh.getValueForKey(key));
                        newI18nString.setComment(rh.getCommentForKey(key));
                    }
                }
                else {
                    rh.addProperty(key, newI18nString.getValue(), newI18nString.getComment(), true);
                }
                registerChange(srcDataObject, rh.getResource(), newI18nString.getKey(), null);
            }
        }
    }

    private static boolean isValueUpToDate(ResourceHolder rh, I18nString i18nString) {
        String storedValue = rh.getValueForKey(i18nString.getKey());
        String storedComment = rh.getCommentForKey(i18nString.getKey());
        if ("".equals(storedComment)) // NOI18N
            storedComment = null;
        String newValue = i18nString.getValue();
        String newComment = i18nString.getComment();
        if ("".equals(newComment)) // NOI18N
            newComment = null;
        return Objects.equals(storedValue, newValue) && Objects.equals(storedComment, newComment);
    }

    private static void updateAllData(FormI18nString newI18nString, String localeSuffix) {
        // Not nice we deal with the data format that is internal to the properties
        // module, but need to workaround bug 240650 somehow - by applying all the changes
        // at once. Trying to modify the added item subsequently might happen when the
        // PropertiesStructure is in an inconsistent state and add the item for the second time.
        String[] allData = (newI18nString.allData instanceof String[]) ? (String[]) newI18nString.allData : null;
        if (allData != null) {
            for (int i=0; i < allData.length; i+=3) {
                String locale = allData[i];
                if (localeSuffix.equals(locale)) {
                    allData[i+1] = newI18nString.getValue();
                    allData[i+2] = newI18nString.getComment();
                    break;
                }
            }
        }
    }

    /**
     * Returns property editor to be used for editing internationalized
     * property of given type (e.g. String). If an existing suitable editor is
     * passed then it is returned and no new property editor is created.
     */
    @Override
    public PropertyEditor getPropertyEditor(Class type, PropertyEditor existing) {
        return existing instanceof FormI18nStringEditor ? existing : new FormI18nStringEditor();
    }

    private static boolean isPlainStringEditor(PropertyEditor pe) {
        return pe != null && pe.getClass().getName().endsWith(".StringEditor"); // NOI18N
    }

    /**
     * Provides a component usable as property customizer (so typically a modal
     * dialog) that allows to choose (or create) a properties bundle file within
     * the project of given form data object. The selected file should be
     * written to the given property editor (via setValue) as a resource name
     * string.
     */
    @Override
    public Component getBundleSelectionComponent(final PropertyEditor prEd, FileObject srcFile) {
        try {
            final FileSelector fs = new FileSelector(srcFile, JavaResourceHolder.getTemplate());
            return fs.getDialog(NbBundle.getMessage(I18nServiceImpl.class, "CTL_SELECT_BUNDLE_TITLE"), // NOI18N
                                new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent ev) {
                    DataObject bundleDO = fs.getSelectedDataObject();
                    if (bundleDO != null) {
                        ClassPath cp = ClassPath.getClassPath(bundleDO.getPrimaryFile(), ClassPath.SOURCE);
                        if (cp != null) {
                            String bundleName = cp.getResourceName(bundleDO.getPrimaryFile(), '/', false);
                            prEd.setValue(bundleName);
                        }
                    }
                }
            });
        }
        catch (IOException ex) {
            // means that template for properties file was not found - unlikely
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        }
        return null;
    }

    /**
     * Returns all currently available locales for given bundle in two arrays
     * of strings. The first one containes locale suffixes, the second one
     * corresponding display names for the user (should be unique).
     * Returning null means that working with design locales is not supported
     * by this service.
     */
    @Override
    public String[][] getAvailableLocales(FileObject srcFile, String bundleName) {
        PropertiesDataObject dobj = null;
        try {
            dobj = getPropertiesDataObject(srcFile, bundleName);
        }
        catch (IOException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        }
        if (dobj == null) {
            return new String[][] {{},null};
        }

        List list = new ArrayList();
        list.add(dobj.getPrimaryEntry());
        list.addAll(dobj.secondaryEntries());
        try {
            String baseName = dobj.getName() + "_"; // NOI18N
            for (FileObject fo : dobj.getPrimaryFile().getParent().getChildren()) {
                String fileName = fo.getNameExt();
                if (fileName.endsWith(".properties") && fileName.startsWith(baseName)) { // NOI18N
                    DataObject dobj2 = DataObject.find(fo);
                    if (dobj2 instanceof PropertiesDataObject) {
                        list.add(((MultiDataObject)dobj2).getPrimaryEntry());
                    }
                }
            }
        } catch (IOException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        }
        list.sort(new Comparator() {
            @Override
            public int compare(Object o1, Object o2) {
                MultiDataObject.Entry e1 = (MultiDataObject.Entry) o1;
                MultiDataObject.Entry e2 = (MultiDataObject.Entry) o2;
                return e1.getFile().getName().compareTo(e2.getFile().getName());
            }
        });

        String[] locales = new String[list.size()];
        String[] displays = new String[list.size()];
        for (int i=0; i < list.size(); i++) {
            MultiDataObject.Entry entry = (MultiDataObject.Entry) list.get(i);
            locales[i] = org.netbeans.modules.properties.Util.getLocaleSuffix(entry);
            displays[i] = org.netbeans.modules.properties.Util.getLocaleLabel(entry);
        }
        return new String[][] { locales, displays };
    }

    /**
     * Provides a visual component (modal dialog) usable as a property
     * customizer that allows create a new locale file for given bundle (default
     * bundle name provided). The created locale should be written as a string
     * (locale suffix) to the given propery editor.
     */
    @Override
    public Component getCreateLocaleComponent(final PropertyEditor prEd, FileObject srcFile, String bundleName) {
        final PropertiesDataObject propertiesDO;
        try {
            propertiesDO = getPropertiesDataObject(srcFile, bundleName);
        }
        catch (DataObjectNotFoundException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            return null;
        }
        final Dialog[] dialog = new Dialog[1];
        final LocalePanel localePanel = new LocalePanel();

        DialogDescriptor dialogDescriptor = new DialogDescriptor(
            localePanel,
            NbBundle.getBundle(PropertiesDataObject.class).getString("CTL_NewLocaleTitle"), // NOI18N
            true,
            DialogDescriptor.OK_CANCEL_OPTION,
            DialogDescriptor.OK_OPTION,
            new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent evt) {
                    if (evt.getSource() == DialogDescriptor.OK_OPTION) {
                        String locale = localePanel.getLocale().toString();
                        org.netbeans.modules.properties.Util.createLocaleFile(
                                propertiesDO, locale, false);
                        prEd.setValue("_" + locale); // NOI18N
                    }
                    dialog[0].setVisible(false);
                    dialog[0].dispose();
                }
            }
        );
        dialog[0] = DialogDisplayer.getDefault().createDialog(dialogDescriptor);
        return dialog[0];
    }

    /**
     * Saves properties files edited for given source object (form). This method
     * is called when a form is being saved - so the corresponding bundle is
     * saved as well.
     */
    @Override
    public void autoSave(DataObject srcDataObject) {
        Map/*<DataObject, ChangeInfo>*/ relatedMap = (Map) changesMap.remove(srcDataObject);
        if (relatedMap != null) {
            for (Iterator it=relatedMap.keySet().iterator(); it.hasNext(); ) {
                DataObject propertiesDO = (DataObject) it.next();
                // [not sure: should we auto-save only bundles not opened in the editor?
                //  perhaps it's OK to save always...]
                SaveCookie save = (SaveCookie) propertiesDO.getCookie(SaveCookie.class);
                if (save != null) {
                    try {
                        save.save();
                    }
                    catch (IOException ex) {
                        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                    }
                }
            }
        }
    }

    /**
     * Called when a form is closed without saving changes. The changes in
     * corresponding properties file need to be discarded (reverted) as well.
     */
    @Override
    public void close(DataObject srcDataObject) {
        Map/*<DataObject, ChangeInfo>*/ relatedMap = (Map) changesMap.remove(srcDataObject);
        if (relatedMap != null) {
            for (Iterator it=relatedMap.entrySet().iterator(); it.hasNext(); ) {
                Map.Entry/*<DataObject, ChangeInfo>*/ e = (Map.Entry) it.next();
                PropertiesDataObject propertiesDO = (PropertiesDataObject) e.getKey();
                ChangeInfo changes = (ChangeInfo) e.getValue();
                JavaResourceHolder rh = new JavaResourceHolder();
                rh.setResource(propertiesDO);
                for (Iterator/*<Map.Entry<String, Object>>*/ it2=changes.changed.entrySet().iterator(); it2.hasNext(); ) {
                    Map.Entry/*<String, Object>*/ e2 = (Map.Entry) it2.next();
                    String key = (String) e2.getKey();
                    Object allData = e2.getValue();
                    rh.setAllData(key, allData);
                }
                for (Iterator it2=changes.added.iterator(); it2.hasNext(); ) {
                    String key = (String) it2.next();
                    rh.removeProperty(key);
                }
                // [not sure: should we save the bundle for consistency?
                //  perhaps not necessary...]
//                SaveCookie save = (SaveCookie) propertiesDO.getCookie(SaveCookie.class);
//                if (save != null) {
//                    try {
//                        save.save();
//                    }
//                    catch (IOException ex) {
//                        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
//                    }
//                }
            }
        }
    }

    /**
     * Checks project of given form whether it is suitable to be automatically
     * internationalized by default. Currently new forms in module projects
     * should be set to auto i18n, while standard user (J2SE) projects not.
     * [If we decide all projects should be internationalized, we can remove
     *  this method.]
     */
    @Override
    public boolean isDefaultInternationalizableProject(FileObject srcFile) {
        return isNbBundleAvailable(srcFile);
    }

    static boolean isNbBundleAvailable(FileObject srcFile) {
        // is there a good way to recognize that NbBundle is available?
        // - execution CP may not work if everything is cleaned
        // - looking for NbBundle.java in sources of execution CP roots is expensive
        // - checking project impl. class name is ugly
        // - don't know how to check if there is "org.openide.util" module
        ClassPath classPath = ClassPath.getClassPath(srcFile, ClassPath.EXECUTE);
        if (classPath != null && classPath.findResource("org/openide/util/NbBundle.class") != null) // NOI18N
            return true;

        // hack: check project impl. class name
        Project p = FileOwnerQuery.getOwner(srcFile);
        if (p != null && p.getClass().getName().startsWith("org.netbeans.modules.apisupport.") // NOI18N
                && p.getClass().getName().endsWith("Project")) // NOI18N
            return true;

        return false;
    }

    @Override
    public List<URL> getResourceFiles(FileObject srcFile, String bundleName) {
        PropertiesDataObject dobj = null;
        try {
            dobj = getPropertiesDataObject(srcFile, bundleName);
            if (dobj != null) {
                List<URL> list = new ArrayList<URL>();
                list.add(dobj.getPrimaryEntry().getFile().toURL());
                for (MultiDataObject.Entry e : dobj.secondaryEntries()) {
                    list.add(e.getFile().toURL());
                }
                return list;
            } else {
                FileObject root = getResourcesRoot(srcFile);
                if (root != null) {
                    return Collections.singletonList(
                        new File(FileUtil.toFile(root).getPath()
                            + File.separator + bundleName + ".properties").toURI().toURL()); // NOI18N
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(I18nServiceImpl.class.getName()).log(Level.INFO, null, ex); // NOI18N
        }
        return Collections.emptyList();
    }

    // -----

    private static PropertiesDataObject getPropertiesDataObject(FileObject srcFile, String bundleName)
        throws DataObjectNotFoundException
    {
        if (bundleName.startsWith("/")) // NOI18N
            bundleName = bundleName.substring(1);
        if (!bundleName.toLowerCase().endsWith(".properties")) // NOI18N
            bundleName = bundleName + ".properties"; // NOI18N
        FileObject bundleFile = org.netbeans.modules.i18n.Util.getResource(srcFile, bundleName);
        if (bundleFile != null) {
            DataObject dobj = DataObject.find(bundleFile);
            if (dobj instanceof PropertiesDataObject)
                return (PropertiesDataObject) dobj;
        }
        return null;
    }

    private static DataObject createPropertiesDataObject(FileObject srcFile,
                                                         String filePath)
        throws IOException
    {
        if (filePath == null) {
            return null;
        }

        FileObject root = getResourcesRoot(srcFile);
        return org.netbeans.modules.properties.Util.createPropertiesDataObject(root, filePath);
    }

    private static FileObject getResourcesRoot(FileObject srcFile) {
        FileObject root = null;
        Project owner = FileOwnerQuery.getOwner(srcFile);
        if (owner != null) {
            // this is for projects that have split sources/resources folder structures.
            Sources srcs = ProjectUtils.getSources(owner);
            SourceGroup[] grps = srcs.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_RESOURCES);
            if (grps != null && grps.length > 0) {
                root = grps[0].getRootFolder();
            }
        }
        if (root == null) {
            root = ClassPath.getClassPath(srcFile, ClassPath.SOURCE).getRoots()[0];
        }
        return root;
    }

    /**
     * Keeps original data from properties file when first change is done in
     * given properties file for given source file (form). If the source file
     * is discarded later, all relevant changes in the properties file are reverted.
     */
    private void registerChange(DataObject srcDO, DataObject propertiesDO, String key, Object allData) {
        if (propertiesDO == null) {
            return;
        }
        Map/*<DataObject, ChangeInfo>*/ relatedMap = (Map) changesMap.get(srcDO);
        if (relatedMap == null) {
            relatedMap = new HashMap();
            changesMap.put(srcDO, relatedMap);
        }
        ChangeInfo changes = (ChangeInfo) relatedMap.get(propertiesDO);
        if (changes == null) {
            changes = new ChangeInfo();
            relatedMap.put(propertiesDO, changes);
        }
        if (!changes.changed.containsKey(key) && !changes.added.contains(key)) {
            // original state of this key not registered yet
            if (allData != null) // something changed in existing data
                changes.changed.put(key, allData); // allData contains original data
            else // new key added
                changes.added.add(key);
        }
    }
}
