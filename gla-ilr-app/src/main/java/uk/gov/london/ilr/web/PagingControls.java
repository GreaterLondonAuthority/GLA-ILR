/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ilr.web;

import org.springframework.data.domain.Page;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Represents the UI elements for navigating a large paged list of data.
 *
 * Encapsulates the logic for mapping pages onto UI elements, allowing for
 * simpler unit testing and also enabling multiple implementations of the
 * paging control logic without affecting the pages on which the elements
 * are displayed.
 *
 * The elements() method returns a list of the UI controls that should be
 * displayed, with each having the text to display in the control and the
 * number of the page that the control should lead to.
 *
 * @author Steve Leach
 */
public class PagingControls<T> {

    static final int MAX_CONTROLS = 7;
    static final String GAP_TEXT = "...";

    private List<Element> elements = new LinkedList<>();
    private Page<T> page;

    public static class Element {
        private final String text;
        private final int pageNum;
        private final boolean isCurrent;

        public Element(String text, int pageNum, boolean isCurrent) {
            this.text = text;
            this.pageNum = pageNum;
            this.isCurrent = isCurrent;
        }

        public String getText() {
            return text;
        }

        public int getPageNum() {
            return pageNum;
        }

        public boolean isCurrent() {
            return isCurrent;
        }
    }

    /**
     * Creates a PagingControls object from the specified Page object.
     */
    public PagingControls(Page<T> page) {
        if (page != null) {
            this.page = page;
        }

        beforeBuild();

        buildElements(page);

        afterBuild();
    }

    /**
     * Called before building the elements.
     *
     * Can be overridden to prepare for building.
     */
    protected void beforeBuild() {}

    /**
     * Builds the page control elements.
     *
     * Uses a simple 7 element strategy, but can be overridden by subclasses
     * to provide alternative behaviour.
     */
    protected void buildElements(Page<?> page) {
        if ((page == null) || (page.getTotalPages() == 0)) {
            // No pages = no elements
        } else if (page.getTotalPages() <= MAX_CONTROLS) {
            addAllPages(page);
        } else if (isNearStart(page)) {
            addStartPages(page);
        } else if (isNearEnd(page)) {
            addEndPages(page);
        } else {
            addMiddlePages(page);
        }
    }

    boolean isNearStart(Page<?> page) {
        return (page.getNumber() + 1) <= (MAX_CONTROLS - 3);
    }

    boolean isNearEnd(Page<?> page) {
        return (page.getTotalPages() - (page.getNumber() + 1)) <= (MAX_CONTROLS - 3);
    }

    /**
     * Less pages than max elements, so display them all
     */
    protected final void addAllPages(Page<?> page) {
        for (int n = 1; n <= page.getTotalPages(); n++) {
            addControl(n, n == page.getNumber() + 1);
        }
    }

    /**
     * More pages than we can display, and near the start.
     */
    private void addStartPages(Page<?> page) {
        for (int n = 1; n <= 5; n++) {
            addControl(n, n == page.getNumber() + 1);
        }

        addControl(gapText(page), 6);

        addControl(page.getTotalPages());
    }

    /**
     * More pages than we can display, and in the middle
     */
    private void addMiddlePages(Page<?> page) {
        addControl(1);

        addControl(gapText(page), page.getNumber() - 1);

        addControl(page.getNumber());
        addControl(page.getNumber() + 1, true);
        addControl(page.getNumber() + 2);

        addControl(gapText(page), page.getNumber() + 3);

        addControl(page.getTotalPages());
    }

    /**
     * More pages than we can display, and near the end
     */
    private void addEndPages(Page<?> page) {
        addControl(1);

        addControl(gapText(page), page.getTotalPages() - 5);

        for (int n = 4; n >= 0; n--) {
            addControl(page.getTotalPages() - n, page.getTotalPages() - n == page.getNumber() + 1);
        }
    }

    /**
     * Called after building the elements.
     *
     * Can be overridden to release any resources after building.
     */
    protected void afterBuild() {}

    /**
     * Provides the text that should be used to indicate gaps in the page range.
     */
    protected String gapText(Page<?> page) {
        return GAP_TEXT;
    }


    protected void addControl(String text, int pageNum, boolean isCurrent) {
        elements.add(new Element(text, pageNum, isCurrent));
    }

    protected void addControl(String text, int pageNum) {
        elements.add(new Element(text, pageNum, false));
    }

    protected void addControl(int pageNum, boolean isCurrent) {
        addControl(Integer.toString(pageNum), pageNum, isCurrent);
    }

    protected void addControl(int pageNum) {
        addControl(Integer.toString(pageNum), pageNum);
    }

    /**
     * Returns a list of all the page control elements.
     */
    public final List<Element> elements() {
        return Collections.unmodifiableList(elements);
    }

    /**
     * Returns the element with the specified (0-based) index.
     */
    public final Element element(int index) {
        return elements.get(index);
    }

    /**
     * Returns the number of data items in each page.
     */
    public int getPageSize() {
        return this.page.getSize();
    }

    public Page<T> getPage() {
        return this.page;
    }

    public boolean isEmpty() {
        return page.isEmpty();
    }

    /**
     * Returns current page number
     */
    public int getPageNumber() {
        return page.getPageable().getPageNumber();
    }

    public int getTotalPages() {
        return page.getTotalPages();
    }

    public long getTotalElements() {
        return page.getTotalElements();
    }

    /**
     * Returns the index of the first element in the page (51-100 of 200)
     */
    public int getStartIndex() {
        return (page.getTotalElements() == 0) ? 0 : (page.getSize() * page.getPageable().getPageNumber()) + 1;
    }

    /**
     * Returns the index of the last element in the page (51-100 of 200)
     */
    public long getEndIndex() {
        return Math.min((page.getTotalElements() == 0) ? 0 : getStartIndex() + page.getSize() - 1, page.getTotalElements());
    }

    public List<T> getContent() {
        return page.getContent();
    }

}
