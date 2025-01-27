package org.opensourceway.sbom.model.pojo.response.vul.cve;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ComponentReport implements Serializable {
    /** The HTTP status code. */
    private Integer code;

    /** The message of the response. */
    private String message;

    /** The number of vulnerabilities. */
    private Integer count;

    /** The list of known vulnerabilities. */
    List<CveManagerVulnerability> data;

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public List<CveManagerVulnerability> getData() {
        return data;
    }

    public void setData(List<CveManagerVulnerability> data) {
        this.data = data;
    }
}
