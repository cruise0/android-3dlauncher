package com.MyLauncher;

import javax.microedition.khronos.opengles.GL10;

import android.opengl.GLU;
import android.opengl.Matrix;

public class OGLPicker {

	private float[] persMat = new float[16];
	
	private int[] viewMat = new int[4];

//	/**
//	 * ģ��ʵ��GLU.gluLookAt()������������ͬ�������������󷵻�
//	 * 
//	 * @param eye
//	 * @param center
//	 * @param up
//	 * @param out
//	 *            - ���صļ���������
//	 */
//	public static void gluLookAt(Vector3f eye, Vector3f center, Vector3f up,
//			Matrix4f out) {
//		tmpF.x = center.x - eye.x;
//		tmpF.y = center.y - eye.y;
//		tmpF.z = center.z - eye.z;
//
//		tmpF.normalize();
//		tmpUp.set(up);
//		tmpUp.normalize();
//
//		tmpS.cross(tmpF, tmpUp);
//		tmpT.cross(tmpS, tmpF);
//
//		out.m00 = tmpS.x;
//		out.m10 = tmpT.x;
//		out.m20 = -tmpF.x;
//		out.m30 = 0;
//
//		out.m01 = tmpS.y;
//		out.m11 = tmpT.y;
//		out.m21 = -tmpF.y;
//		out.m31 = 0;
//
//		out.m02 = tmpS.z;
//		out.m12 = tmpT.z;
//		out.m22 = -tmpF.z;
//		out.m32 = 0;
//
//		out.m03 = 0;
//		out.m13 = 0;
//		out.m23 = 0;
//		out.m33 = 1;
//
//		tmpMat.setIdentity();
//		tmpMat.setTranslation(-eye.x, -eye.y, -eye.z);
//
//		out.mul(tmpMat);
//	}

	/**
	 * ģ��ʵ��GLU.gluPersective()������������ͬ�������������뷵�ؾ�����
	 * 
	 * @param fovy
	 * @param aspect
	 * @param zNear
	 * @param zFar
	 * @param out
	 *            - ����������
	 */
	public void gluPersective( GL10 gl , float fovy, float aspect, float zNear,
			float zFar ) {
		float sine, cotangent, deltaZ;
		float radians = (float) (fovy / 2 * Math.PI / 180);

		deltaZ = zFar - zNear;
		sine = (float) Math.sin(radians);

		if ((deltaZ == 0) || (sine == 0) || (aspect == 0)) {
			return;
		}

		cotangent = (float) Math.cos(radians) / sine;

		
		Matrix.setIdentityM( persMat , 0 );

		persMat[0] = cotangent / aspect;
		persMat[5] = cotangent;
		persMat[10] = -(zFar + zNear) / deltaZ;
		persMat[11] = -1;
		persMat[14] = -2 * zNear * zFar / deltaZ;
		persMat[15] = 0;
		
		GLU.gluPerspective(gl, fovy, aspect, zNear, zFar);
		
	}

	/**
	 * ����ʰȡ����
	 * 
	 * @param screenX
	 *            - ��Ļ����X
	 * @param screenY
	 *            - ��Ļ����Y
	 */
	public void update(GL10 gl, float screenX, float screenY) {
		// AppConfig.gMatView.fillFloatArray(AppConfig.gpMatrixViewArray);

		// ����OpenGL����ϵԭ��Ϊ���½ǣ�����������ϵԭ��Ϊ���Ͻ�
		// ��ˣ���OpenGl�е�YӦ����Ҫ�õ�ǰ�ӿڸ߶ȣ���ȥ��������Y
		float openglY = viewMat[3] - screenY;
		
		float[] modelMat = new float[16];
		Matrix.setIdentityM( modelMat , 0);
		
		float[] P0 = new float[4];
		float[] P1 = new float[4];
		
		// z = 0 , �õ�P0
		GLU.gluUnProject(screenX, openglY, 0.0f, modelMat ,
				0, persMat , 0, viewMat, 0, P0, 0);
		
//		// �������ԭ��P0
//		gPickRay.mvOrigin.set(gpObjPosArray[0], gpObjPosArray[1],
//				gpObjPosArray[2]);

		// z = 1 ���õ�P1
		GLU.gluUnProject(screenX, openglY, 1.0f, modelMat ,
				0, persMat , 0, viewMat, 0, P1, 0);
		
//		// �������ߵķ���P1 - P0
//		gPickRay.mvDirection.set(gpObjPosArray[0], gpObjPosArray[1],
//				gpObjPosArray[2]);
//		gPickRay.mvDirection.sub(gPickRay.mvOrigin);
//		// ������һ��
//		gPickRay.mvDirection.normalize();
	}

	public void setViewPort(GL10 gl, int x, int y, int width, int height) {
		viewMat[0] = x;
		viewMat[1] = y;
		viewMat[2] = width;
		viewMat[3] = height;

		gl.glViewport(x, y, width, height);
	}
}
