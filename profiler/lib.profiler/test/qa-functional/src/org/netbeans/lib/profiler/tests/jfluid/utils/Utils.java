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

package org.netbeans.lib.profiler.tests.jfluid.utils;

import org.netbeans.lib.profiler.ProfilingEventListener;
import org.netbeans.lib.profiler.tests.jfluid.CommonProfilerTestCase;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;


public class Utils {
    //~ Constructors -------------------------------------------------------------------------------------------------------------

    public Utils() {
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public static void copyFile(File file, File target) {
        try {
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(target));
            byte[] buffer = new byte[10240];
            int len = 0;

            while ((len = bis.read(buffer)) > 0) {
                bos.write(buffer, 0, len);
            }

            bis.close();
            bos.close();
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static void copyFolder(File folder, File target) {
        File[] lst = folder.listFiles();

        if (!target.exists()) {
            target.mkdirs();
        }

        for (int i = 0; i < lst.length; i++) {
            File nw = new File(target, lst[i].getName());

            if (lst[i].isDirectory()) {
                copyFolder(lst[i], nw);
            } else {
                copyFile(lst[i], nw);
            }
        }
    }

    public static ProfilingEventListener createProfilingListener(final CommonProfilerTestCase test) {
        return new ProfilingEventListener() {
                public void targetAppStarted() {
                    test.log("app started");
                    test.setStatus(CommonProfilerTestCase.STATUS_RUNNING);
                }

                public void targetAppStopped() {
                    test.log("app stoped");
                }

                public void targetAppSuspended() {
                    test.log("app suspended");
                }

                public void targetAppResumed() {
                    test.log("app resumed");
                }

                public void attachedToTarget() {
                    test.log("app attached to target");
                    test.setStatus(CommonProfilerTestCase.STATUS_RUNNING);
                }

                public void detachedFromTarget() {
                    test.log("app detached from target");
                }

                public void targetVMTerminated() {
                    test.log("vm terminated");
                    test.setStatus(CommonProfilerTestCase.STATUS_FINISHED);
                }
            };
    }

    public static void removeFolder(File folder) {
        File[] lst = folder.listFiles();

        if (lst == null) {
            System.err.println("null files " + folder.getAbsolutePath());

            return;
        }

        for (int i = 0; i < lst.length; i++) {
            if (lst[i].isDirectory()) {
                removeFolder(lst[i]);
            } else {
                lst[i].delete();
            }
        }

        folder.delete();
    }
}
