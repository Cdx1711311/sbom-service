package org.openeuler.sbom.manager.controller;

import org.apache.commons.lang3.ArrayUtils;
import org.openeuler.sbom.manager.constant.SbomConstants;
import org.openeuler.sbom.manager.model.Package;
import org.openeuler.sbom.manager.model.Product;
import org.openeuler.sbom.manager.model.RawSbom;
import org.openeuler.sbom.manager.model.vo.BinaryManagementVo;
import org.openeuler.sbom.manager.model.vo.PackagePurlVo;
import org.openeuler.sbom.manager.model.vo.PackageUrlVo;
import org.openeuler.sbom.manager.model.vo.PageVo;
import org.openeuler.sbom.manager.model.vo.ProductConfigVo;
import org.openeuler.sbom.manager.model.vo.VulnerabilityVo;
import org.openeuler.sbom.manager.service.SbomService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Controller
@RequestMapping(path = "/sbom-api")
public class SbomController {

    private static final Logger logger = LoggerFactory.getLogger(SbomController.class);

    @Autowired
    private SbomService sbomService;

    @PostMapping("/uploadSbomFile")
    public @ResponseBody ResponseEntity uploadSbomFile(HttpServletRequest request, @RequestParam String productId) throws IOException {//HttpServletRequest request
        MultipartFile file = ((MultipartHttpServletRequest) request).getFile("uploadFileName");
        if (file == null || file.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body("upload file is empty");
        }
        String fileName = file.getOriginalFilename();
        logger.info("upload {}`s sbom file name: {}, file length: {}", productId, fileName, file.getBytes().length);

        try {
            sbomService.readSbomFile(productId, fileName, file.getBytes());
        } catch (Exception e) {
            logger.error("uploadSbomFile failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }

        return ResponseEntity.status(HttpStatus.ACCEPTED).body("Success");
    }

    @RequestMapping("/exportSbomFile")
    public void exportSbomFile(HttpServletRequest request, HttpServletResponse response, @RequestParam String productId, @RequestParam String spec,
                               @RequestParam String specVersion, @RequestParam String format) throws IOException {
        logger.info("download original sbom file productId:{}, use spec:{}, specVersion:{}, format:{}",
                productId,
                spec,
                specVersion,
                format);
        RawSbom rawSbom = null;
        String errorMsg = null;

        try {
            rawSbom = sbomService.writeSbomFile(productId, spec, specVersion, format);
        } catch (Exception e) {
            logger.error("exportSbomFile failed", e);
            errorMsg = e.getMessage();
        }

        response.reset();

        if (rawSbom == null) {
            String returnContent =
                    StringUtils.hasText(errorMsg) ? errorMsg :
                            "can not find %s`s sbom, use spec:%s, specVersion:%s, format:%s".formatted(
                                    productId,
                                    spec,
                                    specVersion,
                                    format);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.setContentType("text/plain");
            response.addHeader("Content-Length", "" + returnContent.getBytes(StandardCharsets.UTF_8).length);
            //CORS
            String origin = request.getHeader("origin");
            if (SbomConstants.ALLOW_ORIGINS.contains(origin)) {
                response.addHeader("Access-Control-Allow-Origin", origin);
                response.addHeader("Access-Control-Allow-Methods", "POST");
                response.addHeader("Access-Control-Allow-Headers", "Content-Type");
                response.addHeader("Access-Control-Expose-Headers", "Content-Disposition");
            }

            OutputStream outputStream = new BufferedOutputStream(response.getOutputStream());
            outputStream.write(returnContent.getBytes(StandardCharsets.UTF_8));
            outputStream.flush();
        } else {
            byte[] exportContent = rawSbom.getValue();
            String fileName = "%s-%s-sbom.%s".formatted(productId, spec, format);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition", "attachment;filename=" +
                    URLEncoder.encode(fileName, StandardCharsets.UTF_8));
            response.addHeader("Content-Length", "" + exportContent.length);

            OutputStream outputStream = new BufferedOutputStream(response.getOutputStream());
            outputStream.write(exportContent);
            outputStream.flush();
        }
    }

    @RequestMapping("/exportSbom")
    public void exportSbom(HttpServletRequest request, HttpServletResponse response, @RequestParam String productId, @RequestParam String spec,
                           @RequestParam String specVersion, @RequestParam String format) throws IOException {
        logger.info("download sbom metadata productId:{}, use spec:{}, specVersion:{}, format:{}",
                productId,
                spec,
                specVersion,
                format);
        byte[] sbom = null;
        String errorMsg = null;

        try {
            sbom = sbomService.writeSbom(productId, spec, specVersion, format);
        } catch (Exception e) {
            logger.error("export sbom metadata failed", e);
            errorMsg = e.getMessage();
        }

        response.reset();
        if (ArrayUtils.isEmpty(sbom)) {
            String returnContent =
                    StringUtils.hasText(errorMsg) ? errorMsg :
                            "can not find %s`s sbom metadata, use spec:%s, specVersion:%s, format:%s".formatted(
                                    productId,
                                    spec,
                                    specVersion,
                                    format);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.setContentType("text/plain");
            response.addHeader("Content-Length", "" + returnContent.getBytes(StandardCharsets.UTF_8).length);

            OutputStream outputStream = new BufferedOutputStream(response.getOutputStream());
            outputStream.write(returnContent.getBytes(StandardCharsets.UTF_8));
            outputStream.flush();
        } else {
            String fileName = "%s-%s-sbom.%s".formatted(productId, spec, format);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition", "attachment;filename=" +
                    URLEncoder.encode(fileName, StandardCharsets.UTF_8));
            response.addHeader("Content-Length", "" + sbom.length);
            //CORS
            String origin = request.getHeader("origin");
            if (SbomConstants.ALLOW_ORIGINS.contains(origin)) {
                response.addHeader("Access-Control-Allow-Origin", origin);
                response.addHeader("Access-Control-Allow-Methods", "POST");
                response.addHeader("Access-Control-Allow-Headers", "Content-Type");
                response.addHeader("Access-Control-Expose-Headers", "Content-Disposition");
            }

            OutputStream outputStream = new BufferedOutputStream(response.getOutputStream());
            outputStream.write(sbom);
            outputStream.flush();
        }
    }

    @PostMapping("/querySbomPackages")
    public @ResponseBody ResponseEntity querySbomPackages(@RequestParam("productId") String productId,
                                                          @RequestParam(value = "packageName", required = false) String packageName,
                                                          @RequestParam(value = "isExactly", required = false) Boolean isExactly,
                                                          @RequestParam(name = "page", required = false, defaultValue = "0") Integer page,
                                                          @RequestParam(name = "size", required = false, defaultValue = "15") Integer size) {
        logger.info("query sbom packages by productId:{}, packageName:{}, isExactly:{}, page:{}, size:{}",
                productId,
                packageName,
                isExactly,
                page,
                size);
        PageVo<Package> packagesPage = sbomService.getPackageInfoByNameForPage(productId, packageName, isExactly, page, size);

        logger.info("query sbom packages result:{}", packagesPage);
        return ResponseEntity.status(HttpStatus.OK).body(packagesPage);
    }

    @GetMapping("/querySbomPackages/{productId}/{packageName}/{isExactly}")
    public @ResponseBody ResponseEntity getPackagesInfoByName(@PathVariable("productId") String productId,
                                                              @PathVariable("packageName") String packageName,
                                                              @PathVariable(value = "isExactly") boolean isExactly) {
        logger.info("query sbom packages by productId:{}, packageName:{}, isExactly:{}", productId, packageName, isExactly);
        List<Package> packagesList = sbomService.queryPackageInfoByName(productId, packageName, isExactly);

        logger.info("query sbom packages result:{}", packagesList);
        return ResponseEntity.status(HttpStatus.OK).body(packagesList);
    }

    @GetMapping("/querySbomPackage/{packageId}")
    public @ResponseBody ResponseEntity getPackageInfoById(@PathVariable("packageId") String packageId) {
        logger.info("query sbom package by packageId:{}", packageId);
        Package packageInfo = null;
        try {
            packageInfo = sbomService.queryPackageInfoById(packageId);
        } catch (RuntimeException e) {
            logger.error("query sbom package error:", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }

        logger.info("query sbom package result:{}", packageInfo);
        if (packageInfo == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("packageId:%s is not exist".formatted(packageId));
        }
        return ResponseEntity.status(HttpStatus.OK).body(packageInfo);
    }

    @GetMapping("/queryPackageBinaryManagement/{packageId}/{binaryType}")
    public @ResponseBody ResponseEntity queryPackageBinaryManagement(@PathVariable("packageId") String packageId,
                                                                     @PathVariable("binaryType") String binaryType) {
        logger.info("query package binary management by packageId:{}, binaryType:{}", packageId, binaryType);

        BinaryManagementVo result = sbomService.queryPackageBinaryManagement(packageId, binaryType);

        logger.info("query package binary management result:{}", result);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }


    @PostMapping("/querySbomPackagesByBinary")
    public @ResponseBody ResponseEntity queryPackageInfoByBinary(@RequestParam("productId") String productId,
                                                                 @RequestParam("binaryType") String binaryType,
                                                                 @RequestParam("type") String type,
                                                                 @RequestParam(name = "namespace", required = false) String namespace,
                                                                 @RequestParam(name = "name", required = false) String name,
                                                                 @RequestParam(name = "version", required = false) String version,
                                                                 @RequestParam(name = "page", required = false, defaultValue = "0") Integer page,
                                                                 @RequestParam(name = "size", required = false, defaultValue = "15") Integer size) {
        logger.info("query package info by packageId:{}, binaryType:{}, type:{}, namespace:{}, name:{}, version:{}", productId,
                binaryType,
                type,
                namespace,
                name,
                version);

        PackageUrlVo purl = new PackageUrlVo(type, namespace, name, version);
        Pageable pageable = PageRequest.of(page, size);
        PageVo<PackagePurlVo> queryResult = null;

        try {
            queryResult = sbomService.queryPackageInfoByBinaryViaSpec(productId,
                    binaryType,
                    purl,
                    pageable);
        } catch (RuntimeException e) {
            logger.error("query sbom packages failed.", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        } catch (Exception e) {
            logger.error("query sbom packages failed.", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("query sbom packages failed.");
        }

        logger.info("query sbom packages result:{}", queryResult == null ? 0 : queryResult.getTotalElements());
        return ResponseEntity.status(HttpStatus.OK).body(queryResult);
    }

    @GetMapping("/queryProductType")
    public @ResponseBody ResponseEntity queryProductType() {
        logger.info("query product type");
        List<String> queryResult;

        try {
            queryResult = sbomService.queryProductType();
        } catch (RuntimeException e) {
            logger.error("query product type failed.", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        } catch (Exception e) {
            logger.error("query product type failed.", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("query product type failed.");
        }

        logger.info("query product type result:{}", queryResult);
        return ResponseEntity.status(HttpStatus.OK).body(queryResult);
    }

    @GetMapping("/queryProductConfig/{productType}")
    public @ResponseBody ResponseEntity queryProductConfigByProductType(@PathVariable("productType") String productType) {
        logger.info("query product config by productType:{}", productType);
        List<ProductConfigVo> queryResult;

        try {
            queryResult = sbomService.queryProductConfigByProductType(productType);
        } catch (RuntimeException e) {
            logger.error("query product config failed.", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        } catch (Exception e) {
            logger.error("query product config failed.", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("query product config failed.");
        }

        logger.info("query product config result size:{}", queryResult.size());
        return ResponseEntity.status(HttpStatus.OK).body(queryResult);
    }

    @PostMapping("/queryProduct/{productType}")
    public @ResponseBody ResponseEntity queryProductByFullAttributes(@PathVariable("productType") String productType, @RequestBody Map<String, Object> attributes) {
        logger.info("query product info by productType:{}, attributes:{}", productType, attributes);
        attributes.put("productType", productType);

        try {
            Product queryResult = sbomService.queryProductByFullAttributes(attributes);

            if (queryResult == null) {
                logger.info("query product info result is null");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("product is not exist");
            } else {
                logger.info("query product info result:{}", queryResult);
                return ResponseEntity.status(HttpStatus.OK).body(queryResult);
            }
        } catch (RuntimeException e) {
            logger.error("query product info failed.", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        } catch (Exception e) {
            logger.error("query product info failed.", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("query product info failed.");
        }
    }

    @GetMapping("/queryPackageVulnerability/{packageId}")
    public @ResponseBody ResponseEntity queryVulnerabilityByPackageId(@PathVariable("packageId") String packageId,
                                                                      @RequestParam(name = "page", required = false, defaultValue = "0") Integer page,
                                                                      @RequestParam(name = "size", required = false, defaultValue = "15") Integer size) {
        logger.info("query package vulnerability by packageId: {}", packageId);
        PageVo<VulnerabilityVo> vulnerabilities;
        Pageable pageable = PageRequest.of(page, size);
        try {
            vulnerabilities = sbomService.queryVulnerabilityByPackageId(packageId, pageable);
        } catch (RuntimeException e) {
            logger.error("query package vulnerability error: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        } catch (Exception e) {
            logger.error("query package vulnerability error: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("query package vulnerability error");
        }

        logger.info("query package vulnerability result:{}", Objects.isNull(vulnerabilities) ? 0 : vulnerabilities.getTotalElements());
        return ResponseEntity.status(HttpStatus.OK).body(vulnerabilities);
    }

}
