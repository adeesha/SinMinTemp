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


import org.xml.sax.Attributes;
import org.xml.sax.SAXException;


public class HtmlContentHandlerDivaina extends HTMLContentHandler {

    private String metaRefresh;
    private String metaLocation;    
    private boolean isEntryStarted;

    public HtmlContentHandlerDivaina() {
        isEntryStarted = false;
        isWithinBodyElement = false;
        isParagraphStarted = false;
        isDateAndAuthorDiscovered = false;
        isTopicDiscovered = false;
        isWithinElement = false;
        isDatabasesendOK=false;

        bodyText = new StringBuilder();
        databaseAuthor= new StringBuilder();
        databaseContent= new StringBuilder();
        databaseDate= new StringBuilder();
        databaseTopic= new StringBuilder();
        
        
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        Element element = HtmlFactory.getElement(localName);
        // modified by adeesha. 

     
            
            
             if (element == Element.META) {
           String name = attributes.getValue("name");
            String content = attributes.getValue("content");
            
            if("description".equals(name)&&"".equals(content)){
                System.out.println("not a valid URL");
                return;
            }
            if("description".equals(name)&&!"".equals(content)){
                System.out.println("valid URL");
                isDatabasesendOK=true;
               
            }
            
           
        }
            
            
            
            

            if (isEntryStarted) {
                if (element == Element.P) {
                    isParagraphStarted = true;
                    isWithinElement = true;
                    // String pClass = attributes.getValue("");
                    // System.out.println("sff");
                }

            }
            if (isEntryStarted && isParagraphStarted) {
                if (element != Element.P) {
                    isParagraphStarted = false;
                    isEntryStarted = false;
                    // String pClass = attributes.getValue("");
                    // System.out.println("sff");
                }

            }


            if (element == Element.SPAN) {



                String spanStyle = attributes.getValue("class");
                if (spanStyle != null) {
                    if (spanStyle.equalsIgnoreCase("entry-content")) {

                        isEntryStarted = true;

                    }
                }

                return;
            }
        

        if (element == Element.P) {

            //  isParagraphstarting=true;

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
//                        String pClass = attributes.getValue("class");
//                        if (pClass != null) {
//                                if(pClass.equalsIgnoreCase("leftbar_news_heading")){
//
//                                isLeftbarNewsHeading=true;
//
//                            }
//                        }
            return;
        }



        /// end of modification





        if (element == Element.A || element == Element.AREA || element == Element.LINK) {

            String href = attributes.getValue("href");
            if (href != null) {
                
            }
            return;
        }

        if (element == Element.IMG) {
            String imgSrc = attributes.getValue("src");
            if (imgSrc != null) {
               
            }
            return;
        }

        if (element == Element.IFRAME || element == Element.FRAME || element == Element.EMBED) {
            String src = attributes.getValue("src");
            if (src != null) {
              
            }
            return;
        }

        if (element == Element.BASE) {
            if (base != null) { // We only consider the first occurrence of the
                // Base element.
                String href = attributes.getValue("href");
                if (href != null) {
                    base = href;
                }
            }
            return;
        }

        if (element == Element.META) {
            String equiv = attributes.getValue("http-equiv");
            String content = attributes.getValue("content");
            if (equiv != null && content != null) {
                equiv = equiv.toLowerCase();

                // http-equiv="refresh" content="0;URL=http://foo.bar/..."
                if (equiv.equals("refresh") && (metaRefresh == null)) {
                    int pos = content.toLowerCase().indexOf("url=");
                    if (pos != -1) {
                        metaRefresh = content.substring(pos + 4);
                    }
                   
                }

                // http-equiv="location" content="http://foo.bar/..."
                if (equiv.equals("location") && (metaLocation == null)) {
                    metaLocation = content;
                  
                }
            }
            return;
        }

        if (element == Element.BODY) {
            isWithinBodyElement = true;
        }
        
    }

  
}
