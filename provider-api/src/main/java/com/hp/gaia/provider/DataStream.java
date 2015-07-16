package com.hp.gaia.provider;

import java.io.Closeable;

/**
 * Represents stream of data blocks which are to be processed independently. User of the data structure is responsible
 * for invocation of {@link DataStream#close()}.
 * <p>
 * Typical use case for splitting data into multiple data blocks is paging to avoid transfer of huge data blocks and
 * transfer of independent files. Another use case could be transfer of data blocks of varying Content-Type. Each data
 * block must have a unique bookmark.
 * <p>
 * Implementation should be lazy and attempt to read data blocks from remote server only when requested and avoid any
 * excessive buffers.
 */
public interface DataStream extends Closeable {

    /**
     * Returns true if the next data block is ready. Normally it should return false and data should be fetched from
     * remote server lazily. The only difference is when blocks of different Content-Types need to be transported and
     * data provider already received the data. This allows user of this interface to avoid inefficiency of having to
     * re-read the same data.
     */
    boolean isNextReady();

    /**
     * Returns the next data block or <code>null</code> if no next data block is available.
     */
    Data next() throws AccessDeniedException;
}
