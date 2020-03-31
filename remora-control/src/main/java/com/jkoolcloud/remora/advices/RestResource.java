/// *
// *
// * Copyright (c) 2019-2020 NasTel Technologies, Inc. All Rights Reserved.
// *
// * This software is the confidential and proprietary information of NasTel
// * Technologies, Inc. ("Confidential Information"). You shall not disclose
// * such Confidential Information and shall use it only in accordance with
// * the terms of the license agreement you entered into with NasTel
// * Technologies.
// *
// * NASTEL MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF
// * THE SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO
// * THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
// * PURPOSE, OR NON-INFRINGEMENT. NASTEL SHALL NOT BE LIABLE FOR ANY DAMAGES
// * SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING
// * THIS SOFTWARE OR ITS DERIVATIVES.
// *
// * CopyrightVersion 1.0
// */
//
// package com.jkoolcloud.remora.advices;
//
// import javax.ws.rs.*;
// import javax.ws.rs.core.Context;
// import javax.ws.rs.core.Response;
//
// import org.jboss.resteasy.spi.HttpRequest;
//
// @Path("/")
// public class RestResource {
//
// @GET
// @Path("basic")
// @Produces("text/plain")
// public String getBasic() {
// return "basic";
// }
//
// @PUT
// @Path("basic")
// @Consumes("text/plain")
// public void putBasic(String body) {
// }
//
// @GET
// @Path("queryParam")
// @Produces("text/plain")
// public String getQueryParam(@QueryParam("param") String param) {
// return param;
// }
//
// @GET
// @Path("matrixParam")
// @Produces("text/plain")
// public String getMatrixParam(@MatrixParam("param") String param) {
// return param;
// }
//
// @GET
// @Path("uriParam/{param}")
// @Produces("text/plain")
// public int getUriParam(@PathParam("param") int param) {
// return param;
// }
//
// @GET
// @Path("header")
// public Response getHeader() {
// return Response.ok().header("header", "headervalue").build();
// }
//
// @GET
// @Path("request")
// @Produces("text/plain")
// public String getRequest(@Context HttpRequest req) {
// return req.getRemoteAddress() + "/" + req.getRemoteHost();
// }
//
// }
