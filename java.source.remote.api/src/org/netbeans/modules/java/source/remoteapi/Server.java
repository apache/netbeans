/**
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
package org.netbeans.modules.java.source.remoteapi;

import java.io.DataOutputStream;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.File;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;

import javax.ws.rs.core.Application;
import javax.ws.rs.ext.RuntimeDelegate;

import org.netbeans.modules.java.source.remote.api.ResourceRegistration;
import org.netbeans.modules.parsing.impl.indexing.DefaultCacheFolderProvider;
import org.netbeans.modules.parsing.impl.indexing.implspi.CacheFolderProvider;
import org.openide.filesystems.FileUtil;
import org.openide.modules.Places;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

/**
 *
 * @author lahvac
 */
public class Server {

    public static void main(String... args) throws Exception {
        System.setProperty("netbeans.user", args[1]);
        Class<?> main = Class.forName("org.netbeans.core.startup.Main");
        main.getDeclaredMethod("initializeURLFactory").invoke(null);
        DefaultCacheFolderProvider.getInstance().setCacheFolder(FileUtil.toFileObject(new File(args[2], "index")));
        CacheFolderProvider.getCacheFolderForRoot(Places.getUserDirectory().toURI().toURL(), EnumSet.noneOf(CacheFolderProvider.Kind.class), CacheFolderProvider.Mode.EXISTENT);
        startImpl(Integer.parseInt(args[0]));
    }

    public static void start(int reportPort) {
        try {
            startImpl(reportPort);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private static void startImpl(int reportPort) throws UnknownHostException, IOException {
        int port = 9998;

//        List<String> args = new ArrayList<String>(Arrays.asList(origArgs));
//
//        if (args.size() > 1 && "--port".equals(args.get(0))) {
//            args.remove(0);
//            port = Integer.parseInt(args.remove(0));
//        }
        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
        
        Set<Class<?>> resources = Lookup.getDefault().lookupAll(ResourceRegistration.class).stream().map(rr -> rr.getResourceClass()).collect(Collectors.toSet());
        //TODO: shutdown..
        HttpHandler hh = RuntimeDelegate.getInstance().createEndpoint(new Application() {
            @Override
            public Set<Class<?>> getClasses() {
                return resources;
            }
        }, HttpHandler.class);
        
        server.createContext("/", hh);
        
        server.start();
        
        try (Socket report = new Socket(InetAddress.getLocalHost(), reportPort);
             OutputStream out = report.getOutputStream();
             DataOutputStream dos = new DataOutputStream(out)) {
            dos.writeInt(server.getAddress().getPort());
        }
    }
    
}
