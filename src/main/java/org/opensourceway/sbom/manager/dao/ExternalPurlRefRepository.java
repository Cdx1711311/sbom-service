package org.opensourceway.sbom.manager.dao;

import org.opensourceway.sbom.manager.model.ExternalPurlRef;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ExternalPurlRefRepository extends JpaRepository<ExternalPurlRef, UUID>, JpaSpecificationExecutor<ExternalPurlRef> {

    @Query(value = "SELECT * FROM external_purl_ref WHERE category = :category AND type = :type AND pkg_id = :pkgId",
            nativeQuery = true)
    List<ExternalPurlRef> queryPackageRef(@Param("pkgId") UUID pkgId, @Param("category") String category, @Param("type") String type);

    @Query(value = "SELECT A.* FROM external_purl_ref A, package B WHERE A.pkg_id = B.id AND B.sbom_id = :sbomId",
            nativeQuery = true)
    List<ExternalPurlRef> findBySbomId(UUID sbomId);
}