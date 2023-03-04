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

package org.netbeans.modules.subversion;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.openide.filesystems.*;


/**
 * @author rmatous
 */
public class MissingBeforeDeleteTest extends TestBaseHid {

    public MissingBeforeDeleteTest(String name) {
        super(name);
    }

    @Override
    protected Level logLevel() {
        return Level.ALL;
    }

    private FileObject root;
    protected void setUp() throws Exception {
        super.setUp();
        root = testedFS.findResource(getResourcePrefix());               
        Logger.getLogger("").addHandler(new SVNInterceptor());   
//        MockServices.setServices(new Class[] {TestAnnotationProvider.class});                
//        
//        // ping
//        File file = new File("dil");
//        file = FileUtil.normalizeFile(file);                
//        final FileObject fo = FileUtil.toFileObject(file);                
//        // interceptor init
//        TestAnnotationProvider.instance.init();        
        
    }

    public void testMissingBeforeDelete() throws Exception {
        FileUtil.createData(root,"subpackage/newclass.java");
        FileObject subpackage = root.getFileObject("subpackage");
        subpackage.delete();
        
        fail();
    }

    @Override
    protected String[] getResources(String testName) {
        return new String[] {"dil"};
    }
    
    class SVNInterceptor extends Handler {
        public void publish(LogRecord rec) {
            if(rec.getMessage().indexOf("doDelete") > -1) {
                System.out.println("");
            }            
        }

        @Override
        public void flush() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void close() throws SecurityException {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }    
    
}
