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

package org.netbeans.modules.form;

import java.beans.PropertyEditor;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.List;
import org.openide.filesystems.FileObject;

/**
 * Interface used by the form editor to manipulate resources (property values
 * stored externally, represented by ResourceValue). Implementation is provided
 * by the Swing App Framework module, storing the resources in properties files.
 * Theoretically there could be other implementors, but practically there is no
 * need for that.
 * @see ResourceSupport
 * 
 * @author Tomas Pavek
 */
public interface ResourceService {

    /**
     * Called when a new form is created and opened to do some preparation work.
     * @param srcFile the source java file just created
     */
    void prepareNew(FileObject srcFile);

    /**
     * Finds ResourceValue for given key, locale and source file. If no data is
     * available for given specific locale then a less specific or default
     * locale data is returned.
     * @param key the key
     * @param type the type of the value (needed for the infrastructure to
     *        determine the right converter from String)
     * @param localeSuffix the locale variant to be used (localization suffix
     *        including the initial underscore, e.g. _cs_CZ)
     * @param srcFile the source java file for which the resource is obtained
     * @return ResourceValue object with found data, or null
     */
    ResourceValue get(String key, Class type, String localeSuffix, FileObject srcFile);

    /**
     * Collects all available keys matching given regular expression (via
     * String.matches - i.e. key.matches(keyRegex) must be true).
     * @param keyRegex the regular expression that the key should match
     * @param srcFile the source file whose resources are to be searched
     * @return Collection of matching keys
     */
    Collection<String> findKeys(String keyRegex, FileObject srcFile);

    /**
     * Creates ResourceValue object for given key and value. It is not saved
     * to the file yet. (For that purpose 'update' method is called later.)
     * @param key the key (not checked for uniqueness - don't care if a value
     *        already exists)
     * @param type the type of the value (needed for the infrastructure to
     *        determine the right converter from String)
     * @param value the value object to be stored as a resource; used as a
     *        fallback if stringValue is not valid for constructing the value
     * @param stringValue textual representation of the value; needed if the
     *        value object itself can't be saved directly
     * @param srcFile the source java file for which the resource is created
     *        (used to determine where to store the resource)
     * @return initialized but not saved ResourceValue object 
     */
    ResourceValue create(String key, Class type, Object value, String stringValue, FileObject srcFile);

    /**
     * Creates a new ResourceValue object with a new key. Does not save it to
     * the file at this moment, just returns the changed object. (The 'update'
     * method is called later.)
     * @param resource the ResourceValue to be changed
     * @param newKey the new key
     * @return new ResourceValue instance with the new key (otherwise the same)
     */
    ResourceValue changeKey(ResourceValue resource, String newKey);

    /**
     * Creates new ResourceValue object with a changed value. Does not save it to
     * the file at this moment, just returns the changed object. (The 'update'
     * method is called later.)
     * @param resource the ResourceValue to be changed
     * @param newValue the new value object; used as a fallback if the string
     *        representation is not usable for constructing the value
     * @param newStringValue textual representation of the value; needed if the
     *        value object itself can't be saved directly
     * @return new ResourceValue instance with the new value (otherwise the same)
     */
    ResourceValue changeValue(ResourceValue resource, Object newValue, String newStringValue);

    /**
     * Creates a new ResourceValue corresponding to given locale (so getValue
     * will return the new locale specific value). If no data is available for
     * this specific locale then a less specific or default locale data is provided.
     * @param resource the ResourceValue to be changed
     * @param localeSuffix the locale variant to be used (localization suffix
     *        including the initial underscore, e.g. _cs_CZ)
     * @return new ResourceValue instance containing data for the given locale
     */
    ResourceValue switchLocale(ResourceValue resource, String localeSuffix);

    /**
     * Obtains all currently available locales of properties files for given
     * source file. Returns two arrays of strings. The first one containes
     * locale suffixes, the second one corresponding display names for the user
     * (should be unique).
     * @param srcFile the source java file whose resources are in question
     * @return array of available locales (locale suffixes and display names)
     */
    String[][] getAvailableLocales(FileObject srcFile);

    /**
     * Provides a visual component (modal dialog) usable as a property
     * customizer that allows create a new locale file for default properties
     * file belonging to given source file. It writes the created locale as
     * a string (locale suffix) to the given property editor.
     * @param prEd a property editor where the component writes the created
     *        locale suffix
     * @param srcFile the source java file whose defining the resources scope
     *        (first suitable properties file will be used for creating
     *         additional locale variant)
     * @return visual component of the customizer
     */
    java.awt.Component getCreateLocaleComponent(PropertyEditor prEd, FileObject srcFile);

    /**
     * Updates properties file according to given ResourceValue objects - oldValue
     * is removed, newValue added. Update goes into given locale - parent files
     * are updated too if given key is not present in them. New properties file
     * is created if needed.
     * @param oldValue the value to be removed or null
     * @param newValue the value to be added or null
     * @param srcFile the source java file whose properties files are going to be
     *        modified
     * @param localeSuffix the locale variant to be updated (localization suffix
     *        including the initial underscore, e.g. _cs_CZ)
     * @throws java.io.IOException when there is a problem with updating.
     */
    void update(ResourceValue oldValue, ResourceValue newValue,
                FileObject srcFile, String localeSuffix)
        throws IOException;

    /**
     * Saves properties files edited for given source object (form). This method
     * is called when the source is being saved - so the corresponding
     * properties files can be saved as well.
     * @param srcFile the source java file whose properties files should be saved
     */
    void autoSave(FileObject srcFile);

    /**
     * Called when the source file (form) is closed without saving changes.
     * The changes in corresponding properties file should be discarded as well.
     * @param srcFile the source java file whose properties files should be closed
     */
    void close(FileObject srcFile);

    /**
     * Returns whether forms in the project represented by given file should
     * actively be set to use the resources - i.e. not only the resources
     * are allowed, but they should even be used by default.
     * @param fileInProject a file representing the projevt (whatever source
     *        file from the project)
     * @return true if the given project can use resources, false if the
     *         project is not suitable for resources
     */
    boolean projectWantsUseResources(FileObject fileInProject);

    /**
     * Returns whether the project represented by given file allows to use the
     * resources infrastructure (corresponding libraries are available on
     * projects classpath). If this method returns true, form editor will allow
     * forms in the project to use resources.
     * @param fileInProject a file representing the projevt (whatever source
     *        file from the project)
     * @return true if the given project uses the resources infrastracture
     *         (corresponding libraries are on the classpath)
     */
    boolean projectUsesResources(FileObject fileInProject);

//    /**
//     * 
//     * @param fileInProject a file representing the projevt (whatever source
//     *        file from the project)
//     * @return if the update was successful (might be refused for some types of
//     *         projects)
//     */
//    boolean updateProjectForResources(FileObject fileInProject);

    boolean isExcludedProperty(Class componentType, String propName);

    String getInjectionCode(Object component, String variableName, FileObject srcFile);

    ResourcePanel createResourcePanel(Class valueType, FileObject srcFile);

    /**
     * Returns the files that define resources for given source file.
     * Practically this includes the properties file (resource map) and all its
     * locale or OS variants. Other files referenced from the resource map
     * (like icons) are not included. The returned files can be used e.g. for
     * backup during a refactoring operation.
     * @param srcFile the source java file for which the resource files should
     *        be returned
     * @return list of properties files defining resources for given source file
     */
    List<URL> getResourceFiles(FileObject srcFile);
}
