package com.cgvsu.math;

//аффинные преобрзования
import com.cgvsu.math.typesMatrix.Matrix4f;
import com.cgvsu.math.typesVectors.Vector3f;
import com.cgvsu.math.typesVectors.Vector4f;
import com.cgvsu.model.Model;

import java.util.ArrayList;

public class ATTransformator {

    private Matrix4f transformationMatrix;

    //конструктор
    private ATTransformator(Matrix4f transformationMatrix) {
        this.transformationMatrix = new Matrix4f(transformationMatrix.getBase());
    }
    //применяем к векторам наши функции с изменениями
    public Vector3f applyTransformationToVector(Vector3f vector) {
        Vector4f fourVector = new Vector4f(vector.getX(), vector.getY(), vector.getZ(), 1);
        Vector4f transformedVector = transformationMatrix.multiplied(fourVector);
        if (transformedVector.get(3) == 0) {
            return new Vector3f(transformedVector.getX(), transformedVector.getY(), transformedVector.getZ());
        } else {
            return new Vector3f(new double[]{transformedVector.getX()/transformedVector.getW(), transformedVector.getY()/transformedVector.getW(), transformedVector.getZ()/transformedVector.getW()});
        }
    }

    public ArrayList<Vector3f> applyTransformationToVectorList(ArrayList<Vector3f> vectorList) {
        ArrayList<Vector3f> transformedVectorList = new ArrayList<>();
        for (Vector3f vector : vectorList) {
            transformedVectorList.add(applyTransformationToVector(vector));
        }
        return transformedVectorList;
    } //практик сказал добавить

    public Model applyTransformationToModel(Model originalModel) {
        Model transformedModel = new Model();

        for (Vector3f vertex : originalModel.vertices) {
            transformedModel.vertices.add(applyTransformationToVector(vertex));
        }

        transformedModel.textureVertices.addAll(originalModel.textureVertices);
        transformedModel.normals.addAll(originalModel.normals);
        transformedModel.polygons.addAll(originalModel.polygons);

        return transformedModel;
    } //практик сказал добавить

    public static class ATBuilder {
        private Matrix4f currentMatrix;

        public ATBuilder() {
            this.currentMatrix = new Matrix4f(true);
        }
        //масштабирование
        private ATBuilder scale(double sX, double sY, double sZ) {
            Matrix4f scaleMatrix = new Matrix4f(new double[][]{
                    {sX, 0, 0, 0},
                    {0, sY, 0, 0},
                    {0, 0, sZ, 0},
                    {0, 0, 0, 1}
            });
            this.currentMatrix = this.currentMatrix.multiplied(scaleMatrix);
            return this;
        }
        //повороты по всем осям
        private ATBuilder rotateX(double rX) {
            double cosX = Math.cos(Math.toRadians(rX));
            double sinX = Math.sin(Math.toRadians(rX));

            Matrix4f rotationMatrix = new Matrix4f(new double[][]{
                    {1, 0, 0, 0},
                    {0, cosX, sinX, 0},
                    {0, -sinX, cosX, 0},
                    {0, 0, 0, 1}
            });
            this.currentMatrix = this.currentMatrix.multiplied(rotationMatrix);
            return this;
        }

        private ATBuilder rotateY(double rY) {
            double cosY = Math.cos(Math.toRadians(rY));
            double sinY = Math.sin(Math.toRadians(rY));

            Matrix4f rotationMatrix = new Matrix4f(new double[][]{
                    {cosY, 0, sinY, 0},
                    {0, 1, 0, 0},
                    {-sinY, 0, cosY, 0},
                    {0, 0, 0, 1}
            });
            this.currentMatrix = this.currentMatrix.multiplied(rotationMatrix);
            return this;
        }

        private ATBuilder rotateZ(double rZ) {
            double cosZ = Math.cos(Math.toRadians(rZ));
            double sinZ = Math.sin(Math.toRadians(rZ));

            Matrix4f rotationMatrix = new Matrix4f(new double[][]{
                    {cosZ, sinZ, 0, 0},
                    {-sinZ, cosZ, 0, 0},
                    {0, 0, 1, 0},
                    {0, 0, 0, 1}
            });
            this.currentMatrix = this.currentMatrix.multiplied(rotationMatrix);
            return this;
        }

        //Параллельным переносом (translation) в методичке см стр 55
        private ATBuilder translate(double tX, double tY, double tZ) {
            Matrix4f translationMatrix = new Matrix4f(new double[][]{
                    {1, 0, 0, tX},
                    {0, 1, 0, tY},
                    {0, 0, 1, tZ},
                    {0, 0, 0, 1}
            });

            this.currentMatrix = this.currentMatrix.multiplied(translationMatrix);
            return this;
        }



       /* //cldbu

        private ATBuilder shear(double shXY, double shXZ, double shYX, double shYZ, double shZX, double shZY) {
            Matrix4f shearMatrix = new Matrix4f(new double[][]{
                    {1, shXY, shXZ, 0},
                    {shYX, 1, shYZ, 0},
                    {shZX, shZY, 1, 0},
                    {0, 0, 0, 1}
            });
            this.currentMatrix = this.currentMatrix.multiplied(shearMatrix);
            return this;
        }

        public ATBuilder shearXY(double shXY) {
            return shear(shXY, 0, 0, 0, 0, 0);
        }

        public ATBuilder shearXZ(double shXZ) {
            return shear(0, shXZ, 0, 0, 0, 0);
        }

        public ATBuilder shearYX(double shYX) {
            return shear(0, 0, shYX, 0, 0, 0);
        }

        public ATBuilder shearYZ(double shYZ) {
            return shear(0, 0, 0, shYZ, 0, 0);
        }

        public ATBuilder shearZX(double shZX) {
            return shear(0, 0, 0, 0, shZX, 0);
        }

        public ATBuilder shearZY(double shZY) {
            return shear(0, 0, 0, 0, 0, shZY);
        }

        //cldbu*/



        public ATBuilder scaleByX(double sX) {
            return scale(sX, 1, 1);
        }

        public ATBuilder scaleByY(double sY) {
            return scale(1, sY, 1);
        }

        public ATBuilder scaleByZ(double sZ) {
            return scale(1, 1, sZ);
        }

        public ATBuilder scaleByVertor(Vector3f vector) {
            return scale(vector.getX(), vector.getY(), vector.getZ());
        }

        public ATBuilder scaleByCoordinates(double sX, double sY, double sZ) {
            return scale(sX, sY, sZ);
        }

        public ATBuilder translateByX(double tX) {
            return translate(tX, 0, 0);
        }

        public ATBuilder translateByY(double tY) {
            return translate(0, tY, 0);
        }

        public ATBuilder translateByZ(double tZ) {
            return translate(0, 0, tZ);
        }

        public ATBuilder translateByVector(Vector3f vector) {
            return translate(vector.get(0), vector.get(1), vector.get(2));
        }

        public ATBuilder translateByCoordinates(double tX, double tY, double tZ) {
            return translate(tX, tY, tZ);
        }

        public ATBuilder rotateByX(double rX) {
            return rotateX(rX);
        }

        public ATBuilder rotateByY(double rY) {
            return rotateY(rY);
        }

        public ATBuilder rotateByZ(double rZ) {
            return rotateZ(rZ);
        }




        public ATTransformator build() {
            return new ATTransformator(currentMatrix);
        }

    }


    @Override
    public String toString() {
        return transformationMatrix.toString();
    }

    public void clean() {
        this.transformationMatrix = new Matrix4f(true);
    }

    public Matrix4f getTransformationMatrix() {
        return transformationMatrix;
    }
}