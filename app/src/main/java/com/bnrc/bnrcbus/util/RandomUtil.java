package com.bnrc.bnrcbus.util;

/**
 * JAVA 返回随机数，并根据概率、比率
 * @author firefliex
 *
 */

public class RandomUtil
{

    /**
     * 0出现的概率为%50
     */
    public static double rate0 = 0.80;
    /**
     * 1出现的概率为%20
     */
    public static double rate1 = 0.15;
    /**
     * 2出现的概率为%15
     */
    public static double rate2 = 0.05;

    /**
     * Math.random()产生一个double型的随机数，判断一下
     * 例如0出现的概率为%50，则介于0到0.50中间的返回0
     * @return int
     *
     */
    public static int PercentageRandom()
    {
        double randomNumber;
        randomNumber = Math.random();
        if (randomNumber >= 0 && randomNumber <= rate0)
        {
            return 1;
        }
        else if (randomNumber >= rate0  && randomNumber <= rate0 + rate1)
        {
            return 2;
        }
        else if (randomNumber >= rate0 + rate1
                && randomNumber <= rate0 + rate1 + rate2)
        {
            return 3;
        }
        return -1;
    }
}
