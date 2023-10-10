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

package org.openide.execution;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import org.openide.ServiceType;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;

/**
 * Base class for scripting interpreters.
 * @author dstrupl
 * @deprecated Was used only in obsolete modules.
 */
@Deprecated
public abstract class ScriptType extends org.openide.ServiceType {

    /** generated Serialized Version UID */
    private static final long serialVersionUID = 4893207884933024341L;

    /**
     * The script type can decide whether it will be able to execute
     * the given file object.
     * @param fo a file to test
     * @return true if the script can operate on this file
     */
    public abstract boolean acceptFileObject(FileObject fo);
    
    /**
     * Evaluate the script given in the form of a Reader.
     * @param r 
     * @param context
     * @return whatever is the result of the script. It can be null.
     */
    public abstract Object eval(java.io.Reader r, Context context) throws InvocationTargetException;
    
    /** Calls eval(Reader, Context) with getDefaultContext() as
     * the second argument.
     */
    public final Object eval(java.io.Reader r) throws InvocationTargetException {
        return eval(r, getDefaultContext());
    }
    
    /**
     * Evaluate the script given in the form of a string.
     * @param script
     * @param context
     * @return whatever is the result of the script. It can be null.
     */
    public abstract Object eval(String script, Context context) throws InvocationTargetException;
    
    /** Calls eval(String, Context) with getDefaultContext() as
     * the second argument.
     */
    public final Object eval(String script) throws InvocationTargetException {
        return eval(script, getDefaultContext());
    }
    
    /**
     * Execute the script given in the form of a Reader.
     * @param r the contents of the script
     * @param context the context in which to evaluate it
     */
    public abstract void exec(java.io.Reader r, Context context) throws InvocationTargetException;
    
    /** Calls exec(Reader, Context) with getDefaultContext() as
     * the second argument.
     */
    public final void exec(java.io.Reader r) throws InvocationTargetException {
        exec(r, getDefaultContext());
    }
    
    /**
     * Execute the script given in the form of a string.
     * @param script
     * @param context
     */
    public abstract void exec(String script, Context context) throws InvocationTargetException;
    
    /** Calls exec(String, Context) with getDefaultContext() as
     * the second argument.
     */
    public final void exec(String script) throws InvocationTargetException {
        exec(script, getDefaultContext());
    }

    /**
     * Adds variable with name to the variables known by the script type.
     * @param name the name for the newly created variable
     * @param value initial value variable value (can be null).
     */
    public abstract void addVariable(String name, Object value);
    
    /** Get all registered script types.
    * @return enumeration of <code>ScriptType</code>s
    * @deprecated Please use {@link org.openide.util.Lookup} instead.
    */
    public static java.util.Enumeration scriptTypes () {
        return Collections.enumeration(Lookup.getDefault().lookup(new Lookup.Template(ScriptType.class)).allInstances());
    }

    /** 
     * Find the script type implemented as a given class.
     * @param clazz the class of the script type looked for
     * @return the desired script type or <code>null</code> if it does not exist
    * @deprecated Please use {@link org.openide.util.Lookup} instead.
     */
    public static ScriptType find (Class clazz) {
        return (ScriptType)Lookup.getDefault().lookup(clazz);
    }

    /** 
     * Find the script type with requested name.
     * @param name (display) name of script type to find
     * @return the desired script type or <code>null</code> if it does not exist
     */
    public static ScriptType find (String name) {
        ServiceType t = ((ServiceType.Registry)Lookup.getDefault().lookup(ServiceType.Registry.class)).find (name);
        if (t instanceof ScriptType) {
            return (ScriptType)t;
        } else {
            return null;
        }
    }

    /** Get the default script type.
    * @return the default script type
    * @deprecated Probably meaningless, find all available types and filter with {@link #acceptFileObject} instead.
    */
    public static ScriptType getDefault () {
        java.util.Enumeration en = scriptTypes ();
        if (en.hasMoreElements()) {
            return (ScriptType)en.nextElement ();
        } else {
            throw new RuntimeException("No script type registered."); // NOI18N
        }
    }
    
    static Context getDefaultContext() {
        return new Context();
    }

    /** Scripting context.
     * Using instances of this class you can add additional parameters
     * to the script execution using methods eval or exec.
     */
    public static class Context {
    }    
}
