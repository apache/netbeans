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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.freemarker.templates;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.Writer;
import java.util.HashMap;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import junit.framework.Test;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.LocalFileSystem;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;

/**
 *
 * @author Jaroslav Tulach
 */
public class SpeedTest extends NbTestCase {
    private HashMap<String, String> parameters;
    private DataObject obj;
    private File[] whereTo;
    private DataFolder target;
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
        
        target = DataFolder.findFolder(lfs.getRoot());
        FileObject fo = lfs.findResource("template.txt");
        obj = DataObject.find(fo);
        obj.setTemplate(true);
        obj.getPrimaryFile().setAttribute("javax.script.ScriptEngine", "freemarker");
        
        ScriptEngineManager mgr = new ScriptEngineManager();
        eng = mgr.getEngineByName("freemarker");
        assertNotNull("We do have such engine", eng);
        eng.getContext().setAttribute(FileObject.class.getName(), fo, ScriptContext.ENGINE_SCOPE);
        eng.getContext().getBindings(ScriptContext.ENGINE_SCOPE).putAll(parameters);

            
        whereTo = new File[10000];
        for (int i = 0; i < whereTo.length; i++) {
            whereTo[i] = new File(getWorkDir(), "outFile"+i+".txt");
        }
    }
    
    @Override
    protected void tearDown() throws Exception {
        for (int i = 0; i < whereTo.length; i++) {
            BufferedReader r = new BufferedReader(new FileReader(whereTo[i]));
            String s = r.readLine();
            r.close();
            assertEquals("Content for " + whereTo[i], "<html><h1>SOME_TITLE</h1></html>", s);
        }
    }
    
    public void testSpeedThruCreateFromTemplate() throws Exception {
        for (int i = 0; i < whereTo.length; i++) {
            obj.createFromTemplate(target, whereTo[i].getName(), parameters);
        }
    }
    
    public void XtestSpeedThruScriptAPI() throws Exception {
        InputStreamReader is;
        for (int i = 0; i < whereTo.length; i++) {
            Writer w = new BufferedWriter(new FileWriter(whereTo[i]));
            eng.getContext().setWriter(w);
            is = new InputStreamReader(obj.getPrimaryFile().getInputStream());
            eng.eval(is);
            is.close();
            w.close();
        }
    }
}
