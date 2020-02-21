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

package org.netbeans.modules.cnd.api.execution;

import java.util.List;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.cnd.api.utils.ImportUtils;

/**
 *
 */
public class ExecutionSupportTest extends NbTestCase {
    private static final boolean TRACE = false;

    public ExecutionSupportTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    @Override
    protected int timeOut() {
        return 500000;
    }

    public void testExecutionSupport4() throws Exception {
        String source = " -DMA=main -DEXT=\\\"ExternalClass.h\\\" -DQQ=namespace\\ qq\\ {\\ namespace\\ in\\ { -DQQ_CLOSE=}}"+
                        " -DUSE=qq\\:\\:in -DIMPL=ImplClass -DQUOTE\\(name,extension\\)=<name.extension> -DRET\\(index\\)=ret\\[index\\] -DFOO=foo\\(\\) -I.";
        List<String> res = ImportUtils.parseArgs(source);
        assert res.size() == 10;
        for(int i = 0; i < res.size(); i++){
           String p = res.get(i);
            if (TRACE) {
                System.err.println(p);
            }
            if (i==0){
                assert "-DMA=main".equals(p);
            } else if (i==1) {
                assert "-DEXT=\\\"ExternalClass.h\\\"".equals(p);
            } else if (i == 2) {
                assert "-DQQ=namespace qq { namespace in {".equals(p);
            } else if (i == 3) {
                assert "-DQQ_CLOSE=}}".equals(p);
            } else if (i == 4) {
                assert "-DUSE=qq::in".equals(p);
            } else if (i == 5) {
                assert "-DIMPL=ImplClass".equals(p);
            } else if (i == 6) {
                assert "-DQUOTE(name,extension)=<name.extension>".equals(p);
            } else if (i == 7) {
                assert "-DRET(index)=ret[index]".equals(p);
            } else if (i == 8) {
                assert "-DFOO=foo()".equals(p);
            } else if (i == 9) {
                assert "-I.".equals(p);
            }
        }
    }

    public void testExecutionSupport3() throws Exception {
        String source = "-DMA='main' -DEXT='\"ExternalClass.h\"' -DQQ='namespace qq {' -DQQ_CLOSE='}'";
        List<String> res = ImportUtils.parseArgs(source);
        assert res.size() == 4;
        for(int i = 0; i < res.size(); i++){
            String p = res.get(i);
            if (TRACE) {
                System.err.println(p);
            }
            if (i==0){
                assert "-DMA='main'".equals(p);
            } else if (i==1) {
                assert "-DEXT='\"ExternalClass.h\"'".equals(p);
            } else if (i == 2) {
                assert "-DQQ='namespace qq {'".equals(p);
            } else if (i == 3) {
                assert "-DQQ_CLOSE='}'".equals(p);
            }
        }
    }
    
    public void testExecutionSupport2() throws Exception {
        String source = "./configure -DM=CPU\\ =\\ 6 CXXFLAGS=-g3\\ -gdwarf-2 -G Unix\\ Makefiles";
        List<String> res = ImportUtils.parseEnvironment(source);
        assert res.size() == 1;
        for(int i = 0; i < res.size(); i++){
            String p = res.get(i);
            if (TRACE) {
                System.err.println(p);
            }
            if (i == 0) {
                assert "CXXFLAGS=-g3 -gdwarf-2".equals(p);
            }
        }
        res = ImportUtils.quoteList(res);
        for(int i = 0; i < res.size(); i++){
            String p = res.get(i);
            if (TRACE) {
                System.err.println(p);
            }
            if (i == 0) {
                assert "CXXFLAGS=\"-g3 -gdwarf-2\"".equals(p);
            }
        }
        res = ImportUtils.parseArgs(source);
        assert res.size() == 5;
        for(int i = 0; i < res.size(); i++){
            String p = res.get(i);
            if (TRACE) {
                System.err.println(p);
            }
            if (i==0){
                assert "./configure".equals(p);
            } else if (i==1) {
                assert "-DM=CPU = 6".equals(p);
            } else if (i == 2) {
                assert "CXXFLAGS=-g3 -gdwarf-2".equals(p);
            } else if (i == 3) {
                assert "-G".equals(p);
            } else if (i == 4) {
                assert "Unix Makefiles".equals(p);
            }
        }
    }

    public void testExecutionSupport1() throws Exception {
        String source = "CXX=CC CFLAGS=\"-g -xinstrument=datarace\" CXXFLAGS=\"-g -xinstrument=datarace\"";
        List<String> res = ImportUtils.parseEnvironment(source);
        assert res.size() == 3;
        for(int i = 0; i < res.size(); i++){
            String p = res.get(i);
            if (TRACE) {
                System.err.println(p);
            }
            if (i == 0) {
                assert "CXX=CC".equals(p);
            } else if (i == 1) {
                assert "CFLAGS=-g -xinstrument=datarace".equals(p);
            } else if (i == 2) {
                assert "CXXFLAGS=-g -xinstrument=datarace".equals(p);
            }
        }
        res = ImportUtils.quoteList(res);
        for(int i = 0; i < res.size(); i++){
            String p = res.get(i);
            if (TRACE) {
                System.err.println(p);
            }
            if (i == 0) {
                assert "CXX=CC".equals(p);
            } else if (i == 1) {
                assert "CFLAGS=\"-g -xinstrument=datarace\"".equals(p);
            } else if (i == 2) {
                assert "CXXFLAGS=\"-g -xinstrument=datarace\"".equals(p);
            }
        }

        res = ImportUtils.parseArgs(source);
        assert res.size() == 3;
        for(int i = 0; i < res.size(); i++){
            String p = res.get(i);
            if (TRACE) {
                System.err.println(p);
            }
            if (i == 0) {
                assert "CXX=CC".equals(p);
            } else if (i == 1) {
                assert "CFLAGS=-g -xinstrument=datarace".equals(p);
            } else if (i == 2) {
                assert "CXXFLAGS=-g -xinstrument=datarace".equals(p);
            }
        }
    }

    public void testExecutionSupport() throws Exception {
        String source = "./configure -DM=\"CPU = 6\" CPPFLAGS=-g3 CFLAGS=\'-g3 -gdwarf-2\' -DH --help -DM=\"'6\" CXXFLAGS=\"-g3 -gdwarf-2\" -G \"Unix Makefiles\" -DCMAKE_BUILD_TYPE=Debug";
        List<String> res = ImportUtils.parseEnvironment(source);
        assert res.size() == 3;
        for(int i = 0; i < res.size(); i++){
            String p = res.get(i);
            if (TRACE) {
                System.err.println(p);
            }
            if (i == 0) {
                assert "CPPFLAGS=-g3".equals(p);
            } else if (i == 1) {
                assert "CFLAGS=-g3 -gdwarf-2".equals(p);
            } else if (i == 2) {
                assert "CXXFLAGS=-g3 -gdwarf-2".equals(p);
            }
        }
        res = ImportUtils.quoteList(res);
        for(int i = 0; i < res.size(); i++){
            String p = res.get(i);
            if (TRACE) {
                System.err.println(p);
            }
            if (i == 0) {
                assert "CPPFLAGS=-g3".equals(p);
            } else if (i == 1) {
                assert "CFLAGS=\"-g3 -gdwarf-2\"".equals(p);
            } else if (i == 2) {
                assert "CXXFLAGS=\"-g3 -gdwarf-2\"".equals(p);
            }
        }
        res = ImportUtils.parseArgs(source);
        assert res.size() == 11;
        for(int i = 0; i < res.size(); i++){
            String p = res.get(i);
            if (TRACE) {
                System.err.println(p);
            }
            if (i==0){
                assert "./configure".equals(p);
            } else if (i==1) {
                assert "-DM=\"CPU = 6\"".equals(p);
            } else if (i == 2) {
                assert "CPPFLAGS=-g3".equals(p);
            } else if (i == 3) {
                assert "CFLAGS=-g3 -gdwarf-2".equals(p);
            } else if (i==4){
                assert "-DH".equals(p);
            } else if (i==5){
                assert "--help".equals(p);
            } else if (i==6){
                assert "-DM=\"'6\"".equals(p);
            } else if (i == 7) {
                assert "CXXFLAGS=-g3 -gdwarf-2".equals(p);
            } else if (i == 8) {
                assert "-G".equals(p);
            } else if (i == 9) {
                assert "Unix Makefiles".equals(p);
            } else if (i == 10) {
                assert "-DCMAKE_BUILD_TYPE=Debug".equals(p);
            }
        }
    }

}
