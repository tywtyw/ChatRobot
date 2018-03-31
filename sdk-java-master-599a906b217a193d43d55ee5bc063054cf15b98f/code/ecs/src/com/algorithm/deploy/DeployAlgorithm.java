package com.algorithm.deploy;

import com.algorithm.util.Flavor;
import com.algorithm.input.Input;

import java.util.ArrayList;
import java.util.List;

/**
 * 创建时间： 2018-03-13 12:49:04
 *
 * 部署算法，直接随意放置
 *
 * @author long
 */
public class DeployAlgorithm {

    /**
     * 使用贪心背包求解
     * @param needs
     * @param input
     * @return
     */
    public List<int[]> useKnapsack(int[] needs, Input input) {
        List<int[]> res = new ArrayList<>();

        int totalCount = 0;
        for ( int need : needs ) {
            totalCount += need;
        }
        while ( totalCount > 0 ) {
            int[] rank = new int[totalCount], values = new int[totalCount], sizes = new int[totalCount];
            int idx = 0;
            for ( int i = 1; i < needs.length; i++ ) {
                for ( int j = 0; j < needs[i]; j++ ) {
                    rank[idx] = i;
                    values[idx] = Flavor.CPU[i];
                    sizes[idx] = Flavor.MEM[i];
                    idx++;
                }
            }
            int valueMax = 0, packVolumn = 0;
            if (input.target == 0) {
                valueMax = input.cpu;
                packVolumn = input.mem;
            } else {
                valueMax = input.mem;
                packVolumn = input.cpu;
                int[] tmp = values;
                values = sizes;
                sizes = tmp;
            }
            List<Integer> oneChoose = Knapsack.zeroOnePack(valueMax, packVolumn, rank, values, sizes);
            int[] vms = new int[needs.length];
            for ( int flavor : oneChoose ) {
                needs[flavor]--;
                vms[flavor]++;
                totalCount--;
            }
            res.add(vms);
        }

        return res;
    }

    /**
     * 啥都不使用来求解
     * @param needs
     * @param input
     * @return
     */
    public List<int[]> useNothing(int[] needs, Input input) {
        List<int[]> res = new ArrayList<>();
        List<Integer> needsTotal = new ArrayList<>();
        for ( int i = 1; i < needs.length; i++ ) {
            for ( int j = 0; j < needs[i]; j++ ) {
                needsTotal.add(i);
            }
        }

        while ( !needsTotal.isEmpty() ) {
            int[] src = new int[2];
            src[0] = input.cpu;
            src[1] = input.mem;
            int[] vms = new int[needs.length];
            boolean deploy = true;
            while ( deploy ) {
                int idx = needsTotal.size() - 1;
                while ( idx >= 0 ) {
                    int flavor = needsTotal.get(idx);
                    if ( src[0] >= Flavor.CPU[flavor] && src[1] >= Flavor.MEM[flavor] ) {
                        src[0] -= Flavor.CPU[flavor];
                        src[1] -= Flavor.MEM[flavor];
                        break;
                    }
                    idx--;
                }
                if ( idx == -1 ) {
                    deploy = false;
                } else {
                    vms[needsTotal.get(idx)]++;
                    needsTotal.remove(idx);
                }
            }
            res.add(vms);
        }
        return res;
    }

}
