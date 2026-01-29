package com.example.hotels_app.repository.specification;

import com.example.hotels_app.entity.Hotel;
import com.example.hotels_app.model.request.HotelFilter;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;


public interface HotelSpecifications {

    static Specification<Hotel> withFilter(HotelFilter filter){
        return(root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (filter.getId() != null)
                predicates.add(cb.equal(root.get("id"), filter.getId()));

            if (filter.getName() != null)
                predicates.add(cb.like(cb.lower(root.get("name")), "%" + filter.getName().toLowerCase() + "%"));

            if (filter.getTitle() != null)
                predicates.add(cb.like(cb.lower(root.get("title")), "%" + filter.getTitle().toLowerCase() + "%"));

            if (filter.getCity() != null)
                predicates.add(cb.equal(cb.lower(root.get("city")), filter.getCity().toLowerCase()));

            if (filter.getAddress() != null)
                predicates.add(cb.like(cb.lower(root.get("address")), "%" + filter.getAddress().toLowerCase() + "%"));

            if (filter.getDistance() != null)
                predicates.add(cb.lessThanOrEqualTo(root.get("distance"), filter.getDistance()));

            if (filter.getRating() != null)
                predicates.add(cb.greaterThanOrEqualTo(root.get("rating"), filter.getRating()));

            if (filter.getRatingCount() != null)
                predicates.add(cb.greaterThanOrEqualTo(root.get("ratingCount"), filter.getRatingCount()));

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
