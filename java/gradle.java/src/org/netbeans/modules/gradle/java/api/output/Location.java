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

package org.netbeans.modules.gradle.java.api.output;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Laszlo Kishalmi
 */
public final class Location {

    final String fileName;
    final String target;
    private Integer lineNum = null;

    public Location(String fileName, String target) {
        this.fileName = fileName;
        this.target = target;
        try {
            lineNum = Integer.parseInt(target);
        } catch (NumberFormatException ex) {
        }
    }

    public Location(String loc) {
        int i = loc != null ? loc.indexOf(':') : 0;
        if ((i > 0) && (loc != null)) {
            fileName = loc.substring(0, i);
            target = loc.substring(i + 1);
        } else {
            fileName = loc;
            target = null;
        }
        try {
            lineNum = Integer.parseInt(target);
        } catch (NumberFormatException ex) {
        }
    }

    public String getFileName() {
        return fileName;
    }

    public String getTarget() {
        return target;
    }

    public Integer getLineNum() {
        return lineNum;
    }

    public boolean isLine() {
        return lineNum != null;
    }

    public boolean isMethod() {
        return (target != null) && (lineNum == null);
    }

    @Override
    public String toString() {
        return target != null ? fileName + ":" + target : fileName;
    }

    private static final Pattern CALLSTACK_ITEM_PARSER = Pattern.compile("(.*)at (\\w[\\w\\.\\$<>]*)\\.(\\w+)\\((\\w+)\\.java\\:([0-9]+)\\)");
    
    public static final Location locationFromCallStackItem(String item) {
        Matcher m = CALLSTACK_ITEM_PARSER.matcher(item);
        if (m.matches()) {
            StringBuilder ret = new StringBuilder(item.length());
            String className = m.group(2);
            String methodName = m.group(3);
            String fileNameBase = m.group(4);
            String line = m.group(5);
            int lastDot = className.lastIndexOf('.');
            String pkg = lastDot > 0 ? className.substring(0, lastDot) : "";
            if (fileNameBase != null) {
                ret.append(pkg.replace('.', '/')).append('/').append(fileNameBase);
            } else {
                ret.append(className.replace('.', '/'));
            }
            ret.append(".java");
            return new Location(ret.toString(), line != null ? line : methodName);
        } else {
            return null;
        }
    }

    public interface Finder {
        FileObject findFileObject(Location loc);
    }
}
