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
        } catch (Exception exc) {
            logger.error("Gitea Update failed", exc);
        }
    }
}
