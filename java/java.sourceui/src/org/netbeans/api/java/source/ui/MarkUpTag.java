package org.netbeans.api.java.source.ui;


import java.util.Set;

class MarkUpTag {

        String tagName;
        int lineSourceOffset;
        int markupLineOffset;
        int nameLineOffset;
        int start;
        int end;
        Set<MarkUpTagAttribute> markUpTagAttributes;
        boolean isTagApplicableToNextLine;

        String name() {
            return tagName;
        }

        Set<MarkUpTagAttribute> attributes() {
            return markUpTagAttributes;
        }

        int start() {
            return start;
        }

        int end() {
            return end;
        }

        @Override
        public String toString() {
            return "Tag{" +
                    "name='" + tagName + '\'' +
                    ", start=" + start +
                    ", end=" + end +
                    ", attributes=" + markUpTagAttributes +
                    '}';
        }
    }