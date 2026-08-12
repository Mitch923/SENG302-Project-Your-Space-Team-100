package nz.ac.canterbury.seng302.homehelper.utils;

import org.springframework.data.domain.Sort;

/**
 * Sorting options for competition designs by vote count or name.
 */
public enum DesignSortingType {

    /**
     * Vote count ascending (lowest first).
     */
    VOTES_ASC(Sort.by("voteCount").ascending()),

    /**
     * Vote count descending (highest first).
     */
    VOTES_DESC(Sort.by("voteCount").descending()),

    /**
     * Name ascending (A–Z).
     */
    NAME_ASC(Sort.by("name").ascending()),

    /**
     * Name descending (Z–A).
     */
    NAME_DESC(Sort.by("name").descending());

    private final Sort sort;

    /**
     * @param sort sort configuration for this option
     */
    DesignSortingType(Sort sort) {
        this.sort = sort;
    }

    public Sort getSort() {
        return sort;
    }
}
