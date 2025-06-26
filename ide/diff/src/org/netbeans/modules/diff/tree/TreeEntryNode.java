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

package org.netbeans.modules.diff.tree;

import java.beans.IntrospectionException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import org.openide.util.lookup.ProxyLookup;

public class TreeEntryNode extends AbstractNode {
    public static TreeEntryNode create(TreeEntry bean) {
        try {
            List<Lookup> proxyLookup = new ArrayList<>(2);
            if(bean.getFile2() != null) {
                proxyLookup.add(bean.getFile2().getLookup());
            }
            return new TreeEntryNode(bean, new ProxyLookup(proxyLookup.toArray(Lookup[]::new)));
        } catch (IntrospectionException ex) {
            Exceptions.printStackTrace(ex);
            return null;
        }
    }

    private final TreeEntry te;

    private TreeEntryNode(TreeEntry bean, Lookup lkp) throws IntrospectionException {
        super(Children.LEAF, lkp);
        this.te = bean;
        setDisplayName(bean.getName());
        setName(bean.getRelativePath());
    }

    @Override
    protected Sheet createSheet() {
        Sheet sheet = super.createSheet();
        Sheet.Set props = sheet.get(Sheet.PROPERTIES);
        if (props == null) {
            props = Sheet.createPropertiesSet();
            sheet.put(props);
        }
        props.put(new PropertySupport.Name(this));
        props.put(new Filename());
        props.put(new RelativeName());
        props.put(new IsIdentical());
        props.put(new Path1());
        props.put(new Path2());
        props.put(new Modtime1());
        props.put(new Modtime2());
        return sheet;
    }

    @Messages({"PROP_filename=Filename"})
    private class Filename extends PropertySupport.ReadOnly<String> {

        public Filename() {
            super("filename", String.class, Bundle.PROP_filename(), "");
        }

        @Override
        public String getValue() {
            return te.getName();
        }
    }

    @Messages({"PROP_relativePath=Relative path"})
    private class RelativeName extends PropertySupport.ReadOnly<String> {

        public RelativeName() {
            super("relativePath", String.class, Bundle.PROP_relativePath(), "");
        }

        @Override
        public String getValue() {
            return te.getRelativePath();
        }
    }

    @Messages({"PROP_identical=Files are identical"})
    private class IsIdentical extends PropertySupport.ReadOnly<Boolean> {

        public IsIdentical() {
            super("identical", Boolean.class, Bundle.PROP_identical(), "");
        }

        @Override
        public Boolean getValue() {
            return te.isFilesIdentical();
        }
    }

    @Messages({"PROP_modtime1=Modification time 1"})
    private class Modtime1 extends PropertySupport.ReadOnly<Date> {

        public Modtime1() {
            super("modtime1", Date.class, Bundle.PROP_modtime1(), "");
        }

        @Override
        public Date getValue() {
            return te.getFile1() != null ? te.getFile1().lastModified() : null;
        }
    }

    @Messages({"PROP_modtime2=Modification time 2"})
    private class Modtime2 extends PropertySupport.ReadOnly<Date> {

        public Modtime2() {
            super("modtime2", Date.class, Bundle.PROP_modtime2(), "");
        }

        @Override
        public Date getValue() {
            return te.getFile2() != null ? te.getFile2().lastModified() : null;
        }
    }

    @Messages({"PROP_path1=Pfad 1"})
    private class Path1 extends PropertySupport.ReadOnly<String> {

        public Path1() {
            super("path1", String.class, Bundle.PROP_path1(), "");
        }

        @Override
        public String getValue() {
            return te.getFile1() != null ? te.getFile1().getPath() : null;
        }
    }

    @Messages({"PROP_path2=Pfad 2"})
    private class Path2 extends PropertySupport.ReadOnly<String> {

        public Path2() {
            super("path2", String.class, Bundle.PROP_path2(), "");
        }

        @Override
        public String getValue() {
            return te.getFile2() != null ? te.getFile2().getPath() : null;
        }
    }
}
