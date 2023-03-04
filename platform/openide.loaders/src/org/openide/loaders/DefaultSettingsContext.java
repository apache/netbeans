/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.openide.loaders;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import javax.naming.*;
import org.openide.filesystems.FileObject;

/** JNDI context providing DataObject's settings. The context does not support
 * subcontexts.
 *
 * @author  Jan Pokorsky
 */
final class DefaultSettingsContext implements Context, NameParser {

    private final DataObject dobj;
    private final Hashtable env;

    /** Creates a new instance of DefaultSettingsContext */
    public DefaultSettingsContext(DataObject dobj) {
        this.dobj = dobj;
        env = new Hashtable();
    }
    
    public Object addToEnvironment(String propName, Object propVal) throws NamingException {
        return null;
    }
    
    public void bind(Name name, Object obj) throws NamingException {
        String attrName = getRelativeName(name);
        FileObject fo = dobj.getPrimaryFile();
        Object attrVal = fo.getAttribute(attrName);
        if (attrVal != null) {
            throw new NameAlreadyBoundException(attrName + " = " + attrVal); // NOI18N
        }
        
        try {
            fo.setAttribute(attrName, obj);
        } catch (IOException ex) {
            NamingException ne = new NamingException(attrName + " = " + obj); // NOI18N
            ne.setRootCause(ex);
        }
    }
    
    public void bind(String name, Object obj) throws NamingException {
        bind(parse(name), obj);
    }
    
    public void close() throws NamingException {
    }
    
    /** Unsupported. */
    public Name composeName(Name name, Name prefix) throws NamingException {
        throw new OperationNotSupportedException();
    }
    
    /** Unsupported. */
    public String composeName(String name, String prefix) throws NamingException {
        throw new OperationNotSupportedException();
    }
    
    /** Unsupported. */
    public Context createSubcontext(Name name) throws NamingException {
        throw new OperationNotSupportedException();
    }
    
    /** Unsupported. */
    public Context createSubcontext(String name) throws NamingException {
        throw new OperationNotSupportedException();
    }
    
    /** Unsupported. */
    public void destroySubcontext(Name name) throws NamingException {
        throw new OperationNotSupportedException();
    }
    
    /** Unsupported. */
    public void destroySubcontext(String name) throws NamingException {
        throw new OperationNotSupportedException();
    }
    
    public Hashtable getEnvironment() throws NamingException {
        return env;
    }
    
    public String getNameInNamespace() throws NamingException {
        // it does not seem a better name is necessary
        return "."; //NOI18N
    }
    
    public NameParser getNameParser(Name name) throws NamingException {
        return this;
    }
    
    public NameParser getNameParser(String name) throws NamingException {
        return this;
    }
    
    public NamingEnumeration/*<NameClassPair>*/ list(String name) throws NamingException {
        return list(parse(name));
    }
    
    public NamingEnumeration/*<NameClassPair>*/ list(Name name) throws NamingException {
        if (name == null) throw new InvalidNameException("name cannot be null"); // NOI18N
        
        int size = name.size();
        if (size == 0) throw new InvalidNameException("name cannot be empty"); // NOI18N
        if (size > 1 || !".".equals(name.get(0))) {
            throw new InvalidNameException("subcontexts unsupported: " + name); // NOI18N
        }
        
        return new BindingEnumeration(dobj.getPrimaryFile());
    }
    
    public NamingEnumeration<Binding> listBindings(Name name) throws NamingException {
        return list(name);
    }
    
    public NamingEnumeration<Binding> listBindings(String name) throws NamingException {
        return list(name);
    }
    
    public Object lookup(String name) throws NamingException {
        return lookup(parse(name));
    }
    
    public Object lookup(Name name) throws NamingException {
        String attrName = getRelativeName(name);
        return dobj.getPrimaryFile().getAttribute(attrName);
    }
    
    public Object lookupLink(String name) throws NamingException {
        return lookupLink(parse(name));
    }
    
    public Object lookupLink(Name name) throws NamingException {
        return lookup(name);
    }
    
    public void rebind(Name name, Object obj) throws NamingException {
        String attrName = getRelativeName(name);
        FileObject fo = dobj.getPrimaryFile();
        
        try {
            fo.setAttribute(attrName, obj);
        } catch (IOException ex) {
            NamingException ne = new NamingException(name + " = " + obj); // NOI18N
            ne.setRootCause(ex);
        }
    }
    
    public void rebind(String name, Object obj) throws NamingException {
        rebind(parse(name), obj);
    }
    
    public Object removeFromEnvironment(String propName) throws NamingException {
        return null;
    }
    
    public void rename(Name oldName, Name newName) throws NamingException {
        String oldAttrName = getRelativeName(oldName);
        String newAttrName = getRelativeName(newName);
        FileObject fo = dobj.getPrimaryFile();
        Object attrVal = fo.getAttribute(newAttrName);
        
        if (attrVal != null) {
            throw new NameAlreadyBoundException(newAttrName + " = " + attrVal); // NOI18N
        }
        
        try {
            attrVal = fo.getAttribute(oldAttrName);
            fo.setAttribute(newAttrName, attrVal);
            fo.setAttribute(oldAttrName, null);
        } catch (IOException ex) {
            NamingException ne = new NamingException(oldName + "->" + newName); // NOI18N
            ne.setRootCause(ex);
        }
    }
    
    public void rename(String oldName, String newName) throws NamingException {
        rename(parse(oldName), parse(newName));
    }
    
    public void unbind(String name) throws NamingException {
        unbind(parse(name));
    }
    
    public void unbind(Name name) throws NamingException {
        String attrName = getRelativeName(name);
        FileObject fo = dobj.getPrimaryFile();
        
        if (fo.getAttribute(attrName) == null) {
            NamingException ne = new NameNotFoundException();
            ne.setResolvedName(name);
            throw ne;
        }
        
        try {
            fo.setAttribute(attrName, null);
        } catch (IOException ex) {
            NamingException ne = new NamingException(); // NOI18N
            ne.setResolvedName(name);
            ne.setRootCause(ex);
        }
    }
    
    // NameParser impl //////////////////////////////////////////////////////
    
    public Name parse(String name) throws NamingException {
        if (name == null) throw new InvalidNameException("name cannot be null"); //NOI18N
        
        return new CompositeName(name);
    }
    
    /** validates name and returns its relative value */
    private String getRelativeName(Name name) throws NamingException {
        if (name == null) throw new InvalidNameException("name cannot be null"); // NOI18N
        if (name.isEmpty()) throw new InvalidNameException("name cannot be empty"); // NOI18N
        
        String rel = null;
        Enumeration en = name.getAll();
        while (en.hasMoreElements()) {
            if (rel == null) {
                String item = (String) en.nextElement();
                if (".".equals(item)) continue; // NOI18N
                if ("..".equals(item)) { // NOI18N
                    throw new InvalidNameException("subcontexts unsupported: " + name); // NOI18N
                }
                
                rel = item;
            } else {
                throw new InvalidNameException("subcontexts unsupported: " + name); // NOI18N
            }
        }
        return rel;
    }
    
    public String toString() {
        String retValue = super.toString();
        return retValue + '[' + dobj + ']';
    }
    
    /** Enumerates file attributes as jndi bindings.
     */
    private static final class BindingEnumeration implements NamingEnumeration<Binding> {
        
        private final Enumeration<String> en;
        private final FileObject fo;
        
        public BindingEnumeration(FileObject fo) {
            this.fo = fo;
            this.en = fo.getAttributes();
        }
        
        public void close() throws NamingException {
        }
        
        public boolean hasMore() throws NamingException {
            return hasMoreElements();
        }
        
        public boolean hasMoreElements() {
            return en.hasMoreElements();
        }
        
        public Binding next() throws NamingException {
            return nextElement();
        }
        
        public Binding nextElement() {
            String name = en.nextElement();
            Object val = fo.getAttribute(name);
            return new Binding(name, val);
        }
        
    }
    
}
