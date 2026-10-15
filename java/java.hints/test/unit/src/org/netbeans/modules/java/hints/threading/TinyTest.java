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
package org.netbeans.modules.java.hints.threading;

import org.junit.Assume;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.hints.test.api.HintTest;

/**
 *
 * @author lahvac
 */
public class TinyTest extends NbTestCase {

    public TinyTest(String name) {
        super(name);
    }

    public void testNotifyOnCondition1() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "     private void n(java.util.concurrent.locks.Condition c) throws InterruptedException {\n" +
                       "         c.notify();\n" +
                       "     }\n" +
                       "}\n")
                .run(Tiny.class)
                .findWarning("3:11-3:17:verifier:ERR_NotifyOnCondition(notify)")
                .applyFix("FIX_NotifyOnConditionFix(signal)")
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public class Test {\n" +
                              "     private void n(java.util.concurrent.locks.Condition c) throws InterruptedException {\n" +
                              "         c.signal();\n" +
                              "     }\n" +
                              "}\n");
    }

    public void testNotifyOnConditionSelf() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public abstract class Test implements java.util.concurrent.locks.Condition {\n" +
                       "     private void n() throws InterruptedException {\n" +
                       "         notify();\n" +
                       "     }\n" +
                       "}\n")
                .run(Tiny.class)
                .findWarning("3:9-3:15:verifier:ERR_NotifyOnCondition(notify)")
                .applyFix("FIX_NotifyOnConditionFix(signal)")
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public abstract class Test implements java.util.concurrent.locks.Condition {\n" +
                              "     private void n() throws InterruptedException {\n" +
                              "         signal();\n" +
                              "     }\n" +
                              "}\n");
    }
    
    public void testNotifyOnCondition2() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "     private void n(java.util.concurrent.locks.Condition c) throws InterruptedException {\n" +
                       "         c.notifyAll();\n" +
                       "     }\n" +
                       "}\n")
                .run(Tiny.class)
                .findWarning("3:11-3:20:verifier:ERR_NotifyOnCondition(notifyAll)")
                .applyFix("FIX_NotifyOnConditionFix(signalAll)")
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public class Test {\n" +
                              "     private void n(java.util.concurrent.locks.Condition c) throws InterruptedException {\n" +
                              "         c.signalAll();\n" +
                              "     }\n" +
                              "}\n");
    }

    public void testWaitOnCondition1() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "     private void n(java.util.concurrent.locks.Condition c) throws InterruptedException {\n" +
                       "         c.wait();\n" +
                       "     }\n" +
                       "}\n")
                .run(Tiny.class)
                .assertContainsWarnings("3:11-3:15:verifier:ERR_WaitOnCondition");
    }

    public void testWaitOnCondition2() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "     private void n(java.util.concurrent.locks.Condition c) throws InterruptedException {\n" +
                       "         c.wait(1L);\n" +
                       "     }\n" +
                       "}\n")
                .run(Tiny.class)
                .assertContainsWarnings("3:11-3:15:verifier:ERR_WaitOnCondition");
    }

    public void testWaitOnCondition3() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "     private void n(java.util.concurrent.locks.Condition c) throws InterruptedException {\n" +
                       "         c.wait(1L, 1);\n" +
                       "     }\n" +
                       "}\n")
                .run(Tiny.class)
                .assertContainsWarnings("3:11-3:15:verifier:ERR_WaitOnCondition");
    }

    public void testThreadRun() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "     private void n(Thread t) {\n" +
                       "         t.run();\n" +
                       "     }\n" +
                       "}\n")
                .run(Tiny.class)
                .findWarning("3:11-3:14:verifier:ERR_ThreadRun")
                .applyFix("FIX_ThreadRun")
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public class Test {\n" +
                              "     private void n(Thread t) {\n" +
                              "         t.start();\n" +
                              "     }\n" +
                              "}\n");
    }

    public void testThreadRunSelf() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public abstract class Test extends Thread {\n" +
                       "     private void n() {\n" +
                       "         run();\n" +
                       "     }\n" +
                       "}\n")
                .run(Tiny.class)
                .findWarning("3:9-3:12:verifier:ERR_ThreadRun")
                .applyFix("FIX_ThreadRun")
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public abstract class Test extends Thread {\n" +
                              "     private void n() {\n" +
                              "         start();\n" +
                              "     }\n" +
                              "}\n");
    }

    public void testThreadStartInConstructor1() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "     private void n(Thread t) {\n" +
                       "         t.start();\n" +
                       "     }\n" +
                       "}\n")
                .run(Tiny.class)
                .assertWarnings();
    }

    public void testThreadStartInConstructor2() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "     protected Test() {\n" +
                       "         Thread t = null;\n" +
                       "         t.start();\n" +
                       "     }\n" +
                       "}\n")
                .run(Tiny.class)
                .assertWarnings("4:11-4:16:verifier:ERR_ThreadStartInConstructor");
    }

    public void testThreadYield() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "     private void n() {\n" +
                       "         Thread.yield();\n" +
                       "     }\n" +
                       "}\n")
                .run(Tiny.class)
                .assertWarnings("3:16-3:21:verifier:ERR_ThreadYield");
    }

    public void testThreadSuspend() throws Exception {
        Assume.assumeTrue(Runtime.version().feature() <= 22);
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "     private void n(Thread t) {\n" +
                       "         t.suspend();\n" +
                       "     }\n" +
                       "}\n")
                .run(Tiny.class)
                .assertWarnings("3:11-3:18:verifier:ERR_ThreadSuspend(suspend)");
    }

    public void testNestedSynchronized1() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "     private void n(Object o1, Object o2) {\n" +
                       "         synchronized(o1) {\n" +
                       "             if (o2 != null) {\n" +
                       "                 synchronized(o2) {\n" +
                       "                     System.err.println(1);\n" +
                       "                 }\n" +
                       "             }\n" +
                       "         }\n" +
                       "     }\n" +
                       "}\n")
                .run(Tiny.class)
                .assertWarnings("3:9-3:21:verifier:ERR_NestedSynchronized");
    }

    public void testNestedSynchronized2() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "     private synchronized void n(Object o1, Object o2) {\n" +
                       "         synchronized(o1) {\n" +
                       "             if (o2 != null) {\n" +
                       "                 synchronized(o2) {\n" +
                       "                     System.err.println(1);\n" +
                       "                 }\n" +
                       "             }\n" +
                       "         }\n" +
                       "     }\n" +
                       "}\n")
                .run(Tiny.class)
                .assertWarnings("3:9-3:21:verifier:ERR_NestedSynchronized");
    }

    public void testNestedSynchronized3() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "     private synchronized void n(Object o1, Object o2) {\n" +
                       "         synchronized(o1) {\n" +
                       "             if (o2 != null) {\n" +
                       "             }\n" +
                       "         }\n" +
                       "     }\n" +
                       "}\n")
                .run(Tiny.class)
                .assertWarnings("3:9-3:21:verifier:ERR_NestedSynchronized");
    }

    public void testNestedSynchronized4() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "     private void n(Object o1, Object o2) {\n" +
                       "         synchronized(o1) {\n" +
                       "             if (o2 != null) {\n" +
                       "             }\n" +
                       "         }\n" +
                       "     }\n" +
                       "}\n")
                .run(Tiny.class)
                .assertWarnings();
    }

    public void testNestedSynchronized5() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "     private void n(Object o1, Object o2) {\n" +
                       "         synchronized(o1) {\n" +
                       "             if (o2 != null) {\n" +
                       "                 synchronized(o2) {\n" +
                       "                      if (o2 != null) {\n" +
                       "                          synchronized(o2) {\n" +
                       "                               System.err.println(1);\n" +
                       "                          }\n" +
                       "                      }\n" +
                       "                 }\n" +
                       "             }\n" +
                       "         }\n" +
                       "     }\n" +
                       "}\n")
                .run(Tiny.class)
                .assertWarnings("3:9-3:21:verifier:ERR_NestedSynchronized");
    }

    public void testEmptySynchronized() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "     private void n(Object o1, Object o2) {\n" +
                       "         synchronized(o1) {}\n" +
                       "     }\n" +
                       "}\n")
                .run(Tiny.class)
                .assertWarnings("3:9-3:21:verifier:ERR_EmptySynchronized");
    }

    public void testSynchronizedOnLock() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "     private void n(java.util.concurrent.locks.Lock l) {\n" +
                       "         synchronized(l) {\n" +
                       "             System.err.println(1);\n" +
                       "         }\n" +
                       "     }\n" +
                       "}\n")
                .run(Tiny.class)
                .findWarning("3:9-3:21:verifier:ERR_SynchronizedOnLock")
                .applyFix("FIX_SynchronizedOnLock")
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public class Test {\n" +
                              "     private void n(java.util.concurrent.locks.Lock l) {\n" +
                              "         l.lock();\n" +
                              "         try {\n" +
                              "             System.err.println(1);\n" +
                              "         } finally {\n" +
                              "              l.unlock();\n" +
                              "         }\n" +
                              "     }\n" +
                              "}\n");
    }

    public void testVolatileArray1() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "     private volatile String[] arr;\n" +
                       "}\n")
                .run(Tiny.class)
                .assertWarnings("2:31-2:34:verifier:ERR_VolatileArrayField");
    }

    public void testVolatileArray2() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "     private volatile int[] arr = {0};\n" +
                       "}\n")
                .run(Tiny.class)
                .assertWarnings("2:28-2:31:verifier:ERR_VolatileArrayField");
    }

    public void testVolatileArray3() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "     private String[] arr;\n" +
                       "}\n")
                .run(Tiny.class)
                .assertWarnings();
    }

    public void testVolatileArray4() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "     private volatile String arr;\n" +
                       "}\n")
                .run(Tiny.class)
                .assertWarnings();
    }

    public void testUnlockNotInFinally() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "     private void n(java.util.concurrent.locks.Lock l) {\n" +
                       "         l.lock();\n" +
                       "         System.err.println(1);\n" +
                       "         System.err.println(1);\n" +
                       "         l.unlock();\n" +
                       "     }\n" +
                       "}\n")
                .run(UnlockOutsideFinally.class)
                .findWarning("3:9-3:18:verifier:ERR_UnlockOutsideTryFinally")
                .applyFix("FIX_UnlockOutsideTryFinally")
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public class Test {\n" +
                              "     private void n(java.util.concurrent.locks.Lock l) {\n" +
                              "         l.lock();\n" +
                              "         try {\n" +
                              "             System.err.println(1);\n" +
                              "             System.err.println(1);\n" +
                              "         } finally {\n" +
                              "              l.unlock();\n" +
                              "         }\n" +
                              "     }\n" +
                              "}\n");
    }

    public void testUnlockNotInFinallySelf() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public abstract class Test implements java.util.concurrent.locks.Lock {\n" +
                       "     private void n() {\n" +
                       "         lock();\n" +
                       "         System.err.println(1);\n" +
                       "         System.err.println(1);\n" +
                       "         unlock();\n" +
                       "     }\n" +
                       "}\n")
                .run(UnlockOutsideFinally.class)
                .findWarning("3:9-3:16:verifier:ERR_UnlockOutsideTryFinally")
                .applyFix("FIX_UnlockOutsideTryFinally")
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public abstract class Test implements java.util.concurrent.locks.Lock {\n" +
                              "     private void n() {\n" +
                              "         lock();\n" +
                              "         try {\n" +
                              "             System.err.println(1);\n" +
                              "             System.err.println(1);\n" +
                              "         } finally {\n" +
                              "              unlock();\n" +
                              "         }\n" +
                              "     }\n" +
                              "}\n");
    }

    public void testLockUnlock186434() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "     private void n(java.util.concurrent.locks.Lock l) {\n" +
                       "         l.lock();\n" +
                       "         l.unlock();\n" +
                       "     }\n" +
                       "}\n")
                .run(Tiny.class)
                .assertWarnings();
    }

    public void test191283() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "     private void n(java.util.concurrent.locks.Lock l) {\n" +
                       "         System.err.println(2);\n" +
                       "         l.lock();\n" +
                       "         System.err.println(1);\n" +
                       "         System.err.println(1);\n" +
                       "         l.unlock();\n" +
                       "         System.err.println(3);\n" +
                       "     }\n" +
                       "}\n")
                .run(UnlockOutsideFinally.class)
                .findWarning("4:9-4:18:verifier:ERR_UnlockOutsideTryFinally")
                .applyFix("FIX_UnlockOutsideTryFinally")
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public class Test {\n" +
                              "     private void n(java.util.concurrent.locks.Lock l) {\n" +
                              "         System.err.println(2);\n" +
                              "         l.lock();\n" +
                              "         try {\n" +
                              "             System.err.println(1);\n" +
                              "             System.err.println(1);\n" +
                              "         } finally {\n" +
                              "              l.unlock();\n" +
                              "         }\n" +
                              "         System.err.println(3);\n" +
                              "     }\n" +
                              "}\n");
    }
    
    public void testUnsyncedWait1() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "     private void n(Object o) throws InterruptedException {\n" +
                       "         o.wait();\n" +
                       "     }\n" +
                       "}\n")
                .run(Tiny.class)
                .assertWarnings("3:11-3:15:verifier:ERR_UnsyncedWait");
    }

    public void testUnsyncedWait2() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "     private void n(Object o) throws InterruptedException {\n" +
                       "         wait();\n" +
                       "     }\n" +
                       "}\n")
                .run(Tiny.class)
                .assertWarnings("3:9-3:13:verifier:ERR_UnsyncedWait");
    }

    public void testUnsyncedWait3() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "     private synchronized void n(Object o) throws InterruptedException {\n" +
                       "         o.wait();\n" +
                       "     }\n" +
                       "}\n")
                .run(Tiny.class)
                .assertWarnings("3:11-3:15:verifier:ERR_UnsyncedWait");
    }

    public void testUnsyncedWait4() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "     private void n(Object o) throws InterruptedException {\n" +
                       "         synchronized(o) {\n" +
                       "             o.wait();\n" +
                       "         }\n" +
                       "     }\n" +
                       "}\n")
                .run(Tiny.class)
                .assertWarnings();
    }

    public void testUnsyncedWait5() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "     private synchronized void n(Object o) throws InterruptedException {\n" +
                       "         wait();\n" +
                       "     }\n" +
                       "}\n")
                .run(Tiny.class)
                .assertWarnings();
    }

    public void testUnsyncedWait6() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "     private void n(Object o) {\n" +
                       "         new Runnable() {\n" +
                       "             public synchronized void run() {\n" +
                       "                 wait();\n" +
                       "             }\n" +
                       "         }\n" +
                       "     }\n" +
                       "}\n", false)
                .run(Tiny.class)
                .assertWarnings();
    }

    public void testUnsyncedWait7() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "     private void n(Object o) {\n" +
                       "         new Runnable() {\n" +
                       "             public void run() {\n" +
                       "                 synchronized (this) {while (true) {wait();}}\n" +
                       "             }\n" +
                       "         }\n" +
                       "     }\n" +
                       "}\n", false)
                .run(Tiny.class)
                .assertWarnings();
    }

    public void testUnsyncedWait192218a() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "     private static void n() throws InterruptedException {\n" +
                       "         synchronized (Test.class) { Test.class.wait();}\n" +
                       "     }\n" +
                       "}\n")
                .run(Tiny.class)
                .assertWarnings();
    }

    public void testUnsyncedWait192218b() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "     private static synchronized void n() throws InterruptedException {\n" +
                       "         Test.class.wait();\n" +
                       "     }\n" +
                       "}\n")
                .run(Tiny.class)
                .assertWarnings();
    }

    public void testUnsyncedNotify() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "     private synchronized void n(Object o) throws InterruptedException {\n" +
                       "         o.notify();\n" +
                       "     }\n" +
                       "}\n")
                .run(Tiny.class)
                .assertWarnings("3:11-3:17:verifier:ERR_UnsyncedNotify");
    }

    public void testSleepInSync() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "     private synchronized void n() throws Exception {\n" +
                       "         Thread.sleep(1000);\n" +
                       "     }\n" +
                       "}\n")
                .run(Tiny.class)
                .assertWarnings("3:16-3:21:verifier:ERR_SleepInSync");
    }

    public void testSleepInLoop() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "     private void n() throws Exception {\n" +
                       "         while(true) {\n" +
                       "             Thread.sleep(1000);\n" +
                       "         }\n" +
                       "     }\n" +
                       "}\n")
                .run(Tiny.class)
                .assertWarnings("4:20-4:25:verifier:ERR_SleepInLoop");
    }
    
    public void testFieldCanBeFinal1() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "     private int i;\n" +
                       "     public Test() {\n" +
                       "         i = 0;\n" +
                       "     }\n" +
                       "}\n")
                .run(Tiny.class)
                .findWarning("2:17-2:18:verifier:" + Bundle.ERR_CanBeFinal("i"))
                .applyFix(Bundle.FIX_CanBeFinal("i"))
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public class Test {\n" +
                              "     private final int i;\n" +
                              "     public Test() {\n" +
                              "         i = 0;\n" +
                              "     }\n" +
                              "}\n");
    }
    public void testFieldCanBeFinalNoPublic() throws Exception {
        HintTest.create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "     public int i;\n" +
                       "     public Test() {\n" +
                       "         i = 0;\n" +
                       "     }\n" +
                       "}\n")
                .run(Tiny.class)
                .assertWarnings();
    }
}
