/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with this
 * work for additional information regarding copyright ownership. The ASF
 * licenses this file to You under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package parser;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;


public class HtmlContentHandlerLankaDeepa extends HTMLContentHandler {
  
    public HtmlContentHandlerLankaDeepa(){
        table="Lankadeepa";
    }
    
    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        Element element = HtmlFactory.getElement(localName); 
        
        ///Start of section used to scan the page///
        
        //First of all find the meta element with the name "content" and see if there is any content.
        if (element == Element.META) {
            String name = attributes.getValue("name");
            String content = attributes.getValue("content");

            if ("description".equals(name) && "".equals(content)) {
                System.out.println("not a valid URL");
                return;
            }
            if ("description".equals(name) && !"".equals(content)) {
                System.out.println("valid URL");
                isDatabasesendOK = true;
            }
        }
        
        //Raise the body element flag
        if (element == Element.BODY) {
            isWithinBodyElement = true;
        }
        
        //Scroll down until we meet the Span with the class attribute set to "entry-content" and raise the entry flag.
        if (element == Element.SPAN) {
            String spanStyle = attributes.getValue("class");
            if (spanStyle != null) {
                if (spanStyle.equalsIgnoreCase("entry-content")) {
                    isEntryStarted = true;
                }
            }
            return;
        }
        
        //Scroll down the entry till you find the first paragraph
        if (isEntryStarted) {
            if (element == Element.P) {
                isParagraphStarted = true;
                isWithinElement = true;               
            }
        }
        
        //Discover meta data and raise the appropriate flags
        if (element == Element.P) {
            String style = attributes.getValue("style");
            if (style != null) {
                if (style.contains("font-size:12px; color:#c28282;")) {
                    isDateAndAuthorDiscovered = true;
                    isWithinElement = true;
                }
                if (style.contains("font-size:26px;")) {
                    isTopicDiscovered = true;
                    isWithinElement = true;
                }
            }
            return;
        }
        
        //When the first non paragraph is found, put down the flags to stop extracting information 
        if (isEntryStarted && isParagraphStarted) {
            if (element != Element.P) {
                isParagraphStarted = false;
                isEntryStarted = false;                
            }
        }

         ///End of section used to scan the page///


        ///Start of section where code stubs for future enhancements are kept///
        
        if (element == Element.A || element == Element.AREA || element == Element.LINK) {

            String href = attributes.getValue("href");
            if (href != null) {
                anchorFlag = true;
                //In case we decide to extract hyper link info, a flag is to be raised here.
            }
            return;
        }  
        
        if (element == Element.IMG) {
            String imgSrc = attributes.getValue("src");
            if (imgSrc != null) {
               //In case we decide to extract image info, a flag is to be raised here.
            }
            return;
        }

        if (element == Element.IFRAME || element == Element.FRAME || element == Element.EMBED) {
            String src = attributes.getValue("src");
            if (src != null) {
              //In case we decide to extract iframe info, a flag is to be raised here.
            }
            return;
        }
        
        
        ///End of section where code stubs for future enhancements are kept///

        
        
        //Discovering the base URL (Unused)
        if (element == Element.BASE) {
            if (base != null) { // We only consider the first occurrence of the Base element.
                String href = attributes.getValue("href");
                if (href != null) {
                    base = href;
                }
            }
            return;
        }
              

    }

    

}
