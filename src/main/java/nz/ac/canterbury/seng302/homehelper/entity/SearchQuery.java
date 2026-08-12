package nz.ac.canterbury.seng302.homehelper.entity;

// Data object for search queries
public class SearchQuery {

    private String query;

    private int page;

    private int resultsPerPage;

    private long id;

    private int totalPages;

    public SearchQuery(String query, int page, int resultsPerPage, int totalPages, long id) {
        this.query = query;
        this.page = page;
        this.resultsPerPage = resultsPerPage;
        this.totalPages = totalPages;
        this.id = id;
    }

    public SearchQuery(String query, long id) {
        this.setDefaultValues();
        this.query = query;
        this.id = id;
    }

    public SearchQuery(long id) {
        this.setDefaultValues();
        this.id = id;
    }

    public SearchQuery() {
        this.setDefaultValues();
    }

    public void setDefaultValues() {
        this.query = "";
        this.page = 1;
        this.resultsPerPage = 4;
        this.totalPages = 1;
        this.id = 0;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getResultsPerPage() {
        return resultsPerPage;
    }

    public void setResultsPerPage(int resultsPerPage) {
        this.resultsPerPage = resultsPerPage;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public void resetIfNotMatches(long id) {
        if (this.id != id) {
            this.setDefaultValues();
            this.id = id;
        }
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }


}
