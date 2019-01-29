package com.meicam.vrview;

import android.content.Context;
import android.content.res.Resources;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.Matrix;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;


public class Sphere{

    private static final float UNIT_SIZE = 1f;// 单位尺寸

    private float radius=2f;// 球的半径

    final double angleSpan = Math.PI/90f;// 将球进行单位切分的角度
    int vCount = 0;// 顶点个数，先初始化为0

    private Resources res;

    private int mHProgram;
    private int mHUTexture;
    private int mHProjMatrix;
    private int mHViewMatrix;
    private int mHPosition;
    private int mHCoordinate;

    private int textureId;

    private float[] mViewMatrix=new float[16];
    private float[] mProjectMatrix=new float[16];

    private FloatBuffer posBuffer;
    private FloatBuffer cooBuffer;


    public Sphere(Context context,int texId){
        this.res=context.getResources();
        this.textureId = texId;
    }


    public void setCenterPoint(float x, float y ,float z) {
        Matrix.setLookAtM(mViewMatrix, 0, 0f, 0f, 0f, x, y, z, 0f, 0f, 1.0f);
    }

    public void setCenterPointExt(float x, float y ,float z, float up) {
        Matrix.setLookAtM(mViewMatrix, 0, 0f, 0f, 0f, x, y, z, 0f, 0f, up);
    }

    public void create(){
        mHProgram=Gl2Utils.createGlProgramByRes(res,"sphere.vert","sphere.frag");
        mHProjMatrix=GLES20.glGetUniformLocation(mHProgram,"uProjMatrix");
        mHViewMatrix=GLES20.glGetUniformLocation(mHProgram,"uViewMatrix");
        mHUTexture=GLES20.glGetUniformLocation(mHProgram,"uTexture");
        mHPosition=GLES20.glGetAttribLocation(mHProgram,"aPosition");
        mHCoordinate=GLES20.glGetAttribLocation(mHProgram,"aCoordinate");
        calculateAttribute();
    }

    public void setSize(int width,int height){
        //计算宽高比
        float ratio=(float)width/height;
        //设置透视投影
        //Matrix.frustumM(mProjectMatrix, 0, -ratio*skyRate, ratio*skyRate, -1*skyRate, 1*skyRate, 1, 200);
        //透视投影矩阵/视锥
        Matrix.perspectiveM(mProjectMatrix,0,90,ratio,1f,300);
        //设置相机位置
        Matrix.setLookAtM(mViewMatrix, 0, 0f, 0f, 0f, 1.0f, 0.0f, 0.0f, 0f, 0f, 1.0f);

        //Matrix.scaleM(mModelMatrix,0,2,2,2);
    }

//    public void setMatrix(float[] matrix){
//        System.arraycopy(matrix,0,mRotateMatrix,0,16);
//    }

    public void draw(){
        GLES20.glUseProgram(mHProgram);
        GLES20.glUniformMatrix4fv(mHProjMatrix,1,false,mProjectMatrix,0);
        GLES20.glUniformMatrix4fv(mHViewMatrix,1,false,mViewMatrix,0);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,textureId);

        GLES20.glEnableVertexAttribArray(mHPosition);
        GLES20.glVertexAttribPointer(mHPosition,3,GLES20.GL_FLOAT,false,0,posBuffer);
        GLES20.glEnableVertexAttribArray(mHCoordinate);
        GLES20.glVertexAttribPointer(mHCoordinate,2,GLES20.GL_FLOAT,false,0,cooBuffer);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vCount);
        GLES20.glDisableVertexAttribArray(mHPosition);
    }


    private void calculateAttribute(){
        ArrayList<Float> alVertix = new ArrayList<>();
        ArrayList<Float> textureVertix = new ArrayList<>();
        for (double vAngle = 0; vAngle < Math.PI; vAngle = vAngle + angleSpan){

            for (double hAngle = 0; hAngle < 2*Math.PI; hAngle = hAngle + angleSpan){
                float x0 = (float) (radius* Math.sin(vAngle) * Math.cos(hAngle));
                float y0 = (float) (radius* Math.sin(vAngle) * Math.sin(hAngle));
                float z0 = (float) (radius * Math.cos((vAngle)));

                float x1 = (float) (radius* Math.sin(vAngle) * Math.cos(hAngle + angleSpan));
                float y1 = (float) (radius* Math.sin(vAngle) * Math.sin(hAngle + angleSpan));
                float z1 = (float) (radius * Math.cos(vAngle));

                float x2 = (float) (radius* Math.sin(vAngle + angleSpan) * Math.cos(hAngle + angleSpan));
                float y2 = (float) (radius* Math.sin(vAngle + angleSpan) * Math.sin(hAngle + angleSpan));
                float z2 = (float) (radius * Math.cos(vAngle + angleSpan));

                float x3 = (float) (radius* Math.sin(vAngle + angleSpan) * Math.cos(hAngle));
                float y3 = (float) (radius* Math.sin(vAngle + angleSpan) * Math.sin(hAngle));
                float z3 = (float) (radius * Math.cos(vAngle + angleSpan));

                float s0 = 1 - (float) (hAngle / Math.PI/2);
                float s1 = 1 - (float) ((hAngle + angleSpan)/Math.PI/2);
                float t0 = (float) (vAngle / Math.PI);
                float t1 = (float) ((vAngle + angleSpan) / Math.PI);

                alVertix.add(x1);
                alVertix.add(y1);
                alVertix.add(z1);
                alVertix.add(x0);
                alVertix.add(y0);
                alVertix.add(z0);
                alVertix.add(x3);
                alVertix.add(y3);
                alVertix.add(z3);


                alVertix.add(x1);
                alVertix.add(y1);
                alVertix.add(z1);
                alVertix.add(x3);
                alVertix.add(y3);
                alVertix.add(z3);
                alVertix.add(x2);
                alVertix.add(y2);
                alVertix.add(z2);

                textureVertix.add(s1);// x1 y1对应纹理坐标
                textureVertix.add(t0);
                textureVertix.add(s0);// x0 y0对应纹理坐标
                textureVertix.add(t0);
                textureVertix.add(s0);// x3 y3对应纹理坐标
                textureVertix.add(t1);


                textureVertix.add(s1);// x1 y1对应纹理坐标
                textureVertix.add(t0);
                textureVertix.add(s0);// x3 y3对应纹理坐标
                textureVertix.add(t1);
                textureVertix.add(s1);// x2 y2对应纹理坐标
                textureVertix.add(t1);

            }
        }
        vCount = alVertix.size() / 3;
        posBuffer = convertToFloatBuffer(alVertix);
        cooBuffer=convertToFloatBuffer(textureVertix);
    }

    private FloatBuffer convertToFloatBuffer(ArrayList<Float> data){
        float[] d=new float[data.size()];
        for (int i=0;i<d.length;i++){
            d[i]=data.get(i);
        }

        ByteBuffer buffer=ByteBuffer.allocateDirect(data.size()*4);
        buffer.order(ByteOrder.nativeOrder());
        FloatBuffer ret=buffer.asFloatBuffer();
        ret.put(d);
        ret.position(0);
        return ret;
    }
}
