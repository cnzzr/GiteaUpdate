package cn.lampv.tools;

import jodd.datetime.JDateTime;
import org.junit.Assert;
import org.junit.Test;

public class JoddTest {

    @Test
    public void testJDatetime(){
        new JDateTime("03/19/2019 07:22:46 AM","MM/DD/YYYY hh:mm:ss a").convertToDate();
        Assert.assertTrue(true);
    }
}
