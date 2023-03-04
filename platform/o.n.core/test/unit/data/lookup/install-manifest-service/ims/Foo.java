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

package ims;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.URL;
import org.openide.ServiceType;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

public class Foo extends ServiceType {
    private static final long serialVersionUID = 54629387456L;
    public transient ClassLoader loader;
    public transient String loaderToString;
    public transient URL resource;
    public transient String text;
    public Foo() {
        init();
    }
    private void init() {
        loader = Lookup.getDefault().lookup(ClassLoader.class);
        if (loader == null) {
            Thread.dumpStack();
            System.err.println("Lookup=" + Lookup.getDefault());
        }
        loaderToString = loader != null ? loader.toString() : null;
        resource = loader != null ? loader.getResource("ims/Bundle.properties") : null;
        text = NbBundle.getMessage(Foo.class, "foo");
        if (loader == null) throw new NullPointerException("no classloader");
        if (resource == null) throw new NullPointerException("no ims/Bundle.properties from " + loaderToString);
        System.err.println("loader=" + loaderToString + " resource=" + resource + " text=" + text);
    }
    public String getName() {
        return "foo";
    }
    public HelpCtx getHelpCtx() {
        return null;
    }
    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        ois.defaultReadObject();
        //System.err.println("readObject");
        //Thread.dumpStack();
        init();
    }
}
