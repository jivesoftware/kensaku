/*
 * Copyright 2013 Jive Software, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.jivesoftware.os.kensaku.service;

import com.jivesoftware.os.jive.utils.jaxrs.util.ResponseHelper;
import com.jivesoftware.os.jive.utils.logger.MetricLogger;
import com.jivesoftware.os.jive.utils.logger.MetricLoggerFactory;
import com.jivesoftware.os.kensaku.shared.KensakuDocument;
import com.jivesoftware.os.kensaku.shared.KensakuQuery;
import com.jivesoftware.os.kensaku.shared.KensakuResults;
import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

@Path("/kensaku")
public class KensakuRestEndpoints {

    private static final MetricLogger LOG = MetricLoggerFactory.getLogger();
    private final KensakuService kensakuService;

    public KensakuRestEndpoints(@Context KensakuService kensakuService) {
        this.kensakuService = kensakuService;
    }

    @POST
    @Consumes("application/json")
    @Path("/add")
    public Response add(List<KensakuDocument> documents) {
        try {
            LOG.debug("Attempting to add: " + documents);
            kensakuService.add(documents);
            return ResponseHelper.INSTANCE.jsonResponse("Success");
        } catch (Exception x) {
            LOG.warn("Failed to add: " + documents, x);
            return ResponseHelper.INSTANCE.errorResponse("Failed to add " + documents, x);
        }
    }

    @POST
    @Consumes("application/json")
    @Path("/remove")
    public Response remove(List<KensakuDocument> documents) {
        try {
            LOG.debug("Attempting to remove: " + documents);
            kensakuService.remove(documents);
            return ResponseHelper.INSTANCE.jsonResponse("Success");
        } catch (Exception x) {
            LOG.warn("Failed to remove: " + documents, x);
            return ResponseHelper.INSTANCE.errorResponse("Failed to remove " + documents, x);
        }
    }

    @POST
    @Consumes("application/json")
    @Path("/searchLocally")
    public Response searchLocally(@QueryParam("partitionId") int partitionId, KensakuQuery query) {
        try {
            LOG.debug("Attempting to search locally: " + query);
            KensakuResults kensakuResults = kensakuService.searchLocally(partitionId, query);
            return ResponseHelper.INSTANCE.jsonResponse(kensakuResults);
        } catch (Exception x) {
            LOG.warn("Failed to search locally: " + query, x);
            return ResponseHelper.INSTANCE.errorResponse("Failed to search locally " + query, x);
        }
    }

    @POST
    @Consumes("application/json")
    @Path("/search")
    public Response search(KensakuQuery query) {
        try {
            LOG.debug("Attempting to search: " + query);
            KensakuResults kensakuResults = kensakuService.search(query);
            return ResponseHelper.INSTANCE.jsonResponse(kensakuResults);
        } catch (Exception x) {
            LOG.warn("Failed to search: " + query, x);
            return ResponseHelper.INSTANCE.errorResponse("Failed to search " + query, x);
        }
    }

//    @GET
//    @Consumes("application/json")
//    @Path("/get")
//    public Response search(@QueryParam("tenantId") String tenantId, @QueryParam("releaseId")  @DefaultValue("") String releaseId) {
//
//        return null;
//    }
}