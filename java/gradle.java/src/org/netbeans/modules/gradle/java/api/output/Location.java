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
package org.netbeans.modules.gradle.java.api.output;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.openide.filesystems.FileObject;

/**
 * This class represents a location in a Java file or Class which usually
 * presented in Sting form as {@code a/b/c/ClassName$SubClas1$SubClass2.java:123} or
 * {@code a/b/c/ClassName$SubClas1$SubClass2.java:methodName()}
 *
 * @author Laszlo Kishalmi
 */
public final class Location {

    final String fileName;
    final String target;
    final String[] classNames;
    private Integer lineNum = null;

    /**
     * Parses the given string in the format {@code a/b/c/ClassName$SubClas1$SubClass2.java:123} or
     * {@code a/b/c/ClassName$SubClas1$SubClass2.java:methodName()} to a Location
     *
     * @param loc the location String
     * @return the Location object represented by the location String
     * @since 1.17
     */
    public static Location parseLocation(String loc) {
        assert loc != null;

        // example MyFile$NestedClass.java:123
        // or // example MyFile$NestedClass.java:getMethod()
        int targetDelimiterIndex = loc.lastIndexOf(':');
        if (targetDelimiterIndex == -1) {
            targetDelimiterIndex = loc.length();
        }
        // the last dot will be before the .java extension
        // unless there's no extension and this may be before the classname
        // but those cases not supported, really
        int extensionIndx = loc.lastIndexOf('.', targetDelimiterIndex);
        if (extensionIndx == -1) {
            extensionIndx = targetDelimiterIndex;
        }
        // is the dot right before the File's name (after the package's name)
        int packageSlashIndx = loc.lastIndexOf('/', extensionIndx - 1);

        String[] classNames = loc.substring(packageSlashIndx + 1, extensionIndx).split("\\$");
        String ext = loc.substring(extensionIndx, targetDelimiterIndex);
        String fileName = loc.substring(0, packageSlashIndx + 1) + classNames[0] + ext;

        String target;
        if (targetDelimiterIndex < loc.length() - 1) {
            target = loc.substring(targetDelimiterIndex + 1);
        } else {
            target = null;
        }

        return new Location(fileName, classNames, target);
    }

    /**
     * Parses and creates a location item out of a string.
     * @param loc the string representation of the location
     * @deprecated in favor of {@linkplain  #parseLocation(java.lang.String)}
     */
    @Deprecated
    public Location(String loc) {
        Location l = parseLocation(loc);
        this.fileName = l.fileName;
        this.classNames = l.classNames;
        this.target = l.target;
        this.lineNum = l.lineNum;
    }

    /**
     * Parses and creates a location item out of a string as a file name and a
     * target which can be either a method name or a line number.
     * @param fileName the file name part of the location
     * @param target the line number or method name.
     *
     * @deprecated in favor of {@linkplain  #parseLocation(java.lang.String)}
     */
    @Deprecated
    public Location(String fileName, String target) {
        Location loc = parseLocation(fileName + " : " + target);
        this.fileName = loc.fileName;
        this.classNames = loc.classNames;
        this.target = loc.target;
        this.lineNum = loc.lineNum;
    }

    private Location(String fileName, String[] classNames, String target) {
        this.fileName = fileName;
        this.target = target;
        this.classNames = classNames;
        try {
            lineNum = Integer.parseInt(target);
        } catch (NumberFormatException ex) {
        }
    }

    /**
     * Returns a new location instance without the target(line number or method name)
     * of this location.
     *
     * @return a Location without target information.
     * @since 1.17
     */
    public Location withNoTarget() {
        return new Location(fileName, classNames, null);
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

    /**
     * Get the classes represented in this Location, the outmost and then the
     * inner nested classes as well.
     *
     * @return the class name nesting hierarchy
     * @since 1.17
     */
    String[] getClassNames() {
        return classNames;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(fileName.substring(0, fileName.indexOf(classNames[0])));
        for (int i = 0; i < classNames.length; i++) {
            String className = classNames[i];
            sb.append(className);
            sb.append("$");
        }
        sb.setLength(sb.length() - 1);
        sb.append(fileName.substring(
                fileName.indexOf(classNames[0]) + classNames[0].length()));
        if (target != null) {
            sb.append(":");
            sb.append(target);
        }
        return sb.toString();
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
            return Location.parseLocation(ret.toString() + ":" + line != null ? line : methodName);
        } else {
            return null;
        }
    }

    public interface Finder {
        FileObject findFileObject(Location loc);
    }
}
