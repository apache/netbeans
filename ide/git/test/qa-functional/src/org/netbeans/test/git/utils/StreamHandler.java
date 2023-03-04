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
package org.netbeans.test.git.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 *
 * @author peter
 */
public class StreamHandler extends Thread {

    InputStream in = null;
    OutputStream out = null;

    /**
     * Creates a new instance of StreamHandler
     */
    public StreamHandler(InputStream in, OutputStream out) {
        this.in = new BufferedInputStream(in);
        if (out != null) {
            this.out = new BufferedOutputStream(out);
        }
    }

    public void run() {
        try {
            try {
                try {
                    int i;
                    while ((i = in.read()) != -1) {
                        if (out != null) {
                            out.write(i);
                        }
                    }
                } finally {
                    in.close();
                }
                if (out != null) {
                    out.flush();
                }
            } finally {
                if (out != null) {
                    out.close();
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
