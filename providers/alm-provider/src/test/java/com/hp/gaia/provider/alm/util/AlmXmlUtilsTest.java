package com.hp.gaia.provider.alm.util;


import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by belozovs on 8/26/2015.
 * Test for ALM XML Utils
 */
public class AlmXmlUtilsTest {

    private AlmXmlUtils almXmlUtils;

    private static final String xmlString = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><Audits TotalResults=\"3\"><Audit><Id>2</Id><Action>UPDATE</Action><ParentId>1\n" +
            "        </ParentId><ParentType>defect</ParentType><Time>2015-08-23 09:56:23</Time><User>sa</User><Properties><Property Label=\"Status\" Name=\"status\"><NewValue>New\n" +
            "                </NewValue><OldValue></OldValue></Property><Property Label=\"Assigned To\" Name=\"owner\"><NewValue>sa</NewValue><OldValue></OldValue></Property><Property Label=\"Severity\" Name=\"severity\"><NewValue>3-High</NewValue><OldValue></OldValue></Property></Properties></Audit><Audit><Id>5</Id><Action>UPDATE</Action><ParentId>1\n" +
            "        </ParentId><ParentType>defect</ParentType><Time>2015-08-23 10:06:28</Time><User>sa</User><Properties><Property Label=\"Severity\" Name=\"severity\"><NewValue>5-Urgent</NewValue><OldValue>3-High</OldValue></Property></Properties></Audit><Audit><Id>4</Id><Action>UPDATE</Action><ParentId>1\n" +
            "        </ParentId><ParentType>defect</ParentType><Time>2015-08-24 20:35:31</Time><User>sa</User><Properties><Property Label=\"Severity\" Name=\"severity\"><NewValue>4-Very High</NewValue><OldValue>5-Urgent</OldValue></Property></Properties></Audit></Audits>";


    @Before
    public void setUp() throws Exception {
        almXmlUtils = new AlmXmlUtils();
    }

    @Test
    public void testGetHighestTagIntegerValue() throws Exception {

        int highestValue = almXmlUtils.getHighestTagIntegerValue(xmlString, "Id");
        Assert.assertEquals("The highest value should be 5", 5, highestValue);
    }

    @Test
    public void testCountTags() throws Exception {

        int counter = almXmlUtils.countTags(xmlString, "Audit");
        Assert.assertEquals("The number of audit events should be 3", 3, counter);
    }

    @Test
    public void testGetIntAttributeValue() throws Exception {
        int totalResults = almXmlUtils.getIntegerAttributeValue(xmlString, "Audits", "TotalResults");
        Assert.assertEquals("The total result attribute should be 3", 3, totalResults);
    }

}