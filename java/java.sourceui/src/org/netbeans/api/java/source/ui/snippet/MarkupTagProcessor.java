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
package org.netbeans.api.java.source.ui.snippet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author aksinsin
 */
public class MarkupTagProcessor {
    
    private static final List<String> SUPPORTED_SNIPPET_MARKUP_TAGS = Arrays.asList("highlight", "replace", "link", "start");

    public ProcessedTags process(List<SourceLineMeta> parseResult ){
        Map<Integer, List<ApplicableMarkupTag>> markUpTagOnLine = new TreeMap<>();
        Map<Integer, List<Region>> regionTagOnLine = new TreeMap<>();
        List<String> errorList = new ArrayList<>();
        List<Region> regionList = new ArrayList<>();
        
        int thisLine = 1;
        int nextLine = 1;
        
        main:
        for(SourceLineMeta fullLineInfo : parseResult){
            nextLine++;
            if (!regionList.isEmpty()) {
                List<Region> newRegionList = new ArrayList<>(regionList);
                regionTagOnLine.put(thisLine, newRegionList);
                addMarkupTags(thisLine, transformRegionAttributeToMarkupTag(newRegionList), markUpTagOnLine);
            }
            //checkng no attribute on this line
            if (!fullLineInfo.getThisLineMarkUpTags().isEmpty()) {
                for (MarkupTag markUpTag : fullLineInfo.getThisLineMarkUpTags()) {
                    if (SUPPORTED_SNIPPET_MARKUP_TAGS.contains(markUpTag.getTagName())) {

                        Map<String, String> markupAttribute = new HashMap<>();
                        boolean isSubStringOrRegexArrive = false;
                        //remove duplicate attributes and
                        //if markup tag contains attributes regex and substring simultaneously then take first attribute and discard remaining
                        for (MarkupTagAttribute markUpTagAttribute : markUpTag.getMarkUpTagAttributes()) {
                            if (isSubStringOrRegexArrive
                                    && (markUpTagAttribute.getName().equals("substring")
                                    || markUpTagAttribute.getName().equals("regex"))) {
                                continue;
                            }
                            markupAttribute.putIfAbsent(markUpTagAttribute.getName(), markUpTagAttribute.getValue());
                            if (!isSubStringOrRegexArrive && (markUpTagAttribute.getName().equals("substring")
                                    || markUpTagAttribute.getName().equals("regex"))) {
                                isSubStringOrRegexArrive = true;
                            }
                        }

                        if (markupAttribute.containsKey("region")) {
                            String regionVal = markupAttribute.get("region") != null ? markupAttribute.get("region") : "anonymous";//provide annonymous region here if region value is empty
                            markupAttribute.remove("region");
                            Region region = new Region(regionVal, markupAttribute, markUpTag.getTagName());
                            regionList.add(region);
                            List<Region> newRegionList = new ArrayList<>(regionList);
                            regionTagOnLine.put(markUpTag.isTagApplicableToNextLine() ? nextLine : thisLine, newRegionList);
                            if(!markUpTag.isTagApplicableToNextLine()){
                                addMarkupTags(thisLine, transformRegionAttributeToMarkupTag(newRegionList), markUpTagOnLine);
                            }
                        } else {
                            ApplicableMarkupTag markupTag = new ApplicableMarkupTag(markupAttribute, markUpTag.getTagName());
                            List<ApplicableMarkupTag> markupTagList = new ArrayList<>();
                            markupTagList.add(markupTag);
                            addMarkupTags(markUpTag.isTagApplicableToNextLine() ? nextLine : thisLine, markupTagList, markUpTagOnLine);
                        }
                    }
                    if (markUpTag.getTagName().equals("end")) {
                        List<Region> newRegionList = new ArrayList<>(regionList);
                        regionTagOnLine.put(thisLine, newRegionList);
                        addMarkupTags(thisLine, transformRegionAttributeToMarkupTag(newRegionList), markUpTagOnLine);

                        Map<String, String> eAttrib = new HashMap<>();
                        for (MarkupTagAttribute markUpTagAttribute : markUpTag.getMarkUpTagAttributes()) {
                            eAttrib.putIfAbsent(markUpTagAttribute.getName(), markUpTagAttribute.getValue());
                        }

                        String regionVal = "anonymous";
                        if (eAttrib.containsKey("region")) {
                            regionVal = eAttrib.get("region") == null || eAttrib.get("region").trim().isEmpty() ? "anonymous" : eAttrib.get("region");
                            for (int i = regionList.size() - 1; i >= 0; i--) {
                                if (regionList.get(i).getValue().equals(regionVal)) {
                                    regionList.remove(i);
                                    break;
                                }
                            }
                        } else if (!regionList.isEmpty()) {
                            regionList.remove(regionList.size() - 1);//if no region defined then end with last region
                        } else {//no region defined only @end is provided, this case considered as invalid
                            //report error with @end tag and region value;
                            errorList.add(String.format("error: snippet markup: no region to end @end <sub>^</sub><b><i>%s</b></i>", regionVal));
                            break main;
                        }
                    }
                }
            }
            thisLine++;
        }
        if(!regionList.isEmpty()){
            for(Region region :regionList){
                String error;
                if(region.markupTagName.equals("end")){
                    error = String.format("error: snippet markup: no region to end <b><i>%s %s</b></i>", region.markupTagName, region.value);
                } else{
                    error = String.format("error: snippet markup: unpaired region <b><i>%s %s</b></i>", region.markupTagName, region.value);
                }
                errorList.add(error);
            }
        }
        return new ProcessedTags(markUpTagOnLine, regionTagOnLine, errorList);
    }
    
    private List<ApplicableMarkupTag> transformRegionAttributeToMarkupTag(List<Region> regionList){
        
        List<ApplicableMarkupTag> markupTag = new ArrayList<>();
        regionList.iterator().forEachRemaining(region-> markupTag.add(new ApplicableMarkupTag(region.getAttributes(), region.getMarkupTagName())));
        return markupTag; 
    }
    
    private void addMarkupTags(Integer thisLine, List<ApplicableMarkupTag> markupTagList, Map<Integer, List<ApplicableMarkupTag>> markUpTagOnLine) {
        if (markUpTagOnLine.containsKey(thisLine)) {
            markUpTagOnLine.get(thisLine).addAll(markupTagList);
        } else {
            markUpTagOnLine.put(thisLine, markupTagList);
        }
    }
    
    public class Region{
        private final String markupTagName;
        private final String value;
        private final Map<String, String> attributes;

        Region(String value, Map<String, String> attributes, String markupTagName){
            this.value = value == null || value.isEmpty() ? "anonymous" : value;
            this.attributes = attributes;
            this.markupTagName = markupTagName;
        }

        public Map<String, String> getAttributes() {
            return attributes;
        }

        public String getValue() {
            return value;
        }
        
        public String getMarkupTagName() {
            return markupTagName;
        }

        @Override
        public String toString() {
            return "Region{" + "value=" + value + ", attributes=" + attributes + '}';
        }
    }
    
    public class ApplicableMarkupTag{
        private final String markupTagName;
        private Map<String, String> attributes;

        ApplicableMarkupTag(Map<String, String> attributes, String markupTagName){
            this.attributes = attributes;
            this.markupTagName = markupTagName;
        }

        public Map<String, String> getAttributes() {
            return attributes;
        }
        
        public String getMarkupTagName() {
            return markupTagName;
        }

        @Override
        public String toString() {
            return "Attrib{" + "attributes=" + attributes + '}';
        }

    }
    
    public class ProcessedTags{
        private Map<Integer, List<ApplicableMarkupTag>> markUpTagLineMapper;
        private Map<Integer, List<Region>> regionTagLineMapper;
        private List<String> errorList = new ArrayList<>();

        public ProcessedTags(Map<Integer, List<ApplicableMarkupTag>> markUpTagLineMapper, Map<Integer, List<Region>> regionTagLineMapper, List<String> errorList) {
            this.markUpTagLineMapper = markUpTagLineMapper;
            this.regionTagLineMapper = regionTagLineMapper;
            this.errorList = errorList;
        }

        public Map<Integer, List<ApplicableMarkupTag>> getMarkUpTagLineMapper() {
            return markUpTagLineMapper;
        }

        public Map<Integer, List<Region>> getRegionTagLineMapper() {
            return regionTagLineMapper;
        }

        public List<String> getErrorList() {
            return errorList;
        }
        
        
    }
    
}
