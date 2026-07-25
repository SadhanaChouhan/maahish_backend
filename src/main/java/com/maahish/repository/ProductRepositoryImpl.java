package com.maahish.repository;

import com.maahish.entity.Product;
import com.maahish.enums.ProductStatus;
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

@Repository
public class ProductRepositoryImpl implements ProductRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Page<Product> searchProducts(
            String keyword,
            Long categoryId,
            String fabric,
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
        List<Predicate> predicates = buildPredicates(cb, root, keyword, categoryId, fabric, color, occasion, minPrice, maxPrice);
        query.where(predicates.toArray(new Predicate[0]));
        query.orderBy(buildOrder(cb, root, sortBy, sortDir));

        TypedQuery<Product> typedQuery = entityManager.createQuery(query);
        typedQuery.setFirstResult((int) pageable.getOffset());
        typedQuery.setMaxResults(pageable.getPageSize());
        List<Product> products = typedQuery.getResultList();

        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<Product> countRoot = countQuery.from(Product.class);
        countQuery.select(cb.count(countRoot));
        countQuery.where(buildPredicates(cb, countRoot, keyword, categoryId, fabric, color, occasion, minPrice, maxPrice)
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
            String fabric,
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
        if (StringUtils.hasText(fabric)) {
            predicates.add(cb.equal(cb.lower(root.get("fabric")), fabric.toLowerCase()));
        }
        if (StringUtils.hasText(color)) {
            predicates.add(cb.equal(cb.lower(root.get("color")), color.toLowerCase()));
        }
        if (StringUtils.hasText(occasion)) {
            predicates.add(cb.equal(cb.lower(root.get("occasion")), occasion.toLowerCase()));
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

    private Order buildOrder(CriteriaBuilder cb, Root<Product> root, String sortBy, String sortDir) {
        String field = StringUtils.hasText(sortBy) ? sortBy : "createdAt";
        Sort.Direction direction = "asc".equalsIgnoreCase(sortDir) ? Sort.Direction.ASC : Sort.Direction.DESC;
        return direction == Sort.Direction.ASC ? cb.asc(root.get(field)) : cb.desc(root.get(field));
    }
}
