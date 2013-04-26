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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import webcrawler.BasicCrawlController;
import webcrawler.SQLCommunicator;

public class HtmlContentHandler extends DefaultHandler {

    private final int MAX_ANCHOR_LENGTH = 100;

    private enum Element {

        A, AREA, LINK, IFRAME, FRAME, EMBED, IMG, BASE, META, BODY, P, SPAN,HTML
    }

    private static class HtmlFactory {

        private static Map<String, Element> name2Element;

        static {
            name2Element = new HashMap<>();
            for (Element element : Element.values()) {
                name2Element.put(element.toString().toLowerCase(), element);
            }
        }

        public static Element getElement(String name) {
            return name2Element.get(name);
        }
    }
    private String base;
    private String metaRefresh;
    private String metaLocation;
    private boolean isWithinBodyElement;
    private StringBuilder bodyText;
    
    private StringBuilder databaseAuthor;
    private StringBuilder databaseDate;
            private StringBuilder databaseTopic;
                    private StringBuilder databaseContent;
    
    
    private List<ExtractedUrlAnchorPair> outgoingUrls;
    private ExtractedUrlAnchorPair curUrl = null;
    private boolean anchorFlag = false;
    private StringBuilder anchorText = new StringBuilder();
    private boolean isEntryStarted;
    private boolean isParagraphStarted;
    private boolean isDateAndAuthorDiscovered;
    private boolean isTopicDiscovered;
    private boolean isWithinElement;
    private boolean isDatabasesendOK;

    public HtmlContentHandler() {
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
        outgoingUrls = new ArrayList<>();
        
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        Element element = HtmlFactory.getElement(localName);
        // modified by adeesha. 

        if (BasicCrawlController.isConetenReadEnable) {
            
            
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
                anchorFlag = true;
                curUrl = new ExtractedUrlAnchorPair();
                curUrl.setHref(href);
                outgoingUrls.add(curUrl);
            }
            return;
        }

        if (element == Element.IMG) {
            String imgSrc = attributes.getValue("src");
            if (imgSrc != null) {
                curUrl = new ExtractedUrlAnchorPair();
                curUrl.setHref(imgSrc);
                outgoingUrls.add(curUrl);
            }
            return;
        }

        if (element == Element.IFRAME || element == Element.FRAME || element == Element.EMBED) {
            String src = attributes.getValue("src");
            if (src != null) {
                curUrl = new ExtractedUrlAnchorPair();
                curUrl.setHref(src);
                outgoingUrls.add(curUrl);
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
                    curUrl = new ExtractedUrlAnchorPair();
                    curUrl.setHref(metaRefresh);
                    outgoingUrls.add(curUrl);
                }

                // http-equiv="location" content="http://foo.bar/..."
                if (equiv.equals("location") && (metaLocation == null)) {
                    metaLocation = content;
                    curUrl = new ExtractedUrlAnchorPair();
                    curUrl.setHref(metaRefresh);
                    outgoingUrls.add(curUrl);
                }
            }
            return;
        }

        if (element == Element.BODY) {
            isWithinBodyElement = true;
        }
        
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        Element element = HtmlFactory.getElement(localName);

        if (isWithinElement) {
            isWithinElement = false;
            BufferedWriter writer = null;
            try {
                writer = new BufferedWriter(new FileWriter("./output1.txt", true));
               writer.newLine();
                writer.flush();
                writer.close();

            } catch (IOException ex) {
                Logger.getLogger(HtmlContentHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        if (element == Element.A || element == Element.AREA || element == Element.LINK) {
            anchorFlag = false;
            if (curUrl != null) {
                String anchor = anchorText.toString().replaceAll("\n", " ").replaceAll("\t", " ").trim();
                if (!anchor.isEmpty()) {
                    if (anchor.length() > MAX_ANCHOR_LENGTH) {
                        anchor = anchor.substring(0, MAX_ANCHOR_LENGTH) + "...";
                    }
                    curUrl.setAnchor(anchor);
                }
                anchorText.delete(0, anchorText.length());
            }
            curUrl = null;
        }
        // comment for commit 2013.04.26
        if (element == Element.BODY) {
            isWithinBodyElement = false;
            if(isDatabasesendOK){
            SQLCommunicator.communicate(databaseAuthor.toString(), databaseDate.toString(), databaseTopic.toString(), databaseContent.toString());
            }
            }
      
    }
    
    @Override
    public void characters(char ch[], int start, int length) throws SAXException {
        // modified by Adeesha	


        if (isTopicDiscovered) {
            isTopicDiscovered = false;
            BufferedWriter writer = null;
            try {
                databaseTopic.append(ch, start, length);
                writer = new BufferedWriter(new FileWriter("./output1.txt", true));
                writer.write("Topic   : ");
                writer.write(new String(ch, start, length));

                writer.flush();
                writer.close();

            } catch (IOException ex) {
                Logger.getLogger(HtmlContentHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        if (isDateAndAuthorDiscovered) {
            isDateAndAuthorDiscovered = false;
            
            String tempauthoranddate = new String(ch);
               String[] tempauthoranddatearray = tempauthoranddate.split("\\|");
            
            databaseDate.append(tempauthoranddatearray[0], start, tempauthoranddatearray[0].length());
            databaseAuthor.append(tempauthoranddatearray[1], start, length-tempauthoranddatearray[0].length());
            BufferedWriter writer = null;
            try {
                writer = new BufferedWriter(new FileWriter("./output1.txt", true));
                writer.write("Date and Author   : ");
                writer.write(new String(ch, start, length));

                writer.flush();
                writer.close();

            } catch (IOException ex) {
                Logger.getLogger(HtmlContentHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        if (isParagraphStarted) {
            databaseContent.append(ch, start, length);
            BufferedWriter writer = null;
            try {
                writer = new BufferedWriter(new FileWriter("./output1.txt", true));

                writer.write(new String(ch, start, length));
               
                writer.flush();
                writer.close();

            } catch (IOException ex) {
                Logger.getLogger(HtmlContentHandler.class.getName()).log(Level.SEVERE, null, ex);
            }

            //isParagraphStarted=false;
        }

        // end of modification


        if (isWithinBodyElement) {
            bodyText.append(ch, start, length);

            if (anchorFlag) {
                anchorText.append(new String(ch, start, length));
            }
        }
    }

    public String getBodyText() {
        return bodyText.toString();
    }

    public List<ExtractedUrlAnchorPair> getOutgoingUrls() {
        return outgoingUrls;
    }

    public String getBaseUrl() {
        return base;
    }
}
