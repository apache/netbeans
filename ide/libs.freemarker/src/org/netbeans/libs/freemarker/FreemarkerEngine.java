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


import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Properties;
import java.util.Set;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import javax.script.AbstractScriptEngine;
import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptException;
import javax.script.SimpleBindings;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

/* Taken from A. Sundararajan and adopted by Jaroslav Tulach 
 * for NetBeans needs.
 * 
 * @author A. Sundararajan
 */
class FreemarkerEngine extends AbstractScriptEngine {

    public static final String STRING_OUTPUT_MODE = "com.sun.script.freemarker.stringOut";
    public static final String FREEMARKER_CONFIG = "com.sun.script.freemarker.config";
    public static final String FREEMARKER_PROPERTIES = "com.sun.script.freemarker.properties";
    public static final String FREEMARKER_TEMPLATE_DIR = "com.sun.script.freemarker.template.dir";
    public static final String FREEMARKER_TEMPLATE = "org.openide.filesystems.FileObject";
    private static final String FREEMARKER_EXCEPTION_HANDLER = "org.netbeans.libs.freemarker.exceptionHandler";
    
    private static Map<FileObject,Template> templates = Collections.synchronizedMap(
        new WeakHashMap<>()
    );

    // my factory, may be null
    private volatile ScriptEngineFactory factory;
    private volatile Configuration conf;
    private volatile FileObject fo;

    public FreemarkerEngine(ScriptEngineFactory factory) {
        this.factory = factory;
        try {
            freemarker.log.Logger.selectLoggerLibrary(freemarker.log.Logger.LIBRARY_JAVA);
        } catch (ClassNotFoundException ex) {
            try {
                freemarker.log.Logger.selectLoggerLibrary(freemarker.log.Logger.LIBRARY_NONE);
            } catch (ClassNotFoundException ex1) {
                Exceptions.printStackTrace(ex1);
            }
        }
    }   

    public FreemarkerEngine() {
        this(null);
    }
	
    // ScriptEngine methods
    @Override
    public Object eval(String str, ScriptContext ctx) 
                       throws ScriptException {	
        return eval(new StringReader(str), ctx);
    }

    @Override
    public Object eval(Reader reader, ScriptContext ctx)
                       throws ScriptException { 
        ctx.setAttribute("context", ctx, ScriptContext.ENGINE_SCOPE);
        initFreeMarkerConfiguration(ctx);
        String fileName = getFilename(ctx);
        boolean outputAsString = isStringOutputMode(ctx);
        Writer out;
        if (outputAsString) {
            out = new StringWriter();
        } else {
            out = ctx.getWriter();
        }
        
        Template template = null;
        try {
            if (fo != null) {
                template = templates.remove(fo);
            }
            
            if (template == null) {
                template = new MyTemplate(fo, fileName, reader, conf);
                Object exceptionHandler = ctx.getAttribute(FREEMARKER_EXCEPTION_HANDLER);
                if (exceptionHandler instanceof TemplateExceptionHandler) {
                    template.setTemplateExceptionHandler((TemplateExceptionHandler) exceptionHandler);
                }
            } else {
                ((MyTemplate)template).conf = conf;
            }
            template.process(null, out);
            out.flush();
            if (fo != null) {
                templates.put(fo, template);
            }
        } catch (Exception exp) {
            throw new ScriptException(exp);
        }
        return outputAsString? out.toString() : null;
    }

    @Override
    public ScriptEngineFactory getFactory() {
        synchronized (this) {
            if (factory == null) {
                factory = new FreemarkerFactory();
            }
        }
        return factory;
    }

    @Override
    public Bindings createBindings() {
        return new SimpleBindings();
    }

    // internals only below this point  
    private static String getFilename(ScriptContext ctx) {
        Object tfo = ctx.getAttribute(FREEMARKER_TEMPLATE);
        if (tfo instanceof FileObject) {
            return ((FileObject)tfo).getPath();
        }
        Object fileName = ctx.getAttribute(ScriptEngine.FILENAME);
        if (fileName != null) {
            return fileName.toString();
        }
        return "unknown";
    }

    private static boolean isStringOutputMode(ScriptContext ctx) {
        Object flag = ctx.getAttribute(STRING_OUTPUT_MODE);
        if (flag != null) {
            return flag.equals(Boolean.TRUE);
        } else {
            return false;
        }
    }

    private void initFreeMarkerConfiguration(ScriptContext ctx) {
        if (conf == null) {
            synchronized (this) {
                if (conf != null) {
                    return;
                }
                Object cfg = ctx.getAttribute(FREEMARKER_CONFIG);
                if (cfg instanceof Configuration) {
                    conf = (Configuration) cfg;
                    return;
                }

                Object tfo = ctx.getAttribute(FREEMARKER_TEMPLATE);
                fo = tfo instanceof FileObject ? (FileObject)tfo : null;
                
                Configuration tmpConf = new RsrcLoader(fo, ctx);
                try {
                    initConfProps(tmpConf, ctx);
                    initTemplateDir(tmpConf, fo, ctx);
                } catch (RuntimeException rexp) {
                    throw rexp;
                } catch (Exception exp) {
                    throw new RuntimeException(exp);
                }
                conf = tmpConf;
            }
        }
    }    

    private static void initConfProps(Configuration conf, ScriptContext ctx) {         
        try {
            Properties props = null;
            Object tmp = ctx.getAttribute(FREEMARKER_PROPERTIES);
            if (props instanceof Properties) {
                props = (Properties) tmp;
            } else {
                String propsName = System.getProperty(FREEMARKER_PROPERTIES);
                if (propsName != null) {                    
                    File propsFile = new File(propsName);
                    if (propsFile.exists() && propsFile.canRead()) {
                        props = new Properties();
                        try (FileInputStream fis = new FileInputStream(propsFile)) {
                            props.load(fis);
                        }
                    }               
                }
            }
            if (props != null) {
                Set<Object> keys = props.keySet();
                for (Object obj : keys) {
                    String key;
                    if (obj instanceof String) {
                        key = (String) obj;
                    } else {
                        continue;
                    }
                    try {
                        conf.setSetting(key, props.get(key).toString());
                    } catch (TemplateException te) {
                        // ignore
                    }
                }
            }
        } catch (RuntimeException re) {
            throw re;
        } catch (Exception exp) {
            throw new RuntimeException(exp);
        }
    }

    private static void initTemplateDir(Configuration conf, FileObject fo, ScriptContext ctx) {
        try {
            Object tmp = ctx.getAttribute(FREEMARKER_TEMPLATE_DIR);
            String dirName;
            if (tmp != null) {
                dirName = tmp.toString();
            } else {
                if (fo != null) {
                    return;
                }
                tmp = System.getProperty(FREEMARKER_TEMPLATE_DIR);
                dirName = (tmp == null)? "." : tmp.toString();
            }
            File dir = new File(dirName);
            if (dir.exists() && dir.isDirectory()) {
                conf.setDirectoryForTemplateLoading(dir);
            }
        } catch (IOException exp) {
            throw new RuntimeException(exp);
        }
    }
    
    private static final class MyTemplate extends Template 
    implements FileChangeListener {
        public Configuration conf;
        
        public MyTemplate(FileObject fo, String s, Reader r, Configuration c) throws IOException {
            super(s, r, c);
            if (fo != null) {
                fo.addFileChangeListener(FileUtil.weakFileChangeListener(this, fo));
            }
        }

        @Override
        public Configuration getConfiguration() {
            return conf == null ? super.getConfiguration() : conf;
        }
        
        @Override
        public void fileFolderCreated(FileEvent fe) {
            clear();
        }
        @Override
        public void fileDataCreated(FileEvent fe) {
            clear();
        }
        @Override
        public void fileChanged(FileEvent fe) {
            clear();
        }
        @Override
        public void fileDeleted(FileEvent fe) {
            clear();
        }
        @Override
        public void fileRenamed(FileRenameEvent fe) {
            clear();
        }
        @Override
        public void fileAttributeChanged(FileAttributeEvent fe) {
            clear();
        }
        private void clear() {
            templates.clear();
        }
    } // end of MyTemplate
    
    
    
    
}
