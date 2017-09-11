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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.api.server.properties;

/**
 * The set of properties to persist. Every property set is persisted unless
 * the whole object is not removed by the {@link #remove()} call.
 * <p>
 * In the scope of namespace used in {@link InstancePropertiesManager}
 * the object has assigned unique id identifying it.
 *
 * @author Petr Hejl
 */
public abstract class InstanceProperties {

    private final String id;

    /**
     * Creates the new InstanceProperties.
     *
     * @param id id of the properties, unique in the scope of the namespace
     * @see InstancePropertiesManager
     */
    public InstanceProperties(String id) {
        this.id = id;
    }

    /**
     * Returns unique id of these properties. It is guaranteed that this id is
     * unique in the scope of single namespace used in manager (however it
     * is not related directly to it).
     * <p>
     * Client may use it for its own purposes (don't have to), but client
     * can't influence the actual value of id in any way.
     *
     * @return id of the properties unique in the scope of the property set
     * @see InstancePropertiesManager
     * @see InstancePropertiesManager#createProperties(String)
     */
    public final String getId() {
        return id;
    }

    /**
     * Returns the value of the given property. If the value was not assigned
     * to the property default value is returned. This method is designed to be
     * used in conjuction with {@link #putString(String, String)}.
     *
     * @param key name of the property
     * @param def default value
     * @return the value of the property or defined default value
     */
    public abstract String getString(String key, String def);

    /**
     * Associates the specified value with the specified property. This
     * method is expected to be used in conjuction with
     * {@link #getString(String, String)}.
     *
     * @param key name of the property
     * @param value value to set
     */
    public abstract void putString(String key, String value);

    /**
     * Returns the value of the given property. If the value was not assigned
     * to the property or it is not valid integer the default value is returned.
     * Valid stored values are "true" and "false" (case insensitive). This
     * method is designed to be used in conjuction with
     * {@link #putBoolean(String, boolean)}.
     *
     * @param key name of the property
     * @param def default value
     * @return the value of the property or defined default value
     */
    public abstract boolean getBoolean(String key, boolean def);

    /**
     * Associates the specified value with the specified property. This
     * method is expected to be used in conjuction with
     * {@link #getBoolean(String, boolean)}.
     *
     * @param key name of the property
     * @param value value to set
     */
    public abstract void putBoolean(String key, boolean value);

    /**
     * Returns the value of the given property. If the value was not assigned
     * to the property or it is not valid integer the default value is returned.
     * Valid string values associated with the property are values parseable
     * with {@link java.lang.Integer#parseInt(String)}. However this method
     * is designed to be used in conjuction with {@link #putInt(String, int)}.
     *
     * @param key name of the property
     * @param def default value
     * @return the value of the property or defined default value
     */
    public abstract int getInt(String key, int def);

    /**
     * Associates the specified value with the specified property. This
     * method is expected to be used in conjuction with
     * {@link #getInt(String, int)}.
     *
     * @param key name of the property
     * @param value value to set
     */
    public abstract void putInt(String key, int value);

    /**
     * Returns the value of the given property. If the value was not assigned
     * to the property or it is not valid long the default value is returned.
     * Valid string values associated with the property are values parseable
     * with {@link java.lang.Long#parseLong(String)}. However this method
     * is designed to be used in conjuction with {@link #putLong(String, long)}.
     *
     * @param key name of the property
     * @param def default value
     * @return the value of the property or defined default value
     */
    public abstract long getLong(String key, long def);

    /**
     * Associates the specified value with the specified property. This
     * method is expected to be used in conjuction with
     * {@link #getLong(String, long)}.
     *
     * @param key name of the property
     * @param value value to set
     */
    public abstract void putLong(String key, long value);

    /**
     * Returns the value of the given property. If the value was not assigned
     * to the property or it is not valid float the default value is returned.
     * Valid string values associated with the property are values parseable
     * with {@link java.lang.Float#parseFloat(String)}. However this method
     * is designed to be used in conjuction with
     * {@link #putFloat(String, float)}.
     *
     * @param key name of the property
     * @param def default value
     * @return the value of the property or defined default value
     */
    public abstract float getFloat(String key, float def);

    /**
     * Associates the specified value with the specified property. This
     * method is expected to be used in conjuction with
     * {@link #getFloat(String, float)}.
     *
     * @param key name of the property
     * @param value value to set
     */
    public abstract void putFloat(String key, float value);

    /**
     * Returns the value of the given property. If the value was not assigned
     * to the property or it is not valid double the default value is returned.
     * Valid string values associated with the property are values parseable
     * with {@link java.lang.Double#parseDouble(String)}. However this method
     * is designed to be used in conjuction with
     * {@link #putDouble(String, double)}.
     *
     * @param key name of the property
     * @param def default value
     * @return the value of the property or defined default value
     */
    public abstract double getDouble(String key, double def);

    /**
     * Associates the specified value with the specified property. This
     * method is expected to be used in conjuction with
     * {@link #getDouble(String, double)}.
     *
     * @param key name of the property
     * @param value value to set
     */
    public abstract void putDouble(String key, double value);

    /**
     * Removes the value of the given property, if any.
     *
     * @param key name of the property
     */
    public abstract void removeKey(String key);

    /**
     * Removes this instance from the persistent space. All values of
     * previously set are lost. The result of call to
     * {@link InstancePropertiesManager#getProperties(String)} with appropriate
     * parameter will not contain this set of properties anymore.
     * <p>
     * Return value of any method after removal is not defined and most
     * likely will lead to {@link java.lang.IllegalStateException}.
     */
    public abstract void remove();

}
