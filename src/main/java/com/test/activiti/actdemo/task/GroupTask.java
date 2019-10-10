package com.test.activiti.actdemo.task;

import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;

public class GroupTask implements TaskListener {
    @Override
    public void notify(DelegateTask delegateTask) {
        delegateTask.addCandidateUser("sQ");
        delegateTask.addCandidateUser("sW");
        delegateTask.addCandidateUser("sE");
    }
}
