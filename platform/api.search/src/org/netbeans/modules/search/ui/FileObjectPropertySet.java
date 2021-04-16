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
package org.netbeans.modules.search.ui;

import java.lang.reflect.InvocationTargetException;
import java.util.Date;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
import org.openide.nodes.Node.Property;

/**
 *
 * @author jhavlin
 */
public class FileObjectPropertySet extends Node.PropertySet {

    private final FileObject fileObject;
    private final Property<?>[] properties;

    public FileObjectPropertySet(FileObject fileObject) {
        this.fileObject = fileObject;
        properties = new Property<?>[] {
            new PathProperty(), new SizeProperty(), new LastModifiedProperty()
        };
    }

    @Override
    public Property<?>[] getProperties() {
        return properties;
    }

    private class SizeProperty extends Property<Long> {

        public SizeProperty() {
            super(Long.class);
        }

        @Override
        public boolean canRead() {
            return true;
        }

        @Override
        public Long getValue() throws IllegalAccessException,
        InvocationTargetException {
            return fileObject.getSize();
        }

        @Override
        public boolean canWrite() {
            return false;
        }

        @Override
        public void setValue(Long val) throws IllegalAccessException,
                IllegalArgumentException, InvocationTargetException {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getName() {
            return "size";                                              //NOI18N
        }
    }

    private class LastModifiedProperty extends Property<Date> {

        public LastModifiedProperty() {
            super(Date.class);
        }

        @Override
        public boolean canRead() {
            return true;
        }

        @Override
        public Date getValue() throws IllegalAccessException,
                InvocationTargetException {
            return fileObject.lastModified();
        }

        @Override
        public boolean canWrite() {
            return false;
        }

        @Override
        public void setValue(Date val) throws IllegalAccessException,
                IllegalArgumentException, InvocationTargetException {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getName() {
            return "lastModified";                                      //NOI18N
        }
    }

    private class PathProperty extends Property<String> {

        public PathProperty() {
            super(String.class);
        }

        @Override
        public boolean canRead() {
            return true;
        }

        @Override
        public String getValue() throws IllegalAccessException,
                InvocationTargetException {
            return fileObject.getPath();
        }

        @Override
        public boolean canWrite() {
            return false;
        }

        @Override
        public void setValue(String val) throws IllegalAccessException,
                IllegalArgumentException, InvocationTargetException {
            throw new UnsupportedOperationException();                  //NOI18N
        }

        @Override
        public String getName() {
            return "path";                                              //NOI18N
        }
    }
}
