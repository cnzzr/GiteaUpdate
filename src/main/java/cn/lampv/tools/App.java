package cn.lampv.tools;

import cn.lampv.tools.gitea.GiteaMaster;
import jodd.datetime.JDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Hello world!
 */
public class App {
    private static Logger logger = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) {
        try {
            GiteaMaster.GetInstance().Update();
            new JDateTime("03/19/2019 07:22:46 AM","MM/DD/YYYY hh:mm:ss a").convertToDate();
        } catch (Exception exc) {
            logger.error("出错", exc);
        }
    }
}
