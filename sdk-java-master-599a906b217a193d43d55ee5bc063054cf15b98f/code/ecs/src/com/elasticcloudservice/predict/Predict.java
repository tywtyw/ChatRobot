package com.elasticcloudservice.predict;


import com.algorithm.deploy.DeployAlgorithm;
import com.algorithm.deploy.DeployAlgorithmPlus;
import com.algorithm.input.Input;
import com.algorithm.predict.LinearRegression;
import com.algorithm.util.DataClean;
import com.algorithm.util.ResToString;
import com.algorithm.vm.VM;

import java.util.ArrayList;
import java.util.List;

public class Predict {

	public static String[] predictVm(String[] ecsContent, String[] inputContent) {

		/** =========do your work here========== **/

		// 训练数据清洗
        List<VM> trainDatas = DataClean.clearDatas(ecsContent);

        // 解析 Input
        Input input = new Input(inputContent);

		// 预测算法
//        XJBPredict xjbp = new XJBPredict();
//        int[] needs = xjbp.predict(trainDatas, input);
        LinearRegression lr = new LinearRegression();
        int[] needs = lr.predict(trainDatas, input);

        // 部署算法
        DeployAlgorithm da = new DeployAlgorithm();
        int[] needsCopy = new int[needs.length];
        System.arraycopy(needs, 0, needsCopy, 0, needs.length);
        ArrayList<int[]> deploy = DeployAlgorithmPlus.Solution(needsCopy,input);
//        List<int[]> deploy = da.useKnapsack(needsCopy, input);
//        List<int[]> deploy = da.useNothing(needsCopy, input);

		return ResToString.covert(needs, deploy, input);
	}

}
