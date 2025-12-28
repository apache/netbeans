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

import java.util.*;
import org.openide.*;
import org.openide.util.Lookup;

/**
 * An abstract class which defines interface for persistence managers (being
 * responsible for loading and saving forms) and provides a basic registration
 * facility.
 * PersistenceManager implementations should be able to deal with multiple
 * forms being saved and loaded by one instance of persistence manager (but
 * not concurrently).
 *
 * @author Ian Formanek, Tomas Pavek
 */

public abstract class PersistenceManager {

    // -------------------
    // abstract interface

    /** This method is used to check if the persistence manager can read the
     * given form (if it understands the form file format).
     * 
     * @param formObject form data object representing the form.
     * @return true if this persistence manager can load the form
     * @exception PersistenceException if any unexpected problem occurred
     */
    public abstract boolean canLoadForm(FormDataObject formObject)
        throws PersistenceException;

    /** This method loads the form from given data object.
     * @param formObject FormDataObject representing the form files
     * @param formModel FormModel to be filled with loaded data
     * @param nonfatalErrors List to be filled with errors occurred during
     *        loading which are not fatal (but should be reported)
     * @exception PersistenceException if some fatal problem occurred which
     *            prevents loading the form
     */
    public abstract void loadForm(FormDataObject formObject,
                                  FormModel formModel,
                                  List<Throwable> nonfatalErrors)
        throws PersistenceException;

    /** This method saves the form to given data object.
     * @param formObject FormDataObject representing the form files
     * @param formModel FormModel to be saved
     * @param nonfatalErrors List to be filled with errors occurred during
     *        saving which are not fatal (but should be reported)
     * @exception PersistenceException if some fatal problem occurred which
     *            prevents saving the form
     */
    public abstract void saveForm(FormDataObject formObject,
                                  FormModel formModel,
                                  List<Throwable> nonfatalErrors)
        throws PersistenceException;

    // ------------
    // static registry [provisional only]

    private static List<PersistenceManager> managers;
    private static List<String> managersByName;

    public static void registerManager(PersistenceManager manager) {
        getManagersList().add(manager);
    }

    public static void unregisterManager(PersistenceManager manager) {
        getManagersList().remove(manager);
    }

    static void registerManager(String managerClassName) {
        getManagersNamesList().add(managerClassName);
    }

    public static Iterator<PersistenceManager> getManagers() {
        ClassLoader classLoader = null;
        Iterator<String> iter = getManagersNamesList().iterator();
        while (iter.hasNext()) { // create managers registered by name
            if (classLoader == null)
                classLoader = Lookup.getDefault().lookup(ClassLoader.class);

            String pmClassName = iter.next();
            try {
                PersistenceManager manager = (PersistenceManager)
                        classLoader.loadClass(pmClassName).getDeclaredConstructor().newInstance();
                getManagersList().add(manager);
            }
            catch (Exception ex1) {
                notifyError(ex1, pmClassName);
            }
            catch (LinkageError ex2) {
                notifyError(ex2, pmClassName);
            }
        }
        getManagersNamesList().clear(); // [is it OK to lose unsuccessful managers?]

        return getManagersList().iterator();
    }

    private static List<PersistenceManager> getManagersList() {
        if (managers == null) {
            managers = new ArrayList<PersistenceManager>();
            managers.add(new GandalfPersistenceManager());
        }
        return managers;
    }

    private static List<String> getManagersNamesList() {
        if (managersByName == null)
            managersByName = new ArrayList<String>();
        return managersByName;
    }

    private static void notifyError(Throwable th, String pmClassName) {
        String msg = FormUtils.getFormattedBundleString(
            "FMT_ERR_PersistenceManagerInstantiation", // NOI18N
            new Object[] { pmClassName });

        ErrorManager errorManager = ErrorManager.getDefault();
        errorManager.annotate(th, msg);
        errorManager.notify(ErrorManager.EXCEPTION, th);
    }
}
