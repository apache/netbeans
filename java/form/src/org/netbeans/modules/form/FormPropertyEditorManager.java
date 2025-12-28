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

import java.beans.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Takes care of registration and finding property editors used by the Form Editor.
 *
 * @author Ian Formanek, Tomas Pavek
 */
public final class FormPropertyEditorManager {

    /**
     * Maps property type to property editor class. There are 2 maps - one for
     * findings of the standard PropertyEditorManager, one for our specific
     * search path (org.netbeans.modules.form.editors2 etc).
     */
    private static Map<String, Map<Class, Class>> editorClassCache;

    /**
     * Maps property type to list of property editor classes. For externally
     * registered editors (like i18n).
     */
    private static Map<Class, List<Class>> expliciteEditors;

    private static final String[] DEFAULT_EDITOR_SEARCH_PATH = {
        "org.netbeans.modules.form.editors2", // this needs to be first // NOI18N
        "org.netbeans.beaninfo.editors", // NOI18N
        "org.netbeans.modules.swingapp" // NOI18N
    };

    // -------

    public static synchronized PropertyEditor findEditor(FormProperty property) {
        Class type = property.getValueType();
        FormModel form = property.getPropertyContext().getFormModel();
        List<PropertyEditor> list = findEditors(type, form, false);
        return list.isEmpty() ? null : list.get(0);
    }

    public static synchronized PropertyEditor[] getAllEditors(FormProperty property) {
        Class type = property.getValueType();
        FormModel form = property.getPropertyContext().getFormModel();
        List<PropertyEditor> list = findEditors(type, form, true);
        return list.toArray(new PropertyEditor[0]);
    }

    public static synchronized void registerEditor(Class propertyType, Class editorClass) {
        List<Class> classList;
        if (expliciteEditors != null) {
            classList = expliciteEditors.get(propertyType);
        } else {
            classList = null;
            expliciteEditors = new HashMap<Class, List<Class>>();
        }
        if (classList == null) {
            classList = new LinkedList<Class>();
            classList.add(editorClass);
            expliciteEditors.put(propertyType, classList);
        } else if (!classList.contains(editorClass)) {
            classList.add(editorClass);
        }
    }

    private static List<Class> getRegisteredEditorClasses(Class propertyType) {
        List<Class> classList = expliciteEditors != null ? expliciteEditors.get(propertyType) : null;
        return classList != null ? classList : Collections.EMPTY_LIST;
    }

    /**
     * Finds a low-level property editor from a set of known editors suitable
     * for the GUI builder. It's here for ResourceWrapperEditor implementations
     * to find the basic editor they wrap.
     * @param type the type of the property
     * @return newly created instance of found PropertyEditor, or null if not found
     */
    public static PropertyEditor findBasicEditor(Class type) {
        return findPrimaryEditor(type, false);
    }

    private static List<PropertyEditor> findEditors(Class type, FormModel targetForm, boolean all) {
        List<PropertyEditor> editorList = new ArrayList<PropertyEditor>(5);

        // 1st look for primary editor among known editors
        PropertyEditor prEd = findPrimaryEditor(type, true);
        if (prEd != null) {
            editorList.add(prEd);
            if (!all) {
                return editorList;
            }
        }

        // 2nd search custom search path (theoretically there can be a package
        // from a JAR that is on user project's classpath)
        if (isEditorInCache(type, "2")) { // NOI18N
            prEd = createEditorFromCache(type, "2"); // NOI18N
        } else {
            prEd = null;
            String typeName = type.getSimpleName();
            if (!typeName.contains("[")) { // not an array type // NOI18N
                Class editorClass = null;
                List<String> defaultSearchPath = Arrays.asList(DEFAULT_EDITOR_SEARCH_PATH);
                for (String path : FormLoaderSettings.getInstance().getEditorSearchPath()) {
                    if (path != null && path.length() > 0 && !defaultSearchPath.contains(path)) {
                        String name = path + "." + typeName + "Editor"; // NOI18N
                        try {
                            editorClass = FormUtils.loadClass(name, targetForm);
                            prEd = createEditorInstance(editorClass);
                            if (prEd != null) {
                                break;
                            } // otherwise failed instantiating, continue
                        }
                        catch (Exception e) {} // silently ignore
                        catch (LinkageError e) {} // silently ignore
                        editorClass = null;
                    }
                }
                addEditorClassToCache(type, editorClass, "2"); // also cache nonexistence // NOI18N
            } // ignore array types
        }
        if (prEd != null) {
            editorList.add(prEd);
            if (!all) {
                return editorList;
            }
        }

        // 3rd additional specialized editors registered using registerEditor(...)
        for (Class cls : getRegisteredEditorClasses(type)) {
            prEd = createEditorInstance(cls);
            if (prEd != null) {
                editorList.add(prEd);
                if (!all) {
                    return editorList;
                }
            }
        }

        // 4th general ComponentChooserEditor
        if (editorList.isEmpty() && isComponentType(type)) {
            editorList.add(new ComponentChooserEditor(new Class[] { type }));
            if (!all) {
                return editorList;
            }
        }

        // 5th general editors for connection and custom code
        editorList.add(new RADConnectionPropertyEditor(type, RADConnectionPropertyEditor.Type.FormConnection));
        editorList.add(new RADConnectionPropertyEditor(type, RADConnectionPropertyEditor.Type.CustomCode));

        return editorList;
    }

    /**
     * Finds a property editor in a known set of property editors provided by
     * form module and NB platform. It guarantees that the known editors needed
     * for the GUI builder are found first, java.beans.PropertyEditorManager is
     * consulted only secondarily.
     * @param type the type of the property
     * @param wrapperAllowed true if the PE may wrap another PE, false to find
     *        only the basic editor
     * @return newly created instance of found PropertyEditor, or null if not found
     */
    private static PropertyEditor findPrimaryEditor(Class type, boolean wrapperAllowed) {
        // look at the known search path first
        PropertyEditor prEd = null;
        String cacheKey = wrapperAllowed ? "1a" : "1b"; // NOI18N
        if (isEditorInCache(type, cacheKey)) {
            prEd = createEditorFromCache(type, cacheKey);
        } else {
            String typeName = type.getSimpleName();
            if (!typeName.contains("[") // not an array type // NOI18N
                    && !type.equals(Object.class)) { // Issue 204469
                Class editorClass = null;
                for (String path : DEFAULT_EDITOR_SEARCH_PATH) {
                    String name = path + "." + typeName + "Editor"; // NOI18N
                    try {
                        editorClass = FormUtils.loadSystemClass(name);
                        if (wrapperAllowed || !ResourceWrapperEditor.class.isAssignableFrom(editorClass)) {
                            prEd = createEditorInstance(editorClass);
                            if (prEd != null) {
                                break;
                            } // otherwise failed instantiating, continue
                        }
                    }
                    catch (Exception e) {} // silently ignore
                    catch (LinkageError e) {} // silently ignore
                    editorClass = null;
                }
                addEditorClassToCache(type, editorClass, cacheKey); // also cache nonexistence
            } // ignore array types
        }
        if (prEd == null) {
            // if not found on NB search path, try java.beans.PropertyEditorManager
            cacheKey = "1c"; // NOI18N
            if (isEditorInCache(type, cacheKey)) {
                prEd = createEditorFromCache(type, cacheKey);
            } else {
                prEd = (type != Object.class) ? PropertyEditorManager.findEditor(type) : null;
                addEditorToCache(type, prEd, cacheKey); // also cache nonexistence
            }
        }
        return prEd;
    }

    private static boolean isEditorInCache(Class propertyType, String key) {
        return getEditorClassCache(key).containsKey(propertyType);
        // the cache may also hold the information that there is no property editor for given type
    }

    private static PropertyEditor createEditorFromCache(Class propertyType, String key) {
        Class editorClass = getEditorClassCache(key).get(propertyType);
        return editorClass != null ? createEditorInstance(editorClass) : null;
    }

    private static void addEditorToCache(Class propertyType, PropertyEditor editor, String key) {
        if (editor == null) {
            addEditorClassToCache(propertyType, null, key);
        } else {
            // Caching the class for editor instance is a bit tricky - the instance
            // is created by PropertyEditorManager, but we may not be able to re-create
            // it just from a class. We assume it is possible if the class has a no-arg
            // public constructor. Otherwise we don't cache the property editor class.
            Class editorClass = editor.getClass();
            try {
                Constructor ctor = editorClass.getConstructor();
                if (ctor != null && (ctor.getModifiers() & Modifier.PUBLIC) == Modifier.PUBLIC) {
                    addEditorClassToCache(propertyType, editorClass, key);
                }
            } catch (NoSuchMethodException ex) {} // ignore
        }
    }

    private static void addEditorClassToCache(Class propertyType, Class editorClass, String key) {
        getEditorClassCache(key).put(propertyType, editorClass);
    }

    private static Map<Class,Class> getEditorClassCache(String key) {
        if (editorClassCache == null) {
            editorClassCache = new HashMap<>();
        }
        Map<Class, Class> classCache = editorClassCache.get(key);
        if (classCache == null) {
            classCache = new WeakHashMap<Class,Class>();
            // the weakness of the map is for classes from user projects
            // (property types which we remember we have no property editor for)
            editorClassCache.put(key, classCache);
        }
        return classCache;
    }

//    /**
//     * Returns true if given type has a specific property editor wrapping the
//     * default one - so the default searching mechanism is not used to find
//     * such an editor. Used for subclasses of ResourceWrapperEditor.
//     */
//    private static boolean hasWrappingEditor(Class type) {
//        return type == String.class
//               || type == java.awt.Font.class
//               || type == java.awt.Color.class
//               || type == javax.swing.Icon.class;
//    }

    /**
     * Returns true if given type can be considered as "component type" - i.e.
     * can expect components of this type in the form, so it makes sense to
     * have ComponentChooserEditor as one of the property editors for this type.
     */
    private static boolean isComponentType(Class type) {
        return !type.equals(Object.class) && !type.equals(String.class)
                && !type.isEnum() && !type.isPrimitive()
                && !Number.class.isAssignableFrom(type);
    }

    private static PropertyEditor createEditorInstance(Class cls) {
        try {
            return (PropertyEditor) cls.getDeclaredConstructor().newInstance();
        } catch (Exception ex) {
            log(ex, "Error instantiating property editor: "+cls.getName()); // NOI18N
        } catch (LinkageError ex) {
            log(ex, "Error instantiating property editor: "+cls.getName()); // NOI18N
        }
        return null;
    }

    private static Logger logger;
    private static void log(Throwable ex, String msg) {
        if (logger == null) {
            logger = Logger.getLogger(FormPropertyEditorManager.class.getName());
        }
        logger.log(Level.INFO, msg, ex);
    }
}
