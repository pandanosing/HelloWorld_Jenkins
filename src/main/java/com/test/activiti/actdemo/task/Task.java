package com.test.activiti.actdemo.task;

import org.activiti.engine.delegate.DelegateTask;

public class Task implements org.activiti.engine.delegate.TaskListener {
    @Override
    public void notify(DelegateTask delegateTask) {

        delegateTask.setAssignee("张全蛋");
    }
}
