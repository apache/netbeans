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

package org.netbeans.modules.templates;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.api.scripting.Scripting;
import org.netbeans.api.templates.CreateDescriptor;
import org.netbeans.api.templates.CreateFromTemplateHandler;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.lookup.ServiceProvider;


/** Processes templates that have associated attribute
* with name of the scripting engine.
*
* @author  Jaroslav Tulach
*/
@ServiceProvider(service=CreateFromTemplateHandler.class, position = 1000)
public class ScriptingCreateFromTemplateHandler extends CreateFromTemplateHandler {
    private static final Logger LOG = Logger.getLogger(ScriptingCreateFromTemplateHandler.class.getName());
    
    public static final String SCRIPT_ENGINE_ATTR = "javax.script.ScriptEngine";
    
    private static ScriptEngineManager manager;
    
    private static final String ENCODING_PROPERTY_NAME = "encoding"; //NOI18N
    
    @Override
    public boolean accept(CreateDescriptor desc) {
        return engine(desc.getTemplate()) != null;
    }

    @Override
    public List<FileObject> createFromTemplate(CreateDescriptor desc) throws IOException {
        FileObject template = desc.getTemplate();
        String name = desc.getProposedName();
        Map<String, Object> values = new HashMap<>(desc.getParameters());
        FileObject f = desc.getTarget();
        
        boolean noExt = desc.hasFreeExtension() && name.indexOf('.') != -1;
        
        String extWithDot;
        if (noExt) {
            extWithDot = null;
        } else {
            extWithDot = '.' + template.getExt();
            if (name.endsWith(extWithDot)) { // Test whether the extension happens to be there already
                // And remove it if yes, it will be appended to the unique name.
                name = name.substring(0, name.length() - extWithDot.length());
            }
        }
        
        String nameUniq = FileUtil.findFreeFileName(f, name, noExt ? null : template.getExt());
        
        ScriptEngine eng = engine(template);
        Bindings bind = eng.getContext().getBindings(ScriptContext.ENGINE_SCOPE);
        if (!values.containsKey("name")) { // NOI18N
            values.put("name", nameUniq); // NOI18N
        }
        bind.putAll(values);
        
        if (desc.getTemplate().isFolder()) {
            FileObject folder = FileUtil.createFolder(desc.getTarget(), nameUniq);
            CreateFromTemplateHandler.copyAttributesFromTemplate(null, desc.getTemplate(), folder);
            return CreateFromTemplateHandler.defaultCopyContents(desc, desc.getTemplate(), folder);
        }

        FileObject output = FileUtil.createData(f, noExt ? nameUniq : nameUniq + extWithDot);
        Charset targetEnc = FileEncodingQuery.getEncoding(output);
        Charset sourceEnc = FileEncodingQuery.getEncoding(template);

        if(!values.containsKey(ENCODING_PROPERTY_NAME)) {
            bind.put(ENCODING_PROPERTY_NAME, targetEnc.name());
        }
        
        FileLock lock = output.lock();
        try (Writer w = new OutputStreamWriter(output.getOutputStream(lock), targetEnc);
             Reader is = new InputStreamReader(template.getInputStream(), sourceEnc);
            /*IndentWriter w2 = new IndentWriter(doc, 0, w, false) */) {
            StringWriter sw = new StringWriter();
            ScriptEngine eng2 = desc.isPreformatted() ? null : indentEngine();
            
            eng.getContext().setWriter(new PrintWriter(eng2 != null ? sw : w));
            //eng.getContext().setBindings(bind, ScriptContext.ENGINE_SCOPE);
            eng.getContext().setAttribute(FileObject.class.getName(), template, ScriptContext.ENGINE_SCOPE);
            eng.getContext().setAttribute(ScriptEngine.FILENAME, template.getNameExt(), ScriptContext.ENGINE_SCOPE);
            eng.eval(is);
            
            if (eng2 != null) {
                eng2.getContext().setAttribute("mimeType", template.getMIMEType(), ScriptContext.ENGINE_SCOPE);
                eng2.getContext().setWriter(w);
                eng2.eval(new StringReader(sw.toString()));
            }
        }catch (ScriptException ex) {
            IOException io = new IOException(ex.getMessage(), ex);
            throw io;
        } finally {
            lock.releaseLock();
        }
        return Collections.singletonList(output);
    }
    
    public static ScriptEngine indentEngine() {
        return getEngine(ID_INDENT_ENGINE);
    }
    
    private static final String ID_INDENT_ENGINE = "org.netbeans.api.templates.IndentEngine"; // NOI18N
    
    public static ScriptEngine getEngine(String engName) {
        synchronized (ScriptingCreateFromTemplateHandler.class) {
            if (manager == null) {
                manager = Scripting.createManager();
            }
        }
        return manager.getEngineByName(engName);
    }
    
    public static ScriptEngine engine(FileObject fo) {
        Object obj = fo.getAttribute(SCRIPT_ENGINE_ATTR); // NOI18N
        if (obj instanceof ScriptEngine) {
            return (ScriptEngine)obj;
        }
        if (obj instanceof String) {
            return getEngine((String)obj);
        }
        return null;
    }

    /*
    public static Document createDocument(String mimeType) {
        Document doc;
        try {
            doc = LineDocumentUtils.createDocument(mimeType);
        } catch (IllegalArgumentException ex) {
            // mainly for tests
            doc = new PlainDocument();
            doc.putProperty("mimeType", mimeType);
        }
        return doc;
    }
    */
}
