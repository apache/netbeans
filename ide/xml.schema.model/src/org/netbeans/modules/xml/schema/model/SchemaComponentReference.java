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

package org.netbeans.modules.xml.schema.model;

import java.util.*;

/**
 *
 * @author  Todd Fast, todd.fast@sun.com
 */
public class SchemaComponentReference<T extends SchemaComponent> extends Object
{
    /**
     *
     *
     */
    private SchemaComponentReference(T component)
    {
        super();

		if (component==null)
		{
			throw new IllegalArgumentException(
				"Parameter \"component\" cannot be null");
		}

		this.component=component;
    }


	/**
	 *
	 *
	 */
	public boolean equals(Object other)
	{
		if (!(other instanceof SchemaComponentReference))
			return false;

		return this.get()==((SchemaComponentReference)other).get();
	}


	/**
	 *
	 *
	 */
	public int hashCode()
	{
		// TODO: What's a reasonable hash algorithm here to avoid collision
		// with the hashCode of the referenced object?  We want the reference's
		// hashcode to be based on the referent, but different.
		return get().hashCode();
	}


	/**
	 *
	 *
	 */
	public String toString()
	{
		return getClass().getName()+"<"+get().getClass().getName()+">";
	}

	
	/**
	 *
	 *
	 */
	public T get()
	{
		return component;
	}


	/**
	 *
	 *
	 */
	public static <C extends SchemaComponent> 
		SchemaComponentReference<C> create(C component)
	{
		SchemaComponentReference<C> reference=
			new SchemaComponentReference<C>(component);

		return reference;
	}




	////////////////////////////////////////////////////////////////////////////
	// Instance members
	////////////////////////////////////////////////////////////////////////////

	private T component;
}
