package cn.lampv.tools.gitea;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
public class UpdateUrl {
    private String name;
    private String url;
    private Date lastModify;
    private String sha256;
}
