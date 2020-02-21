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
package org.netbeans.modules.cnd.gotodeclaration.type;

import javax.swing.Icon;
import org.netbeans.spi.jumpto.type.TypeDescriptor;
import org.openide.filesystems.FileObject;

/**
 * A wrapper used for tracin
 */
/* package-local */
class TracingTypeDescriptor extends TypeDescriptor {
    
    private TypeDescriptor delegate;
    private String name;

    TracingTypeDescriptor(TypeDescriptor delegate) {
	this.delegate = delegate;
	name = delegate.getSimpleName();
    }
	    
    @Override
    public String getContextName() {
	System.err.printf("TypeDescriptor.getContextName(%s)\n", name);
	return delegate.getContextName();
    }

    @Override
    public FileObject getFileObject() {
	System.err.printf("TypeDescriptor.getFileObject(%s)\n", name);
	return delegate.getFileObject();
    }

    @Override
    public Icon getIcon() {
	System.err.printf("TypeDescriptor.getIcon(%s)\n", name);
	return delegate.getIcon();
    }

    @Override
    public int getOffset() {
	System.err.printf("TypeDescriptor.getOffset(%s)\n", name);
	return delegate.getOffset();
    }

    @Override
    public String getOuterName() {
	System.err.printf("TypeDescriptor.getOuterName(%s)\n", name);
	return delegate.getOuterName();
    }

    @Override
    public Icon getProjectIcon() {
	System.err.printf("TypeDescriptor.getProjectIcon(%s)\n", name);
	return delegate.getProjectIcon();
    }

    @Override
    public String getProjectName() {
	System.err.printf("TypeDescriptor.getProjectName(%s)\n", name);
	return delegate.getProjectName();
    }

    @Override
    public String getSimpleName() {
	System.err.printf("TypeDescriptor.getSimpleName(%s)\n", name);
	return delegate.getSimpleName();
    }

    @Override
    public String getTypeName() {
	System.err.printf("TypeDescriptor.getTypeName(%s)\n", name);
	return delegate.getTypeName();
    }

    @Override
    public void open() {
	System.err.printf("TypeDescriptor.open(%s)\n", name);
	delegate.open();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final TracingTypeDescriptor other = (TracingTypeDescriptor) obj;
        if (this.delegate != other.delegate && (this.delegate == null || !this.delegate.equals(other.delegate))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 73 * hash + (this.delegate != null ? this.delegate.hashCode() : 0);
        return hash;
    }
    
}
