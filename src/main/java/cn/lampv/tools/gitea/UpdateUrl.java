package cn.lampv.tools.gitea;

import lombok.Builder;
import lombok.Data;

import java.util.Date;

/**
 * 更新程序Model
 */
@Data
@Builder
public class UpdateUrl {
    /**
     * 可执行程序名
     */
    private String name;
    /**
     * 下载地址
     */
    private String url;
    /**
     * 更新时间
     */
    private Date lastModify;
    /**
     * 文件Hash
     */
    private String sha256;
}
