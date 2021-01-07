package com.business.enums;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: dbs01
 * Date: 13-9-21
 * Time: 下午10:58
 * To change this template use File | Settings | File Templates.
 */
public enum ExecutingState {
    //注意，不要改变枚举顺序，ArchiveWorkflowInfo、QATaskWorkflowInfo、WorkflowInfo类的state值与该枚举的ordinal()对应
    Hold,
    Processing,      //正在处理
    Completed,        //成功
    PartialSuccess,   //部分成功
    Failed;           //失败

    //将若干DataExecutingState综合为ExecutingState
    public static ExecutingState mergeState(List<DataExecutingState> dataExecutingStates){
        if(dataExecutingStates.contains(DataExecutingState.Processing)){
            return Processing;
        }
        if(dataExecutingStates.contains(DataExecutingState.Completed)&&
                dataExecutingStates.contains(DataExecutingState.Failed)){
            return PartialSuccess;
        }
        return dataExecutingStates.contains(DataExecutingState.Completed)?Completed:Failed;
    }


}
