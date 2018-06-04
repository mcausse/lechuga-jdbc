package org.fijoles.jdbc.extractor;

import java.util.List;

public class Pager<T> {

    final int pageSize;
    final int numPage;

    final int totalRows;
    final int totalPages;

    final List<T> page;

    public Pager(int pageSize, int numPage, int totalRows, int totalPages, List<T> page) {
        super();
        this.pageSize = pageSize;
        this.numPage = numPage;
        this.totalRows = totalRows;
        this.totalPages = totalPages;
        this.page = page;
    }

    public int getPageSize() {
        return pageSize;
    }

    public int getNumPage() {
        return numPage;
    }

    public int getTotalRows() {
        return totalRows;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public List<T> getPage() {
        return page;
    }

    @Override
    public String toString() {
        return String.format("Pager [pageSize=%s, numPage=%s, totalRows=%s, totalPages=%s, page=%s]", pageSize, numPage,
                totalRows, totalPages, page);
    }

}