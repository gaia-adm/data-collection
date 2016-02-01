package com.hp.gaia.provider.alm.util;

import org.apache.commons.io.Charsets;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.ByteArrayInputStream;
import java.io.StringReader;

/**
 * Created by belozovs on 8/25/2015.
 * Some auxiliary methods to handle XML content retrieved from ALM 12
 */
public class AlmXmlUtils {

    private final static Log log = LogFactory.getLog(AlmXmlUtils.class);

    /**
     * Find the highest value for tags with integer values, for example, highest ID returned
     * NOTE: returns 0, in case of exception
     *
     * @param xmlString - XML to be checked
     * @param tagName   - tag name whose values should be checked
     * @return - the highest integer value for the given tag name
     */
    public int getHighestTagIntegerValue(String xmlString, String tagName) {

        int highest = 0;
        try {
            XMLInputFactory factory = XMLInputFactory.newInstance();
            XMLStreamReader reader = factory.createXMLStreamReader(new ByteArrayInputStream(xmlString.getBytes()), Charsets.UTF_8.name());
            while (reader.hasNext()) {
                int event = reader.next();
                if (event == XMLStreamConstants.START_ELEMENT && reader.getLocalName().equals(tagName)) {
                    int current = Integer.parseInt(reader.getElementText());
                    if (current > highest) {
                        highest = current;
                        reader.next();
                    }
                }
            }
        } catch (XMLStreamException e) {
            e.printStackTrace();
        }
        log.debug("Highest value of " + tagName + " is " + highest);
        return highest;
    }

    /**
     * Count elements by tag name
     * NOTE: returns 0, in case of exception
     *
     * @param xmlString - XML to be checked
     * @param tagName   - tag name whose occurrences should be counted
     * @return - number of occurrences
     */
    public int countTags(String xmlString, String tagName) {
        
        int counter = 0;
        XMLInputFactory factory = XMLInputFactory.newInstance();
        try {
            XMLStreamReader reader = factory.createXMLStreamReader(new ByteArrayInputStream(xmlString.getBytes()), Charsets.UTF_8.name());
            while (reader.hasNext()) {
                int event = reader.next();
                if (event == XMLStreamConstants.START_ELEMENT && reader.getLocalName().equals(tagName)) {
                    counter++;
                }
            }
        } catch (XMLStreamException e) {
            e.printStackTrace();
        }
        log.debug("Found " + tagName + " elements: " + counter);
        return counter;
    }

    /**
     * Get a value of the numeric attribute of the tag
     * NOTE: returns 0, in case of exception
     *
     * @param xmlString - XML to be checked
     * @param tagName   - tag name
     * @param attrName  - attribute name with the integer value
     * @return - integer value of the attribute
     */
    public int getIntegerAttributeValue(String xmlString, String tagName, String attrName) {

        int result = 0;
        XMLInputFactory factory = XMLInputFactory.newInstance();
        try {
            XMLStreamReader reader = factory.createXMLStreamReader(new ByteArrayInputStream(xmlString.getBytes()), Charsets.UTF_8.name());
            while (reader.hasNext()) {
                int event = reader.next();
                if (event == XMLStreamConstants.START_ELEMENT && reader.getLocalName().equals(tagName)) {
                    result = Integer.parseInt(reader.getAttributeValue(reader.getNamespaceURI(), "TotalResults"));
                }
            }
        } catch (XMLStreamException e) {
            e.printStackTrace();
        }
        log.debug("Value of attribute " + attrName + " in tag " + tagName + " is " + result);
        return result;
    }

    public static String getTagValue(String xml, String tagName) {

        String ret = null;
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new InputSource(new StringReader(xml)));
            Element element = document.getDocumentElement();
            NodeList list = element.getElementsByTagName(tagName);
            if (list != null && list.getLength() > 0) {
                NodeList subList = list.item(0).getChildNodes();
                if (subList != null && subList.getLength() > 0) {
                    ret = subList.item(0).getNodeValue();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return ret;
    }
}