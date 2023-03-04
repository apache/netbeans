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
package org.netbeans.modules.intent;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.netbeans.api.intent.Intent;
import org.netbeans.spi.intent.Result;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;

/**
 *
 * @author jhavlin
 */
public class IntentHandler implements Comparable<IntentHandler> {

    public static final RequestProcessor RP = new RequestProcessor(
            IntentHandler.class);

    private static final Result IGNORING_RESULT = new Result() {

        @Override
        public void setResult(Object result) {
        }

        @Override
        public void setException(Exception exception) {
        }
    };

    @Override
    public int compareTo(IntentHandler o) {
        return this.getPosition() - o.getPosition();
    }

    private enum Type {
        RETURN, SETBACK
    }

    private final String className;
    private final String methodName;
    private final String displayName;
    private final String icon;
    private final Type type;
    private final int position;

    public static IntentHandler create(FileObject fo) {
        String n = fo.getName();
        int lastDash = n.lastIndexOf('-');
        if (lastDash <= 0 || lastDash + 1 >= n.length()) {
            throw new IllegalArgumentException("Invalid handler file"); //NOI18N
        }
        String className = n.substring(0, lastDash).replace('-', '.');
        String methodName = n.substring(lastDash + 1);
        String displayName = (String) fo.getAttribute("displayName");   //NOI18N
        String icon = (String) fo.getAttribute("icon");                 //NOI18N
        int position = (Integer) fo.getAttribute("position");           //NOI18N
        Type type = Type.valueOf((String) fo.getAttribute("type"));     //NOI18N

        return new IntentHandler(className, methodName, displayName, icon,
                type, position);
    }

    private IntentHandler(String className, String methodName,
            String displayName, String icon, Type type, int position) {

        this.className = className;
        this.methodName = methodName;
        this.displayName = displayName;
        this.icon = icon;
        this.type = type;
        this.position = position;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getIcon() {
        return icon;
    }

    public int getPosition() {
        return position;
    }

    public boolean isSetBack() {
        return type == Type.SETBACK;
    }

    public void handle(Intent intent, Result resultOrNull) {

        Result result = resultOrNull == null ? IGNORING_RESULT : resultOrNull;

        ClassLoader cls = Lookup.getDefault().lookup(ClassLoader.class);
        if (cls == null) {
            throw new IllegalStateException("Classloader not found");   //NOI18N
        } else {
            try {
                Class<?> c = Class.forName(className, true, cls);
                if (isSetBack()) {
                    Method m = c.getDeclaredMethod(methodName, Intent.class,
                            Result.class);
                    m.invoke(null, intent, result);
                } else {
                    Method m = c.getDeclaredMethod(methodName, Intent.class);
                    Object res = m.invoke(null, intent);
                    result.setResult(res);
                }
            } catch (InvocationTargetException e) {
                Throwable cause = e.getCause();
                if (cause instanceof Exception) {
                    result.setException((Exception) cause);
                } else {
                    result.setException(new Exception(cause));
                }
            } catch (ReflectiveOperationException e) {
                result.setException(e);
            }
        }
    }
}
