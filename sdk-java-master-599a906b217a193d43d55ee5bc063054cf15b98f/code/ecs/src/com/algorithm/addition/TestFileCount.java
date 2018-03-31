package com.algorithm.addition;

import com.algorithm.util.Flavor;

import java.io.*;

/**
 * 用于构建 test.txt , 统计其中每一种 flavor 的发生次数。
 *
 * @date 2018-03-17 13:50:07
 * @author long
 */
public class TestFileCount {
    public void count(String path) {
        BufferedReader br = null;
        int[] flavorCount = new int[Flavor.MAX_RANK+1];
        int totalCount = 0;
        try {
            br = new BufferedReader(new FileReader(new File(path)));
            String line;
            while ( (line = br.readLine()) != null ) {
                int flavor = Integer.parseInt(line.split("\t")[1].substring(6));
                if ( flavor <= 0 || flavor >= flavorCount.length ) {
                    continue;
                }
                flavorCount[flavor]++;
                totalCount++;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.out.println(totalCount);
        for ( int i = 1; i < flavorCount.length; i++ ) {
            System.out.println("flavor" + i + " " + flavorCount[i]);
        }
    }

}
