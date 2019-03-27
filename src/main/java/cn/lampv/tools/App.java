package cn.lampv.tools;

import cn.lampv.tools.gitea.GiteaMaster;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 启动程序
 */
public class App {
    private static Logger logger = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) {
        System.out.println("Gitea Update: start....");
        try {
            GiteaMaster.GetInstance().Update();
            System.out.println(" finished.");
        } catch (Exception exc) {
            System.err.println(" error: " + exc.getMessage());
            logger.error("Gitea Update failed", exc);
        } finally {
            System.out.println(" exit.");
        }
    }
}
