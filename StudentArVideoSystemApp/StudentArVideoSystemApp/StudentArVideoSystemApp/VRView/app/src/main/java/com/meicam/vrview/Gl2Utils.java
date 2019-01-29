/*
 *
 * FastDrawerHelper.java
 * 
 * Created by Wuwang on 2016/11/17
 * Copyright © 2016年 深圳哎吖科技. All rights reserved.
 */
package com.meicam.vrview;

import android.content.res.Resources;
import android.opengl.GLES20;
import android.util.Log;

import java.io.InputStream;

/**
 * Description:
 */
public class Gl2Utils {

    public static final String TAG="GLUtils";
    public static boolean DEBUG=true;

    private Gl2Utils(){

    }

    //通过路径加载Assets中的文本内容
    public static String uRes(Resources mRes,String path){
        StringBuilder result=new StringBuilder();
        try{
            InputStream is=mRes.getAssets().open(path);
            int ch;
            byte[] buffer=new byte[1024];
            while (-1!=(ch=is.read(buffer))){
                result.append(new String(buffer,0,ch));
            }
        }catch (Exception e){
            return null;
        }
        return result.toString().replaceAll("\\r\\n","\n");
    }

    public static int createGlProgramByRes(Resources res,String vert,String frag){
        return createGlProgram(uRes(res,vert),uRes(res,frag));
    }

    //创建GL程序
    public static int createGlProgram(String vertexSource, String fragmentSource){
        int vertex=loadShader(GLES20.GL_VERTEX_SHADER,vertexSource);
        if(vertex==0)return 0;
        int fragment=loadShader(GLES20.GL_FRAGMENT_SHADER,fragmentSource);
        if(fragment==0)return 0;
        int program= GLES20.glCreateProgram();
        if(program!=0){
            GLES20.glAttachShader(program,vertex);
            GLES20.glAttachShader(program,fragment);
            GLES20.glLinkProgram(program);
            int[] linkStatus=new int[1];
            GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS,linkStatus,0);
            if(linkStatus[0]!= GLES20.GL_TRUE){
                glError(1,"Could not link program:"+ GLES20.glGetProgramInfoLog(program));
                GLES20.glDeleteProgram(program);
                program=0;
            }
        }
        return program;
    }

    //加载shader
    public static int loadShader(int shaderType,String source){
        int shader= GLES20.glCreateShader(shaderType);
        if(0!=shader){
            GLES20.glShaderSource(shader,source);
            GLES20.glCompileShader(shader);
            int[] compiled=new int[1];
            GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS,compiled,0);
            if(compiled[0]==0){
                glError(1,"Could not compile shader:"+shaderType);
                glError(1,"GLES20 Error:"+ GLES20.glGetShaderInfoLog(shader));
                GLES20.glDeleteShader(shader);
                shader=0;
            }
        }
        return shader;
    }

    public static void glError(int code,Object index){
        if(DEBUG&&code!=0){
            Log.e(TAG,"glError:"+code+"---"+index);
        }
    }

}
