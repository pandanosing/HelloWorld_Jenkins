package com.test.activiti.actdemo;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.InputStream;
import java.util.List;
import java.util.zip.ZipInputStream;

/**
 * Created by lijian on 2019-09-29.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class GroupTaskTestDemo {

    @Autowired
    ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();

    /**部署流程定义*/
    @Test
    public void deploymentProcessDefinition(){
        Deployment deployment = processEngine.getRepositoryService().createDeployment()
                .addClasspathResource("processes/GroupTask4.bpmn")
                .addClasspathResource("processes/GroupTask4.png").deploy();
        //获取流程定义
        System.out.println("部署ID："+deployment.getId());//
        System.out.println("部署名称："+deployment.getName());//
    }

    /**启动流程实例*/
    @Test
    public void startProcessInstance(){
        //流程定义的key
        String processDefinitionKey = "jtq_group";
        ProcessInstance pi = processEngine.getRuntimeService()//与正在执行的流程实例和执行对象相关的Service
                .startProcessInstanceByKey(processDefinitionKey);//使用流程定义的key启动流程实例，key对应helloworld.bpmn文件中id的属性值，使用key值启动，默认是按照最新版本的流程定义启动
        System.out.println("流程实例ID:"+pi.getId());//流程实例ID    101
        System.out.println("流程定义ID:"+pi.getProcessDefinitionId());//流程定义ID
    }

    //查看组任务.组成员可以获取到任务id,从而对任务进行拾取
    @Test
    public  void findGroupTask(){
        String assign="sW";
        List<Task> list = processEngine.getTaskService()
                .createTaskQuery().taskCandidateUser(assign).list();
        if(list!=null && list.size()>0){
            for(Task task:list){
                System.out.println("任务ID:"+task.getId());
                System.out.println("任务名称:"+task.getName());
                System.out.println("任务的创建时间:"+task.getCreateTime());
                System.out.println("任务的办理人:"+task.getAssignee());
                System.out.println("流程实例ID："+task.getProcessInstanceId());
                System.out.println("执行对象ID:"+task.getExecutionId());
                System.out.println("流程定义ID:"+task.getProcessDefinitionId());
                System.out.println("########################################################");
            }
        }

    }

    //拾取任务
    @Test
    public void  climMyTask(){
        String assgin="sQ";
        processEngine.getTaskService().claim("110004",assgin);
        System.out.println("任务领取成功");

    }
    //quit  task
    @Test
    public void  quitMyTask(){
        processEngine.getTaskService().setAssignee("110004",null);
        System.out.println("放弃领取的任务");

    }

    //add assign
    @Test
    public void addAssign(){
        processEngine.getTaskService().addCandidateUser("7404","F");
        System.out.println("添加一个组成员成功！！");
    }



    @Test
    public void completeMyPersonalTask(){
        //任务ID
        String taskId = "110004";
        processEngine.getTaskService()//与正在执行的任务管理相关的Service
                .complete(taskId);
        System.out.println("完成任务：任务ID："+taskId);
    }

}
