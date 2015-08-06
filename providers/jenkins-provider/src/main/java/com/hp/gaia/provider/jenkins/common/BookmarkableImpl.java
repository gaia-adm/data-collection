package com.hp.gaia.provider.jenkins.common;

import com.hp.gaia.provider.Bookmarkable;

public class BookmarkableImpl implements Bookmarkable {

    private final String bookmark;

    public BookmarkableImpl(final String bookmark) {
        this.bookmark = bookmark;
    }

    public String getBookmark() {
        return bookmark;
    }

    @Override
    public String bookmark() {
        return bookmark;
    }
}
