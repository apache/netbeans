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

import java.awt.Window;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public class TrackingAgent {

    private static final String TRACKING_HOOKS = "org/netbeans/agent/hooks/TrackingHooks";

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
                                                        "s" + "newFileOutputStream",
                                                        "s" + "(Ljava/io/FileOutputStream;Ljava/io/File;)V",
                                                        "0C,%1s,%2s",
                                                        "07,%0s",
                                                        "0A,%4s,%3s"
                                                      ),
                                                      "2A2BB8,%5s"), //aload0, aload1, invokespecial #5
            new TrackingTransformer.MethodEnhancement("java/io/FileOutputStream",
                                                      "close",
                                                      "()V",
                                                      Arrays.asList(
                                                        "s" + TRACKING_HOOKS,
                                                        "s" + "fileOutputStreamClose",
                                                        "s" + "(Ljava/io/FileOutputStream;)V",
                                                        "0C,%1s,%2s",
                                                        "07,%0s",
                                                        "0A,%4s,%3s"
                                                      ),
                                                      "2AB8,%5s"), //aload0, invokespecial #5
            new TrackingTransformer.MethodEnhancement("java/awt/Window", //TODO: all constructors!
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
                                                      "2AB8,%5s") //aload0, invokespecial #5
    );

    private static Instrumentation instrumentation;

    public static void premain(String arg, Instrumentation i) {
        instrumentation = i;
    }

    public static void install() {
        ClassFileTransformer trackingTransformer = new TrackingTransformer();
        try {
            instrumentation.addTransformer(trackingTransformer, true);
            instrumentation.retransformClasses(System.class, Runtime.class, FileOutputStream.class, Files.class, File.class, Window.class);
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
            try {
                List<MethodEnhancement> thisClassEnhancements = toInject.stream().filter(me -> {/*System.err.println("me=" + me); */return className.equals(me.className);}).collect(Collectors.toList());
            if (thisClassEnhancements.isEmpty()) {
                System.err.println("not rewriting: " + className);
                return classfileBuffer;
            }
            List<Edit> injectBytes = new ArrayList<>();
            System.err.println("transforming: " + className);
            int p = 4 + 2 + 2;
            int cpStart = p;
            int cpEntries = readShort(classfileBuffer, p);
                System.err.println("cpEntries: " + cpEntries);
            p += 2;
            List<Object> constantPool = new ArrayList<>();
            constantPool.add(null);
            for (int entry = 1; entry < cpEntries; entry++) {
                System.err.println("entry: " + entry);
                byte tag = classfileBuffer[p++];
                System.err.println("tag: " + tag);
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
                Optional<MethodEnhancement> me = thisClassEnhancements.stream().filter(me_ -> constantPool.get(nameIdx).equals(me_.methodName)).filter(me_ -> constantPool.get(descriptor).equals(me_.methodDescriptions)).findAny();
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
                        pp += 8 * exceptions;
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
            private final String methodDescriptions;
            private final List<String> constantPool;
            private final String code2Inject;

            public MethodEnhancement(String className, String methodName, String methodDescriptions, List<String> constantPool, String code2Inject) {
                this.className = className;
                this.methodName = methodName;
                this.methodDescriptions = methodDescriptions;
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
