/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.ignite.ci.web.rest.board;

import com.google.inject.Injector;
import org.apache.ignite.ci.user.ITcBotUserCreds;
import org.apache.ignite.ci.web.CtxListener;
import org.apache.ignite.tcbot.engine.board.BoardService;
import org.apache.ignite.tcbot.engine.ui.BoardSummaryUi;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

@Path(DefectRestService.BOARD)
@Produces(MediaType.APPLICATION_JSON)
public class DefectRestService {
    static final String BOARD = "defect";

    /** Servlet Context. */
    @Context
    private ServletContext ctx;

    /** Current Request. */
    @Context
    private HttpServletRequest req;


    @POST
    @Path("resolve")
    public void resolveDefect(@FormParam("id") Integer defectId) {
        ITcBotUserCreds creds = ITcBotUserCreds.get(req);

        CtxListener.getInjector(ctx)
                .getInstance(BoardService.class)
                .resolveDefect(defectId, creds);
    }

}
