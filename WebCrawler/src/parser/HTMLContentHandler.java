/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package parser;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.xml.sax.SAXException;
import java.util.logging.Level;
import java.util.logging.Logger;
import webcrawler.SQLCommunicator;

/**
 *
 * @author Nisansa
 */
public class HTMLContentHandler extends ContentHandler{
    
    protected final int MAX_ANCHOR_LENGTH = 100;  
    protected boolean anchorFlag = false;
    
    protected String base;
    protected String table;
    
    protected boolean isWithinElement=false;
    protected boolean isWithinBodyElement=false;
    protected boolean isDatabasesendOK=false;
    protected boolean isDateAndAuthorDiscovered=false;
    protected boolean isTopicDiscovered=false;
    protected boolean isParagraphStarted=false;
    protected boolean isEntryStarted=false;
    
    
    protected StringBuilder bodyText= new StringBuilder();
    protected StringBuilder databaseAuthor= new StringBuilder();
    protected StringBuilder databaseDate= new StringBuilder();
    protected StringBuilder databaseTopic= new StringBuilder();
    protected StringBuilder databaseContent= new StringBuilder();
    protected StringBuilder anchorText = new StringBuilder();
    
    protected enum Element {

        A, AREA, LINK, IFRAME, FRAME, EMBED, IMG, BASE, META, BODY, P, SPAN,HTML
    }
    
    protected static class HtmlFactory {

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
    
    //Finish all and write to database
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
                Logger.getLogger(HtmlContentHandlerLankaDeepa.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        if (element == Element.A || element == Element.AREA || element == Element.LINK) {
            anchorFlag = false;

        }
        // comment for commit 2013.04.26
        if (element == Element.BODY) {
            isWithinBodyElement = false;
            if (isDatabasesendOK) {
                SQLCommunicator.InsertInToTable(table,new String[]{databaseAuthor.toString(), databaseDate.toString(), databaseTopic.toString(), databaseContent.toString()});
            }
        }

    }
    
    
    //Collect data from elements and update the variables 
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
                Logger.getLogger(HtmlContentHandlerLankaDeepa.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        if (isDateAndAuthorDiscovered) {
            isDateAndAuthorDiscovered = false;

            String tempauthoranddate = new String(ch);
            String[] tempauthoranddatearray = tempauthoranddate.split("\\|");

            databaseDate.append(tempauthoranddatearray[0], start, tempauthoranddatearray[0].length());
            databaseAuthor.append(tempauthoranddatearray[1], start, length - tempauthoranddatearray[0].length());
            BufferedWriter writer = null;
            try {
                writer = new BufferedWriter(new FileWriter("./output1.txt", true));
                writer.write("Date and Author   : ");
                writer.write(new String(ch, start, length));

                writer.flush();
                writer.close();

            } catch (IOException ex) {
                Logger.getLogger(HtmlContentHandlerLankaDeepa.class.getName()).log(Level.SEVERE, null, ex);
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
                Logger.getLogger(HtmlContentHandlerLankaDeepa.class.getName()).log(Level.SEVERE, null, ex);
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

    
    
    @Override
    public String getBodyText() {
        return bodyText.toString();
    }

    /**
     *
     * @return
     */
    @Override
    public String getBaseUrl() {
        return base;
    }
}
