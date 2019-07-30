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
package org.apache.ignite.tcbot.engine.board;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.inject.Inject;
import org.apache.ignite.ci.issue.ChangeUi;
import org.apache.ignite.ci.issue.Issue;
import org.apache.ignite.ci.issue.IssueKey;
import org.apache.ignite.ci.teamcity.ignited.change.ChangeCompacted;
import org.apache.ignite.ci.teamcity.ignited.change.ChangeDao;
import org.apache.ignite.ci.teamcity.ignited.fatbuild.FatBuildCompacted;
import org.apache.ignite.ci.teamcity.ignited.fatbuild.TestCompacted;
import org.apache.ignite.tcbot.common.util.FutureUtil;
import org.apache.ignite.tcbot.engine.chain.BuildChainProcessor;
import org.apache.ignite.tcbot.engine.defect.DefectStorage;
import org.apache.ignite.tcbot.engine.issue.IIssuesStorage;
import org.apache.ignite.tcbot.engine.ui.BoardDefectSummaryUi;
import org.apache.ignite.tcbot.engine.ui.BoardSummaryUi;
import org.apache.ignite.tcbot.persistence.IStringCompactor;
import org.apache.ignite.tcbot.persistence.scheduler.IScheduler;
import org.apache.ignite.tcignited.ITeamcityIgnited;
import org.apache.ignite.tcignited.ITeamcityIgnitedProvider;
import org.apache.ignite.tcignited.build.FatBuildDao;
import org.apache.ignite.tcignited.creds.ICredentialsProv;

public class BoardService {
    @Inject IIssuesStorage storage;
    @Inject FatBuildDao fatBuildDao;
    @Inject ChangeDao changeDao;
    @Inject ITeamcityIgnitedProvider tcProv;
    @Inject DefectStorage defectStorage;
    @Inject IScheduler scheduler;
    @Inject IStringCompactor compactor;

    @Inject BuildChainProcessor buildChainProcessor;

    public BoardSummaryUi summary(ICredentialsProv creds) {
        Stream<Issue> stream = storage.allIssues();

        long minIssueTs = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(2);


        Map<Integer, Future<FatBuildCompacted>> allBuildsMap = new HashMap<>();

        Map<String, BoardDefectSummaryUi> defectsGrouped = new HashMap<>();
        stream
            .filter(issue -> {
                long detected = issue.detectedTs == null ? 0 : issue.detectedTs;

                return detected >= minIssueTs;
            })
            .filter(issue -> {
                IssueKey key = issue.issueKey;
                String srvCode = key.getServer();
                return tcProv.hasAccess(srvCode, creds);
            })
            .forEach(issue -> {
                IssueKey key = issue.issueKey;
                String srvCode = key.getServer();
                //just for init call
                ITeamcityIgnited tcIgn = tcProv.server(srvCode, creds);

                int srvId = ITeamcityIgnited.serverIdToInt(srvCode);
                FatBuildCompacted fatBuild = fatBuildDao.getFatBuild(srvId, key.buildId);
                if (fatBuild == null)
                    return;

                //todo non test failures
                String testName = issue.issueKey().getTestOrBuildName();

                Integer testNameCid = compactor.getStringIdIfPresent(testName);

                List<Future<FatBuildCompacted>> futures = buildChainProcessor.replaceWithRecent(fatBuild, allBuildsMap, tcIgn);

                int[] changes = fatBuild.changes();
                Map<Long, ChangeCompacted> all = changeDao.getAll(srvId, changes);

                TreeSet<String> collect1 = all.values().stream().map(ChangeCompacted::commitFullVersion).collect(Collectors.toCollection(TreeSet::new));
                BoardDefectSummaryUi defect = defectsGrouped.computeIfAbsent(collect1.toString(), _k -> new BoardDefectSummaryUi());

                Set<String> collect = issue.changes.stream().map(ChangeUi::username).collect(Collectors.toSet());

                defect.branch = fatBuild.branchName(compactor);

                defect.addIssue();

                Stream<FatBuildCompacted> results = FutureUtil.getResults(futures);
                List<FatBuildCompacted> freshRebuild = results.collect(Collectors.toList());
                if(!freshRebuild.isEmpty()) {
                    FatBuildCompacted buildCompacted = freshRebuild.get(0);

                    Optional<TestCompacted> any = buildCompacted.getAllTests()
                        .filter(t -> t.testName() == testNameCid)
                        .findAny();

                    if(any.isPresent()) {
                        boolean failed = any.get().isFailedTest(compactor);
                        if(!failed)
                            defect.addFixedIssue();
                        else
                            defect.addNotFixedIssue();
                    }
                }


                defect.usernames = collect.toString();
            });

        BoardSummaryUi res = new BoardSummaryUi();
        for (BoardDefectSummaryUi next : defectsGrouped.values())
            res.addDefect(next);

        return res;
    }
}
