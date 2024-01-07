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
package org.netbeans.libs.freemarker;

import freemarker.cache.TemplateLoader;
import freemarker.core.Environment;
import freemarker.template.Configuration;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.script.Bindings;
import javax.script.ScriptContext;
import org.netbeans.api.queries.FileEncodingQuery;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Utilities;

/**
 *  Velocity templates resource loader rewritten for Freemarker to
 *  access resources via FileSystem.
 * 
 * @author Petr Zajac, adopted by Jaroslav Tulach
 */

final class RsrcLoader extends Configuration 
implements TemplateLoader, TemplateExceptionHandler {
    private static final Logger LOG = Logger.getLogger(FreemarkerEngine.class.getName());
    private final FileObject fo;
    private final ScriptContext map;
    private final Bindings engineScope;

    RsrcLoader(FileObject fo, ScriptContext map) {
        this.fo = fo;
        this.map = map;
        this.engineScope = map.getBindings(ScriptContext.ENGINE_SCOPE);
        setTemplateLoader(this);
        setTemplateExceptionHandler(this);
        Logger.getLogger("freemarker.runtime").setLevel(Level.OFF);
    }

    @Override
    public void handleTemplateException(TemplateException ex, Environment env, Writer w) throws TemplateException {
        try {
            w.append(ex.getLocalizedMessage());
            LOG.log(Level.INFO, "Failure processing " + fo, ex);
            LOG.log(Level.INFO, "Bindings:"); // NOI18N
            for (Map.Entry<String, Object> entry : engineScope.entrySet()) {
                LOG.log(Level.INFO, "  key: " + entry.getKey() + " value: " + entry.getValue()); // NOI18N
            }
        } catch (IOException e) {
            Exceptions.printStackTrace(e);
        }
    }

    private FileObject getFile(String name) {
       FileObject tmp = (getFolder() == null) ? null : getFolder().getFileObject(name);
       if (tmp == null) {
           try {
               tmp = FileUtil.toFileObject(FileUtil.normalizeFile(Utilities.toFile(new URI(name))));
           } catch (URISyntaxException ex) {
           } catch (IllegalArgumentException iae) {
           }
       }
       return tmp;
    } 

    private FileObject getFolder() {
        try {
            return fo.getFileSystem().getRoot();
        }
        catch (FileStateInvalidException ex) {
            // ok
        }
        return FileUtil.getConfigRoot();
    }

    @Override
    public Object findTemplateSource(String string) throws IOException {
        FileObject tmp = getFile(string);
        return tmp == null ? null : new Wrap(tmp);
    }

    @Override
    public long getLastModified(Object object) {
        return ((Wrap)object).fo.lastModified().getTime();
    }

    @Override
    public Reader getReader(Object object, String encoding) throws IOException {
        Wrap w = (Wrap)object;
        if (w.reader == null) {
           Charset chset = FileEncodingQuery.getEncoding(w.fo);
           w.reader = new InputStreamReader(w.fo.getInputStream(), chset);
        }
        return w.reader;
    }

    @Override
    public void closeTemplateSource(Object object) throws IOException {
        Wrap w = (Wrap)object;
        if (w.reader != null) {
            w.reader.close();
        }
    }
        
    public Object put(String string, Object object) {
        assert false;
        return null;
    }

    @Override
    public TemplateModel getSharedVariable(String string) {
        Object value = map.getAttribute(string);
        if (value == null) {
            value = engineScope.get(string);
        }
        if (value == null && fo != null) {
            value = fo.getAttribute(string);
        }
        try {
            return getObjectWrapper().wrap(value);
        } catch (TemplateModelException ex) {
            Exceptions.printStackTrace(ex);
            return null;
        }
    }

    @Override
    public Set getSharedVariableNames() {
        LinkedHashSet<String> keys = new LinkedHashSet<>();

        if (map != null) {
            keys.addAll(map.getBindings(map.ENGINE_SCOPE).keySet());
        }

        if (fo != null) {
            Enumeration<String> en = fo.getAttributes();
            while (en.hasMoreElements()) {
                keys.add(en.nextElement());
            }
        }

        return keys;
    }

    private static final class Wrap {
        public FileObject fo;
        public Reader reader;
        
        public Wrap(FileObject fo) {
            this.fo = fo;
        }
    } // end Wrap

}
