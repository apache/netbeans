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
package org.netbeans.api.templates;

import freemarker.ext.beans.BeansWrapper;
import freemarker.template.Configuration;
import freemarker.template.Template;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.Writer;
import java.util.HashMap;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import junit.framework.Test;
import org.netbeans.api.scripting.Scripting;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.LocalFileSystem;

/**
 *
 * @author Jaroslav Tulach
 */
public class SpeedTest extends NbTestCase {
    private HashMap<String, String> parameters;
    private FileObject fo;
    private File[] whereTo;
    private ScriptEngine eng;
    
    public SpeedTest(String name) {
        super(name);
    }
    
    public static Test suite() {
        return NbTestSuite.speedSuite(SpeedTest.class, 2, 3);
    }
    
    @Override
    protected void setUp() throws Exception {
        clearWorkDir();
        
        FileWriter w = new FileWriter(new File(getWorkDir(), "template.txt"));
        w.write("<html><h1>${title}</h1></html>");
        w.close();
        
        
        parameters = new HashMap<String,String>();
        parameters.put("title", "SOME_TITLE");

        LocalFileSystem lfs = new LocalFileSystem();
        lfs.setRootDirectory(getWorkDir());
        fo = lfs.findResource("template.txt");
        

        ScriptEngineManager mgr = Scripting.createManager();
        eng = mgr.getEngineByName("freemarker");
        assertNotNull("We do have such engine", eng);
        eng.getContext().setAttribute(FileObject.class.getName(), fo, ScriptContext.ENGINE_SCOPE);
        eng.getContext().getBindings(ScriptContext.ENGINE_SCOPE).putAll(parameters);
            
        whereTo = new File[10000];
        for (int i = 0; i < whereTo.length; i++) {
            whereTo[i] = new File(getWorkDir(), "outFile"+i+".txt");
        }
    }
    
    
    public void testSpeedOfFreemarker() throws Exception {
        Configuration cfg = new Configuration();
        cfg.setDirectoryForTemplateLoading(getWorkDir());
        cfg.setObjectWrapper(new BeansWrapper());
        Template templ = cfg.getTemplate("template.txt");
        
        for(int i = 0; i < whereTo.length; i++) {
            Writer out = new BufferedWriter(new FileWriter(whereTo[i]));
            templ.process(parameters, out);
            out.close();
        }
    }

    public void testSpeedThruIntegration() throws Exception {
        for (int i = 0; i < whereTo.length; i++) {
            Writer w = new BufferedWriter(new FileWriter(whereTo[i]));
            eng.getContext().setWriter(w);
            InputStreamReader is = new InputStreamReader(fo.getInputStream());
            eng.eval(is);
            is.close();
            w.close();
        }
    }
}
