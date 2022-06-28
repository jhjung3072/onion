package com.onion.user;

import com.onion.domain.Location;
import com.onion.domain.QUser;
import com.onion.domain.Tag;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;

import java.util.Set;


public class UserPredicates {


    public static Predicate findByTagsAndLocation(Set<Tag> tags, Location location) {
        QUser user = QUser.user;
        return user.location.in(location).and(user.tags.any().in(tags));
    }
}
