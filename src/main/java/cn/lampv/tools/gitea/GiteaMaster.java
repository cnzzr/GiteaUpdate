package cn.lampv.tools.gitea;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.JSONSerializer;
import jodd.datetime.JDateTime;
import jodd.http.HttpRequest;
import jodd.http.HttpResponse;
import jodd.io.FileUtil;
import jodd.io.NetUtil;
import jodd.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * 获取Gitea.exe 最新版本
 *
 * @author cnzzr
 */
@Slf4j
public class GiteaMaster {
    //见 lombok.config private static Logger logger = LoggerFactory.getLogger(GiteaMaster.class);
    static String GiteaMaster = "https://dl.gitea.io/gitea/master/";

    public static GiteaMaster GetInstance() {
        return new GiteaMaster();
    }

    /**
     * 更新
     *
     * @return
     */
    public boolean Update() {
        //String fullVersionName = "gitea-master-windows-4.0-386.exe.xz";
        String fullVersionName = "gitea-master-windows-4.0-amd64.exe";
        String giteaFile = "gitea.exe";
        File exe = new File(giteaFile);
        if (!exe.exists()) {
            logger.error("{} 不存在", giteaFile);
        } else if (!exe.canWrite()) {
            logger.error("{} 无法写入,请修改文件权限或计划任务执行权限", giteaFile);
            return false;
        }

        try {
            List<UpdateUrl> toDownload = FetchVersion(fullVersionName);
            for (UpdateUrl model : toDownload) {
                File file = new File(model.getName());
                if (file.exists()) {
                    String oldFileHash = calcHash(file);
                    //TODO hash一致时还需要判断主程序文件是否匹配，有可能上次更新未成功替换
                    if (model.getSha256().equalsIgnoreCase(oldFileHash)) {
                        logger.info("文件已存在且Hash一致，本地文件={} sha256={}", file.getCanonicalPath(), oldFileHash);
                        continue;
                    }
                    logger.info("本地文件已经存在，Hash与最新版本不一致，将删除本地文件后重新下载");
                    FileUtil.delete(file);
                }
                // 2下载文件
                // 3 检查文件Hash
                String url = model.getUrl();
                downloadFile(url, file);
                if (!file.exists()) {
                    continue;
                }
                logger.info("文件下载成功 {}", file.getCanonicalPath());

                String fileHash = calcHash(file);
                if (!model.getSha256().equalsIgnoreCase(fileHash)) {
                    logger.error("文件下载失败。Hash不匹配，本地文件hash={} sha256={}", fileHash, model.getSha256());
                    continue;
                }
                //停止服务
                boolean isStoped = exec("net stop gitea2");
                //备份原文件
                if (isStoped) {
                    boolean r = false;
                    try {
                        FileUtil.copyFile(file, new File(giteaFile));
                        //启动服务
                        r = exec("net start gitea2");
                    } finally {
                        // 如果exe文件替换失败则重启gitea服务
                        if (!r) {
                            exec("net start gitea2");
                        }
                    }
                    //break
                    return r;
                }
            }
        } catch (Exception exc) {
            logger.error("更新Gitea失败", exc);
        }
        return false;
    }

    private boolean exec(String cmd) {
        Runtime runtime = Runtime.getRuntime();
        try {
            Process process = runtime.exec(cmd);
            //打印执行的输出结果
            InputStream is = process.getInputStream();
            InputStreamReader isr = new InputStreamReader(is, "gbk"); //gbk：解决输出乱码
            BufferedReader br = new BufferedReader(isr);
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                stringBuilder.append(line);
            }
            logger.debug("命令输出 {}", stringBuilder);
            return true;
        } catch (UnsupportedEncodingException e) {
            logger.error("exec获取命令输出", e);
        } catch (IOException e) {
            logger.error("exec执行命令", e);
        }
        return false;
    }

    private String calcHash(File fileName) throws NoSuchAlgorithmException, FileNotFoundException {
        byte[] buffer = new byte[8192];
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(fileName));
        int count;
        try {
            while ((count = bis.read(buffer)) > 0) {
                digest.update(buffer, 0, count);
            }
            byte[] hash = digest.digest();
            return bytesToHex(hash);
        } catch (Exception e) {
            logger.error("计算文件Hash出错", e);
        } finally {
            try {
                bis.close();
            } catch (IOException e) {
                // Ignored
            }
        }
        return null;
    }

    private String bytesToHex(byte[] hash) {
        StringBuffer hexString = new StringBuffer();
        for (int i = 0; i < hash.length; i++) {
            String hex = Integer.toHexString(0xff & hash[i]);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }

    /**
     * Downloads resource to a file, potentially very efficiently.
     */
    private static void downloadFile(String url, File file) throws IOException {
        long start = System.currentTimeMillis();
        URLConnection urlConnection = new URL(url).openConnection();
        urlConnection.setReadTimeout(15 * 60 * 1000);
        //urlConnection.addRequestProperty("REFERER","https://dl.gitea.io/gitea/master/");
        //AWS S3 下载必需参数
        urlConnection.addRequestProperty("User-agent", " Mozilla/5.0 (Windows NT 6.3; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko)");
        InputStream inputStream = urlConnection.getInputStream();
        // NetUtil 方法代码
        //InputStream inputStream = new URL(url).openStream();
        ReadableByteChannel rbc = Channels.newChannel(inputStream);
        FileOutputStream fos = new FileOutputStream(file);
        // 24=16M 26=64M 28=256M
        fos.getChannel().transferFrom(rbc, 0, 1 << 30);
        logger.info("下载耗时 {} ms", System.currentTimeMillis() - start);
    }

    /**
     * 获取版本信息
     *
     * @param fullVersionName 例如 gitea-master-windows-4.0-amd64.exe
     * @return
     * @throws MalformedURLException
     * @throws IOException
     */
    public List<UpdateUrl> FetchVersion(String fullVersionName) throws MalformedURLException, IOException {
        String match = StringUtil.isBlank(fullVersionName) ? "amd64.exe" : fullVersionName;

        String trQuery = "div > table > tbody > tr";
        Document giteaMaster = Jsoup.parse(new URL(GiteaMaster), 15 * 1000);
        Elements elements = giteaMaster.select(trQuery);
        logger.debug("解析到TR行数 {}", elements.size());

        // 需要获取 exe文件以及 Hash值
        Map<String, UpdateUrl> results = new HashMap<String, UpdateUrl>();
        String name, modified, url;
        Date lastModify;

        for (Element element : elements) {
            name = element.getElementsByClass("name").text();
            modified = element.getElementsByClass("date").text();
            if (StringUtil.isNotBlank(name) && name.contains(match)) {
                name = StringUtil.replaceChars(name, new char[]{'\r', '\n', '\t'}, new char[]{' ', ' ', ' '}).trim();
                // PGP SIGNATURE https://dl.gitea.io/gitea/master/gitea-master-windows-4.0-amd64.exe.asc
                if (name.endsWith("asc")) {
                    continue;
                }
                url = (GiteaMaster.endsWith("/") ? GiteaMaster : (GiteaMaster + "/")) + name;
                if (name.endsWith("sha256")) {
                    String key = name.substring(0, name.lastIndexOf('.'));
                    UpdateUrl model = results.get(key);
                    if (null != model) {
                        String hash = FetchHash(url);
                        model.setSha256(hash);
                        logger.debug("设置 {} 的Hash值为 {}", key, hash);
                    }
                    continue;
                }
                lastModify = new JDateTime(modified, "MM/DD/YYYY hh:mm:ss a").convertToDate();
                results.put(name, UpdateUrl.builder().name(name).url(url).lastModify(lastModify).sha256("").build());
            }
        }
        logger.info("分析结果 {}", JSON.toJSONString(results));

        List<UpdateUrl> out = new ArrayList<UpdateUrl>();
        for (String key : results.keySet()) {
            if (key.equalsIgnoreCase(match)) {
                out.add(results.get(key));
            }
        }
        return out;
    }

    /**
     * 取 sha256文件内容
     *
     * @param url
     * @return
     */
    private String FetchHash(String url) {
        HttpResponse response = HttpRequest.get(url).send();
        String content = response.bodyText();
        if (StringUtil.isNotBlank(content)) {
            logger.debug("获取hash文件内容 {}", content);
            return content.substring(0, content.indexOf(' '));
        }
        return null;
    }

}
