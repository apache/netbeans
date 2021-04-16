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
package org.netbeans.modules.java.disco.archive;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.openide.util.RequestProcessor;
import org.openide.windows.IOColorLines;
import org.openide.windows.InputOutput;

public abstract class AbstractCommandLineUnarchiver implements Unarchiver {

    protected abstract @NonNull String[] getProbeCommand();

    protected abstract @NonNull String[] getCommand(@NonNull File input, @NonNull File targetDir);

    protected abstract @NonNull String getName();

    private boolean probe() {
        //TODO: cache probe result?
        try {
            Process probe = new ProcessBuilder().command(getProbeCommand())
                    .start();
            if (!probe.waitFor(10, TimeUnit.SECONDS) || probe.exitValue() != 0) {
                probe.destroyForcibly();

                return false;
            }
        } catch (IOException | InterruptedException ioe) {
            return false;
        }
        return true;
    }

    @Override
    public void uncompress(File zipFile, File targetDir, InputOutput io) throws InterruptedException, IOException {
        if (!isSupported(zipFile))
            throw new UnsupportedOperationException();
        if (!probe())
            throw new UnsupportedOperationException(getName() + " not available");
        ProcessBuilder pb = new ProcessBuilder();
        Process unzip = pb.command(
                getCommand(zipFile, targetDir))
                .start();
        io.getOut().println("Running : " + pb.command().toString());
        RequestProcessor.getDefault().post(new Runnable() {
            @Override
            public void run() {
                try {
                    InputStream cliIO = unzip.getInputStream();
                    BufferedReader br = new BufferedReader(new InputStreamReader(cliIO));
                    String s;
                    while ((s = br.readLine()) != null) {
                        io.getOut().println(s);
                    }
                } catch (IOException ex) {
                    io.getOut().append("Exception ").append(ex.toString()).println();
                }
            }
        });
        RequestProcessor.getDefault().post(new Runnable() {
            @Override
            public void run() {
                try {
                    InputStream cliIO = unzip.getErrorStream();
                    BufferedReader br = new BufferedReader(new InputStreamReader(cliIO));
                    String s;
                    while ((s = br.readLine()) != null) {
                        if (IOColorLines.isSupported(io))
                            IOColorLines.println(io, s, Color.RED);
                        else
                            io.getOut().println(s);
                    }
                } catch (IOException ex) {
                    io.getOut().append("Exception ").append(ex.toString()).println();
                }
            }
        });
        int exitCode = unzip.waitFor();
        io.getOut().println("Exit code: " + exitCode);
        if (0 != exitCode)
            throw new IOException(getName() + " failed");
    }

}
