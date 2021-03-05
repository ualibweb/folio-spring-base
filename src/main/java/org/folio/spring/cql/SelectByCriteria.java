package org.folio.spring.cql;

import static java.util.Collections.singletonList;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import javax.persistence.EntityGraph;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@Component
public class SelectByCriteria {

  @PersistenceContext
  private EntityManager entityManager;

  private static final String FETCH_PROPERTIES_NAME = "javax.persistence.loadgraph";

  private static MultiValueMap<String, String> filter(String id, String userId) {
    LinkedMultiValueMap<String, String> map = new LinkedMultiValueMap<>();
    map.put(id, singletonList(userId));
    return map;
  }

  public <E> List<E> getByCriteria(Class<E> entityCls, int offset, int limit,
      MultiValueMap<String, String> filters, List<String> include) {
    CriteriaBuilder builder = entityManager.getCriteriaBuilder();

    CriteriaQuery<E> criteria = builder.createQuery(entityCls);
    Root<E> root = criteria.from(entityCls);

    List<Predicate> predicates = new ArrayList<>();
    if (filters != null) {
      for (Entry<String, List<String>> filter : filters.entrySet()) {
        if (filter.getValue().size() == 1) {
          predicates.add(
              builder.equal(root.get(filter.getKey()), filter.getValue().get(0)));
        } else {
          predicates.add(builder.or((Predicate[]) filter.getValue().stream()
              .map(e -> root.get(filter.getKey())).toArray()));
        }
      }
    }

    criteria.where(builder.and(predicates.toArray(new Predicate[0])));

    EntityGraph graph = entityManager.createEntityGraph(entityCls);
    include.forEach(graph::addSubgraph);

    List<E> users = entityManager
        .createQuery(criteria)
        .setHint(FETCH_PROPERTIES_NAME, graph)
        .setFirstResult(offset)
        .setMaxResults(limit)
        .getResultList();
    return users;
  }

}
