package com.hp.gaia.provider.alm;

import com.hp.gaia.provider.alm.util.AlmRestUtils;
import com.hp.gaia.provider.alm.util.RestConstants;
import junit.framework.Assert;
import org.junit.Test;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Created by belozovs on 10/15/2015.
 */

public class IssueChangeStateTest {

    @Test
    public void getStartDateTest() throws Exception {

        String dateString = AlmRestUtils.getQueryDate(30);
        SimpleDateFormat sdf = new SimpleDateFormat(RestConstants.ALM_DATE_TIME_FORMAT);
        Date olderDate = sdf.parse(dateString);
        Calendar calendar = GregorianCalendar.getInstance();
        calendar.setTime(olderDate);

        LocalDate olderLocalDate = LocalDate.of(olderDate.getYear() + 1900, olderDate.getMonth() + 1, olderDate.getDate());
        LocalDate now = LocalDate.now();
        long days = ChronoUnit.DAYS.between(olderLocalDate, now);

        Assert.assertEquals("Difference should be 30 days", 30, days);
    }
}
