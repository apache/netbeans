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
package org.netbeans.agent;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

public class TrackingAgent {

    private static final String TRACKING_HOOKS = "org/netbeans/agent/hooks/TrackingHooksCallback";

    //<editor-fold defaultstate="collapsed" desc="Transformations">
    private static final List<TrackingTransformer.MethodEnhancement> toInject = Arrays.asList(
            new TrackingTransformer.MethodEnhancement("java/lang/System",
                                                      "exit",
                                                      "(I)V",
                                                      Arrays.asList(
                                                        "s" + TRACKING_HOOKS,
                                                        "s" + "exitCallback",
                                                        "s" + "(I)V",
                                                        "0C,%1s,%2s",
                                                        "07,%0s",
                                                        "0A,%4s,%3s"
                                                      ),
                                                      "1AB8,%5s"), //iload0, invokespecial #5
            new TrackingTransformer.MethodEnhancement("java/lang/Runtime",
                                                      "exit",
                                                      "(I)V",
                                                      Arrays.asList(
                                                        "s" + TRACKING_HOOKS,
                                                        "s" + "exitCallback",
                                                        "s" + "(I)V",
                                                        "0C,%1s,%2s",
                                                        "07,%0s",
                                                        "0A,%4s,%3s"
                                                      ),
                                                      "1BB8,%5s"), //iload1, invokespecial #5
            new TrackingTransformer.MethodEnhancement("java/lang/Runtime",
                                                      "halt",
                                                      "(I)V",
                                                      Arrays.asList(
                                                        "s" + TRACKING_HOOKS,
                                                        "s" + "exitCallback",
                                                        "s" + "(I)V",
                                                        "0C,%1s,%2s",
                                                        "07,%0s",
                                                        "0A,%4s,%3s"
                                                      ),
                                                      "1BB8,%5s"), //iload1, invokespecial #5
            new TrackingTransformer.MethodEnhancement("java/io/FileOutputStream",
                                                      "<init>",
                                                      "(Ljava/io/File;)V",
                                                      Arrays.asList(
                                                        "s" + TRACKING_HOOKS,
                                                        "s" + "write",
                                                        "s" + "(Ljava/io/File;)V",
                                                        "0C,%1s,%2s",
                                                        "07,%0s",
                                                        "0A,%4s,%3s"
                                                      ),
                                                      "2A2BB8,%5s"), //aload1, invokespecial #5
            new TrackingTransformer.MethodEnhancement("java/io/FileOutputStream",
                                                      "<init>",
                                                      "(Ljava/lang/String;)V",
                                                      Arrays.asList(
                                                        "s" + TRACKING_HOOKS,
                                                        "s" + "write",
                                                        "s" + "(Ljava/lang/String;)V",
                                                        "0C,%1s,%2s",
                                                        "07,%0s",
                                                        "0A,%4s,%3s"
                                                      ),
                                                      "2A2BB8,%5s"), //aload1, invokespecial #5
            new TrackingTransformer.MethodEnhancement("java/io/FileInputStream",
                                                      "<init>",
                                                      "(Ljava/io/File;)V",
                                                      Arrays.asList(
                                                        "s" + TRACKING_HOOKS,
                                                        "s" + "read",
                                                        "s" + "(Ljava/io/File;)V",
                                                        "0C,%1s,%2s",
                                                        "07,%0s",
                                                        "0A,%4s,%3s"
                                                      ),
                                                      "2A2BB8,%5s"), //aload1, invokespecial #5
//            new TrackingTransformer.MethodEnhancement("java/io/FileInputStream", //delegates to FileInputStream(File)
//                                                      "<init>",
//                                                      "(Ljava/lang/String;)V",
//                                                      Arrays.asList(
//                                                        "s" + TRACKING_HOOKS,
//                                                        "s" + "read",
//                                                        "s" + "(Ljava/lang/String;)V",
//                                                        "0C,%1s,%2s",
//                                                        "07,%0s",
//                                                        "0A,%4s,%3s"
//                                                      ),
//                                                      "2A2BB8,%5s"), //aload1, invokespecial #5
            new TrackingTransformer.MethodEnhancement("java/nio/file/Files",
                                                      "newOutputStream",
                                                      "(Ljava/nio/file/Path;[Ljava/nio/file/OpenOption;)Ljava/io/OutputStream;",
                                                      Arrays.asList(
                                                        "s" + TRACKING_HOOKS,
                                                        "s" + "write",
                                                        "s" + "(Ljava/nio/file/Path;)V",
                                                        "0C,%1s,%2s",
                                                        "07,%0s",
                                                        "0A,%4s,%3s"
                                                      ),
                                                      "2AB8,%5s"), //aload0, invokespecial #5
//            new TrackingTransformer.MethodEnhancement("java/nio/file/Files", //covered by newByteChannel
//                                                      "newInputStream",
//                                                      "(Ljava/nio/file/Path;[Ljava/nio/file/OpenOption;)Ljava/io/InputStream;",
//                                                      Arrays.asList(
//                                                        "s" + TRACKING_HOOKS,
//                                                        "s" + "read",
//                                                        "s" + "(Ljava/nio/file/Path;)V",
//                                                        "0C,%1s,%2s",
//                                                        "07,%0s",
//                                                        "0A,%4s,%3s"
//                                                      ),
//                                                      "2AB8,%5s"), //aload0 , invokespecial #5
            new TrackingTransformer.MethodEnhancement("java/io/File",
                                                      "list",
                                                      "()[Ljava/lang/String;",
                                                      Arrays.asList(
                                                        "s" + TRACKING_HOOKS,
                                                        "s" + "read",
                                                        "s" + "(Ljava/io/File;)V",
                                                        "0C,%1s,%2s",
                                                        "07,%0s",
                                                        "0A,%4s,%3s"
                                                      ),
                                                      "2AB8,%5s"), //aload0, invokespecial #5
            new TrackingTransformer.MethodEnhancement("java/io/File",
                                                      "list",
                                                      "(Ljava/io/FilenameFilter;)[Ljava/lang/String;",
                                                      Arrays.asList(
                                                        "s" + TRACKING_HOOKS,
                                                        "s" + "read",
                                                        "s" + "(Ljava/io/File;)V",
                                                        "0C,%1s,%2s",
                                                        "07,%0s",
                                                        "0A,%4s,%3s"
                                                      ),
                                                      "2AB8,%5s"), //aload0, invokespecial #5
            new TrackingTransformer.MethodEnhancement("java/io/File",
                                                      "listFiles",
                                                      "()[Ljava/io/File;",
                                                      Arrays.asList(
                                                        "s" + TRACKING_HOOKS,
                                                        "s" + "read",
                                                        "s" + "(Ljava/io/File;)V",
                                                        "0C,%1s,%2s",
                                                        "07,%0s",
                                                        "0A,%4s,%3s"
                                                      ),
                                                      "2AB8,%5s"), //aload0, invokespecial #5
            new TrackingTransformer.MethodEnhancement("java/io/File",
                                                      "listFiles",
                                                      "(Ljava/io/FilenameFilter;)[Ljava/io/File;",
                                                      Arrays.asList(
                                                        "s" + TRACKING_HOOKS,
                                                        "s" + "read",
                                                        "s" + "(Ljava/io/File;)V",
                                                        "0C,%1s,%2s",
                                                        "07,%0s",
                                                        "0A,%4s,%3s"
                                                      ),
                                                      "2AB8,%5s"), //aload0, invokespecial #5
            new TrackingTransformer.MethodEnhancement("java/io/File",
                                                      "listFiles",
                                                      "(Ljava/io/FileFilter;)[Ljava/io/File;",
                                                      Arrays.asList(
                                                        "s" + TRACKING_HOOKS,
                                                        "s" + "read",
                                                        "s" + "(Ljava/io/File;)V",
                                                        "0C,%1s,%2s",
                                                        "07,%0s",
                                                        "0A,%4s,%3s"
                                                      ),
                                                      "2AB8,%5s"), //aload0, invokespecial #5
            new TrackingTransformer.MethodEnhancement("java/io/File",
                                                      "mkdir",
                                                      "()Z",
                                                      Arrays.asList(
                                                        "s" + TRACKING_HOOKS,
                                                        "s" + "write",
                                                        "s" + "(Ljava/io/File;)V",
                                                        "0C,%1s,%2s",
                                                        "07,%0s",
                                                        "0A,%4s,%3s"
                                                      ),
                                                      "2AB8,%5s"), //aload0, invokespecial #5
            new TrackingTransformer.MethodEnhancement("java/io/File",
                                                      "mkdirs",
                                                      "()Z",
                                                      Arrays.asList(
                                                        "s" + TRACKING_HOOKS,
                                                        "s" + "write",
                                                        "s" + "(Ljava/io/File;)V",
                                                        "0C,%1s,%2s",
                                                        "07,%0s",
                                                        "0A,%4s,%3s"
                                                      ),
                                                      "2AB8,%5s"), //aload0, invokespecial #5
            new TrackingTransformer.MethodEnhancement("java/io/File",
                                                      "delete",
                                                      "()Z",
                                                      Arrays.asList(
                                                        "s" + TRACKING_HOOKS,
                                                        "s" + "deleteFile",
                                                        "s" + "(Ljava/io/File;)V",
                                                        "0C,%1s,%2s",
                                                        "07,%0s",
                                                        "0A,%4s,%3s"
                                                      ),
                                                      "2AB8,%5s"), //aload0, invokespecial #5
            new TrackingTransformer.MethodEnhancement("java/io/File",
                                                      "canExecute",
                                                      "()Z",
                                                      Arrays.asList(
                                                        "s" + TRACKING_HOOKS,
                                                        "s" + "read",
                                                        "s" + "(Ljava/io/File;)V",
                                                        "0C,%1s,%2s",
                                                        "07,%0s",
                                                        "0A,%4s,%3s"
                                                      ),
                                                      "2AB8,%5s"), //aload0, invokespecial #5
            new TrackingTransformer.MethodEnhancement("java/io/File",
                                                      "canRead",
                                                      "()Z",
                                                      Arrays.asList(
                                                        "s" + TRACKING_HOOKS,
                                                        "s" + "read",
                                                        "s" + "(Ljava/io/File;)V",
                                                        "0C,%1s,%2s",
                                                        "07,%0s",
                                                        "0A,%4s,%3s"
                                                      ),
                                                      "2AB8,%5s"), //aload0, invokespecial #5
            new TrackingTransformer.MethodEnhancement("java/io/File",
                                                      "canWrite",
                                                      "()Z",
                                                      Arrays.asList(
                                                        "s" + TRACKING_HOOKS,
                                                        "s" + "read",
                                                        "s" + "(Ljava/io/File;)V",
                                                        "0C,%1s,%2s",
                                                        "07,%0s",
                                                        "0A,%4s,%3s"
                                                      ),
                                                      "2AB8,%5s"), //aload0, invokespecial #5
            new TrackingTransformer.MethodEnhancement("java/io/File",
                                                      "isDirectory",
                                                      "()Z",
                                                      Arrays.asList(
                                                        "s" + TRACKING_HOOKS,
                                                        "s" + "read",
                                                        "s" + "(Ljava/io/File;)V",
                                                        "0C,%1s,%2s",
                                                        "07,%0s",
                                                        "0A,%4s,%3s"
                                                      ),
                                                      "2AB8,%5s"), //aload0, invokespecial #5
            new TrackingTransformer.MethodEnhancement("java/io/File",
                                                      "isFile",
                                                      "()Z",
                                                      Arrays.asList(
                                                        "s" + TRACKING_HOOKS,
                                                        "s" + "read",
                                                        "s" + "(Ljava/io/File;)V",
                                                        "0C,%1s,%2s",
                                                        "07,%0s",
                                                        "0A,%4s,%3s"
                                                      ),
                                                      "2AB8,%5s"), //aload0, invokespecial #5
            new TrackingTransformer.MethodEnhancement("java/io/File",
                                                      "isHidden",
                                                      "()Z",
                                                      Arrays.asList(
                                                        "s" + TRACKING_HOOKS,
                                                        "s" + "read",
                                                        "s" + "(Ljava/io/File;)V",
                                                        "0C,%1s,%2s",
                                                        "07,%0s",
                                                        "0A,%4s,%3s"
                                                      ),
                                                      "2AB8,%5s"), //aload0, invokespecial #5
            new TrackingTransformer.MethodEnhancement("java/io/File",
                                                      "exists",
                                                      "()Z",
                                                      Arrays.asList(
                                                        "s" + TRACKING_HOOKS,
                                                        "s" + "read",
                                                        "s" + "(Ljava/io/File;)V",
                                                        "0C,%1s,%2s",
                                                        "07,%0s",
                                                        "0A,%4s,%3s"
                                                      ),
                                                      "2AB8,%5s"), //aload0, invokespecial #5
            new TrackingTransformer.MethodEnhancement("java/io/File",
                                                      "length",
                                                      "()J",
                                                      Arrays.asList(
                                                        "s" + TRACKING_HOOKS,
                                                        "s" + "read",
                                                        "s" + "(Ljava/io/File;)V",
                                                        "0C,%1s,%2s",
                                                        "07,%0s",
                                                        "0A,%4s,%3s"
                                                      ),
                                                      "2AB8,%5s"), //aload0, invokespecial #5
            new TrackingTransformer.MethodEnhancement("java/io/File",
                                                      "createNewFile",
                                                      "()Z",
                                                      Arrays.asList(
                                                        "s" + TRACKING_HOOKS,
                                                        "s" + "write",
                                                        "s" + "(Ljava/io/File;)V",
                                                        "0C,%1s,%2s",
                                                        "07,%0s",
                                                        "0A,%4s,%3s"
                                                      ),
                                                      "2AB8,%5s"), //aload0, invokespecial #5
            new TrackingTransformer.MethodEnhancement("java/io/File",
                                                      "setExecutable",
                                                      "(ZZ)Z",
                                                      Arrays.asList(
                                                        "s" + TRACKING_HOOKS,
                                                        "s" + "write",
                                                        "s" + "(Ljava/io/File;)V",
                                                        "0C,%1s,%2s",
                                                        "07,%0s",
                                                        "0A,%4s,%3s"
                                                      ),
                                                      "2AB8,%5s"), //aload0, invokespecial #5
            new TrackingTransformer.MethodEnhancement("java/io/File",
                                                      "setReadable",
                                                      "(ZZ)Z",
                                                      Arrays.asList(
                                                        "s" + TRACKING_HOOKS,
                                                        "s" + "write",
                                                        "s" + "(Ljava/io/File;)V",
                                                        "0C,%1s,%2s",
                                                        "07,%0s",
                                                        "0A,%4s,%3s"
                                                      ),
                                                      "2AB8,%5s"), //aload0, invokespecial #5
            new TrackingTransformer.MethodEnhancement("java/io/File",
                                                      "setWritable",
                                                      "(ZZ)Z",
                                                      Arrays.asList(
                                                        "s" + TRACKING_HOOKS,
                                                        "s" + "write",
                                                        "s" + "(Ljava/io/File;)V",
                                                        "0C,%1s,%2s",
                                                        "07,%0s",
                                                        "0A,%4s,%3s"
                                                      ),
                                                      "2AB8,%5s"), //aload0, invokespecial #5
            new TrackingTransformer.MethodEnhancement("java/io/File",
                                                      "setReadOnly",
                                                      "()Z",
                                                      Arrays.asList(
                                                        "s" + TRACKING_HOOKS,
                                                        "s" + "write",
                                                        "s" + "(Ljava/io/File;)V",
                                                        "0C,%1s,%2s",
                                                        "07,%0s",
                                                        "0A,%4s,%3s"
                                                      ),
                                                      "2AB8,%5s"), //aload0, invokespecial #5
            new TrackingTransformer.MethodEnhancement("java/io/File",
                                                      "setLastModified",
                                                      "(J)Z",
                                                      Arrays.asList(
                                                        "s" + TRACKING_HOOKS,
                                                        "s" + "write",
                                                        "s" + "(Ljava/io/File;)V",
                                                        "0C,%1s,%2s",
                                                        "07,%0s",
                                                        "0A,%4s,%3s"
                                                      ),
                                                      "2AB8,%5s"), //aload0, invokespecial #5
            new TrackingTransformer.MethodEnhancement("java/io/File",
                                                      "lastModified",
                                                      "()J",
                                                      Arrays.asList(
                                                        "s" + TRACKING_HOOKS,
                                                        "s" + "read",
                                                        "s" + "(Ljava/io/File;)V",
                                                        "0C,%1s,%2s",
                                                        "07,%0s",
                                                        "0A,%4s,%3s"
                                                      ),
                                                      "2AB8,%5s"), //aload0, invokespecial #5
//            new TrackingTransformer.MethodEnhancement("java/io/RandomAccessFile", //covered by <init>(File, String)
//                                                      "<init>",
//                                                      "(Ljava/lang/String;Ljava/lang/String;)V",
//                                                      Arrays.asList(
//                                                        "s" + TRACKING_HOOKS,
//                                                        "s" + "readWrite",
//                                                        "s" + "(Ljava/lang/String;)V",
//                                                        "0C,%1s,%2s",
//                                                        "07,%0s",
//                                                        "0A,%4s,%3s"
//                                                      ),
//                                                      "2A2BB8,%5s"), //aload1, invokespecial #5
            new TrackingTransformer.MethodEnhancement("java/io/RandomAccessFile",
                                                      "<init>",
                                                      "(Ljava/io/File;Ljava/lang/String;)V",
                                                      Arrays.asList(
                                                        "s" + TRACKING_HOOKS,
                                                        "s" + "readWrite",
                                                        "s" + "(Ljava/io/File;Ljava/lang/String;)V",
                                                        "0C,%1s,%2s",
                                                        "07,%0s",
                                                        "0A,%4s,%3s"
                                                      ),
                                                      "2A2B2CB8,%5s"), //aload1, aload2, invokespecial #5
            new TrackingTransformer.MethodEnhancement("java/nio/file/Files",
                                                      "delete",
                                                      "(Ljava/nio/file/Path;)V",
                                                      Arrays.asList(
                                                        "s" + TRACKING_HOOKS,
                                                        "s" + "deleteFile",
                                                        "s" + "(Ljava/nio/file/Path;)V",
                                                        "0C,%1s,%2s",
                                                        "07,%0s",
                                                        "0A,%4s,%3s"
                                                      ),
                                                      "2AB8,%5s"), //aload0 , invokespecial #5
            new TrackingTransformer.MethodEnhancement("java/nio/file/Files",
                                                      "createDirectory",
                                                      "(Ljava/nio/file/Path;[Ljava/nio/file/attribute/FileAttribute;)Ljava/nio/file/Path;",
                                                      Arrays.asList(
                                                        "s" + TRACKING_HOOKS,
                                                        "s" + "write",
                                                        "s" + "(Ljava/nio/file/Path;)V",
                                                        "0C,%1s,%2s",
                                                        "07,%0s",
                                                        "0A,%4s,%3s"
                                                      ),
                                                      "2AB8,%5s"), //aload0 , invokespecial #5
//            new TrackingTransformer.MethodEnhancement("java/nio/file/Files", //covered by createDirectory
//                                                      "createDirectories",
//                                                      "(Ljava/nio/file/Path;[Ljava/nio/file/attribute/FileAttribute;)Ljava/nio/file/Path;",
//                                                      Arrays.asList(
//                                                        "s" + TRACKING_HOOKS,
//                                                        "s" + "write",
//                                                        "s" + "(Ljava/nio/file/Path;)V",
//                                                        "0C,%1s,%2s",
//                                                        "07,%0s",
//                                                        "0A,%4s,%3s"
//                                                      ),
//                                                      "2AB8,%5s"), //aload0 , invokespecial #5
            new TrackingTransformer.MethodEnhancement("java/nio/file/Files",
                                                      "newDirectoryStream",
                                                      "(Ljava/nio/file/Path;)Ljava/nio/file/DirectoryStream;",
                                                      Arrays.asList(
                                                        "s" + TRACKING_HOOKS,
                                                        "s" + "read",
                                                        "s" + "(Ljava/nio/file/Path;)V",
                                                        "0C,%1s,%2s",
                                                        "07,%0s",
                                                        "0A,%4s,%3s"
                                                      ),
                                                      "2AB8,%5s"), //aload0 , invokespecial #5
//            new TrackingTransformer.MethodEnhancement("java/nio/file/Files", //covered by newDirectoryStream
//                                                      "newDirectoryStream",
//                                                      "(Ljava/nio/file/Path;Ljava/lang/String;)Ljava/nio/file/DirectoryStream;",
//                                                      Arrays.asList(
//                                                        "s" + TRACKING_HOOKS,
//                                                        "s" + "read",
//                                                        "s" + "(Ljava/nio/file/Path;)V",
//                                                        "0C,%1s,%2s",
//                                                        "07,%0s",
//                                                        "0A,%4s,%3s"
//                                                      ),
//                                                      "2AB8,%5s"), //aload0 , invokespecial #5
            new TrackingTransformer.MethodEnhancement("java/nio/file/Files",
                                                      "newDirectoryStream",
                                                      "(Ljava/nio/file/Path;Ljava/nio/file/DirectoryStream$Filter;)Ljava/nio/file/DirectoryStream;",
                                                      Arrays.asList(
                                                        "s" + TRACKING_HOOKS,
                                                        "s" + "read",
                                                        "s" + "(Ljava/nio/file/Path;)V",
                                                        "0C,%1s,%2s",
                                                        "07,%0s",
                                                        "0A,%4s,%3s"
                                                      ),
                                                      "2AB8,%5s"), //aload0 , invokespecial #5
//            new TrackingTransformer.MethodEnhancement("java/nio/file/Files", //covered by newByteChannel(Path,Set,FileAttribute[])
//                                                      "newByteChannel",
//                                                      "(Ljava/nio/file/Path;[Ljava/nio/file/OpenOption;)Ljava/nio/channels/SeekableByteChannel;",
//                                                      Arrays.asList(
//                                                        "s" + TRACKING_HOOKS,
//                                                        "s" + "readWrite",
//                                                        "s" + "(Ljava/nio/file/Path;)V",
//                                                        "0C,%1s,%2s",
//                                                        "07,%0s",
//                                                        "0A,%4s,%3s"
//                                                      ),
//                                                      "2AB8,%5s"), //aload0 , invokespecial #5
            new TrackingTransformer.MethodEnhancement("java/nio/file/Files",
                                                      "newByteChannel",
                                                      "(Ljava/nio/file/Path;Ljava/util/Set;[Ljava/nio/file/attribute/FileAttribute;)Ljava/nio/channels/SeekableByteChannel;",
                                                      Arrays.asList(
                                                        "s" + TRACKING_HOOKS,
                                                        "s" + "readWrite",
                                                        "s" + "(Ljava/nio/file/Path;Ljava/util/Set;)V",
                                                        "0C,%1s,%2s",
                                                        "07,%0s",
                                                        "0A,%4s,%3s"
                                                      ),
                                                      "2A2BB8,%5s"), //aload0, aload1, invokespecial #5
            new TrackingTransformer.MethodEnhancement("java/nio/file/Files",
                                                      "readAttributes",
                                                      "(Ljava/nio/file/Path;Ljava/lang/Class;[Ljava/nio/file/LinkOption;)Ljava/nio/file/attribute/BasicFileAttributes;",
                                                      Arrays.asList(
                                                        "s" + TRACKING_HOOKS,
                                                        "s" + "read",
                                                        "s" + "(Ljava/nio/file/Path;)V",
                                                        "0C,%1s,%2s",
                                                        "07,%0s",
                                                        "0A,%4s,%3s"
                                                      ),
                                                      "2AB8,%5s"), //aload0, invokespecial #5
            new TrackingTransformer.MethodEnhancement("java/nio/file/Files",
                                                      "readAttributes",
                                                      "(Ljava/nio/file/Path;Ljava/lang/String;[Ljava/nio/file/LinkOption;)Ljava/util/Map;",
                                                      Arrays.asList(
                                                        "s" + TRACKING_HOOKS,
                                                        "s" + "read",
                                                        "s" + "(Ljava/nio/file/Path;)V",
                                                        "0C,%1s,%2s",
                                                        "07,%0s",
                                                        "0A,%4s,%3s"
                                                      ),
                                                      "2AB8,%5s"), //aload0, invokespecial #5
//            new TrackingTransformer.MethodEnhancement("java/nio/file/Files", //covered by readAttribute
//                                                      "getAttribute",
//                                                      "(Ljava/nio/file/Path;Ljava/lang/String;[Ljava/nio/file/LinkOption;)Ljava/lang/Object;",
//                                                      Arrays.asList(
//                                                        "s" + TRACKING_HOOKS,
//                                                        "s" + "read",
//                                                        "s" + "(Ljava/nio/file/Path;)V",
//                                                        "0C,%1s,%2s",
//                                                        "07,%0s",
//                                                        "0A,%4s,%3s"
//                                                      ),
//                                                      "2AB8,%5s"), //aload0, invokespecial #5
            new TrackingTransformer.MethodEnhancement("java/nio/file/Files",
                                                      "getFileAttributeView",
                                                      "(Ljava/nio/file/Path;Ljava/lang/Class;[Ljava/nio/file/LinkOption;)Ljava/nio/file/attribute/FileAttributeView;",
                                                      Arrays.asList(
                                                        "s" + TRACKING_HOOKS,
                                                        "s" + "read",
                                                        "s" + "(Ljava/nio/file/Path;)V",
                                                        "0C,%1s,%2s",
                                                        "07,%0s",
                                                        "0A,%4s,%3s"
                                                      ),
                                                      "2AB8,%5s"), //aload0, invokespecial #5
            new TrackingTransformer.MethodEnhancement("java/nio/file/Files",
                                                      "isDirectory",
                                                      "(Ljava/nio/file/Path;[Ljava/nio/file/LinkOption;)Z",
                                                      Arrays.asList(
                                                        "s" + TRACKING_HOOKS,
                                                        "s" + "read",
                                                        "s" + "(Ljava/nio/file/Path;)V",
                                                        "0C,%1s,%2s",
                                                        "07,%0s",
                                                        "0A,%4s,%3s"
                                                      ),
                                                      "2AB8,%5s"), //aload0, invokespecial #5
            new TrackingTransformer.MethodEnhancement("java/nio/file/Files",
                                                      "isExecutable",
                                                      "(Ljava/nio/file/Path;)Z",
                                                      Arrays.asList(
                                                        "s" + TRACKING_HOOKS,
                                                        "s" + "read",
                                                        "s" + "(Ljava/nio/file/Path;)V",
                                                        "0C,%1s,%2s",
                                                        "07,%0s",
                                                        "0A,%4s,%3s"
                                                      ),
                                                      "2AB8,%5s"), //aload0, invokespecial #5
            new TrackingTransformer.MethodEnhancement("java/nio/file/Files",
                                                      "isHidden",
                                                      "(Ljava/nio/file/Path;)Z",
                                                      Arrays.asList(
                                                        "s" + TRACKING_HOOKS,
                                                        "s" + "read",
                                                        "s" + "(Ljava/nio/file/Path;)V",
                                                        "0C,%1s,%2s",
                                                        "07,%0s",
                                                        "0A,%4s,%3s"
                                                      ),
                                                      "2AB8,%5s"), //aload0, invokespecial #5
            new TrackingTransformer.MethodEnhancement("java/nio/file/Files",
                                                      "isReadable",
                                                      "(Ljava/nio/file/Path;)Z",
                                                      Arrays.asList(
                                                        "s" + TRACKING_HOOKS,
                                                        "s" + "read",
                                                        "s" + "(Ljava/nio/file/Path;)V",
                                                        "0C,%1s,%2s",
                                                        "07,%0s",
                                                        "0A,%4s,%3s"
                                                      ),
                                                      "2AB8,%5s"), //aload0, invokespecial #5
            new TrackingTransformer.MethodEnhancement("java/nio/file/Files",
                                                      "isRegularFile",
                                                      "(Ljava/nio/file/Path;[Ljava/nio/file/LinkOption;)Z",
                                                      Arrays.asList(
                                                        "s" + TRACKING_HOOKS,
                                                        "s" + "read",
                                                        "s" + "(Ljava/nio/file/Path;)V",
                                                        "0C,%1s,%2s",
                                                        "07,%0s",
                                                        "0A,%4s,%3s"
                                                      ),
                                                      "2AB8,%5s"), //aload0, invokespecial #5
            new TrackingTransformer.MethodEnhancement("java/nio/file/Files",
                                                      "isSameFile",
                                                      "(Ljava/nio/file/Path;Ljava/nio/file/Path;)Z",
                                                      Arrays.asList(
                                                        "s" + TRACKING_HOOKS,
                                                        "s" + "read",
                                                        "s" + "(Ljava/nio/file/Path;)V",
                                                        "0C,%1s,%2s",
                                                        "07,%0s",
                                                        "0A,%4s,%3s"
                                                      ),
                                                      "2AB8,%5s,2BB8,%5s"), //aload0, invokespecial #5, aload1, invokespecial #5
            new TrackingTransformer.MethodEnhancement("java/nio/file/Files",
                                                      "isWritable",
                                                      "(Ljava/nio/file/Path;)Z",
                                                      Arrays.asList(
                                                        "s" + TRACKING_HOOKS,
                                                        "s" + "read",
                                                        "s" + "(Ljava/nio/file/Path;)V",
                                                        "0C,%1s,%2s",
                                                        "07,%0s",
                                                        "0A,%4s,%3s"
                                                      ),
                                                      "2AB8,%5s"), //aload0, invokespecial #5
            new TrackingTransformer.MethodEnhancement("java/nio/file/Files",
                                                      "setAttribute",
                                                      "(Ljava/nio/file/Path;Ljava/lang/String;Ljava/lang/Object;[Ljava/nio/file/LinkOption;)Ljava/nio/file/Path;",
                                                      Arrays.asList(
                                                        "s" + TRACKING_HOOKS,
                                                        "s" + "write",
                                                        "s" + "(Ljava/nio/file/Path;)V",
                                                        "0C,%1s,%2s",
                                                        "07,%0s",
                                                        "0A,%4s,%3s"
                                                      ),
                                                      "2AB8,%5s"), //aload0, invokespecial #5
            new TrackingTransformer.MethodEnhancement("java/lang/System",
                                                      "getProperty",
                                                      "(Ljava/lang/String;)Ljava/lang/String;",
                                                      Arrays.asList(
                                                        "s" + TRACKING_HOOKS,
                                                        "s" + "systemProperty",
                                                        "s" + "(Ljava/lang/String;)V",
                                                        "0C,%1s,%2s",
                                                        "07,%0s",
                                                        "0A,%4s,%3s"
                                                      ),
                                                      "2AB8,%5s"), //aload0 , invokespecial #5
            new TrackingTransformer.MethodEnhancement("java/lang/System",
                                                      "getProperty",
                                                      "(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;",
                                                      Arrays.asList(
                                                        "s" + TRACKING_HOOKS,
                                                        "s" + "systemProperty",
                                                        "s" + "(Ljava/lang/String;)V",
                                                        "0C,%1s,%2s",
                                                        "07,%0s",
                                                        "0A,%4s,%3s"
                                                      ),
                                                      "2AB8,%5s"), //aload0 , invokespecial #5
            new TrackingTransformer.MethodEnhancement("java/lang/System",
                                                      "clearProperty",
                                                      "(Ljava/lang/String;)Ljava/lang/String;",
                                                      Arrays.asList(
                                                        "s" + TRACKING_HOOKS,
                                                        "s" + "systemProperty",
                                                        "s" + "(Ljava/lang/String;)V",
                                                        "0C,%1s,%2s",
                                                        "07,%0s",
                                                        "0A,%4s,%3s"
                                                      ),
                                                      "2AB8,%5s"), //aload0 , invokespecial #5
            new TrackingTransformer.MethodEnhancement("java/lang/reflect/Constructor",
                                                      "setAccessible",
                                                      "(Z)V",
                                                      Arrays.asList(
                                                        "s" + TRACKING_HOOKS,
                                                        "s" + "setAccessible",
                                                        "s" + "(Ljava/lang/reflect/AccessibleObject;)V",
                                                        "0C,%1s,%2s",
                                                        "07,%0s",
                                                        "0A,%4s,%3s"
                                                      ),
                                                      "2AB8,%5s"), //aload0 , invokespecial #5
            new TrackingTransformer.MethodEnhancement("java/lang/reflect/Field",
                                                      "setAccessible",
                                                      "(Z)V",
                                                      Arrays.asList(
                                                        "s" + TRACKING_HOOKS,
                                                        "s" + "setAccessible",
                                                        "s" + "(Ljava/lang/reflect/AccessibleObject;)V",
                                                        "0C,%1s,%2s",
                                                        "07,%0s",
                                                        "0A,%4s,%3s"
                                                      ),
                                                      "2AB8,%5s"), //aload0 , invokespecial #5
            new TrackingTransformer.MethodEnhancement("java/lang/reflect/Method",
                                                      "setAccessible",
                                                      "(Z)V",
                                                      Arrays.asList(
                                                        "s" + TRACKING_HOOKS,
                                                        "s" + "setAccessible",
                                                        "s" + "(Ljava/lang/reflect/AccessibleObject;)V",
                                                        "0C,%1s,%2s",
                                                        "07,%0s",
                                                        "0A,%4s,%3s"
                                                      ),
                                                      "2AB8,%5s"), //aload0 , invokespecial #5
            new TrackingTransformer.MethodEnhancement("java/lang/reflect/AccessibleObject",
                                                      "setAccessible",
                                                      "(Z)V",
                                                      Arrays.asList(
                                                        "s" + TRACKING_HOOKS,
                                                        "s" + "setAccessible",
                                                        "s" + "(Ljava/lang/reflect/AccessibleObject;)V",
                                                        "0C,%1s,%2s",
                                                        "07,%0s",
                                                        "0A,%4s,%3s"
                                                      ),
                                                      "2AB8,%5s"), //aload0 , invokespecial #5
            new TrackingTransformer.MethodEnhancement("java/lang/System",
                                                      "setSecurityManager",
                                                      "(Ljava/lang/SecurityManager;)V",
                                                      Arrays.asList(
                                                        "s" + TRACKING_HOOKS,
                                                        "s" + "setSecurityManager",
                                                        "s" + "(Ljava/lang/Object;)V",
                                                        "0C,%1s,%2s",
                                                        "07,%0s",
                                                        "0A,%4s,%3s"
                                                      ),
                                                      "2AB8,%5s"), //aload0, invokespecial #5
            new TrackingTransformer.MethodEnhancement("java/awt/Window",
                                                      "<init>",
                                                      "(Ljava/awt/Frame;)V",
                                                      Arrays.asList(
                                                        "s" + TRACKING_HOOKS,
                                                        "s" + "newAWTWindowCallback",
                                                        "s" + "(Ljava/awt/Window;)V",
                                                        "0C,%1s,%2s",
                                                        "07,%0s",
                                                        "0A,%4s,%3s"
                                                      ),
                                                      "2AB8,%5s"), //aload0, invokespecial #5
            new TrackingTransformer.MethodEnhancement("java/awt/Window",
                                                      "<init>",
                                                      "(Ljava/awt/Window;)V",
                                                      Arrays.asList(
                                                        "s" + TRACKING_HOOKS,
                                                        "s" + "newAWTWindowCallback",
                                                        "s" + "(Ljava/awt/Window;)V",
                                                        "0C,%1s,%2s",
                                                        "07,%0s",
                                                        "0A,%4s,%3s"
                                                      ),
                                                      "2AB8,%5s"), //aload0, invokespecial #5
            new TrackingTransformer.MethodEnhancement("java/awt/Window",
                                                      "<init>",
                                                      "(Ljava/awt/Window;Ljava/awt/GraphicsConfiguration;)V",
                                                      Arrays.asList(
                                                        "s" + TRACKING_HOOKS,
                                                        "s" + "newAWTWindowCallback",
                                                        "s" + "(Ljava/awt/Window;)V",
                                                        "0C,%1s,%2s",
                                                        "07,%0s",
                                                        "0A,%4s,%3s"
                                                      ),
                                                      "2AB8,%5s"), //aload0, invokespecial #5
            new TrackingTransformer.MethodEnhancement("java/lang/ProcessBuilder",
                                                      "start",
                                                      "()Ljava/lang/Process;",
                                                      Arrays.asList(
                                                        "s" + TRACKING_HOOKS,
                                                        "s" + "processBuilderStart",
                                                        "s" + "(Ljava/lang/ProcessBuilder;)V",
                                                        "0C,%1s,%2s",
                                                        "07,%0s",
                                                        "0A,%4s,%3s"
                                                      ),
                                                      "2AB8,%5s") //aload0, invokespecial #5
    );
    //</editor-fold>

    private static Instrumentation instrumentation;

    public static void premain(String arg, Instrumentation i) throws IOException, URISyntaxException {
        instrumentation = i;
        File thisFile = new File(TrackingAgent.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        File hooksFile = new File(thisFile.getParentFile(), "org-netbeans-agent-hooks.jar");
        i.appendToBootstrapClassLoaderSearch(new JarFile(hooksFile));
    }

    public static void install() {
        //TODO: should happen only once
        ClassFileTransformer trackingTransformer = new TrackingTransformer();
        try {
            List<Class<?>> classes2Transform = new ArrayList<>();
            for (String className : toInject.stream().map(me -> me.className.replace('/', '.')).collect(Collectors.toSet())) {
                try {
                    classes2Transform.add(Class.forName(className));
                } catch (ClassNotFoundException ex) {
                    //XXX: warn:
                    System.err.println("cannot instrument:");
                    ex.printStackTrace();
                }
            }
            instrumentation.addTransformer(trackingTransformer, true);
            instrumentation.retransformClasses(classes2Transform.toArray(new Class[0]));
        } catch (UnmodifiableClassException ex) {
            System.err.println("cannot instrument:");
            ex.printStackTrace();
        } finally {
            instrumentation.removeTransformer(trackingTransformer);
        }
    }

    private static class TrackingTransformer implements ClassFileTransformer {

        private static final BiFunction<String, Integer, byte[]> NOOP_INJECTOR = (n, pp) -> null;

        public TrackingTransformer() {
        }

        @Override
        public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
            if (className == null) {
                return classfileBuffer;
            }
            try {
                List<MethodEnhancement> thisClassEnhancements = toInject.stream().filter(me -> {/*System.err.println("className=" + className); */return className.equals(me.className);}).collect(Collectors.toList());
            if (thisClassEnhancements.isEmpty()) {
//                System.err.println("not rewriting: " + className);
                return classfileBuffer;
            }
            List<Edit> injectBytes = new ArrayList<>();
//            System.err.println("transforming: " + className);
            int p = 4 + 2 + 2;
            int cpStart = p;
            int cpEntries = readShort(classfileBuffer, p);
//                System.err.println("cpEntries: " + cpEntries);
            p += 2;
            List<Object> constantPool = new ArrayList<>();
            constantPool.add(null);
            for (int entry = 1; entry < cpEntries; entry++) {
//                System.err.println("entry: " + entry);
                byte tag = classfileBuffer[p++];
//                System.err.println("tag: " + tag);
                switch (tag) {
                    case 1:
                        int size = readShort(classfileBuffer, p);
                        constantPool.add(new String(classfileBuffer, p + 2, size, StandardCharsets.UTF_8));
                        p += 2 + size;
                        break;
                    case 7: case 8: case 16: case 19: case 20:
                        p += 2;
                        constantPool.add(null);
                        break;
                    case 15:
                        p += 3;
                        constantPool.add(null);
                        break;
                    case 3: case 4: case 9: case 10: case 11:
                    case 12: case 17: case 18:
                        p += 4;
                        constantPool.add(null);
                        break;
                    case 5: case 6:
                        p += 8;
                        constantPool.add(null);
                        entry++;
                        constantPool.add(null);
                        break;
                    default:
                        System.err.println("unknown constant pool tag: " + tag);
                        return classfileBuffer;
                }
            }

            int cpEnd = p;
            ByteArrayOutputStreamImpl additionalConstantPool = new ByteArrayOutputStreamImpl();
            int[] cpLastEntry = new int[] {constantPool.size()};

            p += 2; //access flags
            p += 2; //this class
            p += 2; //super class
            int interfacesCount = readShort(classfileBuffer, p); p += 2;
            p += interfacesCount * 2;
            int fields_count = readShort(classfileBuffer, p); p += 2;
            for (int f = 0; f < fields_count; f++) {
                p += 2; //access flags
                p += 2; //name
                p += 2; //descriptor
                p = readAttributes(constantPool, classfileBuffer, p, NOOP_INJECTOR);
            }
            int methods_count = readShort(classfileBuffer, p); p += 2;
            for (int m = 0; m < methods_count; m++) {
                p += 2; //access flags
                int nameIdx = readShort(classfileBuffer, p); p += 2;
                int descriptor = readShort(classfileBuffer, p); p += 2;
                BiFunction<String, Integer, byte[]> injector = (n, pp) -> null;
                Optional<MethodEnhancement> me = thisClassEnhancements.stream().filter(me_ -> constantPool.get(nameIdx).equals(me_.methodName)).filter(me_ -> constantPool.get(descriptor).equals(me_.methodDescriptor)).findAny();
                if (me.isPresent()) {
                    injector = (n, pp) -> {
                        if (!"Code".equals(n)) {
                            return null;
                        }
                        
                        List<Integer> newConstantPoolEntries = new ArrayList<>();

                        for (String cpEntry : me.get().constantPool) {
                            newConstantPoolEntries.add(cpLastEntry[0]++);
                            byte[] data = decodeData(cpEntry, newConstantPoolEntries);
                            additionalConstantPool.write(data);
                        }

                        int maxStack = readShort(classfileBuffer, pp); pp += 2;
                        int maxLocals = readShort(classfileBuffer, pp); pp += 2;
                        int codeLengthStart = pp;
                        int codeLength = readInt(classfileBuffer, pp); pp += 4;
                        byte[] dataToInject = decodeData(me.get().code2Inject, newConstantPoolEntries);
                        int newCodeLength = codeLength + dataToInject.length;
                        injectBytes.add(new Edit(codeLengthStart, 4, new byte[] {
                            (byte) ((newCodeLength >> 24) & 0xFF),
                            (byte) ((newCodeLength >> 16) & 0xFF),
                            (byte) ((newCodeLength >>  8) & 0xFF),
                            (byte) ((newCodeLength >>  0) & 0xFF)
                        }));

                        injectBytes.add(new Edit(pp, 0, dataToInject));
                        pp += codeLength;

                        //TODO: fix exception offsets, StackMapTable offsets, etc.
                        int exceptions = readShort(classfileBuffer, pp); pp += 2;
                        for (int exception = 0; exception < exceptions; exception++) {
                            int start = readShort(classfileBuffer, pp) + dataToInject.length; pp += 2;
                            int end = readShort(classfileBuffer, pp) + dataToInject.length; pp += 2;
                            int handler = readShort(classfileBuffer, pp) + dataToInject.length; pp += 2;
                            pp += 2;
                            injectBytes.add(new Edit(pp - 8, 6, new byte[] {
                                (byte) ((start >>  8) & 0xFF),
                                (byte) ((start >>  0) & 0xFF),
                                (byte) ((end >>  8) & 0xFF),
                                (byte) ((end >>  0) & 0xFF),
                                (byte) ((handler >>  8) & 0xFF),
                                (byte) ((handler >>  0) & 0xFF)
                            }));
                        }
                        pp = readAttributes(constantPool, classfileBuffer, pp, NOOP_INJECTOR);
                        return null;
                    };
                }
                p = readAttributes(constantPool, classfileBuffer, p, injector);
            }
            p = readAttributes(constantPool, classfileBuffer, p, NOOP_INJECTOR);
            injectBytes.add(new Edit(cpStart, 2, new byte[] {(byte) ((cpLastEntry[0] >> 8) & 0xFF), (byte) (cpLastEntry[0] & 0xFF)}));
            injectBytes.add(new Edit(cpEnd, 0, additionalConstantPool.toByteArray()));
            byte[] newBuffer = new byte[classfileBuffer.length + injectBytes.stream().mapToInt(e -> e.newData.length - e.len).sum()];
            int lastCopySource = 0;
            int lastCopyDest = 0;
            Collections.sort(injectBytes, (o1, o2) -> o1.start - o2.start);
            for (Edit edit : injectBytes) {
                int len = edit.start - lastCopySource;
                System.arraycopy(classfileBuffer, lastCopySource, newBuffer, lastCopyDest, len); lastCopySource += len + edit.len; lastCopyDest += len;
                System.arraycopy(edit.newData, 0, newBuffer, lastCopyDest, edit.newData.length); lastCopyDest += edit.newData.length;
            }
            int len = classfileBuffer.length - lastCopySource;
            System.arraycopy(classfileBuffer, lastCopySource, newBuffer, lastCopyDest, len); lastCopySource += len; lastCopyDest += len;
            return newBuffer;
            } catch (Throwable t) {
                t.printStackTrace();
                throw t;
            }
        }

        private int readShort(byte[] classfileBuffer, int p) {
            return (Byte.toUnsignedInt(classfileBuffer[p]) << 8) + Byte.toUnsignedInt(classfileBuffer[p + 1]);
        }

        private int readInt(byte[] classfileBuffer, int p) {
            return (readShort(classfileBuffer, p) << 16) + readShort(classfileBuffer, p + 2);
        }

        private int readAttributes(List<Object> constantPool, byte[] classfileBuffer, int p, BiFunction<String, Integer, byte[]> attributeConvetor) {
            int count = readShort(classfileBuffer, p); p += 2;

            for (int a = 0; a < count; a++) {
                int nameIdx = readShort(classfileBuffer, p); p += 2;
                int len = readInt(classfileBuffer, p); p += 4;
                byte[] newAttribute = attributeConvetor.apply((String) constantPool.get(nameIdx), p);
                p += len;
            }

            return p;
        }

        private byte[] decodeData(String str, List<Integer> cpEntries) {
            byte[] buffer = new byte[2 * str.length()]; //TODO: cache buffer
            int idx = 0;

            for (String element : str.split(",")) {
                if (element.charAt(0) == '%') {
                    int cpEntryIdx = Integer.parseInt(element.substring(1, element.length() - 1));
                    int cpEntry = cpEntries.get(cpEntryIdx);

                    switch (element.charAt(element.length() - 1)) {
                        case 's':
                            buffer[idx++] = (byte) ((cpEntry >> 8) & 0xFF);
                            buffer[idx++] = (byte) ((cpEntry >> 0) & 0xFF);
                            break;
                        case 'i':
                            buffer[idx++] = (byte) ((cpEntry >> 24) & 0xFF);
                            buffer[idx++] = (byte) ((cpEntry >> 16) & 0xFF);
                            buffer[idx++] = (byte) ((cpEntry >>  8) & 0xFF);
                            buffer[idx++] = (byte) ((cpEntry >>  0) & 0xFF);
                            break;
                        default: throw new UnsupportedOperationException();
                    }
                } else if (element.charAt(0) == 's') {
                    buffer[idx++] = 1;
                    byte[] data = element.substring(1).getBytes(StandardCharsets.UTF_8);
                    buffer[idx++] = (byte) ((data.length >> 8) & 0xFF);
                    buffer[idx++] = (byte) ((data.length >> 0) & 0xFF);
                    System.arraycopy(data, 0, buffer, idx, data.length);
                    idx += data.length;
                } else {
                    for (int i = 0; i < element.length(); i += 2) {
                        buffer[idx++] = (byte) Integer.parseInt(element.substring(i, i + 2), 16);
                    }
                }
            }

            return Arrays.copyOf(buffer, idx);
        }

        private static class ByteArrayOutputStreamImpl extends ByteArrayOutputStream {

            public ByteArrayOutputStreamImpl() {
            }

            @Override
            public void write(byte[] b) {
                super.write(b, 0, b.length);
            }
            
        }

        private static final class MethodEnhancement {
            private final String className;
            private final String methodName;
            private final String methodDescriptor;
            private final List<String> constantPool;
            private final String code2Inject;

            public MethodEnhancement(String className, String methodName, String methodDescriptions, List<String> constantPool, String code2Inject) {
                this.className = className;
                this.methodName = methodName;
                this.methodDescriptor = methodDescriptions;
                this.constantPool = constantPool;
                this.code2Inject = code2Inject;
            }
            
        }

        public static final class Edit {
            public final int start;
            public final int len;
            public final byte[] newData;

            public Edit(int start, int len, byte[] newData) {
                this.start = start;
                this.len = len;
                this.newData = newData;
            }

        }
        
    }
}
