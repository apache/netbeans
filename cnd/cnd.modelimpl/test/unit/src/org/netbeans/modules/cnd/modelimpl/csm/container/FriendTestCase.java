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
package org.netbeans.modules.cnd.modelimpl.csm.container;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmFriend;
import org.netbeans.modules.cnd.api.model.CsmFriendClass;
import org.netbeans.modules.cnd.api.model.CsmFriendFunction;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.api.model.services.CsmCacheManager;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileImpl;
import org.netbeans.modules.cnd.modelimpl.impl.services.FriendResolverImpl;
import org.netbeans.modules.cnd.modelimpl.csm.core.ProjectBase;
import org.netbeans.modules.cnd.modelimpl.trace.TraceModelTestBase;

/**
 * base class for friends tests
 *
 */
public class FriendTestCase extends TraceModelTestBase {

    private static final boolean TRACE = false;

    public FriendTestCase(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    //log("CndFriendTestCase.setUp started.");
    //log("Test "+getName()+  " started");
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testFriend() throws Exception {
        performTest("friend.cc"); // NOI18N
    }

    @Override
    protected void performTest(String source) throws Exception {
        File testFile = getDataFile(source);
        assertTrue("File not found " + testFile.getAbsolutePath(), testFile.exists()); // NOI18N
        performModelTest(testFile, System.out, System.err);
        checkFriend();
        ProjectBase project = getProject();
        for (FileImpl file : project.getAllFileImpls()) {
            file.markReparseNeeded(true);
            file.scheduleParsing(true);
        }
        checkFriend();
        FileImpl fileImpl = project.getFile(testFile.getAbsolutePath(), false);
        assertNotNull(fileImpl);
        project.onFileImplRemoved(Collections.<FileImpl>emptyList(), Collections.singleton(fileImpl));
        checkEmpty();
    }

    private void checkEmpty() {
        ProjectBase project = getProject();
        assertNotNull("Project must be valid", project); // NOI18N
        assertTrue("Should be 0 declarations in project", project.findDeclarationsByPrefix("").size() == 0); // NOI18N
        assertTrue("Should be 0 declarations in global namespace", project.getGlobalNamespace().getDeclarations().size() == 0); // NOI18N
        assertTrue("Should be 0 definitions in global namespace", project.getGlobalNamespace().getDefinitions().size() == 0); // NOI18N
        assertTrue("Should be 0 namespaces in global namespace", project.getGlobalNamespace().getNestedNamespaces().size() == 0); // NOI18N
    }

    private String getClassName(Class cls) {
        String s = cls.getName();
        return s.substring(s.lastIndexOf('.') + 1);
    }

    private void checkFriend() {
        CsmCacheManager.enter();
        try {
            checkFriendImpl();
        } finally {
            CsmCacheManager.leave();
        }
    }
    
    private void checkFriendImpl() {
        ProjectBase project = getProject();
        assertNotNull("Project must be valid", project); // NOI18N
        CsmClass clsB = (CsmClass) project.findClassifier("B"); // NOI18N
        assertNotNull("Class B not found", clsB); // NOI18N
        Collection<CsmFriend> friends = clsB.getFriends();
        assertTrue("Should be 5 friends in class B", friends.size() == 5); // NOI18N
        CsmFriendClass friendA2 = null;
        CsmFriendFunction friendMoo2 = null;
        CsmFriendFunction friendMoo = null;
        CsmFriendFunction friendSoo = null;
        CsmFriendFunction friendSoo2 = null;
        for (CsmFriend friend : friends) {
            if ("A2".equals(friend.getName().toString())) { // NOI18N
                friendA2 = (CsmFriendClass) friend;
            } else if ("moo2".equals(friend.getName().toString())) { // NOI18N
                friendMoo2 = (CsmFriendFunction) friend;
            } else if ("moo".equals(friend.getName().toString())) { // NOI18N
                friendMoo = (CsmFriendFunction) friend;
            } else if ("soo".equals(friend.getName().toString())) { // NOI18N
                friendSoo = (CsmFriendFunction) friend;
            } else if ("soo2".equals(friend.getName().toString())) { // NOI18N
                friendSoo2 = (CsmFriendFunction) friend;
            }
        }
        assertNotNull("Friend class declaration A2 not found", friendA2); // NOI18N
        assertNotNull("Friend method declaration moo2 not found", friendMoo2); // NOI18N
        assertNotNull("Friend method declaration moo not found", friendMoo); // NOI18N
        assertNotNull("Friend method declaration soo not found", friendSoo); // NOI18N
        assertNotNull("Friend method declaration soo2 not found", friendSoo2); // NOI18N

        CsmClass clsA2 = friendA2.getReferencedClass();
        assertNotNull("Referenced class A2 for friend not found", clsA2); // NOI18N
        CsmFunction funMoo2 = friendMoo2.getReferencedFunction();
        assertNotNull("Referenced function moo2 for friend not found", funMoo2); // NOI18N
        CsmFunction funMoo = friendMoo.getReferencedFunction();
        assertNotNull("Referenced function moo for friend not found", funMoo); // NOI18N
        CsmFunction funSoo = friendSoo.getReferencedFunction();
        assertNotNull("Referenced function soo for friend not found", funSoo); // NOI18N
        CsmFunction funSoo2 = friendSoo2.getReferencedFunction();
        assertNotNull("Referenced function soo2 for friend not found", funSoo); // NOI18N

        Collection<CsmFriend> list = FriendResolverImpl.getDefault().findFriends(clsA2);
        assertEquals("Should be 1 friend declaration for class A2", 1, list.size()); // NOI18N
        assertEquals("Friend declaration for class A2 has wrong instance", friendA2, list.iterator().next()); // NOI18N
        list = FriendResolverImpl.getDefault().findFriends(funMoo2);
        assertEquals("Should be 1 friend declaration for function moo2", 1, list.size()); // NOI18N
        assertEquals("Friend declaration for function moo2 has wrong instance", friendMoo2, list.iterator().next()); // NOI18N
        list = FriendResolverImpl.getDefault().findFriends(funMoo);
        assertEquals("Should be 1 friend declaration for function moo", 1, list.size()); // NOI18N
        assertEquals("Friend declaration for function moo has wrong instance", friendMoo, list.iterator().next()); // NOI18N
        list = FriendResolverImpl.getDefault().findFriends(funSoo);
        assertEquals("Should be 1 friend declaration for function soo", 1, list.size()); // NOI18N
        assertEquals("Friend declaration for function soo has wrong instance", friendSoo, list.iterator().next());

        Collection<CsmOffsetableDeclaration> declarations = project.findDeclarationsByPrefix("");
        Set<String> set = new HashSet<>();
        for (CsmOffsetableDeclaration decl : declarations) {
            String uName = decl.getUniqueName().toString();
            if (TRACE) {
                System.out.println(uName + " \t" + getClassName(decl.getClass())); // NOI18N
            }
            if ("F:moo2(int)".equals(uName)) {
                assertTrue("moo2(int) is not friend B", FriendResolverImpl.getDefault().isFriend(decl, clsB)); // NOI18N
            } else if ("f:moo(int)".equals(uName)) { // NOI18N
                assertTrue("moo(int) is not friend B", FriendResolverImpl.getDefault().isFriend(decl, clsB)); // NOI18N
            } else if ("D:moo2(int)".equals(uName)) {
                assertTrue("moo2(int) is not friend B", FriendResolverImpl.getDefault().isFriend(decl, clsB)); // NOI18N
            } else if ("d:moo(int)".equals(uName)) { // NOI18N
                assertTrue("moo(int) is not friend B", FriendResolverImpl.getDefault().isFriend(decl, clsB)); // NOI18N
            } else if ("S:S2".equals(uName)) { // NOI18N
                assertFalse("S2 is friend B", FriendResolverImpl.getDefault().isFriend(decl, clsB)); // NOI18N
            } else if ("F:S2::soo()".equals(uName)) { // NOI18N
                assertTrue("S2::soo() is not friend B", FriendResolverImpl.getDefault().isFriend(decl, clsB)); // NOI18N
            } else if ("f:S2::soo2()".equals(uName)) { // NOI18N
                assertTrue("S2::soo2() is not friend B", FriendResolverImpl.getDefault().isFriend(decl, clsB)); // NOI18N
            } else if ("D:S2::soo()".equals(uName)) { // NOI18N
                assertTrue("S2::soo() is not friend B", FriendResolverImpl.getDefault().isFriend(decl, clsB)); // NOI18N
            } else if ("d:S2::soo2()".equals(uName)) { // NOI18N
                assertTrue("S2::soo2() is not friend B", FriendResolverImpl.getDefault().isFriend(decl, clsB)); // NOI18N
            } else if ("F:S2::soo3()".equals(uName)) { // NOI18N
                assertTrue("S2::soo3() is not friend B", FriendResolverImpl.getDefault().isFriend(decl, clsB)); // NOI18N
            } else if ("C:A2".equals(uName)) { // NOI18N
                assertTrue("A2 is not friend B", FriendResolverImpl.getDefault().isFriend(decl, clsB)); // NOI18N
            } else if ("F:A2::foo()".equals(uName)) { // NOI18N
                assertTrue("A2::foo() is not friend B", FriendResolverImpl.getDefault().isFriend(decl, clsB)); // NOI18N
            } else if ("f:A2::foo()".equals(uName)) { // NOI18N
                assertTrue("A2::foo() is not friend B", FriendResolverImpl.getDefault().isFriend(decl, clsB)); // NOI18N
            } else if ("F:ccStyle()".equals(uName)) { // NOI18N
                assertFalse("ccStyle() is friend B", FriendResolverImpl.getDefault().isFriend(decl, clsB)); // NOI18N
            } else if ("C:B".equals(uName)) { // NOI18N
                assertFalse("B is friend B", FriendResolverImpl.getDefault().isFriend(decl, clsB)); // NOI18N
            } else if ("t:B::xxx".equals(uName)) { // NOI18N
                assertFalse("B::xxx is friend B", FriendResolverImpl.getDefault().isFriend(decl, clsB)); // NOI18N
            } else if ("r:B::A2".equals(uName)) { // NOI18N
                assertFalse("B::A2 is friend B", FriendResolverImpl.getDefault().isFriend(decl, clsB)); // NOI18N
            } else if ("F:B::boo()".equals(uName)) { // NOI18N
                assertFalse("B::boo() is friend B", FriendResolverImpl.getDefault().isFriend(decl, clsB)); // NOI18N
            } else if ("f:moo2(int)".equals(uName)) { // NOI18N
                assertTrue("moo2(int) is not friend B", FriendResolverImpl.getDefault().isFriend(decl, clsB)); // NOI18N
            } else if ("F:moo(int)".equals(uName)) { // NOI18N
                assertTrue("moo(int) is friend B", FriendResolverImpl.getDefault().isFriend(decl, clsB)); // NOI18N
            } else if ("f:S2::soo()".equals(uName)) { // NOI18N
                assertTrue("S2::soo() is not friend B", FriendResolverImpl.getDefault().isFriend(decl, clsB)); // NOI18N
            } else if ("F:S2::soo2()".equals(uName)) { // NOI18N
                assertTrue("S2::soo2() is not friend B", FriendResolverImpl.getDefault().isFriend(decl, clsB)); // NOI18N
            } else if ("f:ccStyle(int)".equals(uName)) { // NOI18N
                assertFalse("ccStyle(int) is friend B", FriendResolverImpl.getDefault().isFriend(decl, clsB)); // NOI18N
            } else if ("d:moo2(int)".equals(uName)) { // NOI18N
                assertTrue("moo2(int) is not friend B", FriendResolverImpl.getDefault().isFriend(decl, clsB)); // NOI18N
            } else if ("D:moo(int)".equals(uName)) { // NOI18N
                assertTrue("moo(int) is friend B", FriendResolverImpl.getDefault().isFriend(decl, clsB)); // NOI18N
            } else if ("d:S2::soo()".equals(uName)) { // NOI18N
                assertTrue("S2::soo() is not friend B", FriendResolverImpl.getDefault().isFriend(decl, clsB)); // NOI18N
            } else if ("D:S2::soo2()".equals(uName)) { // NOI18N
                assertTrue("S2::soo2() is not friend B", FriendResolverImpl.getDefault().isFriend(decl, clsB)); // NOI18N
            } else if ("d:ccStyle(int)".equals(uName)) { // NOI18N
                assertFalse("ccStyle(int) is friend B", FriendResolverImpl.getDefault().isFriend(decl, clsB)); // NOI18N
            } else {
                assertTrue("Inexpected declaration " + uName, false); // NOI18N
            }
            assertFalse("Duplicated declaration ", set.contains(uName)); // NOI18N
            set.add(uName);
        }
        assertTrue("Not all declaration found in project", set.size() == 18); // NOI18N
    }
    /*
    int moo2(int);                          //F:moo2(int)                    FunctionImpl
    int moo(int){return 0;}                 //f:moo(int)          FunctionDDImpl
    struct S2 {                             //S:S2                             ClassImpl
    int soo();                          //F:S2::soo()                    MethodImpl
    int soo2(){return 0;} };            //f:S2::soo2() 	MethodDDImpl
    class A2{                               //C:A2                              ClassImpl
    int foo(); };                       //F:A2::foo()                    MethodImpl
    int A2::foo(){ return 0; }              //f:A2::foo() 	FunctionDefinitionImpl
    int ccStyle();                          //F:ccStyle()                    FunctionImpl
    class B{                                //C:B                               ClassImpl
    typedef int xxx;                    //t:B::xxx                        ClassImpl$MemberTypedef
    friend class A2;                    //r:B::A2 	FriendClassImpl
    int boo();                          //F:B::boo()                     MethodImpl
    friend int moo2(int) { return 0; }; //f:moo2(int) 	FriendFunctionDDImpl
    friend int moo(int);                //F:moo(int)                     FriendFunctionImpl
    friend int S2::soo(){ return 0; }   //f:S2::soo() 	FriendFunctionDefinitionImpl
    friend int S2::soo2(); };           //F:S2::soo2()                   FriendFunctionImplEx
    int ccStyle(int){ return 0; }           //f:ccStyle(int) 	FunctionDDImpl
     */
}
