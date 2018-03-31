package com.algorithm.deploy;

import com.algorithm.input.Input;
import com.algorithm.util.Flavor;

import java.util.ArrayList;

/**
 *
 * Created by tyw
 */



public class DeployAlgorithmPlus {
    public static ArrayList<int[]> Solution(int[] needs, Input input){
        ArrayList<int[]> finalResult = new ArrayList<>();
        int totalCount = 0;
        for (Integer need:needs) {
            totalCount += need;
        }

        while (totalCount > 0){
            //取得数据
            int[] flavor = new int[totalCount];
            int[] values = new int[totalCount];
            int[] weights = new int[totalCount];
            for (int i = needs.length - 1,temp = 0; i >= 1;i--){
                for (int j = 0;j < needs[i];j++){
                    flavor[temp] = i;
                    values[temp] = Flavor.CPU[i];
                    weights[temp] = Flavor.MEM[i];
                    temp += 1;
                }
            }
            int valueMax = 0;
            int packMax = 0;
            if (input.target == 0) {
                valueMax = input.cpu;
                packMax = input.mem;
            } else {
                valueMax = input.mem;
                packMax = input.cpu;
                int[] tmp = values;
                values = weights;
                weights = tmp;
            }
            //背包
            ArrayList<Integer> chooseVm = KnapsackBag(valueMax,packMax,flavor,values,weights);
            int[] result = new int[needs.length];
            for (int i = 0; i < chooseVm.size(); i++) {
                result[chooseVm.get(i)] += 1;
                needs[chooseVm.get(i)] -= 1;
                totalCount -= 1;
            }
            finalResult.add(result);
        }
        return finalResult;

    }

    public static ArrayList<Integer> KnapsackBag(int valueMax, int packMax, int[] flavor, int[] values, int[] weights) {
        int[][] dp = new int[flavor.length + 1][packMax + 1];
        int[][] chooseVM = new int[flavor.length + 1][packMax + 1];
        for (int i = 1; i <= flavor.length; i++) {
            for (int j = 1; j <= packMax; j++){
                if (j < weights[i - 1]){
                    dp[i][j] = dp[i - 1][j];
                    chooseVM[i][j] = chooseVM[i - 1][j];
                    continue;
                }
                if (dp[i - 1][j - weights[i - 1]] + values[i - 1] > valueMax ||
                        dp[i - 1][j - weights[i - 1]] + values[i - 1] < dp[i - 1][j]){
                    dp[i][j] = dp[i - 1][j];
                    chooseVM[i][j] = chooseVM[i - 1][j];
                }
                else {
                    dp[i][j] = dp[i - 1][j - weights[i - 1]] + values[i - 1];
                    chooseVM[i][j] = i;
                }
            }
        }
        //回溯找VM
        return tracebackFindVm(dp,chooseVM,packMax,flavor,weights);
    }

    public static ArrayList<Integer> tracebackFindVm(int[][] dp,int[][] chooseVM, int packMax, int[] flavor,int[] weights){
        ArrayList<Integer> vm = new ArrayList<>();
        int i = flavor.length;
        int j = packMax;
        for(;i >= 1 && j > 0;i--){
            if (chooseVM[i][j] == i){
                vm.add(flavor[i - 1]);
                j -= weights[i - 1];
            }
        }
        return vm;
    }

}
