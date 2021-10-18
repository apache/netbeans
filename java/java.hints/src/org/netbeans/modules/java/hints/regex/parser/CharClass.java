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
package org.netbeans.modules.java.hints.regex.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

/**
 *
 * @author sandeemi
 */
public class CharClass extends RegEx{
    private final boolean isNegation;
    private final boolean isIntersection;
    private final List<RegEx> charClassList;
    private final List<Character> allChars;
    private final List<Character> negationChars;
    
    public CharClass(boolean isNegation, boolean isIntersection){
        this.isNegation = isNegation;
        this.isIntersection = isIntersection;
        this.charClassList = new ArrayList<>();
        allChars = new ArrayList<>();
        negationChars = new ArrayList<>();
        for(int c=32; c<=126; c++){
            negationChars.add((char)c);
        }
    }
    
    public void addToClass(RegEx... nextChar){
        for(RegEx next : nextChar) {
            this.charClassList.add(next);
            if(next instanceof Primitive){
                allChars.add((Character)((Primitive) next).getCh());
                negationChars.remove((Character)((Primitive) next).getCh());
            }else if(next instanceof Range){
                char from = ((Range) next).getFrom();
                char to = ((Range) next).getTo();
                for(char c=from; c<=to; c++){
                    allChars.add((Character)c);
                    negationChars.remove((Character)c);
                }
            }else if(next instanceof CharClass){
                CharClass charClass = (CharClass)next;
                if(charClass.isIntersection){
                    handleIntersection(charClass);
                }else{
                    handleUnion(charClass);
                }
            }
        }
    } 

    private void handleUnion(CharClass charClass) {
        List<Character> allCharInt;
        if(charClass.isNegation){
            allCharInt = charClass.getNegationList();
        }else{
            allCharInt = charClass.getAllChar();
        }
        ListIterator<Character> listIterator = allCharInt.listIterator();
        while(listIterator.hasNext()){
            Character c = listIterator.next();
            if(!allChars.contains(c)){
                allChars.add(c);
                negationChars.remove(c);
            }
        }
    }

    private void handleIntersection(CharClass charClass) {
        
        List<Character> allCharInt;
        if(charClass.isNegation){
            allCharInt = charClass.getNegationList();
        }else{
            allCharInt = charClass.getAllChar();
        }
        ListIterator<Character> listIterator = allChars.listIterator();
        while(listIterator.hasNext()){
            Character c = listIterator.next();
            if(!allCharInt.contains(c)){
                listIterator.remove();
                negationChars.add(c);
            }
        }
    }
    
    public boolean isNegation() {
        return isNegation;
    }

    public List<RegEx> getCharClassList() {
        return charClassList;
    }
    
    public List<Character> getAllChar() {
        return allChars;
    }

    public List<Character> getNegationList() {
        return negationChars;
    }
}
