package com.deivid22srk.microsoftrewards.model;

import java.io.Serializable;

public class SearchItem implements Serializable {
    private String searchText;
    private int index;
    private SearchStatus status;
    private long timestamp;

    public enum SearchStatus {
        PENDING("Pendente"),
        IN_PROGRESS("Em andamento"),
        COMPLETED("Concluído"),
        FAILED("Falhou");

        private final String displayName;

        SearchStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public SearchItem(String searchText, int index) {
        this.searchText = searchText;
        this.index = index;
        this.status = SearchStatus.PENDING;
        this.timestamp = System.currentTimeMillis();
    }

    public String getSearchText() {
        return searchText;
    }

    public void setSearchText(String searchText) {
        this.searchText = searchText;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public SearchStatus getStatus() {
        return status;
    }

    public void setStatus(SearchStatus status) {
        this.status = status;
        this.timestamp = System.currentTimeMillis();
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        SearchItem that = (SearchItem) obj;
        return index == that.index && searchText.equals(that.searchText);
    }

    @Override
    public int hashCode() {
        return searchText.hashCode() * 31 + index;
    }

    @Override
    public String toString() {
        return "SearchItem{" +
                "searchText='" + searchText + '\'' +
                ", index=" + index +
                ", status=" + status +
                ", timestamp=" + timestamp +
                '}';
    }
}