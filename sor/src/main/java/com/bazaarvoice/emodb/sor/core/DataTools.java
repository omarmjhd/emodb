package com.bazaarvoice.emodb.sor.core;

import com.bazaarvoice.emodb.common.api.impl.LimitCounter;
import com.bazaarvoice.emodb.sor.api.ReadConsistency;
import com.bazaarvoice.emodb.sor.db.MultiTableScanOptions;
import com.bazaarvoice.emodb.sor.db.MultiTableScanResult;
import com.bazaarvoice.emodb.sor.db.ScanRange;
import com.bazaarvoice.emodb.sor.db.ScanRangeSplits;
import com.bazaarvoice.emodb.table.db.TableSet;
import com.google.common.base.Optional;
import org.joda.time.DateTime;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public interface DataTools {
    /**
     * Returns the names of all placements accessible in the local data center, including internal placements.
     */
    Collection<String> getTablePlacements(boolean includeInternal, boolean localOnly);

    /**
     * Returns a set of scan split point groups that corresponds to the underlying storage topology and would be good
     * start/end locations for parallelization.  Each group contains non-overlapping replicated token ranges so
     * parallelizing by group should evenly distribute the load.  The caller can optionally request only a subrange
     * of the entire token range.
     */
    ScanRangeSplits getScanRangeSplits(String placement, int desiredRecordsPerSplit, Optional<ScanRange> subrange);

    /**
     * Returns a string identifying the backend cluster which serves this placement.  The only implication the caller
     * should infer is that if two placements return the same value then operations to tables in both placements will
     * utilize the same resources.
     */
    String getPlacementCluster(String placement);

    /**
     * For performing a raw scan across multiple tables. If cut off time is provided, then NO Changes after that time will be included.
     */
    Iterator<MultiTableScanResult> multiTableScan(MultiTableScanOptions query, TableSet tables, LimitCounter limit, ReadConsistency consistency, @Nullable DateTime cutoffTime);

    /**
     * Resolve a scan record into a single JSON literal object + metadata.  If allowAsyncCompaction is true then it may
     * also asynchronously compact the record if it can be compacted.
     */
    Map<String, Object> toContent(MultiTableScanResult result, ReadConsistency consistency, boolean allowAsyncCompaction);

    /** Return a consistent TableSet view of all tables in the system. */
    TableSet createTableSet();

}
