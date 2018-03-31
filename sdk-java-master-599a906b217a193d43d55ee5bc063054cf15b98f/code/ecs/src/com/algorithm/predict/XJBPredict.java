package com.algorithm.predict;


import com.algorithm.input.Input;
import com.algorithm.util.DateUtil;
import com.algorithm.util.Flavor;
import com.algorithm.vm.VM;

import java.util.Calendar;
import java.util.List;

/**
 * 创建时间：2018-03-13 11:51:10
 *
 * 预测算法，直接平均
 *
 * @author long
 */
public class XJBPredict {

    private class Week {
        int count;
        int[] flavorsCount;
        public Week () {
            this.count = 0;
            this.flavorsCount = new int[Flavor.MAX_RANK+1];
        }
    }

    public int[] predict(List<VM> trainDatas, Input input) {

        Week[] weeks = new Week[8];
        for ( int i = 0; i < weeks.length; i++ ) {
            weeks[i] = new Week();
        }
        weeksCount(weeks, trainDatas, input);
        weeksFlavorCount(weeks, trainDatas);

        int[] needs = computerNeed(weeks, input);

        return needs;
    }

    /**
     * 统计星期一到星期天，也就是在这个时间段里面，发生了多少个星期一、星期二、星期三。。。
     * @param trainDatas
     * @return
     */
    private void weeksCount(Week[] weeks, List<VM> trainDatas, Input input) {
        Calendar start = (Calendar)trainDatas.get(0).date.clone();
        Calendar end = (Calendar)input.begin.clone();

        while ( !DateUtil.isSameDay(start, end) ) {
            weeks[DateUtil.getDayInWeek(start)].count++;
            DateUtil.addDay(start, 1);
        }
    }

    /**
     * 统计星期一中出现了多少 flavor1、flavor2、 flavor3... 同理星期二、星期三...
     * @param weeks
     * @param trainDatas
     */
    private void weeksFlavorCount(Week[] weeks, List<VM> trainDatas) {
        for (VM vm : trainDatas ) {
            if ( vm.flavor < 1 || vm.flavor > Flavor.MAX_RANK ) {
                continue;
            }
            weeks[DateUtil.getDayInWeek(vm.date)].flavorsCount[vm.flavor]++;
        }
    }

    /**
     * 计算所需的虚拟机，直接平均相加，注意下标从 1 开始
     * @param weeks
     * @param input
     * @return
     */
    private int[] computerNeed(Week[] weeks, Input input) {
        double[] needs = new double[Flavor.MAX_RANK+1];

        Calendar start = (Calendar)input.begin.clone();
        while ( !DateUtil.isSameDay(start, input.end) ) {
            int we = DateUtil.getDayInWeek(start);
            for ( int i = 1; i <= Flavor.MAX_RANK; i++ ) {
                if ( weeks[we].count == 0 ) {
                    continue;
                }
                needs[i] += ((double)weeks[we].flavorsCount[i] / (double)weeks[we].count);
            }
            DateUtil.addDay(start, 1);
        }

        int[] needsRound = new int[needs.length];
        for ( int i = 1; i < needs.length; i++ ) {
            if ( input.vmsCanUse[i] ) {
                needsRound[i] = (int) Math.rint(needs[i]);
            }
        }

        return needsRound;
    }

}
