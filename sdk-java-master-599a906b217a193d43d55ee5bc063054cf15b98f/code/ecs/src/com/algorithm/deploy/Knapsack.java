package com.algorithm.deploy;

import java.util.ArrayList;
import java.util.List;

/**
 * 题目的部署是十分典型的背包问题
 * 使用背包问题的相关算法来求解
 *
 * @date 2018-3-17 09:33:36
 * @author long
 */
public class Knapsack {

    /**
     *  解 01背包问题
     * @param valueMax 最大价值上限
     * @param packVolumn 背包大小
     * @param rank 哪一款 VM
     * @param values 价值大小
     * @param sizes 重量大小
     * @return 所选择的物品
     */
    public static List<Integer> zeroOnePack(int valueMax, int packVolumn, int[] rank, int[] values, int[] sizes) {

        int N = rank.length, W = packVolumn;
        int[][] dp = new int[N+1][W+1];
        int[][] choose = new int[N+1][W+1];
        for ( int i = 1; i <= N; i++ ) {
            for ( int j = 1; j <= W; j++ ) {
                if ( j < sizes[i-1] ) {
                    dp[i][j] = dp[i-1][j];
                    choose[i][j] = choose[i-1][j];
                    continue;
                }
                int value_tmp = dp[i-1][j - sizes[i-1]] + values[i-1];
                if ( value_tmp > valueMax || dp[i][j] > value_tmp ) { // 大于等于和大于有无区别
                    dp[i][j] = dp[i-1][j];
                    choose[i][j] = choose[i-1][j];
                } else {
                    dp[i][j] = value_tmp;
                    choose[i][j] = i;
                }
            }
        }

        List<Integer> res = new ArrayList<>();
        int ii = N, jj = W;
        while ( ii > 0 && jj > 0 ) {
            if ( choose[ii][jj] == ii ) {
                res.add(rank[ii-1]);
                jj -= sizes[ii-1];
            }
            ii--;
        }

        return res;
    }

}
