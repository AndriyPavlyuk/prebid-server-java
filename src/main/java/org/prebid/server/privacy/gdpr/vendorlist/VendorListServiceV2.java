package org.prebid.server.privacy.gdpr.vendorlist;

import io.vertx.core.file.FileSystem;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.apache.commons.collections4.MapUtils;
import org.prebid.server.bidder.BidderCatalog;
import org.prebid.server.exception.PreBidException;
import org.prebid.server.json.JacksonMapper;
import org.prebid.server.privacy.gdpr.vendorlist.proto.VendorListV2;
import org.prebid.server.privacy.gdpr.vendorlist.proto.VendorV2;
import org.prebid.server.vertx.http.HttpClient;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

public class VendorListServiceV2 extends VendorListService<VendorListV2, VendorV2> {

    private static final Logger logger = LoggerFactory.getLogger(VendorListServiceV2.class);

    public VendorListServiceV2(String cacheDir,
                               String endpointTemplate,
                               int defaultTimeoutMs,
                               Integer gdprHostVendorId,
                               BidderCatalog bidderCatalog,
                               FileSystem fileSystem,
                               HttpClient httpClient,
                               JacksonMapper mapper) {
        super(cacheDir, endpointTemplate, defaultTimeoutMs, gdprHostVendorId, bidderCatalog, fileSystem, httpClient,
                mapper);
    }

    protected VendorListV2 toVendorList(String content) {
        try {
            return mapper.mapper().readValue(content, VendorListV2.class);
        } catch (IOException e) {
            final String message = String.format("Cannot parse vendor list from: %s", content);

            logger.error(message, e);
            throw new PreBidException(message, e);
        }
    }

    protected Map<Integer, VendorV2> filterVendorIdToVendors(VendorListV2 vendorList) {
        return vendorList.getVendors().entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    protected boolean isValid(VendorListV2 vendorList) {
        return vendorList.getVendorListVersion() != null
                && vendorList.getLastUpdated() != null
                && MapUtils.isNotEmpty(vendorList.getVendors())
                && isValidVendors(vendorList.getVendors().values());
    }

    private static boolean isValidVendors(Collection<VendorV2> vendors) {
        return vendors.stream()
                .allMatch(vendor -> vendor != null
                        && vendor.getId() != null
                        && vendor.getPurposes() != null
                        && vendor.getLegIntPurposes() != null
                        && vendor.getFlexiblePurposes() != null
                        && vendor.getSpecialPurposes() != null
                        && vendor.getFeatures() != null
                        && vendor.getSpecialFeatures() != null);
    }
}

