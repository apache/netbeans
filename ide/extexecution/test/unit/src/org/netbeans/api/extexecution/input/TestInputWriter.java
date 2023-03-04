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

package org.netbeans.api.extexecution.input;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.openide.windows.OutputListener;
import org.openide.windows.OutputWriter;

/**
 *
 * @author Petr Hejl
 */
public class TestInputWriter extends OutputWriter {

    private List<String> printed = new ArrayList<String>();

    private StringBuilder builder = new StringBuilder();

    private int resetsProcessed;

    private String cache = "";

    public TestInputWriter(Writer w) {
        super(w);
    }

    @Override
    public void println(String s, OutputListener l) throws IOException {
        println(s);
    }

    @Override
    public void print(String s) {
        cache = s;
        builder.append(s);
        super.print(s);
    }

    @Override
    public void println() {
        printed.add(cache);
        builder.append("\n");
        cache = "";
        super.println();
    }


    @Override
    public void reset() throws IOException {
        resetsProcessed++;
        printed.clear();
        builder = new StringBuilder();
        cache = "";
    }

    public List<String> getPrinted() {
        return Collections.unmodifiableList(printed);
    }

    public String getPrintedRaw() {
        return builder.toString();
    }

    public int getResetsProcessed() {
        return resetsProcessed;
    }
}
