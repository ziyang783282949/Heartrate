package me.chinamao.heartrate;

/**
 * Created by DELL on 2018/4/6.
 */

public class MatrixInverse {
    public static float Det(float [][]Matrix,int N)//计算n阶行列式（N=n-1）
    {
        int T0;
        int T1;
        int T2;
        float Num;
        int Cha;
        float [][] B;
        if(N>0)
        {
            Cha=0;
            B=new float[N][N];
            Num=0;
            if(N==1)
            {
                return Matrix[0][0]*Matrix[1][1]-Matrix[0][1]*Matrix[1][0];
            }
            for (T0=0;T0<=N;T0++)//T0循环
            {
                for (T1=1;T1<=N;T1++)//T1循环
                {
                    for (T2=0;T2<=N-1;T2++)//T2循环
                    {
                        if(T2==T0)
                        {
                            Cha=1;
                        }
                        B[T1-1][T2]=Matrix[T1][T2+Cha];
                    }
                    //T2循环
                    Cha=0;
                }
                //T1循环
                Num=Num+Matrix[0][T0]*Det(B,N-1)*(float)Math.pow((-1),T0);
            }
            //T0循环
            return Num;
        } else if(N==0)
        {
            return Matrix[0][0];
        }
        return 0;
    }
    public static float Inverse(float[][]Matrix,int N,float[][]MatrixC){
        int T0;
        int T1;
        int T2;
        int T3;
        float [][]B;
        float Num=0;
        int Chay=0;
        int Chax=0;
        B=new float[N][N];
        float add;
        add=1/Det(Matrix,N);
        for ( T0=0;T0<=N;T0++)
        {
            for (T3=0;T3<=N;T3++)
            {
                for (T1=0;T1<=N-1;T1++)
                {
                    if(T1<T0)
                    {
                        Chax=0;
                    } else
                    {
                        Chax=1;
                    }
                    for (T2=0;T2<=N-1;T2++)
                    {
                        if(T2<T3)
                        {
                            Chay=0;
                        } else
                        {
                            Chay=1;
                        }
                        B[T1][T2]=Matrix[T1+Chax][T2+Chay];
                    }
                    //T2循环
                }//T1循环
                Det(B,N-1);
                MatrixC[T3][T0]=Det(B,N-1)*add*(float)(Math.pow(-1, T0+T3));
            }
        }
        return 0;
    }
    public static float[] changeToFloat(String str){
        String[] strs=str.split(",");
        float[] cc=new float[strs.length];
        for(int i=0;i<strs.length;i++){
            cc[i]=Float.parseFloat(strs[i]);
        }
        return cc;
    }
    public static void main(String[]args)//测试
    {
        float[][] TestMatrix = {
                {1, 22, 34,22},
                {1, 11,5,21} ,
                {0,1,5,11},
                {7,2,13,19}};
        float[][]InMatrix=new float[4][4];
        Inverse(TestMatrix,3,InMatrix);
        String str=new String("");
        for (int i=0;i<4;i++)
        {
            for (int j=0;j<4;j++)
            {
                String strr=String.valueOf(InMatrix[i][j]);
                str+=strr;
                str+=" ";
            }
            str+="\n";
        }
        System.out.println("编程小技巧测试结果：");
        System.out.println(str);
    }
    public static float[][] mul(float a[][], float b[][]) {
        //当a的列数与矩阵b的行数不相等时，不能进行点乘，返回null
        if (a[0].length != b.length)
            return null;
        //c矩阵的行数y，与列数x
        int y = a.length;
        int x = b[0].length;
        float c[][] = new float[y][x];
        for (int i = 0; i < y; i++)
            for (int j = 0; j < x; j++)
                //c矩阵的第i行第j列所对应的数值，等于a矩阵的第i行分别乘以b矩阵的第j列之和
                for (int k = 0; k < b.length; k++)
                    c[i][j] += a[i][k] * b[k][j];
        return c;
    }
    // 将矩阵转置
    public static void reverse(int temp [][]) {
        for (int i = 0; i < temp.length; i++) {
            for (int j = i; j < temp[i].length; j++) {
                int k = temp[i][j] ;
                temp[i][j] = temp[j][i] ;
                temp[j][i] = k ;
            }
        }
    }
    public static float[][] oneWayToTwoAndTran(float[] ff){
        int length=ff.length;
        float[][] cc=new float[length][1];
        for(int i=0;i<length;i++){
            cc[i][0]=ff[i];
        }
        return cc;
    }
    public static float[] watchToPhone(float[] watchAcc, float[] phoneAcc, float[][] inWatchRotateMatix,float[][] reverseMatrix) {
        float[][] w=MatrixInverse.oneWayToTwoAndTran(watchAcc);
        float[][] p=MatrixInverse.oneWayToTwoAndTran(phoneAcc);

        float[][] mull=new float[3][1];
        mull=MatrixInverse.mul(inWatchRotateMatix,w);
        mull=MatrixInverse.mul(reverseMatrix,mull);
        float[] result=new float[3];
        result[0]=mull[0][0];
        result[1]=mull[1][0];
        result[2]=mull[2][0];
        return result;
    }

    /**
     * 计算行列式的值
     * @param a
     * @return
     */
    static double determinant(float[][] a){
        float result2 = 0;
        if(a.length>2){
            //每次选择第一行展开
            for(int i=0;i<a[0].length;i++){
                //系数符号
                double f=Math.pow(-1,i);
                //求余子式
                float[][] yuzs=new float[a.length-1][a[0].length-1];
                for (int j = 0; j < yuzs.length; j++) {
                    for (int j2 = 0; j2 < yuzs[0].length; j2++) {
                        //去掉第一行，第i列之后的行列式即为余子式
                        if(j2<i){
                            yuzs[j][j2]=a[j+1][j2];
                        }else {
                            yuzs[j][j2]=a[j+1][j2+1];
                        }

                    }
                }
                //行列式的拉普拉斯展开式，递归计算
                result2+=a[0][i]*determinant(yuzs)*f;
            }
        }
        else{
            //两行两列的行列式使用公式
            if(a.length==2){
                result2=a[0][0]*a[1][1]-a[0][1]*a[1][0];
            }
            //单行行列式的值即为本身
            else{
                result2=a[0][0];
            }
        }
        return result2;
    }
}
