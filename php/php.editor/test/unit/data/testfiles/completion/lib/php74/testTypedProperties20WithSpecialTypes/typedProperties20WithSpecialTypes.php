<?php

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

namespace TypedProperties1;

class TypedPropertiesParent
{
    protected object $parentField;
    public function parentMethod() {
    }
}

class TypedPropertiesChild extends TypedPropertiesParent
{
    use TypedPropertiesTrait;
    private ?self $nullableSelf;
    private ?parent $nullableParent;

    public function childMethodSelf(self $param): ?self {
        $param->testMethod(); // self
        return new TypedPropertiesChild();
    }

    public function childMethodParent(?parent $param): parent {
        $param->testMethod(); // parent
        return new TypedPropertiesChild();
    }

    public function testMethod() {
        $this->nullableSelf->childMethodSelf($this);
        $this->nullableParent->parentMethod();
        $this->childMethodSelf($this)->childMethodSelf($this);
        $this->childMethodParent(null)->parentMethod();
        $this->traitMethod($this)->parentMethod();
    }
}

trait TypedPropertiesTrait
{
    public function traitMethod(self $param): ?parent {
        return null;
    }
}
