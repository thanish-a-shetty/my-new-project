package com.fintech.chat;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KnowledgeRepository extends JpaRepository<KnowledgeDoc, Long> {

    /**
     * Search knowledge documents by keywords
     * @param keywords the search keywords
     * @param limit maximum number of results
     * @return list of matching knowledge documents
     */
    @Query("SELECT k FROM KnowledgeDoc k WHERE " +
           "LOWER(k.title) LIKE LOWER(CONCAT('%', :keywords, '%')) OR " +
           "LOWER(k.content) LIKE LOWER(CONCAT('%', :keywords, '%')) OR " +
           "LOWER(k.tags) LIKE LOWER(CONCAT('%', :keywords, '%')) " +
           "ORDER BY k.relevanceScore DESC")
    List<KnowledgeDoc> searchByKeywords(@Param("keywords") String keywords, @Param("limit") int limit);

    /**
     * Search knowledge documents by keywords with default limit
     * @param keywords the search keywords
     * @return list of matching knowledge documents
     */
    @Query("SELECT k FROM KnowledgeDoc k WHERE " +
           "LOWER(k.title) LIKE LOWER(CONCAT('%', :keywords, '%')) OR " +
           "LOWER(k.content) LIKE LOWER(CONCAT('%', :keywords, '%')) OR " +
           "LOWER(k.tags) LIKE LOWER(CONCAT('%', :keywords, '%')) " +
           "ORDER BY k.relevanceScore DESC")
    List<KnowledgeDoc> searchByKeywords(@Param("keywords") String keywords);

    /**
     * Find knowledge documents by category
     * @param category the category
     * @return list of knowledge documents in the category
     */
    List<KnowledgeDoc> findByCategory(String category);

    /**
     * Find knowledge documents by tags
     * @param tag the tag to search for
     * @return list of knowledge documents with the tag
     */
    @Query("SELECT k FROM KnowledgeDoc k WHERE LOWER(k.tags) LIKE LOWER(CONCAT('%', :tag, '%'))")
    List<KnowledgeDoc> findByTag(@Param("tag") String tag);

    /**
     * Find knowledge documents by title containing text
     * @param titleText the text to search in title
     * @return list of matching knowledge documents
     */
    @Query("SELECT k FROM KnowledgeDoc k WHERE LOWER(k.title) LIKE LOWER(CONCAT('%', :titleText, '%'))")
    List<KnowledgeDoc> findByTitleContaining(@Param("titleText") String titleText);

    /**
     * Find knowledge documents by content containing text
     * @param contentText the text to search in content
     * @return list of matching knowledge documents
     */
    @Query("SELECT k FROM KnowledgeDoc k WHERE LOWER(k.content) LIKE LOWER(CONCAT('%', :contentText, '%'))")
    List<KnowledgeDoc> findByContentContaining(@Param("contentText") String contentText);

    /**
     * Find active knowledge documents
     * @return list of active knowledge documents
     */
    @Query("SELECT k FROM KnowledgeDoc k WHERE k.active = true ORDER BY k.relevanceScore DESC")
    List<KnowledgeDoc> findActiveDocuments();

    /**
     * Find knowledge documents with high relevance score
     * @param minScore minimum relevance score
     * @return list of knowledge documents above the score threshold
     */
    @Query("SELECT k FROM KnowledgeDoc k WHERE k.relevanceScore >= :minScore ORDER BY k.relevanceScore DESC")
    List<KnowledgeDoc> findByRelevanceScoreGreaterThanEqual(@Param("minScore") Double minScore);

    /**
     * Count knowledge documents by category
     * @param category the category
     * @return count of documents in the category
     */
    long countByCategory(String category);

    /**
     * Count active knowledge documents
     * @return count of active documents
     */
    long countByActiveTrue();

    /**
     * Find knowledge documents created after a specific date
     * @param date the date threshold
     * @return list of knowledge documents created after the date
     */
    @Query("SELECT k FROM KnowledgeDoc k WHERE k.createdAt > :date ORDER BY k.createdAt DESC")
    List<KnowledgeDoc> findByCreatedAtAfter(@Param("date") java.time.LocalDateTime date);

    /**
     * Find knowledge documents updated after a specific date
     * @param date the date threshold
     * @return list of knowledge documents updated after the date
     */
    @Query("SELECT k FROM KnowledgeDoc k WHERE k.updatedAt > :date ORDER BY k.updatedAt DESC")
    List<KnowledgeDoc> findByUpdatedAtAfter(@Param("date") java.time.LocalDateTime date);

    /**
     * Search knowledge documents with multiple criteria
     * @param keywords the search keywords
     * @param category the category filter
     * @param activeOnly whether to filter by active status
     * @return list of matching knowledge documents
     */
    @Query("SELECT k FROM KnowledgeDoc k WHERE " +
           "(LOWER(k.title) LIKE LOWER(CONCAT('%', :keywords, '%')) OR " +
           "LOWER(k.content) LIKE LOWER(CONCAT('%', :keywords, '%')) OR " +
           "LOWER(k.tags) LIKE LOWER(CONCAT('%', :keywords, '%'))) " +
           "AND (:category IS NULL OR k.category = :category) " +
           "AND (:activeOnly = false OR k.active = true) " +
           "ORDER BY k.relevanceScore DESC")
    List<KnowledgeDoc> searchWithFilters(@Param("keywords") String keywords, 
                                        @Param("category") String category, 
                                        @Param("activeOnly") boolean activeOnly);
}
