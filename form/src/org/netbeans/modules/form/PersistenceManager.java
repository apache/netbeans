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
                    classLoader.loadClass(pmClassName).newInstance();
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
