/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
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
