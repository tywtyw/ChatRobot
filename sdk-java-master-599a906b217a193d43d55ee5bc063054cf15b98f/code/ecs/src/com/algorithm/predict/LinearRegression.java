package com.algorithm.predict;

import com.algorithm.util.DateUtil;
import com.algorithm.util.Flavor;
import com.algorithm.input.Input;
import com.algorithm.vm.VM;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * 线性回归来做预测
 *
 * @date 2018-03-17 15:12:05
 * @author long
 */
public class LinearRegression {

    // 至少需要的样本数
    private static final int MIN_SIMPLE_NUM = 5;

    // 最多使用的样本数
    private static final int MAX_SIMPLE_NUM = 20;

    // 需要使用之前多少天来进行预测
    private static final int X_DAY_LEN = 14;

    // 学习率
    private static final double LEARNING_RATE = 0.0001;

    // 学习精度
    private static final int MAX_STEP = 1000;

    private static final double MIN_LOSS = 0.001;

    // 所给数据的时间跨度
    private int dataDateLen = -1;

    // 所需预测的时间跨度
    private int preDateLen = -1;

    // 预测使用的那一部分数据
    private Simple simpleToPredict;

    /**
     * 样本类
     */
    private class Simple {
        public double[] X;
        public double[] Y;

        public Simple() {
            X = new double[Flavor.MAX_RANK * X_DAY_LEN + 1];
            Y = new double[Flavor.MAX_RANK+1];
            X[0] = 1.0;    // 这一位为偏置位
        }

        public void addX(int day, int[] vms) {
            for ( int i = 1, shift = Flavor.MAX_RANK * day + 1; i < vms.length; i++ ) {
                X[shift++] = vms[i];
            }
        }

        public void addY(int[] vms) {
            for ( int i = 1; i < vms.length; i++ ) {
                Y[i] += vms[i];
            }
        }
    }

    /**
     * 预测入口
     * @param trainDatas
     * @param input
     * @return
     */
    public int[] predict(List<VM> trainDatas, Input input) {
        init(trainDatas, input);
        List<Simple> simples = generateX(trainDatas, input);

        // 如果训练所需样本数不够，那就只能 xjb 预测了
        if ( simples.size() < MIN_SIMPLE_NUM ) {
            XJBPredict xjbp = new XJBPredict();
            return  xjbp.predict(trainDatas, input);
        }

        // 下采样样本，样本太多了反而很烂
        List<Simple> downSimples;
        if ( simples.size() > MAX_SIMPLE_NUM ) {
            downSimples = new ArrayList<>();
            for ( int i = simples.size()-1, j = 0; j < MAX_SIMPLE_NUM; j++, i-- ) {
                downSimples.add(simples.get(i));
            }
        } else {
            downSimples = simples;
        }

        int[] needs = new int[Flavor.MAX_RANK+1];
        for ( int i = 1; i < needs.length; i++ ) {
            if ( !input.vmsCanUse[i] ) {
                continue;
            }
            needs[i] = predictFlavor(downSimples, i);
            if ( needs[i] < 0 ) {
                needs[i] = 0;
                System.out.println("预测为负数。");
            }
        }
        return needs;
    }

    /**
     * 预测一种 flavor 的数量
     * @param simples
     * @param flavor
     * @return
     */
    private int predictFlavor(List<Simple> simples, int flavor) {
        double[] weights = new double[simples.get(0).X.length];
//        Arrays.fill(weights, 1.0);
        int step = 0;
        int idx = 0;
        while ( step < MAX_STEP ) {
            double loss = computeLoss(simples, flavor, weights);
            if ( loss <= MIN_LOSS ) {
                break;
            }
            System.out.println("Loss = " + loss);
            SGD(simples.get(idx), flavor, weights);;
            step++;
            idx++;
            idx %= simples.size();
        }
        System.out.println("********************************************");
        System.out.println("Loss = " + computeLoss(simples, flavor, weights));
        System.out.println("********************************************");
        return (int)Math.rint(computeY(simpleToPredict, weights));
    }

    /**
     * 随机梯度下降
     * @param simple
     * @param flavor
     * @param weights
     * @return
     */
    private void SGD(Simple simple, int flavor, double[] weights) {
        double[] weightsNew = new double[weights.length];
        for ( int i = 0; i < weightsNew.length; i++ ) {
            double gradient = LEARNING_RATE * simple.X[i] * (simple.Y[flavor] - computeY(simple, weights));
            weightsNew[i] = weights[i] + gradient;
        }
        System.arraycopy(weightsNew, 0, weights, 0, weightsNew.length);
    }

    /**
     * 计算损失
     * @param simples
     * @param flavor
     * @param weights
     * @return
     */
    private double computeLoss(List<Simple> simples, int flavor, double[] weights) {
        double loss = 0;
        for ( Simple simple : simples ) {
            double y = computeY(simple, weights);
            double tmp = y - simple.Y[flavor];
            loss += Math.pow(tmp, 2);
        }
        return loss / 2;
    }

    /**
     * 计算预测值
     * @param simple
     * @param weights
     * @return
     */
    private double computeY(Simple simple, double[] weights) {
        return helpMultiply(simple.X, weights);
    }

    /**
     * 乘法辅助
     * @param a
     * @param b
     * @return
     */
    private double helpMultiply( double[] a, double[] b ) {
        double res = 0.0;
        for ( int i = 0; i < a.length; i++ ) {
            res += a[i] * b[i];
        }
        return res;
    }

    /**
     * 从数据中生成样本
     * @param trainDatas
     * @param input
     * @return
     */
    private List<Simple> generateX(List<VM> trainDatas, Input input) {
        List<Simple> res = new ArrayList<>();

        int[][] vmCounts = new int[dataDateLen][Flavor.MAX_RANK+1];
        int idx = 0;
        Calendar vmBegin = (Calendar)trainDatas.get(0).date.clone();
        for ( VM vm : trainDatas ) {
            while ( !DateUtil.isSameDay(vm.date, vmBegin) ) {
                DateUtil.addDay(vmBegin, 1);
                idx++;
            }
            if ( vm.flavor <= 0 || vm.flavor > Flavor.MAX_RANK ) {
                continue;
            }
            vmCounts[idx][vm.flavor]++;
        }

        for ( int i = 0, bound = vmCounts.length - X_DAY_LEN - preDateLen;
              i <= bound; i++ ) {
            Simple simple = new Simple();
            for ( int j = 0, bound2 = X_DAY_LEN + preDateLen;  j < bound2; j++ ) {
                if ( j < X_DAY_LEN ) {
                    simple.addX(j, vmCounts[i+j]);
                } else {
                    simple.addY(vmCounts[i+j]);
                }
            }
            res.add(simple);
        }

        // 顺便初始化一下 simpleToPredict
        simpleToPredict = new Simple();
        for ( int i = 0, shift = vmCounts.length - X_DAY_LEN; i < X_DAY_LEN; i++ ) {
            simpleToPredict.addX(i, vmCounts[i+shift]);
        }

        return res;
    }

    /**
     * 初始化，主要统计一下数据的时间跨度
     * @param trainDatas
     * @param input
     */
    private void init(List<VM> trainDatas, Input input) {
        Calendar preBegin = (Calendar)input.begin.clone();
        Calendar preEnd = (Calendar)input.end.clone();
        preDateLen = 0;
        while ( !DateUtil.isSameDay(preBegin, preEnd) ) {
            DateUtil.addDay(preBegin, 1);
            preDateLen++;
        }

        Calendar begin = (Calendar)trainDatas.get(0).date.clone();
        Calendar end = (Calendar)trainDatas.get(trainDatas.size()-1).date.clone();
        dataDateLen = 1; // 最后一天也是完整的一天，长度加一
        while ( !DateUtil.isSameDay(begin, end) ) {
            DateUtil.addDay(begin, 1);
            dataDateLen++;
        }
    }
}
