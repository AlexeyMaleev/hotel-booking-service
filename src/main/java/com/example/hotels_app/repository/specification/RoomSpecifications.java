package com.example.hotels_app.repository.specification;

import com.example.hotels_app.entity.Booking;
import com.example.hotels_app.entity.Hotel;
import com.example.hotels_app.entity.Room;
import com.example.hotels_app.model.request.HotelFilter;
import com.example.hotels_app.model.request.RoomFilter;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

public interface RoomSpecifications {

    static Specification<Room> withFilter(RoomFilter filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter.getId() != null) {
                predicates.add(cb.equal(root.get("id"), filter.getId()));
            }

            if (filter.getName() != null) {
                predicates.add(cb.like(cb.lower(root.get("name")), "%" + filter.getName().toLowerCase() + "%"));
            }

            if (filter.getMinPrice() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("price"), filter.getMinPrice()));
            }

            if (filter.getMaxPrice() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), filter.getMaxPrice()));
            }

            if (filter.getCapacity() != null) {
                predicates.add(cb.equal(root.get("capacity"), filter.getCapacity()));
            }

            if (filter.getArrival() != null && filter.getDeparture() !=null){

                // subquery to bookings table
                Subquery<Long> subquery = query.subquery(Long.class);
                Root<Booking> bookingRoot = subquery.from(Booking.class);
                subquery.select(bookingRoot.get("room").get("id"));

                /*
                Predicate overlap = cb.and(
                        cb.lessThan(bookingRoot.get("arrival"), filter.getDeparture()),
                        cb.greaterThan(bookingRoot.get("departure"), filter.getArrival())
                );
                */

                Instant start = filter.getArrival().atStartOfDay(ZoneOffset.UTC).toInstant();
                Instant end = filter.getDeparture().atStartOfDay(ZoneOffset.UTC).toInstant();

                // Date intersection condition
                Predicate overlap = cb.and(
                        cb.lessThan(bookingRoot.get("arrival"), end),
                        cb.greaterThan(bookingRoot.get("departure"), start)
                );
                subquery.where(overlap);

                // exclude rooms with overlapping dates
                predicates.add(cb.not(root.get("id").in(subquery)));
            }

            if ( filter.getHotelId() != null) {
                //predicates.add(cb.equal(root.get("hotelId"), filter.getHotelId()));
                predicates.add(cb.equal(root.get("hotel").get("id"), filter.getHotelId()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
