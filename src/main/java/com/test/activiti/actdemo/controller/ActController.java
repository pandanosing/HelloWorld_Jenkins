package com.test.activiti.actdemo.controller;

import com.test.activiti.actdemo.service.impl.ActivityDemoServiceImpl;
import com.test.activiti.actdemo.utils.ActivitiUtils;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.engine.*;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricActivityInstanceQuery;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.impl.RepositoryServiceImpl;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.runtime.ProcessInstanceQuery;
import org.activiti.engine.task.Task;
import org.activiti.image.ProcessDiagramGenerator;
import org.activiti.image.impl.DefaultProcessDiagramGenerator;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import sun.misc.IOUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by lijian on 2019-09-28.
 */
@Controller
public class ActController {

    /** 流程定义和部署相关的存储服务 */
    @Autowired
    private RepositoryService repositoryService;

    /** 流程运行时相关的服务 */
    @Autowired
    private RuntimeService runtimeService;

    /** 节点任务相关操作接口 */
    @Autowired
    private TaskService taskService;

    /** 流程图生成器 */
    ProcessDiagramGenerator processDiagramGenerator;

    /** 历史记录相关服务接口 */
    @Autowired
    private HistoryService historyService;


    private static Logger logger=LoggerFactory.getLogger(ActController.class);

    @Autowired
    ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
    /**
     * <p>部署</p>
     * @return
     */
    @RequestMapping(value = "/deploy")
    @ResponseBody
    public String deploy(){
        //根据bpmn文件部署流程
        Deployment deployment = repositoryService.createDeployment()
                .addClasspathResource("processes/helloworld.bpmn").deploy();
        //获取流程定义
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().deploymentId(deployment.getId()).singleResult();
        //启动流程定义，返回流程实例
        return  processDefinition.getId();
    }

    /**
     * <p>启动请假流程</p>
     * @return String 流程实例ID
     */
    @RequestMapping(value="/start")
    @ResponseBody
    public String start() {
        // xml中定义的ID
        String instanceKey = "helloworld";
        logger.info("开启请假流程...");
        // 设置流程参数，开启流程
        Map<String,Object> map = new HashMap<>();
        map.put("userId","王牛牛");
        map.put("jobNumber","A1002");
        map.put("busData","bus data2");
        ProcessInstance instance = runtimeService.startProcessInstanceByKey(instanceKey, map);//使用流程定义的key启动流程实例，key对应helloworld.bpmn文件中id的属性值，使用key值启动，默认是按照最新版本的流程定义启动

        logger.info("启动流程实例成功:{}", instance);
        logger.info("流程实例ID:{}", instance.getId());
        logger.info("流程定义ID:{}", instance.getProcessDefinitionId());

        //验证是否启动成功
        //通过查询正在运行的流程实例来判断
        ProcessInstanceQuery processInstanceQuery = runtimeService.createProcessInstanceQuery();
        //根据流程实例ID来查询
        List<ProcessInstance> runningList = processInstanceQuery.processInstanceId(instance.getProcessInstanceId()).list();
        logger.info("根据流程ID查询条数:{}", runningList.size());

        // 返回流程ID
        return instance.getId();
    }
    // 设置流程变量
    @RequestMapping(value = "/viewVariable")
    public void getVariable(){
        Map<String, Object> map = processEngine.getRuntimeService().getVariables("20005");
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            System.out.println(entry.getKey() + " : " + entry.getValue());
        }
        System.out.println("##########################");
        Map<String, Object> map1 = processEngine.getTaskService().getVariables("20014");
        for (Map.Entry<String, Object> entry : map1.entrySet()) {
            System.out.println(entry.getKey() + " : " + entry.getValue());
        }
    }

    //获得资源图片
    @RequestMapping("/showProcImg")
    public void viewPic() throws Exception{
        ProcessInstance pi = this.processEngine.getRuntimeService().createProcessInstanceQuery()
                .processInstanceId("20005").singleResult();
        BpmnModel bpmnModel = this.processEngine.getRepositoryService().getBpmnModel(pi.getProcessDefinitionId());
        List<String> activeIds = this.processEngine.getRuntimeService().getActiveActivityIds(pi.getId());
        ProcessDiagramGenerator processDiagramGenerator = processEngine.getProcessEngineConfiguration().getProcessDiagramGenerator();
        InputStream is = processDiagramGenerator.generateDiagram(bpmnModel, "png",  Collections.<String> emptyList(),  Collections.<String> emptyList(), "宋体", "微软雅黑", "黑体", null, 2.0);
        File file = new File("d:\\Download\\process.png");
        OutputStream os = new FileOutputStream(file);

        byte[] buffer = new byte[1024];
        int len ;
        while ((len = is.read(buffer)) != -1) {
            os.write(buffer, 0, len);
        }

        os.close();
        is.close();
    }

    /**
     * <p>查看当前流程图</p>
     * @param instanceId 流程实例
     * @param response void 响应
     * @author FRH
     * @time 2018年12月10日上午11:14:12
     * @version 1.0
     */
    @ResponseBody
    @RequestMapping(value="/showImg")
    public void showImg(String instanceId, HttpServletResponse response) {
        /*
         * 参数校验
         */
        logger.info("查看完整流程图！流程实例ID:{}", instanceId);
        if(StringUtils.isBlank(instanceId)) return;

        /*
         *  获取流程实例
         */
        HistoricProcessInstance processInstance = historyService.createHistoricProcessInstanceQuery().processInstanceId(instanceId).singleResult();
        if(processInstance == null) {
            logger.error("流程实例ID:{}没查询到流程实例！", instanceId);
            return;
        }
        // 根据流程对象获取流程对象模型
        BpmnModel bpmnModel = repositoryService.getBpmnModel(processInstance.getProcessDefinitionId());

        /*
         *  查看已执行的节点集合
         *  获取流程历史中已执行节点，并按照节点在流程中执行先后顺序排序
         */
        // 构造历史流程查询
        HistoricActivityInstanceQuery historyInstanceQuery = historyService.createHistoricActivityInstanceQuery().processInstanceId(instanceId);
        // 查询历史节点
        List<HistoricActivityInstance> historicActivityInstanceList = historyInstanceQuery.orderByHistoricActivityInstanceStartTime().asc().list();
        if(historicActivityInstanceList == null || historicActivityInstanceList.size() == 0) {
            logger.info("流程实例ID:{}没有历史节点信息！", instanceId);
            outputImg(response, bpmnModel, null, null);
            return;
        }
        // 已执行的节点ID集合(将historicActivityInstanceList中元素的activityId字段取出封装到executedActivityIdList)
        List<String> executedActivityIdList = historicActivityInstanceList.stream().map(item -> item.getActivityId()).collect(Collectors.toList());

        /*
         *  获取流程走过的线
         */
        // 获取流程定义
        ProcessDefinitionEntity processDefinition = (ProcessDefinitionEntity) ((RepositoryServiceImpl) repositoryService).getDeployedProcessDefinition(processInstance.getProcessDefinitionId());
        List<String> flowIds = ActivitiUtils.getHighLightedFlows(bpmnModel, processDefinition, historicActivityInstanceList);


        /*
         * 输出图像，并设置高亮
         */
        outputImg(response, bpmnModel, flowIds, executedActivityIdList);
    }

    /**
     * <p>输出图像</p>
     * @param response 响应实体
     * @param bpmnModel 图像对象
     * @param flowIds 已执行的线集合
     * @param executedActivityIdList void 已执行的节点ID集合
     * @author FRH
     * @time 2018年12月10日上午11:23:01
     * @version 1.0
     */
    private void outputImg(HttpServletResponse response, BpmnModel bpmnModel, List<String> flowIds, List<String> executedActivityIdList) {
        InputStream imageStream = null;
        ProcessDiagramGenerator processDiagramGenerator = processEngine.getProcessEngineConfiguration().getProcessDiagramGenerator();
        try {
            //imageStream = processDiagramGenerator.generateDiagram(bpmnModel, executedActivityIdList, flowIds, "宋体", "微软雅黑", "黑体", true, "png");
            imageStream = processDiagramGenerator.generateDiagram(bpmnModel, "png", executedActivityIdList, flowIds, "宋体", "微软雅黑", "黑体", null, 2.0);
            // 输出资源内容到相应对象
            byte[] b = new byte[1024];
            int len;
            while ((len = imageStream.read(b, 0, 1024)) != -1) {
                response.getOutputStream().write(b, 0, len);
            }
            response.getOutputStream().flush();
        }catch(Exception e) {
            logger.error("流程图输出异常！", e);
        } finally { // 流关闭
            org.apache.commons.io.IOUtils.closeQuietly(imageStream);
        }
    }






    /**查询当前人的个人任务*/
    @RequestMapping(value = "/viewMyTask")
    public void findMyPersonalTask(){
        String assignee = "李二狗";
        List<Task> list = processEngine.getTaskService()//与正在执行的任务管理相关的Service
                .createTaskQuery()//创建任务查询对象
                .taskAssignee(assignee)//指定个人任务查询，指定办理人
                .list();
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

    /**完成我的任务*/
   @RequestMapping(value="/doneTask")
    public void completeMyPersonalTask(){
        //任务ID
        String taskId = "12502";
        processEngine.getTaskService()//与正在执行的任务管理相关的Service
                .complete(taskId);
        System.out.println("完成任务：任务ID："+taskId);
    }


    /**
     * 删除流程定义
     */
    @RequestMapping(value = "/delProcess")
    public void deleteProcessDefinition(){
        String deploymentId="7501";
        processEngine.getRepositoryService()
                //true 表示级删除：推荐
                .deleteDeployment(deploymentId,true);
        System.out.println("删除成功！");

    }


}
