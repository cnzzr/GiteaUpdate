package cn.lampv.tools;

import static org.junit.Assert.assertTrue;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * Unit test for simple App.
 */
public class AppTest 
{
    /**
     * Rigorous Test :-)
     */
    @Test
    public void shouldAnswerWithTrue()
    {
        assertTrue( true );
    }

    @Test
    public void download() throws IOException {
        String url = "https://dl.gitea.io/gitea/master/gitea-master-windows-4.0-amd64.exe.sha256";
        File exeFile = new File("T:/sha356.txt");

        URL netUrl = new URL(url);

        copyURLToFile(netUrl,exeFile);
    }

    public static void copyURLToFile(URL source, File destination) throws IOException {
        URLConnection urlConnection = source.openConnection();
        //urlConnection.addRequestProperty("REFERER","https://dl.gitea.io/gitea/master/");
        //AWS S3 下载必需参数
        urlConnection.addRequestProperty("user-agent"," Mozilla/5.0 (Windows NT 6.3; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko)");
        InputStream input = urlConnection.getInputStream();
        try {
            FileOutputStream output = FileUtils.openOutputStream(destination);
            try {
                IOUtils.copy(input, output);
            } finally {
                IOUtils.closeQuietly(output);
            }
        } finally {
            IOUtils.closeQuietly(input);
        }
    }
}
