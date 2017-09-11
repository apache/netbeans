/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.j2ee.persistence.api.metadata.orm;

public interface Attributes {

    public void setId(int index, Id value);

    public Id getId(int index);
    
    public int sizeId();
    
    public void setId(Id[] value);
    
    public Id[] getId();
    
    public int addId(Id value);
    
    public int removeId(Id value);
    
    public Id newId();
    
    public void setEmbeddedId(EmbeddedId value);
    
    public EmbeddedId getEmbeddedId();
    
    public EmbeddedId newEmbeddedId();
    
    public void setBasic(int index, Basic value);
    
    public Basic getBasic(int index);
    
    public int sizeBasic();
    
    public void setBasic(Basic[] value);
    
    public Basic[] getBasic();
    
    public int addBasic(Basic value);
    
    public int removeBasic(Basic value);
    
    public Basic newBasic();
    
    public void setVersion(int index, Version value);
    
    public Version getVersion(int index);
    
    public int sizeVersion();
    
    public void setVersion(Version[] value);
    
    public Version[] getVersion();
    
    public int addVersion(Version value);
    
    public int removeVersion(Version value);
    
    public Version newVersion();
    
    public void setManyToOne(int index, ManyToOne value);
    
    public ManyToOne getManyToOne(int index);
    
    public int sizeManyToOne();
    
    public void setManyToOne(ManyToOne[] value);
    
    public ManyToOne[] getManyToOne();
    
    public int addManyToOne(ManyToOne value);
    
    public int removeManyToOne(ManyToOne value);
    
    public ManyToOne newManyToOne();
    
    public void setOneToMany(int index, OneToMany value);
    
    public OneToMany getOneToMany(int index);
    
    public int sizeOneToMany();
    
    public void setOneToMany(OneToMany[] value);
    
    public OneToMany[] getOneToMany();
    
    public int addOneToMany(OneToMany value);
    
    public int removeOneToMany(OneToMany value);
    
    public OneToMany newOneToMany();
    
    public void setOneToOne(int index, OneToOne value);
    
    public OneToOne getOneToOne(int index);
    
    public int sizeOneToOne();
    
    public void setOneToOne(OneToOne[] value);
    
    public OneToOne[] getOneToOne();
    
    public int addOneToOne(OneToOne value);
    
    public int removeOneToOne(OneToOne value);
    
    public OneToOne newOneToOne();
    
    public void setManyToMany(int index, ManyToMany value);
    
    public ManyToMany getManyToMany(int index);
    
    public int sizeManyToMany();
    
    public void setManyToMany(ManyToMany[] value);
    
    public ManyToMany[] getManyToMany();
    
    public int addManyToMany(ManyToMany value);
    
    public int removeManyToMany(ManyToMany value);
    
    public ManyToMany newManyToMany();
    
    public void setEmbedded(int index, Embedded value);
    
    public Embedded getEmbedded(int index);
    
    public int sizeEmbedded();
    
    public void setEmbedded(Embedded[] value);
    
    public Embedded[] getEmbedded();
    
    public int addEmbedded(Embedded value);
    
    public int removeEmbedded(Embedded value);
    
    public Embedded newEmbedded();
    
    public void setTransient(int index, Transient value);
    
    public Transient getTransient(int index);
    
    public int sizeTransient();
    
    public void setTransient(Transient[] value);
    
    public Transient[] getTransient();
    
    public int addTransient(Transient value);
    
    public int removeTransient(Transient value);
    
    public Transient newTransient();
    
}
