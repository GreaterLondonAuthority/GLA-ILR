/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ilr.web;

import org.springframework.data.domain.Page;

import java.util.List;

public class PagingControls<T> {

    private Page<T> page;

    public PagingControls(Page<T> page) {
        this.page = page;
    }

    public Page getPage() {
        return page;
    }

    public boolean isEmpty() {
        return page.isEmpty();
    }

    public int getPageNumber() {
        return page.getPageable().getPageNumber();
    }

    public int getTotalPages() {
        return page.getTotalPages();
    }

    public long getTotalElements() {
        return page.getTotalElements();
    }

    public int getStartIndex() {
        return (page.getTotalElements() == 0) ? 0 : (page.getSize() * page.getPageable().getPageNumber()) + 1;
    }

    public long getEndIndex() {
        return Math.min((page.getTotalElements() == 0) ? 0 : getStartIndex() + page.getSize() - 1, page.getTotalElements());
    }

    public List<T> getContent() {
        return page.getContent();
    }

}
