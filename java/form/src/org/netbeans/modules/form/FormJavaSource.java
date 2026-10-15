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

import java.beans.Introspector;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.java.source.queries.api.Function;
import org.netbeans.modules.java.source.queries.api.Queries;
import org.netbeans.modules.java.source.queries.api.QueryException;
import org.netbeans.modules.java.source.queries.api.Updates;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.loaders.DataObject;

/**
 * Provides information about the forms java source file.
 *
 * @author Tomas Stupka, Tomas Pavek
 */
public final class FormJavaSource {
    private boolean validJavaSource;
    private DataObject formDataObject;
    private FileObject javaFile;
    private String className;
    private String filePath;
    private final Object javaContext;
    private Collection<? extends String> fields;

    private static final String[] PROPERTY_PREFIXES = new String[] {"get", // NOI18N
								    "is"}; // NOI18N
    private static final String JAVA_QUERIES_CONTEXT_KEY = "JavaQueriesContext_key"; // NOI18N

    FormJavaSource(DataObject dob, Object javaContext) {
        this.formDataObject = dob;
        this.javaFile = dob.getPrimaryFile();
        this.javaContext = javaContext;
        this.validJavaSource = ClassPath.getClassPath(javaFile, ClassPath.SOURCE) != null;
    }

    public <T> T query(Function<Queries,T> queryFnc) {
        try {
            setTransientContext(true);
            return Queries.query(getJavaFile().toURL(), queryFnc);
        } catch(QueryException ex) {
            Logger.getLogger(getClass().getName()).log(Level.WARNING, null, ex);
        } finally {
            setTransientContext(false);
        }
        return null;
    }

    public Boolean update(Function<Updates,Boolean> updateFnc) {
        try {
            setTransientContext(true);
            return Updates.update(getJavaFile().toURL(), updateFnc);
        } catch(QueryException ex) {
            Logger.getLogger(getClass().getName()).log(Level.WARNING, null, ex);
        } finally {
            setTransientContext(false);
        }
        return null;
    }

    /**
     * Allows to associate the FileObject with additional context information
     * that can be used by the Java Queries implementation. For JDev.
     */
    private void setTransientContext(boolean set) {
        if (javaContext != null) {
            FileObject file = getJavaFile();
            if (set) {
                ThreadLocal tl;
                synchronized (file) {
                    Object o = file.getAttribute(JAVA_QUERIES_CONTEXT_KEY);
                    tl = o instanceof ThreadLocal ? (ThreadLocal) o : null;
                    if (tl == null) {
                        tl = new ThreadLocal();
                        try {
                            file.setAttribute(JAVA_QUERIES_CONTEXT_KEY, tl);
                        } catch (IOException ex) {
                            Logger.getLogger(getClass().getName()).log(Level.WARNING, null, ex);
                        }
                    }
                }
                tl.set(javaContext);
            } else {
                Object o = file.getAttribute(JAVA_QUERIES_CONTEXT_KEY);
                if (o instanceof ThreadLocal) {
                    ((ThreadLocal)o).remove();
                }
            }
        }
    }

    private String getFormClassName() {
        if (!validJavaSource) {
            return null;
        }
        FileObject file = getJavaFile();
        if (className != null && file.getPath().equals(filePath)) {
            return className;
        }
        Collection<? extends String> names = query(new Function<Queries, Collection<? extends String>>() {
            @Override
            public Collection< ? extends String> apply(Queries queries) throws QueryException {
                return queries.getTopLevelClasses();
            }
        });
        className = null;
        if (names != null) {
            String fileName = file.getName();
            String dotFileName = "." + fileName; // NOI18N
            for (String s : names) {
                if (s.equals(fileName) || s.endsWith(dotFileName)) {
                    className = s;
                    break;
                }
            }
        }
        filePath = file.getPath();
        return className;
    }

    public boolean containsField(String name, boolean refresh) {
	if (refresh) {
	    refresh();
	}	    
	return fields != null && fields.contains(name);
    }	

    private void refresh() {
        final String cls = getFormClassName();
        if (cls != null) {
            fields = query(new Function<Queries, Collection<? extends String>> () {
                @Override
                public Collection< ? extends String> apply(Queries queries) throws QueryException {
                    return queries.getFieldNames(cls, true, null);
                }
            });
        }
    }

    public String getSuperClassName() {
        final String cls = getFormClassName();
        return cls != null ? query(new Function<Queries, String>() {
            @Override
            public String apply(Queries queries) throws QueryException {
                return queries.getSuperClass(cls);
            }
        }) : null;
    }

    public String getClassBinaryName() {
        final String cls = getFormClassName();
        return cls != null ? query(new Function<Queries, String>() {
            @Override
            public String apply(Queries queries) throws QueryException {
                return queries.getClassBinaryName(cls);
            }
        }) : null;
    }

    public boolean modifyInterfaces(final Collection<String> toAdd, final Collection<String> toRemove) {
        final String cls = getFormClassName();
        Boolean ret;
        if (cls != null) {
            ret = update(new Function<Updates, Boolean>() {
                @Override
                public Boolean apply(final Updates updates) throws QueryException {
                    updates.modifyInterfaces(cls, toAdd, toRemove);
                    return true;
                }
            });
        } else {
            ret = null;
        }
        return Boolean.TRUE.equals(ret);
    }

    public void renameField(final String oldName, final String newName) {
        if (containsField(oldName, true)) {
            final String cls = getFormClassName();
            update(new Function<Updates, Boolean>() {
                @Override
                public Boolean apply(Updates updates) throws QueryException {
                    updates.renameField(cls, oldName, newName);
                    return true;
                }
            });
        }
    }

    public int[] getEventHandlerMethodSpan(final String evenHandlerName, final String eventType) {
        final String cls = getFormClassName();
        return cls != null ? query(new Function<Queries, int[]>() {
            @Override
            public int[] apply(Queries queries) throws QueryException {
                String[] paramTypes = eventType != null ? new String[] { eventType } : new String[0];
                return queries.getMethodSpan(cls, evenHandlerName, true, "void", paramTypes); // NOI18N
            }
        }) : null;
    }

    /** Assuming the method is either initComponents or an event handler method,
      * i.e. void return type. */
    public int[] getMethodSpan(final String methodName, final String... paramTypes) {
        final String cls = getFormClassName();
        return cls != null ? query(new Function<Queries, int[]>() {
            @Override
            public int[] apply(Queries queries) throws QueryException {
                return queries.getMethodSpan(cls, methodName, true, "void", paramTypes); // NOI18N
            }
        }) : null;
    }

    /**
     * Returns names for all methods with the specified return type
     * 
     * @param returnType return type
     * @return names of all methods with the given return type.
     */
    public String[] getMethodNames(final Class returnType) {
        final String cls = getFormClassName();
        Collection<? extends String> result = cls != null ? query(
                new Function<Queries, Collection<? extends String>>() {
            @Override
            public Collection< ? extends String> apply(Queries queries) throws QueryException {
                return queries.getMethodNames(cls, true, returnType.getCanonicalName(), new String[0]);
            }
        }) : null;
        return result != null ? result.toArray(new String[0]) : new String[0];
    }

    /**
     * Returns names for all methods with the specified return type which 
     * start with the prefixes "is" and "get"
     * 
     * @param returnType return type.
     * @return names of methods.
     */
    public String[] getPropertyReadMethodNames(Class returnType) {
        String[] names = getMethodNames(returnType);
        List<String> result = new ArrayList<String>(names.length);
        for (String name: names) {
            if(!extractPropertyName(name).equals("")) { // NOI18N	
                // seems to be property method
                result.add(name);
            }		    
        }
        return result.toArray(new String[0]);
    }
    
    public static String extractPropertyName(String methodName) {
	for (int i = 0; i < PROPERTY_PREFIXES.length; i++) {
	    if(methodName.startsWith(PROPERTY_PREFIXES[i]) && 
	       methodName.length() > PROPERTY_PREFIXES[i].length()) 
	    {		    
		return Introspector.decapitalize(methodName.substring(PROPERTY_PREFIXES[i].length()));		     			
	    }	
	}
	return "";  // NOI18N	
    }    

    public void importFQNs(final int[][] ranges) {
        if (validJavaSource) {
            update(new Function<Updates, Boolean>() {
                @Override
                public Boolean apply(Updates updates) throws QueryException {
                    updates.fixImports(ranges);
                    return true;
                }
            });
        }
    }

    public static boolean isInDefaultPackage(FormModel formModel) {
        FileObject fdo = FormEditor.getFormDataObject(formModel).getPrimaryFile();
        ClassPath cp = ClassPath.getClassPath(fdo, ClassPath.SOURCE);
        String name = cp != null ? cp.getResourceName(fdo) : null;
        return name == null || name.indexOf('/') < 0;
    }

    private synchronized FileObject getJavaFile() {
        if (!javaFile.isValid()) {
            javaFile = formDataObject.getPrimaryFile();
        }
        return javaFile;
    }

}
