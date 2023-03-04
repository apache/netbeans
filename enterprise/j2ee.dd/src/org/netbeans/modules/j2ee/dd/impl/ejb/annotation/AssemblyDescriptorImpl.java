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

package org.netbeans.modules.j2ee.dd.impl.ejb.annotation;

import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.OutputStream;
import org.netbeans.modules.j2ee.dd.api.common.MessageDestination;
import org.netbeans.modules.j2ee.dd.api.common.SecurityRole;
import org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException;
import org.netbeans.modules.j2ee.dd.api.ejb.AssemblyDescriptor;
import org.netbeans.modules.j2ee.dd.api.ejb.ContainerTransaction;
import org.netbeans.modules.j2ee.dd.api.ejb.ExcludeList;
import org.netbeans.modules.j2ee.dd.api.ejb.MethodPermission;
import org.netbeans.modules.j2ee.dd.impl.common.annotation.CommonAnnotationHelper;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationModelHelper;

public class AssemblyDescriptorImpl implements AssemblyDescriptor {

    private final AnnotationModelHelper helper;
    
    public AssemblyDescriptorImpl(AnnotationModelHelper helper) {
        this.helper = helper;
    }
    
    public SecurityRole[] getSecurityRole() {
        return CommonAnnotationHelper.getSecurityRoles(helper);
    }
    
    public MessageDestination[] getMessageDestination() throws VersionNotSupportedException {
        return new MessageDestination[0];
    }

    // <editor-fold defaultstate="collapsed" desc="Not implemented methods">

    public Object clone() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public ContainerTransaction[] getContainerTransaction() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public ContainerTransaction getContainerTransaction(int index) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setContainerTransaction(ContainerTransaction[] value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setContainerTransaction(int index, ContainerTransaction value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int sizeContainerTransaction() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int addContainerTransaction(ContainerTransaction value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int removeContainerTransaction(ContainerTransaction value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public ContainerTransaction newContainerTransaction() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public MethodPermission[] getMethodPermission() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public MethodPermission getMethodPermission(int index) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setMethodPermission(MethodPermission[] value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setMethodPermission(int index, MethodPermission value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int addMethodPermission(MethodPermission value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int sizeMethodPermission() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int removeMethodPermission(MethodPermission value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public MethodPermission newMethodPermission() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public SecurityRole getSecurityRole(int index) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setSecurityRole(SecurityRole[] value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setSecurityRole(int index, SecurityRole value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int sizeSecurityRole() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int removeSecurityRole(SecurityRole value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int addSecurityRole(SecurityRole value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public SecurityRole newSecurityRole() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setExcludeList(ExcludeList value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public ExcludeList getExcludeList() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public ExcludeList newExcludeList() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public MessageDestination getMessageDestination(int index) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setMessageDestination(MessageDestination[] value) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setMessageDestination(int index, MessageDestination value) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int sizeMessageDestination() throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int removeMessageDestination(MessageDestination value) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int addMessageDestination(MessageDestination value) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public MessageDestination newMessageDestination() throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void addPropertyChangeListener(PropertyChangeListener pcl) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void removePropertyChangeListener(PropertyChangeListener pcl) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setId(String value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getId() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Object getValue(String propertyName) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void write(OutputStream os) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    // </editor-fold>

} 

