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

package org.netbeans.installer.utils.helper;

/**
 *
 * @author Danila_Dugurov
 */
public class Pair<F, S> {
   
   private F first;
   private S second;
   
   public Pair(F first, S second) {
      this.first = first;
      this.second = second;
   }
   
   public static <F, S> Pair<F,S> create (F first, S second) {
      return new Pair<F,S>(first, second);
   }
   
   public F getFirst() {
      return first;
   }
   
   public S getSecond() {
      return second;
   }
   
   public String toString() {
      return "(" + first + "," + second + ")";
   }
   
   public boolean equals(Object other) {
      if (other == this) return true;
      if (other == null) return false;
      if (other instanceof Pair) {
         Pair pair = (Pair) other;
         if (first != null ? first.equals(pair.first): pair.first == null)
            return second != null ? second.equals(pair.second): pair.second == null;
      }
      return false;
   }
   
   public int hashCode() {
      int result;
      result = (first != null ? first.hashCode() : 0);
      result = 29 * result + (second != null ? second.hashCode() : 0);
      return result;
   }
}
