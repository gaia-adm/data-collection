package com.hp.gaia.provider.alm;

import junit.framework.Assert;
import org.junit.Test;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.time.Month;
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

        IssueChangeState ics = new IssueChangeState();

        String dateString = ics.getQueryDate(30);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        Date olderDate = sdf.parse(dateString);
        Calendar calendar = GregorianCalendar.getInstance();
        calendar.setTime(olderDate);



        LocalDate olderLocalDate = LocalDate.of(olderDate.getYear() + 1900, olderDate.getMonth() + 1, olderDate.getDate());
        LocalDate now = LocalDate.now();
        long days = ChronoUnit.DAYS.between(olderLocalDate, now);

        Assert.assertEquals("Difference should be 30 days", 30, days);

    }
}
