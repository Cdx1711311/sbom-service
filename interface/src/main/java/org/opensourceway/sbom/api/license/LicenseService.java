package org.opensourceway.sbom.api.license;

import org.opensourceway.sbom.model.pojo.vo.license.LicenseInfoVo;
import org.opensourceway.sbom.model.pojo.vo.sbom.PackageUrlVo;

import java.util.List;
import java.util.Map;

public interface LicenseService {

    Integer getBulkRequestSize();

    boolean needRequest();

    String getPurlsForLicense(PackageUrlVo packageUrlVo, String productType, String productVersion);

    Map<String, LicenseInfoVo> getLicenseInfoVoFromPurl(List<String> purls) throws Exception;
}