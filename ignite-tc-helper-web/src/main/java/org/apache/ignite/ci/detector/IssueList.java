package org.apache.ignite.ci.detector;

import java.util.ArrayList;
import java.util.List;

public class IssueList {

    private List<Issue> issues;

    public IssueList(List<Issue> all) {
        issues = all;
    }


    public List<Issue> getIssues() {
        return issues;
    }

    public void setIssues(List<Issue> issues) {
        this.issues = issues;
    }
}