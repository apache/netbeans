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

package org.netbeans.modules.form;

import java.awt.Component;
import java.beans.PropertyEditor;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;

/**
 * Interface of an internationalization service - form editor needs it to
 * perform automatic internationalization of forms. It is designed with respect
 * to the existing i18n architecture - i.e. to keep FormI18nStringEditor working
 * with the values created by the form editor.
 */
public interface I18nService {

    /**
     * Creates I18nValue object for given key and value. Should not be added
     * to the bundle file yet. (For that purpose 'update' method is called later.)
     */
    I18nValue create(String key, String value, DataObject srcDataObject);

    /**
     * Creates a copy of I18nValue, including data from all locales corresponding
     * to the actual key. The copied value does not refer to the original
     * properties file - i.e. can be added to another one.
     * @param value I18nValue to be copied
     * @return the copied I18nValue
     */
    I18nValue copy(I18nValue value);

    /**
     * Creates a new I18nValue object with a new key. Should do no changes to
     * the bundle file at this moment.
     */
    I18nValue changeKey(I18nValue prev, String newKey);

    /**
     * Creates a new I18nValue object with changed value. Should not do any
     * changes to the bundle file.
     */
    I18nValue changeValue(I18nValue prev, String value);

    /**
     * Creates a new I18nValue refering to given locale (both for reading and
     * writing from now).
     */
    I18nValue switchLocale(I18nValue value, String localeSuffix);

    /**
     * Updates bundle file according to given I18nValue objects - oldValue is
     * removed, newValue added. Update goes into given locale - parent files
     * are updated too if given key is not present in them. New properties file
     * is created if needed.
     */
    void update(I18nValue oldValue, I18nValue newValue,
                DataObject srcDataObject, String bundleName, String localeSuffix,
                boolean canRemove)
        throws IOException;

    /**
     * Returns property editor to be used for editing internationalized
     * property of given type (e.g. String). If an existing suitable editor is
     * passed then it is returned and no new property editor is created.
     */
    PropertyEditor getPropertyEditor(Class type, PropertyEditor existing);

    /**
     * Provides a component usable as property customizer (so typically a modal
     * dialog) that allows to choose (or create) a properties bundle file within
     * the project of given form data object. The selected file should be
     * written to the given property editor (via setValue) as a resource name
     * string.
     */
    Component getBundleSelectionComponent(PropertyEditor pe, FileObject srcFile);

    /**
     * Returns all currently available locales for given bundle in two arrays
     * of strings. The first one containes locale suffixes, the second one
     * corresponding display names for the user (should be unique).
     * Returning null means that working with design locales is not supported
     * by this service.
     */
    String[][] getAvailableLocales(FileObject srcFile, String bundleName);

    /**
     * Provides a visual component (modal dialog) usable as a property
     * customizer that allows create a new locale file for given bundle (default
     * bundle name provided). The created locale should be written as a string
     * (locale suffix) to the given propery editor.
     */
    Component getCreateLocaleComponent(PropertyEditor pe, FileObject srcFile, String bundleName);

    /**
     * Saves properties files edited for given source object (form). This method
     * is called when a form is being saved - so the corresponding bundle is
     * saved as well.
     */
    void autoSave(DataObject srcDataObject);

    /**
     * Called when a form is closed without saving changes. The changes in
     * corresponding properties file can be discarded as well.
     */
    void close(DataObject srcDataObject);

    /**
     * Checks project of given form whether it is suitable to be automatically
     * internationalized by default. Currently new forms in module projects
     * should be set to auto i18n, while standard user (J2SE) projects not.
     * [If we decide all projects should be internationalized, we can remove
     *  this method.]
     */
    boolean isDefaultInternationalizableProject(FileObject srcFile);

    /**
     * Returns the properties files that are used to store data for given source
     * file and correspond to given name (resource bundle). It is basically the
     * default properties file and all locale variants. The returned files can
     * be used e.g. for backup (undo).
     */
    List<URL> getResourceFiles(FileObject srcFile, String bundleName);
}
