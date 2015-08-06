package com.hp.gaia.provider;

import javax.validation.constraints.NotNull;
import java.util.Map;

/**
 * Represents an item in {@link DataStream} that can be bookmarked.
 */
public interface Bookmarkable {

    /**
     * Bookmark that identifies this data block or the first item if the stream contains a list of items. Can be used to
     * read the same block again via {@link DataProvider#fetchData(Map, CredentialsProvider, ProxyProvider, String,
     * boolean)} with inclusive=true. However a provider may decide to split a block into smaller blocks at its own
     * discretion as long as they can have unique bookmarks.
     * <p>
     * It may be an id, timestamp or JSON value - implementation specific. In case of paging it must include information
     * necessary for retrieving the same page again. It must also include enough information so that the provider is
     * able to retrieve the next data block with {@link DataProvider#fetchData(Map, CredentialsProvider, ProxyProvider,
     * String, boolean)} with inclusive=false.
     * <p>
     * In some cases it may be advantageous for {@link DataStream} to return instances of {@link Bookmarkable} without
     * actual data - especially in cases when not every RESTcall yields data. An example would be collecting tests from
     * hierarchical builds - not every build has tests, some root builds can be failing and thus without tests. Having
     * to repeat several REST calls just to find out there is nothing new in such case is inefficient and {@link
     * DataStream} may want to return a {@link Bookmarkable} without data just before returning null from {@link
     * DataStream#next()}.
     */
    @NotNull
    String bookmark();
}
