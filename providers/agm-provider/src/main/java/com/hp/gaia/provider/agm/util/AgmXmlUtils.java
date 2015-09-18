package com.hp.gaia.provider.agm.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.ByteArrayInputStream;

/**
 * Created by belozovs on 8/25/2015.
 * Some auxiliary methods to handle XML content retrieved from ALM 12
 */
public class AgmXmlUtils {

    private final static Log log = LogFactory.getLog(AgmXmlUtils.class);

    /**
     * Find the highest value for tags with integer values, for example, highest ID returned
     * NOTE: returns 0, in case of exception
     * @param xmlString - XML to be checked
     * @param tagName - tag name whose values should be checked
     * @return - the highest integer value for the given tag name
     */
    public int getHighestTagIntegerValue(String xmlString, String tagName) {

        int highest = 0;
        try {
            XMLInputFactory factory = XMLInputFactory.newInstance();
            XMLStreamReader reader = factory.createXMLStreamReader(new ByteArrayInputStream(xmlString.getBytes()));
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
     * @param xmlString - XML to be checked
     * @param tagName - tag name whose occurrences should be counted
     * @return - number of occurrences
     */
    public int countTags(String xmlString, String tagName) {
        int counter = 0;
        XMLInputFactory factory = XMLInputFactory.newInstance();
        try {
            XMLStreamReader reader = factory.createXMLStreamReader(new ByteArrayInputStream(xmlString.getBytes()));
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
     * @param xmlString - XML to be checked
     * @param tagName - tag name
     * @param attrName - attribute name with the integer value
     * @return - integer value of the attribute
     */
    public int getIntegerAttributeValue(String xmlString, String tagName, String attrName) {
        int result = 0;
        XMLInputFactory factory = XMLInputFactory.newInstance();
        try {
            XMLStreamReader reader = factory.createXMLStreamReader(new ByteArrayInputStream(xmlString.getBytes()));
            while (reader.hasNext()) {
                int event = reader.next();
                if (event == XMLStreamConstants.START_ELEMENT && reader.getLocalName().equals(tagName)) {
                    result = Integer.parseInt(reader.getAttributeValue(reader.getNamespaceURI(), "TotalResults"));
                }
            }
        } catch (XMLStreamException e) {
            e.printStackTrace();
        }
        log.debug("Value of attribute " + attrName + " in tag " + tagName + " is " +result);
        return result;
    }

}
