package com.maahish.catalog.repository;

import com.maahish.catalog.entity.Product;
import com.maahish.catalog.enums.ProductStatus;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Repository
public class ProductRepositoryImpl implements ProductRepositoryCustom {

    private static final String DEFAULT_SORT_FIELD = "createdAt";
    private static final Map<String, String> ALLOWED_SORT_FIELDS = Map.of(
            "createdAt", "createdAt",
            "updatedAt", "updatedAt",
            "name", "name",
            "price", "price",
            "sellingPrice", "sellingPrice",
            "rating", "rating",
            "reviewCount", "reviewCount",
            "stock", "stock"
    );

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Page<Product> searchProducts(
            String keyword,
            Long categoryId,
            Long fabricTypeId,
            String color,
            String occasion,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            String sortBy,
            String sortDir,
            Pageable pageable) {

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Product> query = cb.createQuery(Product.class);
        Root<Product> root = query.from(Product.class);
        List<Predicate> predicates = buildPredicates(cb, root, keyword, categoryId, fabricTypeId, color, occasion, minPrice, maxPrice);
        query.where(predicates.toArray(new Predicate[0]));
        query.orderBy(buildOrder(cb, root, sortBy, sortDir));

        TypedQuery<Product> typedQuery = entityManager.createQuery(query);
        typedQuery.setFirstResult((int) pageable.getOffset());
        typedQuery.setMaxResults(pageable.getPageSize());
        List<Product> products = typedQuery.getResultList();

        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<Product> countRoot = countQuery.from(Product.class);
        countQuery.select(cb.count(countRoot));
        countQuery.where(buildPredicates(cb, countRoot, keyword, categoryId, fabricTypeId, color, occasion, minPrice, maxPrice)
                .toArray(new Predicate[0]));
        Long total = entityManager.createQuery(countQuery).getSingleResult();

        return new PageImpl<>(products, pageable, total);
    }

    @Override
    public Page<Product> adminSearchProducts(
            String keyword,
            Long categoryId,
            ProductStatus status,
            String sortBy,
            String sortDir,
            Pageable pageable) {

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Product> query = cb.createQuery(Product.class);
        Root<Product> root = query.from(Product.class);
        List<Predicate> predicates = buildAdminPredicates(cb, root, keyword, categoryId, status);
        query.where(predicates.toArray(new Predicate[0]));
        query.orderBy(buildOrder(cb, root, sortBy, sortDir));

        TypedQuery<Product> typedQuery = entityManager.createQuery(query);
        typedQuery.setFirstResult((int) pageable.getOffset());
        typedQuery.setMaxResults(pageable.getPageSize());
        List<Product> products = typedQuery.getResultList();

        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<Product> countRoot = countQuery.from(Product.class);
        countQuery.select(cb.count(countRoot));
        countQuery.where(buildAdminPredicates(cb, countRoot, keyword, categoryId, status)
                .toArray(new Predicate[0]));
        Long total = entityManager.createQuery(countQuery).getSingleResult();

        return new PageImpl<>(products, pageable, total);
    }

    @Override
    public List<Product> findRelatedProducts(Long productId, Long categoryId, int limit) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Product> query = cb.createQuery(Product.class);
        Root<Product> root = query.from(Product.class);
        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.equal(root.get("status"), ProductStatus.ACTIVE));
        predicates.add(cb.notEqual(root.get("id"), productId));
        if (categoryId != null) {
            predicates.add(cb.equal(root.get("category").get("id"), categoryId));
        }
        query.where(predicates.toArray(new Predicate[0]));
        query.orderBy(cb.desc(root.get("rating")));

        return entityManager.createQuery(query).setMaxResults(limit).getResultList();
    }

    private List<Predicate> buildPredicates(
            CriteriaBuilder cb,
            Root<Product> root,
            String keyword,
            Long categoryId,
            Long fabricTypeId,
            String color,
            String occasion,
            BigDecimal minPrice,
            BigDecimal maxPrice) {

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.equal(root.get("status"), ProductStatus.ACTIVE));

        if (StringUtils.hasText(keyword)) {
            String pattern = "%" + keyword.toLowerCase() + "%";
            predicates.add(cb.or(
                    cb.like(cb.lower(root.get("name")), pattern),
                    cb.like(cb.lower(root.get("description")), pattern),
                    cb.like(cb.lower(root.get("productCode")), pattern)
            ));
        }
        if (categoryId != null) {
            predicates.add(cb.equal(root.get("category").get("id"), categoryId));
        }
        if (fabricTypeId != null) {
            predicates.add(cb.equal(root.get("fabricType").get("id"), fabricTypeId));
        }
        if (StringUtils.hasText(color)) {
            predicates.add(cb.equal(cb.lower(root.get("color")), color.toLowerCase()));
        }
        if (StringUtils.hasText(occasion)) {
            String occ = occasion.toLowerCase();
            predicates.add(cb.or(
                    cb.equal(cb.lower(root.get("occasion")), "all"),
                    cb.equal(cb.lower(root.get("occasion")), occ),
                    cb.like(cb.lower(root.get("occasion")), occ + ",%"),
                    cb.like(cb.lower(root.get("occasion")), "%," + occ + ",%"),
                    cb.like(cb.lower(root.get("occasion")), "%," + occ)
            ));
        }
        if (minPrice != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("sellingPrice"), minPrice));
        }
        if (maxPrice != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("sellingPrice"), maxPrice));
        }
        return predicates;
    }

    private List<Predicate> buildAdminPredicates(
            CriteriaBuilder cb,
            Root<Product> root,
            String keyword,
            Long categoryId,
            ProductStatus status) {

        List<Predicate> predicates = new ArrayList<>();
        if (status != null) {
            predicates.add(cb.equal(root.get("status"), status));
        }
        if (StringUtils.hasText(keyword)) {
            String pattern = "%" + keyword.toLowerCase() + "%";
            predicates.add(cb.or(
                    cb.like(cb.lower(root.get("name")), pattern),
                    cb.like(cb.lower(root.get("description")), pattern),
                    cb.like(cb.lower(root.get("productCode")), pattern)
            ));
        }
        if (categoryId != null) {
            predicates.add(cb.equal(root.get("category").get("id"), categoryId));
        }
        return predicates;
    }

    private jakarta.persistence.criteria.Order buildOrder(CriteriaBuilder cb, Root<Product> root, String sortBy, String sortDir) {
        String field = resolveSortField(sortBy);
        Sort.Direction direction = "asc".equalsIgnoreCase(sortDir) ? Sort.Direction.ASC : Sort.Direction.DESC;
        return direction == Sort.Direction.ASC ? cb.asc(root.get(field)) : cb.desc(root.get(field));
    }

    private String resolveSortField(String sortBy) {
        if (!StringUtils.hasText(sortBy)) {
            return DEFAULT_SORT_FIELD;
        }
        return ALLOWED_SORT_FIELDS.getOrDefault(sortBy, DEFAULT_SORT_FIELD);
    }
}
