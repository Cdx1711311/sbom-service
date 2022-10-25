package org.opensourceway.sbom.manager.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.util.UUID;

/**
 * A relationship between two sbom elements.
 */
@Entity
@Table(indexes = {
        @Index(name = "sbom_element_uk", columnList = "sbom_id, element_id, related_element_id, relationship_type", unique = true),
        @Index(name = "sbom_id_idx", columnList = "sbom_id"),
        @Index(name = "sbom_related_element_idx", columnList = "sbom_id, related_element_id"),
})
public class SbomElementRelationship {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    private UUID id;

    /**
     * The source element of this directed relationship.
     */
    @Column(columnDefinition = "TEXT", nullable = false, name = "element_id")
    private String elementId;

    /**
     * The target element of this directed relationship.
     */
    @Column(columnDefinition = "TEXT", nullable = false, name = "related_element_id")
    private String RelatedElementId;

    /**
     * The type of this relationship.
     */
    @Column(columnDefinition = "TEXT", nullable = false, name = "relationship_type")
    private String RelationshipType;

    @Column(columnDefinition = "TEXT")
    private String comment;

    /**
     * Sbom document that holds the relationship.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sbom_id", foreignKey = @ForeignKey(name = "sbom_id_fk"))
    @JsonIgnore
    private Sbom sbom;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getElementId() {
        return elementId;
    }

    public void setElementId(String elementId) {
        this.elementId = elementId;
    }

    public String getRelatedElementId() {
        return RelatedElementId;
    }

    public void setRelatedElementId(String relatedElementId) {
        RelatedElementId = relatedElementId;
    }

    public String getRelationshipType() {
        return RelationshipType;
    }

    public void setRelationshipType(String relationshipType) {
        RelationshipType = relationshipType;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Sbom getSbom() {
        return sbom;
    }

    public void setSbom(Sbom sbom) {
        this.sbom = sbom;
    }
}
